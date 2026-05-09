package com.only4.cap4k.reference.contentstudio.adapter

import com.only4.cap4k.reference.contentstudio.adapter.http.ContentController
import com.only4.cap4k.reference.contentstudio.adapter.http.QueryController
import com.only4.cap4k.reference.contentstudio.adapter.http.ReviewController
import com.only4.cap4k.reference.contentstudio.adapter.portal.api.payload.content.read.GetContentDetailPayload
import com.only4.cap4k.reference.contentstudio.adapter.portal.api.payload.content.read.GetMediaProcessingStatusPayload
import com.only4.cap4k.reference.contentstudio.adapter.portal.api.payload.content.workflow.CreateContentDraftPayload
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HttpControllerTacticalContractTest {

    @Test
    fun `controllers use static mediator access instead of injected handlers or mediator instances`() {
        assertEquals(emptyList<Class<*>>(), ContentController::class.java.constructors.single().parameterTypes.toList())
        assertEquals(emptyList<Class<*>>(), ReviewController::class.java.constructors.single().parameterTypes.toList())
        assertEquals(emptyList<Class<*>>(), QueryController::class.java.constructors.single().parameterTypes.toList())
    }

    @Test
    fun `controllers do not keep local request dto residue`() {
        assertTrue(ContentController::class.java.declaredClasses.isEmpty())
        assertTrue(ReviewController::class.java.declaredClasses.isEmpty())
        assertTrue(QueryController::class.java.declaredClasses.isEmpty())
    }

    @Test
    fun `controller method signatures expose payload contracts instead of application query types`() {
        assertEquals(
            CreateContentDraftPayload.Response::class.java,
            methodReturnType(ContentController::class.java, "create"),
        )
        assertEquals(
            GetContentDetailPayload.Response::class.java,
            methodReturnType(QueryController::class.java, "getContent"),
        )
        assertEquals(
            GetMediaProcessingStatusPayload.Response::class.java,
            methodReturnType(QueryController::class.java, "getMediaProcessing"),
        )
    }

    private fun methodReturnType(type: Class<*>, name: String): Class<*> =
        type.declaredMethods.single { it.name == name }.returnType
}
