package uz.zero.demo

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.web.servlet.AsyncHandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import org.springframework.web.servlet.support.RequestContextUtils
import java.util.*

@Configuration
class WebMvcConfig : WebMvcConfigurer {


    @Bean
    fun myLocaleResolver(): SessionLocaleResolver = SessionLocaleResolver().apply {
        setDefaultLocale(Locale("uz"))
    }


    @Bean
    fun messageSource(): ResourceBundleMessageSource = ResourceBundleMessageSource().apply {
        setBasename("message")
        setDefaultEncoding("UTF-8")
        setUseCodeAsDefaultMessage(true)
    }

    @Bean
    fun errorMessageSource(): ResourceBundleMessageSource = ResourceBundleMessageSource().apply {
        setBasename("error")
        setDefaultEncoding("UTF-8")
        setUseCodeAsDefaultMessage(true)
    }


    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(object : AsyncHandlerInterceptor {
            override fun preHandle(
                request: HttpServletRequest,
                response: HttpServletResponse,
                handler: Any
            ): Boolean {
                request.getHeader("hl")?.let { lang ->
                    RequestContextUtils.getLocaleResolver(request)?.setLocale(request, response, Locale(lang))
                }
                return true
            }
        })
    }
}

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Toshiriq API")
                    .version("1.0.0")
                    .description("Online do‘kon va foydalanuvchi balansi API")
            )
    }
}
