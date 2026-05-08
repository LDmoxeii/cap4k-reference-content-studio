package com.only4.cap4k.reference.contentstudio.application.ports

import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import java.util.UUID

interface ContentRepository {
    fun findById(id: UUID): Content?

    fun save(content: Content): Content
}
