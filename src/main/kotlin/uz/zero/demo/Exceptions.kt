package uz.zero.demo

import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.math.BigDecimal
import java.util.*

@ControllerAdvice
class ExceptionHandler(
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

class CategoryNotFoundException(s: String) : ShopAppException() {
    override fun errorType() = ErrorCode.CATEGORY_NOT_FOUND
}

class ProductNotFoundException(s: String) : ShopAppException() {
    override fun errorType() = ErrorCode.PRODUCT_NOT_FOUND
}

class UserNotFoundException(s: String) : ShopAppException() {
    override fun errorType() = ErrorCode.USER_NOT_FOUND
}

class InsufficientBalanceException(
    private val required: String? = null,
    private val available: BigDecimal? = null
) : ShopAppException() {
    override fun errorType() = ErrorCode.INSUFFICIENT_BALANCE
    override fun getErrorMessageArguments(): Array<Any?> = arrayOf(required, available)
}

class InsufficientStockException(
    private val productName: String
) : ShopAppException() {
    override fun errorType() = ErrorCode.INSUFFICIENT_STOCK
    override fun getErrorMessageArguments(): Array<Any?> = arrayOf(productName)
}

class TransactionNotFoundException(s: String) : ShopAppException() {
    override fun errorType() = ErrorCode.TRANSACTION_NOT_FOUND
}

class InvalidAmountException : ShopAppException() {
    override fun errorType() = ErrorCode.INVALID_AMOUNT
}

class UnauthorizedAccessException : ShopAppException() {
    override fun errorType() = ErrorCode.UNAUTHORIZED_ACCESS
}
