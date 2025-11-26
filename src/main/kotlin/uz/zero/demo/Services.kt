package uz.zero.demo

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

// ====================== CATEGORY SERVICE ======================
interface CategoryService {
    fun getAllCategories(pageable: Pageable): Page<CategoryResponse>
    fun getOne(id: Long): CategoryResponse
    fun add(request: CategoryRequest)
}

@Service
class CategoryServiceImpl(
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository
) : CategoryService {

    @Transactional(readOnly = true)
    override fun getAllCategories(pageable: Pageable): Page<CategoryResponse> =
        categoryRepository.findAllNotDeleted(pageable)
            .map { CategoryResponse.toAdminResponse(it) }

    @Transactional(readOnly = true)
    override fun getOne(id: Long): CategoryResponse =
        categoryRepository.findByIdAndDeletedFalse(id)
            ?.let { CategoryResponse.toAdminResponse(it) }
            ?: throw CategoryNotFoundException("Kategoriya topilmadi: $id")

    @Transactional
    override fun add(request: CategoryRequest) {
        val nextOrder = (categoryRepository.findMaxOrder() ?: 0) + 1
        categoryRepository.save(
            Category(
                name = request.name.trim(),
                order = request.order?: nextOrder,
                description = request.description?.trim()
            )
        )
    }
}

// ====================== PRODUCT SERVICE ======================
interface ProductService {
    fun getAll(pageable: Pageable): Page<ProductResponse>
    fun getOne(id: Long): ProductResponse
    fun create(request: ProductRequest)
    fun delete(id: Long)
}

@Service
class ProductServiceImpl(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) : ProductService {

    @Transactional(readOnly = true)
    override fun getAll(pageable: Pageable): Page<ProductResponse> =
        productRepository.findAllNotDeleted(pageable)
            .map { ProductResponse.from(it) }

    @Transactional(readOnly = true)
    override fun getOne(id: Long): ProductResponse =
        productRepository.findByIdAndDeletedFalse(id)
            ?.let { ProductResponse.from(it) }
            ?: throw ProductNotFoundException("Mahsulot topilmadi: $id")

    @Transactional
    override fun create(request: ProductRequest) {
        val category = categoryRepository.findByIdAndDeletedFalse(request.categoryId)
            ?: throw CategoryNotFoundException("Kategoriya topilmadi: ${request.categoryId}")

        val product = Product(
            name = LocalizedName(
                uz = request.uz.trim().takeIf { it.isNotBlank() } ?: "Nomsiz mahsulot",
                ru = request.ru.trim(),
                en = request.en.trim()
            ),
            count = request.count.coerceAtLeast(0),
            price = request.price.setScale(2),
            category = category
        )

        productRepository.save(product)
    }

    @Transactional
    override fun delete(id: Long) {
        val product = productRepository.trash(id)
            ?: throw ProductNotFoundException("Mahsulot topilmadi yoki allaqachon o‘chirilgan: $id")

        // Agar muvaffaqiyatli trash bo‘lsa — hech nima qilmaymiz
    }
}

// ====================== PAYMENT SERVICE ======================
@Service
@Transactional
class PaymentService(
    private val userRepository: UserRepository,
    private val paymentRepository: UserPaymentTransactionRepository
) {

    fun deposit(userId: Long, amount: BigDecimal) {
        require(amount > BigDecimal.ZERO) { "Summa musbat bo'lishi kerak" }

        val user = userRepository.findByIdAndDeletedFalse(userId)
            ?: throw UserNotFoundException("Foydalanuvchi topilmadi: $userId")

        user.balance = user.balance.add(amount)
        userRepository.save(user)

        paymentRepository.save(
            UserPaymentTransaction(
                user = user,
                amount = amount,
                type = "DEPOSIT"
            )
        )
    }

    fun withdraw(userId: Long, amount: BigDecimal) {
        require(amount > BigDecimal.ZERO) { "Summa musbat bo'lishi kerak" }

        val user = userRepository.findByIdAndDeletedFalse(userId)
            ?: throw UserNotFoundException("Foydalanuvchi topilmadi: $userId")

        if (user.balance < amount) {
            throw InsufficientBalanceException("Balansda yetarli mablag' yo'q")
        }

        user.balance = user.balance.subtract(amount)
        userRepository.save(user)

        paymentRepository.save(
            UserPaymentTransaction(
                user = user,
                amount = amount.negate(),
                type = "WITHDRAW"
            )
        )
    }

    @Transactional(readOnly = true)
    fun getPaymentHistory(userId: Long, pageable: Pageable): Page<PaymentHistoryResponse> =
        paymentRepository.findByUserIdAndDeletedFalseOrderByCreatedDateDesc(userId, pageable)
            .map { PaymentHistoryResponse.from(it) }
}

// ====================== TRANSACTION SERVICE ======================
@Service
@Transactional
class TransactionService(
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val transactionRepository: TransactionRepository
) {

    fun purchase(userId: Long, request: PurchaseRequest) {
        val user = userRepository.findByIdAndDeletedFalse(userId)
            ?: throw UserNotFoundException("Foydalanuvchi topilmadi: $userId")

        var totalAmount = BigDecimal.ZERO
        val items = mutableListOf<TransactionItem>()

        for (itemReq in request.items) {
            val product = productRepository.findByIdAndDeletedFalse(itemReq.productId)
                ?: throw ProductNotFoundException("Mahsulot topilmadi: ${itemReq.productId}")

            if (product.count < itemReq.quantity) {
                throw InsufficientStockException("Mahsulot yetarli emas: ${product.name.localized()}")
            }

            val quantity = BigDecimal(itemReq.quantity)
            val itemTotal = product.price.multiply(quantity)
            totalAmount = totalAmount.add(itemTotal)

            product.count -= itemReq.quantity
            productRepository.save(product)

            items.add(
                TransactionItem(
                    transaction = Transaction(user = user, totalAmount = BigDecimal.ZERO), // vaqtincha
                    product = product,
                    count = itemReq.quantity.toLong(),
                    amount = itemTotal
                )
            )
        }

        if (user.balance.compareTo(totalAmount) < 0) {
            throw InsufficientBalanceException("Balansda yetarli pul yo'q")
        }

        user.balance = user.balance.subtract(totalAmount)

        val transaction = Transaction(
            user = user,
            totalAmount = totalAmount,
            items = items
        )

        items.forEach { it.transaction = transaction }

        transactionRepository.save(transaction)
        userRepository.save(user)
    }

    @Transactional(readOnly = true)
    fun getUserTransactions(userId: Long, pageable: Pageable): Page<UserTransactionResponse> =
        transactionRepository.findByUserIdAndDeletedFalseOrderByCreatedDateDesc(userId, pageable)
            .map { UserTransactionResponse.from(it) }

    @Transactional(readOnly = true)
    fun getTransactionDetails(transactionId: Long): TransactionDetailResponse =
        transactionRepository.findByIdAndDeletedFalse(transactionId)
            ?.let { TransactionDetailResponse.from(it) }
            ?: throw TransactionNotFoundException("Tranzaksiya topilmadi: $transactionId")

    @Transactional(readOnly = true)
    fun getAllTransactions(pageable: Pageable): Page<AdminTransactionResponse> =
        transactionRepository.findAllByDeletedFalseOrderByCreatedDateDesc(pageable)
            .map { AdminTransactionResponse.from(it) }
}