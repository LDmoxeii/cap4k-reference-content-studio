package com.only4.cap4k.reference.contentstudio

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(proxyBeanMethods = false)
@EnableScheduling
@EnableJpaRepositories(basePackages = ["com.only4.cap4k.reference.contentstudio.adapter.domain.repositories"])
@EntityScan(basePackages = ["com.only4.cap4k.reference.contentstudio.domain.aggregates"])
class StartApplication

fun main(args: Array<String>) {
    runApplication<StartApplication>(*args)
}
