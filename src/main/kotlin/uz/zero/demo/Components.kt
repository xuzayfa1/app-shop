package uz.zero.demo

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.stereotype.Component

@Component
class Components {

    @Bean
    @Qualifier("myMessageSource")
    fun myMessageSource(): ResourceBundleMessageSource = ResourceBundleMessageSource().apply {
        setBasename("messages.properties") // messages.properties fayl
        setDefaultEncoding(Charsets.UTF_8.name())
        setUseCodeAsDefaultMessage(true)
    }
}
