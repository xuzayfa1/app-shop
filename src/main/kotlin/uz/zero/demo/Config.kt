package uz.zero.demo

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.web.servlet.AsyncHandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import org.springframework.web.servlet.support.RequestContextUtils
import java.util.*


@Configuration
class WebMvcConfig : WebMvcConfigurer {


    @Bean
    fun localeResolver(): SessionLocaleResolver = SessionLocaleResolver().apply {
        setDefaultLocale(Locale("uz"))
    }


    @Bean
    fun messageSource(): ResourceBundleMessageSource = ResourceBundleMessageSource().apply {
        setBasename("messages")
        setDefaultEncoding("UTF-8")
        setUseCodeAsDefaultMessage(false)
        setFallbackToSystemLocale(false)
    }


    @Bean
    @Qualifier("errorMessageSource")
    fun errorMessageSource(): ResourceBundleMessageSource = ResourceBundleMessageSource().apply {
        setBasename("error")
        setDefaultEncoding("UTF-8")
        setUseCodeAsDefaultMessage(true)
    }


    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(object : AsyncHandlerInterceptor {
            override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
                val lang = request.getHeader("hl")?.takeIf { it.isNotBlank() }?.lowercase() ?: "uz"

                // TO‘G‘RI USUL — Kotlin uchun!
                val locale = when (lang) {
                    "ru" -> Locale("ru")
                    "en" -> Locale("en")
                    else -> Locale("uz")   // ← Locale.of() emas, Locale("uz")!
                }

                LocaleContext.set(lang)  // LocalizedName uchun
                RequestContextUtils.getLocaleResolver(request)?.setLocale(request, response, locale)
                return true
            }

            override fun postHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any, modelAndView: ModelAndView?) {
                LocaleContext.clear()
            }
        })
    }
}