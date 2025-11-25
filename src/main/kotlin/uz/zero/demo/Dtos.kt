package uz.zero.demo

data class BaseMessage(val code: Int? = null, val message: String? = null){
    companion object{
        var OK = BaseMessage(0,"OK")
    }
}

data class CategoryRequest(
    val name: String,
    val description: String,
    val order: Long,
)

data class CategoryResponse(
    var id: Long,
    val name: String,
    val description: String,
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

