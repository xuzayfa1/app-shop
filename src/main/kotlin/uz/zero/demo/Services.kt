package uz.zero.demo


import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface CategoryService {
    fun getAllCategories(pageable: Pageable): Page<CategoryResponse>
    fun getOne(id: Long): CategoryResponse
    fun add(request: CategoryRequest)
}

@Service
class CategoryServiceImpl(
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository, // hozircha ishlatilmayapti
) : CategoryService {

    @Transactional(readOnly = true)
    override fun getAllCategories(pageable: Pageable): Page<CategoryResponse> {
        return categoryRepository.findAllNotDeleted(pageable)
            .map { CategoryResponse.toAdminResponse(it) }
    }

    @Transactional(readOnly = true)
    override fun getOne(id: Long): CategoryResponse {
        return categoryRepository.findByIdAndDeletedFalse(id)
            ?.let { CategoryResponse.toAdminResponse(it) }
            ?: throw CategoryNotFoundException()
    }

    @Transactional
    override fun add(request: CategoryRequest) {
        categoryRepository.save(
            Category(
                name = request.name,
                order = request.order,
                description = request.description
            )
        )
    }
}


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

    override fun getAll(pageable: Pageable): Page<ProductResponse> {
        return productRepository.findAllNotDeleted(pageable)
            .map { ProductResponse.from(it) }
    }

    override fun getOne(id: Long): ProductResponse {
        val product = productRepository.findByIdAndDeletedFalse(id)
            ?: throw ProductNotFoundException()
        return ProductResponse.from(product)
    }

    override fun create(request: ProductRequest) {

        val category = categoryRepository.findByIdAndDeletedFalse(request.categoryId)
            ?: throw CategoryNotFoundException()

        val product = Product(
            name = LocalizedName(request.uz, request.ru, request.en),
            count = request.count,
            category = category
        )

        productRepository.save(product)
    }

    override fun delete(id: Long) {
        productRepository.trash(id)
            ?: throw ProductNotFoundException()
    }
}


