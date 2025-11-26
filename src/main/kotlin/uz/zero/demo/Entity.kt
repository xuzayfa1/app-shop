package uz.zero.demo

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @CreatedDate
    @Column(name = "created_date",nullable = false, updatable = false)
    open var createdDate: LocalDateTime? = null,

    @LastModifiedDate
    @Column(name = "modified_date",nullable = false)
    open var modifiedDate: LocalDateTime? = null,

    @CreatedBy
    @Column(name = "created_by")
    open var createdBy: String? = null,

    @LastModifiedBy
    @Column(name = "last_modified_by")
    open var lastModifiedBy: String? = null,

    @Column(nullable = false)
    @ColumnDefault("false")
    open var deleted: Boolean = false
)

@Entity
@Table(name = "users")
class User(
    var fullname: String,
    @Column(unique = true) var username: String,
    var password: String,
    var balance: BigDecimal = BigDecimal.ZERO,
    var role: String = "USER"
) : BaseEntity()

@Entity
class Category(
    var name: String,
    @Column(name = "orders") var order: Long,
    var description: String? = null
) : BaseEntity()

@Entity
class Product(
    var name: LocalizedName,
    var count: Long,
    var price: BigDecimal,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category
) : BaseEntity()

@Entity
@Table(name = "transactions")
class Transaction(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(name = "total_amount", nullable = false)
    var totalAmount: BigDecimal,

    @OneToMany(mappedBy = "transaction", cascade = [CascadeType.ALL], orphanRemoval = true)
    var items: MutableList<TransactionItem> = mutableListOf()
) : BaseEntity()

@Entity
@Table(name = "transaction_items")
class TransactionItem(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    var transaction: Transaction,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product,

    var count: Long,

    @Column(nullable = false)
    var amount: BigDecimal
) : BaseEntity()

@Entity
@Table(name = "user_payment_transactions")
class UserPaymentTransaction(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(nullable = false)
    var amount: BigDecimal,

    var type: String = "DEPOSIT",

    @Column(name = "created_date", insertable = false, updatable = false)
    var transactionDate: LocalDateTime? = null
) : BaseEntity()

@Embeddable
data class LocalizedName(
    @Column(name = "name_uz", nullable = false)
    @JsonProperty("uz")
    val uz: String = "",

    @Column(name = "name_ru")
    @JsonProperty("ru")
    val ru: String = "",

    @Column(name = "name_en")
    @JsonProperty("en")
    val en: String = ""
) {
    @JsonIgnore
    fun localized(): String = when (LocaleContext.current) {
        "ru" -> ru.takeIf { it.isNotBlank() } ?: uz
        "en" -> en.takeIf { it.isNotBlank() } ?: uz
        else -> uz
    }

    override fun toString(): String = localized()
}

// ====================== LocaleContext ======================
object LocaleContext {
    private val localeHolder = ThreadLocal<String>()

    fun set(locale: String) {
        localeHolder.set(locale.lowercase().takeIf { it in setOf("uz", "ru", "en") } ?: "uz")
    }

    val current: String get() = localeHolder.get() ?: "uz"

    fun clear() = localeHolder.remove()
}