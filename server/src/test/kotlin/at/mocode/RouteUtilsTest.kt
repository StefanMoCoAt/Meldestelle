package at.mocode

import at.mocode.utils.*
import com.benasher44.uuid.uuid4
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.testing.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlin.test.*

/**
 * Comprehensive test suite for RouteUtils utility functions.
 *
 * This test class verifies:
 * - UUID parameter extraction and validation
 * - String parameter extraction and validation
 * - Integer parameter extraction and validation
 * - Query parameter extraction and validation
 * - Safe execution with error handling
 * - Response utility functions
 * - Generic handler functions
 * - Proper HTTP status codes and error messages
 */
class RouteUtilsTest {

    @Test
    fun testGetUuidParameterValid() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            routing {
                get("/test/{id}") {
                    val uuid = call.getUuidParameter("id")
                    if (uuid != null) {
                        call.respond(HttpStatusCode.OK, mapOf("uuid" to uuid.toString()))
                    }
                }
            }
        }

        val testUuid = uuid4()
        client.get("/test/$testUuid").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testGetUuidParameterInvalid() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            routing {
                get("/test/{id}") {
                    val uuid = call.getUuidParameter("id")
                    if (uuid != null) {
                        call.respond(HttpStatusCode.OK, mapOf("uuid" to uuid.toString()))
                    }
                }
            }
        }

        client.get("/test/invalid-uuid").apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun testGetUuidParameterMissing() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            routing {
                get("/test") {
                    val uuid = call.getUuidParameter("id")
                    if (uuid != null) {
                        call.respond(HttpStatusCode.OK, mapOf("uuid" to uuid.toString()))
                    }
                }
            }
        }

        client.get("/test").apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun testGetStringParameterValid() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            routing {
                get("/test/{name}") {
                    val name = call.getStringParameter("name")
                    if (name != null) {
                        call.respond(HttpStatusCode.OK, mapOf("name" to name))
                    }
                }
            }
        }

        client.get("/test/testname").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testGetStringParameterMissing() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            routing {
                get("/test") {
                    val name = call.getStringParameter("name")
                    if (name != null) {
                        call.respond(HttpStatusCode.OK, mapOf("name" to name))
                    }
                }
            }
        }

        client.get("/test").apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun testGetIntParameterValid() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            routing {
                get("/test/{count}") {
                    val count = call.getIntParameter("count")
                    if (count != null) {
                        call.respond(HttpStatusCode.OK, mapOf("count" to count))
                    }
                }
            }
        }

        client.get("/test/42").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testGetIntParameterInvalid() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            routing {
                get("/test/{count}") {
                    val count = call.getIntParameter("count")
                    if (count != null) {
                        call.respond(HttpStatusCode.OK, mapOf("count" to count))
                    }
                }
            }
        }

        client.get("/test/not-a-number").apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun testGetIntParameterMissing() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            routing {
                get("/test") {
                    val count = call.getIntParameter("count")
                    if (count != null) {
                        call.respond(HttpStatusCode.OK, mapOf("count" to count))
                    }
                }
            }
        }

        client.get("/test").apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun testGetQueryParameterValid() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            routing {
                get("/test") {
                    val query = call.getQueryParameter("q")
                    if (query != null) {
                        call.respond(HttpStatusCode.OK, mapOf("query" to query))
                    }
                }
            }
        }

        client.get("/test?q=searchterm").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testGetQueryParameterMissing() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            routing {
                get("/test") {
                    val query = call.getQueryParameter("q")
                    if (query != null) {
                        call.respond(HttpStatusCode.OK, mapOf("query" to query))
                    }
                }
            }
        }

        client.get("/test").apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun testSafeExecuteSuccess() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            routing {
                get("/test") {
                    call.safeExecute {
                        call.respond(HttpStatusCode.OK, "Success")
                    }
                }
            }
        }

        client.get("/test").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testSafeExecuteIllegalArgumentException() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            routing {
                get("/test") {
                    call.safeExecute {
                        throw IllegalArgumentException("Invalid argument")
                    }
                }
            }
        }

        client.get("/test").apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun testSafeExecuteGenericException() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            routing {
                get("/test") {
                    call.safeExecute {
                        throw RuntimeException("Something went wrong")
                    }
                }
            }
        }

        client.get("/test").apply {
            assertEquals(HttpStatusCode.InternalServerError, status)
        }
    }

    @Test
    fun testRespondWithEntityOrNotFoundWithEntity() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            routing {
                get("/test") {
                    val entity = mapOf("id" to "1", "name" to "Test")
                    call.respondWithEntityOrNotFound(entity)
                }
            }
        }

        client.get("/test").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testRespondWithEntityOrNotFoundWithNull() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            routing {
                get("/test") {
                    val entity: Map<String, Any>? = null
                    call.respondWithEntityOrNotFound(entity)
                }
            }
        }

        client.get("/test").apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }
    }

    @Test
    fun testRespondWithList() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            routing {
                get("/test") {
                    val entities = listOf(
                        mapOf("id" to "1", "name" to "Test1"),
                        mapOf("id" to "2", "name" to "Test2")
                    )
                    call.respondWithList(entities)
                }
            }
        }

        client.get("/test").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testHandleFindById() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            routing {
                get("/test/{id}") {
                    call.handleFindById<Map<String, Any>> { id ->
                        mapOf("id" to id.toString(), "name" to "Test Entity")
                    }
                }
            }
        }

        val testUuid = uuid4()
        client.get("/test/$testUuid").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testHandleFindByIdNotFound() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            routing {
                get("/test/{id}") {
                    call.handleFindById<Map<String, Any>> { _ ->
                        null
                    }
                }
            }
        }

        val testUuid = uuid4()
        client.get("/test/$testUuid").apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }
    }

    @Test
    fun testHandleFindByStringParam() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            routing {
                get("/test/{name}") {
                    call.handleFindByStringParam<Map<String, String>>("name") { name ->
                        mapOf("name" to name, "found" to "true")
                    }
                }
            }
        }

        client.get("/test/testname").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testHandleFindByUuidParamList() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            routing {
                get("/test/{id}/items") {
                    call.handleFindByUuidParamList<Map<String, String>>("id") { id ->
                        listOf(
                            mapOf("id" to "1", "parentId" to id.toString()),
                            mapOf("id" to "2", "parentId" to id.toString())
                        )
                    }
                }
            }
        }

        val testUuid = uuid4()
        client.get("/test/$testUuid/items").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testHandleFindByStringParamList() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            routing {
                get("/test/{category}/items") {
                    call.handleFindByStringParamList<Map<String, String>>("category") { category ->
                        listOf(
                            mapOf("id" to "1", "category" to category),
                            mapOf("id" to "2", "category" to category)
                        )
                    }
                }
            }
        }

        client.get("/test/electronics/items").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }
}
