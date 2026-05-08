package com.only4.cap4k.reference.contentstudio.start

import com.only4.cap4k.reference.contentstudio.domain.services.PublicationEligibilityDomainService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(
    proxyBeanMethods = false,
    scanBasePackages = ["com.only4.cap4k.reference.contentstudio"],
)
@EntityScan(basePackages = ["com.only4.cap4k.reference.contentstudio.domain.aggregates"])
@EnableJpaRepositories(basePackages = ["com.only4.cap4k.reference.contentstudio.adapter.domain.repositories"])
class ContentStudioApplication {

    @Bean
    fun publicationEligibilityDomainService(): PublicationEligibilityDomainService =
        PublicationEligibilityDomainService()
}

fun main(args: Array<String>) {
    runApplication<ContentStudioApplication>(*args)
}
