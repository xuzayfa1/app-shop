package uz.zero.demo

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springdoc.core.converters.models.PageableAsQueryParam
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/category")
@Tag(name = "Category", description = "Kategoriya CRUD API")
class CategoryController(
    private val categoryService: CategoryServiceImpl
) {

    @GetMapping
    @PageableAsQueryParam
    fun all(
        @Parameter(hidden = true) pageable: Pageable,
        @RequestHeader(value = "hl", required = false) hl: String?,
        request: HttpServletRequest
    ): Page<CategoryResponse> {
        return categoryService.getAllCategories(pageable)
    }

    @PostMapping
    fun create(
        @RequestBody request: CategoryRequest,
        @RequestHeader(value = "hl", required = false) hl: String?
    ): BaseMessage {
        categoryService.add(request)
        return BaseMessage.OK
    }

    @GetMapping("/{id}")
    fun getOne(
        @PathVariable id: Long,
        @RequestHeader(value = "hl", required = false) hl: String?
    ): CategoryResponse {
        return categoryService.getOne(id)
    }
}


@RestController
@RequestMapping("/api/product")
@Tag(name = "Product", description = "Mahsulot CRUD API")
class ProductController(
    private val productService: ProductServiceImpl
) {

    @GetMapping
    @PageableAsQueryParam
    fun all(
        @Parameter(hidden = true) pageable: Pageable,
        @RequestHeader(value = "hl", required = false) hl: String?,
        request: HttpServletRequest
    ): Page<ProductResponse> {
        return productService.getAll(pageable)
    }

    @GetMapping("/{id}")
    fun getOne(
        @PathVariable id: Long,
        @RequestHeader(value = "hl", required = false) hl: String?
    ): ProductResponse {
        return productService.getOne(id)
    }

    @PostMapping
    fun create(
        @RequestBody request: ProductRequest,
        @RequestHeader(value = "hl", required = false) hl: String?
    ): BaseMessage {
        productService.create(request)
        return BaseMessage.OK
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Long,
        @RequestHeader(value = "hl", required = false) hl: String?
    ): BaseMessage {
        productService.delete(id)
        return BaseMessage.OK
    }
}

@RestController
@RequestMapping("/api/payment")
@Tag(name = "Payment", description = "Hisobga pul o'tkazish va yechish")
class PaymentController(
    private val paymentService: PaymentService
) {

    @PostMapping("/deposit/{userId}")
    fun deposit(
        @PathVariable userId: Long,
        @RequestBody request: PaymentRequest,
        @RequestHeader(value = "hl", required = false) hl: String?
    ): BaseMessage {
        paymentService.deposit(userId, request.amount)
        return BaseMessage.OK
    }

    @PostMapping("/withdraw/{userId}")
    fun withdraw(
        @PathVariable userId: Long,
        @RequestBody request: PaymentRequest,
        @RequestHeader(value = "hl", required = false) hl: String?
    ): BaseMessage {
        paymentService.withdraw(userId, request.amount)
        return BaseMessage.OK
    }

    @GetMapping("/history/{userId}")
    @PageableAsQueryParam
    fun history(
        @PathVariable userId: Long,
        @Parameter(hidden = true) pageable: Pageable,
        @RequestHeader(value = "hl", required = false) hl: String?
    ): Page<PaymentHistoryResponse> {
        return paymentService.getPaymentHistory(userId, pageable)
    }
}

@RestController
@RequestMapping("/api/transaction")
@Tag(name = "Transaction", description = "Sotib olish va tranzaksiya tarixi")
class TransactionController(
    private val transactionService: TransactionService
) {

    // Sotib olish
    @PostMapping("/purchase/{userId}")
    fun purchase(
        @PathVariable userId: Long,
        @RequestBody request: PurchaseRequest,
        @RequestHeader(value = "hl", required = false) hl: String?
    ): BaseMessage {
        transactionService.purchase(userId, request)
        return BaseMessage.OK
    }


    @GetMapping("/my/{userId}")
    @PageableAsQueryParam
    fun myTransactions(
        @PathVariable userId: Long,
        @Parameter(hidden = true) pageable: Pageable,
        @RequestHeader(value = "hl", required = false) hl: String?
    ): Page<UserTransactionResponse> {
        return transactionService.getUserTransactions(userId, pageable)
    }


    @GetMapping("/detail/{id}")
    fun detail(
        @PathVariable id: Long,
        @RequestHeader(value = "hl", required = false) hl: String?
    ): TransactionDetailResponse {
        return transactionService.getTransactionDetails(id)
    }


//    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    @PageableAsQueryParam
    fun adminAll(
        @Parameter(hidden = true) pageable: Pageable,
        @RequestHeader(value = "hl", required = false) hl: String?
    ): Page<AdminTransactionResponse> {
        return transactionService.getAllTransactions(pageable)
    }
}

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    data class RegisterRequest(
        @field:NotBlank val fullname: String,
        @field:NotBlank val username: String,
        @field:NotBlank val password: String
    )

    data class RegisterResponse(
        val id: Long,
        val message: String,
        val username: String,
        val balance: BigDecimal
    )

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<RegisterResponse> {
        if (userRepository.existsByUsernameAndDeletedIsFalse(request.username)) {
            return ResponseEntity.badRequest()
                .body(RegisterResponse(0,"Bu username allaqachon band!", request.username,BigDecimal.ZERO))
        }

        val user = User(
            fullname = request.fullname,
            username = request.username,
            password = passwordEncoder.encode(request.password),
            balance = BigDecimal("1000000"),
            role = "USER"
        )

        val savedUser = userRepository.save(user)

        return ResponseEntity.ok(
            RegisterResponse(
                id = savedUser.id!!,
                message = "Muvaffaqiyatli ro'yxatdan o'tdingiz!",
                username = savedUser.username,
                balance = savedUser.balance
            )
            )
    }
}