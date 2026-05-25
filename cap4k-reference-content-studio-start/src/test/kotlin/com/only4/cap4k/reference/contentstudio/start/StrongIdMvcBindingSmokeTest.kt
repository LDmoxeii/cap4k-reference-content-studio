package com.only4.cap4k.reference.contentstudio.start

import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@ContentStudioSpringBootTest
@Import(StrongIdMvcBindingSmokeTest.Config::class)
class StrongIdMvcBindingSmokeTest(
    @param:Autowired private val restTemplate: TestRestTemplate,
) {

    @Test
    fun `spring mvc binds strong ids from path variables and request params`() {
        val contentId = ContentId.new().toString()

        val response =
            restTemplate.getForEntity(
                "/test-support/strong-id-binding/$contentId?contentId=$contentId",
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEqualTo("$contentId|$contentId")
    }

    @TestConfiguration
    class Config {
        @Bean
        fun strongIdMvcBindingController(): StrongIdMvcBindingController = StrongIdMvcBindingController()
    }

    @RestController
    class StrongIdMvcBindingController {
        @GetMapping("/test-support/strong-id-binding/{pathContentId}")
        fun bind(
            @PathVariable pathContentId: ContentId,
            @RequestParam contentId: ContentId,
        ): String = "${pathContentId}|${contentId}"
    }
}
