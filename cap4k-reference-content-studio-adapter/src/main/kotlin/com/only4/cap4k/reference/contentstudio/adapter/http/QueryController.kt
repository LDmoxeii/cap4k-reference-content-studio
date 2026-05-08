package com.only4.cap4k.reference.contentstudio.adapter.http

import com.only4.cap4k.reference.contentstudio.adapter.application.queries.GetContentDetailQryHandler
import com.only4.cap4k.reference.contentstudio.adapter.application.queries.GetCurrentProcessingStatusQryHandler
import com.only4.cap4k.reference.contentstudio.application.queries.content.read.GetContentDetailQry
import com.only4.cap4k.reference.contentstudio.application.queries.content.read.GetMediaProcessingStatusQry
import java.util.UUID
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class QueryController(
    private val getContentDetailQryHandler: GetContentDetailQryHandler,
    private val getCurrentProcessingStatusQryHandler: GetCurrentProcessingStatusQryHandler,
) {

    @GetMapping("/contents/{contentId}")
    fun getContent(@PathVariable contentId: UUID): GetContentDetailQry.Response =
        getContentDetailQryHandler.exec(GetContentDetailQry.Request(contentId = contentId))

    @GetMapping("/media-processing/{contentId}")
    fun getMediaProcessing(@PathVariable contentId: UUID): GetMediaProcessingStatusQry.Response =
        getCurrentProcessingStatusQryHandler.exec(GetMediaProcessingStatusQry.Request(contentId = contentId))
}
