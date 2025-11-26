package uz.zero.demo

import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal
import java.time.LocalDateTime

data class BaseMessage(val code: Int? = null, val message: String? = null){
    companion object{
        var OK = BaseMessage(0,"OK")
    }
}

data class CategoryRequest(
    @field:NotBlank
    val name: String,
    val description: String? = null,
    val order: Long? = null,
)

data class CategoryResponse(
    var id: Long,
    val name: String,
    val description: String?,
    val order: Long,
){
    companion object {
        fun toAdminResponse(category: Category) = run {
            CategoryResponse(category.id!!,
                category.name,
                category.description,
                category.order)
        }

    }
}

data class ProductResponse(
    val id: Long,
    val name: String,
    val count: Long,
    val categoryId: Long,
) {
    companion object {
        fun from(product: Product) = ProductResponse(
            id = product.id!!,
            name = product.name.localized(),
            count = product.count,
            categoryId = product.category.id!!
        )
    }
}


data class ProductRequest(
    val uz: String,
    val ru: String,
    val en: String,
    val count: Long,
    val price: BigDecimal,
    val categoryId: Long
)

data class ProductAdminResponse(
    val id: Long,
    val uz: String,
    val ru: String,
    val en: String,
    val count: Long,
    val categoryId: Long
) {
    companion object {
        fun from(product: Product) = ProductAdminResponse(
            id = product.id!!,
            uz = product.name.uz,
            ru = product.name.ru,
            en = product.name.en,
            count = product.count,
            categoryId = product.category.id!!
        )
    }
}



data class PaymentRequest(
    val amount: BigDecimal
)


data class PaymentHistoryResponse(
    val id: Long,
    val amount: BigDecimal,
    val type: String,
    val date: LocalDateTime
) {
    companion object {
        fun from(payment: UserPaymentTransaction): PaymentHistoryResponse {
            return PaymentHistoryResponse(
                id = payment.id!!,
                amount = payment.amount,
                type = payment.type,
                date = payment.createdDate!!
            )
        }
    }
}


data class PurchaseItemRequest(
    val productId: Long,
    val quantity: Int
)

data class PurchaseRequest(
    val items: List<PurchaseItemRequest>
)


data class UserTransactionResponse(
    val id: Long,
    val totalAmount: BigDecimal,
    val date: LocalDateTime,
    val itemCount: Int
) {
    companion object {
        fun from(tx: Transaction): UserTransactionResponse {
            return UserTransactionResponse(
                id = tx.id!!,
                totalAmount = tx.totalAmount,
                date = tx.createdDate!!,
                itemCount = tx.items.size
            )
        }
    }
}


data class TransactionItemResponse(
    val productId: Long,
    val productName: String,
    val count: Long,
    val amount: BigDecimal
)

data class TransactionDetailResponse(
    val id: Long,
    val totalAmount: BigDecimal,
    val date: LocalDateTime,
    val items: List<TransactionItemResponse>
) {
    companion object {
        fun from(tx: Transaction): TransactionDetailResponse {
            val items = tx.items.map { item ->
                TransactionItemResponse(
                    productId = item.product.id!!,
                    productName = item.product.name.localized(),
                    count = item.count,
                    amount = item.amount
                )
            }
            return TransactionDetailResponse(
                id = tx.id!!,
                totalAmount = tx.totalAmount,
                date = tx.createdDate!!,
                items = items
            )
        }
    }
}


data class AdminTransactionResponse(
    val id: Long,
    val userId: Long,
    val username: String,
    val fullname: String,
    val totalAmount: BigDecimal,
    val date: LocalDateTime,
    val itemCount: Int
) {
    companion object {
        fun from(tx: Transaction): AdminTransactionResponse {
            return AdminTransactionResponse(
                id = tx.id!!,
                userId = tx.user.id!!,
                username = tx.user.username,
                fullname = tx.user.fullname,
                totalAmount = tx.totalAmount,
                date = tx.createdDate!!,
                itemCount = tx.items.size
            )
        }
    }
}

