package com.only4.cap4k.reference.contentstudio.start

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus

@SpringBootTest(
    classes = [ContentStudioApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.datasource.url=jdbc:h2:mem:content-studio-smoke;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "server.port=0",
    ],
)
class ContentStudioApplicationSmokeTest(
    @Autowired private val restTemplate: TestRestTemplate,
) {

    @Test
    fun `openapi docs are exposed`() {
        val response = restTemplate.getForEntity("/v3/api-docs", String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains("\"openapi\"")
        assertThat(response.body).contains("/contents")
        assertThat(response.body).contains("/media-processing/{contentId}")
    }
}
