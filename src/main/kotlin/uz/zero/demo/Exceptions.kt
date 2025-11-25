package uz.zero.demo

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.util.*

@ControllerAdvice
class ExceptionHandler(
    @Qualifier("myMessageSource")
    private val errorMessageSource: ResourceBundleMessageSource,

) {

    @ExceptionHandler(Throwable::class)
    fun handleOtherExceptions(exception: Throwable): ResponseEntity<Any> {
        return when (exception) {
            is ShopAppException -> {
                ResponseEntity
                    .badRequest()
                    .body(exception.getErrorMessage(errorMessageSource))
            }
            else -> {
                exception.printStackTrace()
                ResponseEntity
                    .badRequest()
                    .body(BaseMessage(100, "Iltimos support bilan bog'laning"))
            }
        }
    }
}

sealed class ShopAppException(message: String? = null) : RuntimeException(message) {
    abstract fun errorType(): ErrorCode
    protected open fun getErrorMessageArguments(): Array<Any?>? = null

    fun getErrorMessage(messageSource: ResourceBundleMessageSource): BaseMessage {
        return BaseMessage(
            errorType().code,
            messageSource.getMessage(
                errorType().toString(),
                getErrorMessageArguments(),
                Locale(LocaleContextHolder.getLocale().language)
            )
        )
    }
}

class CategoryNotFoundException() : ShopAppException() {
    override fun errorType() = ErrorCode.CATEGORY_NOT_FOUND
}

class ProductNotFoundException : ShopAppException() {
    override fun errorType() = ErrorCode.PRODUCT_NOT_FOUND
}
