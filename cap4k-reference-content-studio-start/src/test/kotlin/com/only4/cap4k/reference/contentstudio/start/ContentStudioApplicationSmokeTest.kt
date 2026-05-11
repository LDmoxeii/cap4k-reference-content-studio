package com.only4.cap4k.reference.contentstudio.start

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus

@ContentStudioSpringBootTest
class ContentStudioApplicationSmokeTest(
    @param:Autowired private val restTemplate: TestRestTemplate,
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
