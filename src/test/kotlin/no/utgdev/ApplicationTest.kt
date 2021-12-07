package no.utgdev

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.content.*
import io.ktor.http.content.*
import io.ktor.features.*
import io.ktor.serialization.*
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import java.time.*
import io.ktor.server.engine.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import kotlin.test.*
import io.ktor.server.testing.*
import no.utgdev.plugins.*
import org.junit.Test

class ApplicationTest {
    @Test
    fun testRoot() {
        withTestApplication({ configureRouting() }) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Hello World!", response.content)
            }
        }
    }
}