package uz.zero.demo

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity // ANA SHU!

@SpringBootApplication
@OpenAPIDefinition
@EnableJpaRepositories(repositoryBaseClass = uz.zero.demo.BaseRepositoryImpl::class)
@EnableJpaAuditing
@EnableMethodSecurity
class AppshopApplication

fun main(args: Array<String>) {
    runApplication<AppshopApplication>(*args)
}