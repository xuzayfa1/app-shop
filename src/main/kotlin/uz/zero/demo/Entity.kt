package uz.zero.demo

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.*

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
class BaseEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    var createdDate: Date? = null,

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    var modifiedDate: Date? = null,

    @CreatedBy
    var createdBy: String? = null,

    @LastModifiedBy
    var lastModifiedBy: String? = null,

    @Column(nullable = false)
    @ColumnDefault(value = "false")
    var deleted: Boolean = false,
)

@Entity
class Category(

    var name: String,
    @Column(name = "orders")
    var order: Long,
    var description: String,
): BaseEntity()

@Entity
class Product(
    var name: LocalizedName,
    var count:Long,
    @ManyToOne
    var category: Category
): BaseEntity()

@Embeddable
class LocalizedName(
    @Column(length = 50)
    var uz: String,
    @Column(length = 50)
    var ru: String,
    @Column(length = 50)
    var en: String,
){
    @Transient
    fun localized(): String {
        return when(LocaleContextHolder.getLocale().language){
            "en" -> this.en
            "ru" -> this.ru
            else -> this.uz
        }
    }
}