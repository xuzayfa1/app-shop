package uz.zero.demo

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springdoc.core.converters.models.PageableAsQueryParam
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/category")
@Tag(name = "Category", description = "Kategoriya CRUD API")
class CategoryController(
    private val categoryService: CategoryService
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
    private val productService: ProductService
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