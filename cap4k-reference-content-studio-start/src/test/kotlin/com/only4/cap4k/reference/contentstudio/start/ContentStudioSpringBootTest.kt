package com.only4.cap4k.reference.contentstudio.start

import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    classes = [ContentStudioApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.datasource.url=jdbc:h2:mem:content-studio-test;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "server.port=0",
    ],
)
annotation class ContentStudioSpringBootTest
