package at.mocode.utils

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

/**
 * Utility functions to reduce code duplication in route handlers
 */

/**
 * Safely executes a block and handles common exceptions with appropriate HTTP responses
 */
suspend inline fun ApplicationCall.safeExecute(block: () -> Unit) {
    try {
        block()
    } catch (e: IllegalArgumentException) {
        respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
    } catch (e: Exception) {
        respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
    }
}

/**
 * Extracts and validates a UUID parameter from the route
 */
suspend fun ApplicationCall.getUuidParameter(paramName: String): Uuid? {
    val paramValue = parameters[paramName]
    if (paramValue == null) {
        respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing $paramName"))
        return null
    }

    return try {
        uuidFrom(paramValue)
    } catch (e: IllegalArgumentException) {
        respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format for $paramName"))
        null
    }
}

/**
 * Extracts and validates a string parameter from the route
 */
suspend fun ApplicationCall.getStringParameter(paramName: String): String? {
    val paramValue = parameters[paramName]
    if (paramValue == null) {
        respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing $paramName"))
        return null
    }
    return paramValue
}

/**
 * Extracts and validates an integer parameter from the route
 */
suspend fun ApplicationCall.getIntParameter(paramName: String): Int? {
    val paramValue = parameters[paramName]
    if (paramValue == null) {
        respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing $paramName"))
        return null
    }

    val intValue = paramValue.toIntOrNull()
    if (intValue == null) {
        respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid integer format for $paramName"))
        return null
    }
    return intValue
}

/**
 * Extracts and validates a query parameter
 */
suspend fun ApplicationCall.getQueryParameter(paramName: String): String? {
    val paramValue = request.queryParameters[paramName]
    if (paramValue == null) {
        respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing query parameter '$paramName'"))
        return null
    }
    return paramValue
}

/**
 * Responds with a single entity or 404 if null
 */
suspend inline fun <reified T : Any> ApplicationCall.respondWithEntityOrNotFound(
    entity: T?,
    notFoundMessage: String = "Entity not found"
) {
    if (entity != null) {
        respond(HttpStatusCode.OK, entity)
    } else {
        respond(HttpStatusCode.NotFound, mapOf("error" to notFoundMessage))
    }
}

/**
 * Responds with a list of entities
 */
suspend inline fun <reified T : Any> ApplicationCall.respondWithList(entities: List<T>) {
    respond(HttpStatusCode.OK, entities)
}

/**
 * Safely receives and processes a request body
 */
suspend inline fun <reified T : Any> ApplicationCall.safeReceive(): T? {
    return try {
        receive<T>()
    } catch (e: Exception) {
        respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request body: ${e.message}"))
        null
    }
}

/**
 * Generic handler for find by ID operations
 */
suspend inline fun <reified T : Any> ApplicationCall.handleFindById(
    paramName: String = "id",
    notFoundMessage: String = "Entity not found",
    crossinline findFunction: suspend (Uuid) -> T?
) {
    safeExecute {
        val id = getUuidParameter(paramName) ?: return@safeExecute
        val entity = findFunction(id)
        respondWithEntityOrNotFound(entity, notFoundMessage)
    }
}

/**
 * Generic handler for find by string parameter operations
 */
suspend inline fun <reified T : Any> ApplicationCall.handleFindByStringParam(
    paramName: String,
    notFoundMessage: String = "Entity not found",
    crossinline findFunction: suspend (String) -> T?
) {
    safeExecute {
        val param = getStringParameter(paramName) ?: return@safeExecute
        val entity = findFunction(param)
        respondWithEntityOrNotFound(entity, notFoundMessage)
    }
}

/**
 * Generic handler for find by UUID parameter operations that return lists
 */
suspend inline fun <reified T : Any> ApplicationCall.handleFindByUuidParamList(
    paramName: String,
    crossinline findFunction: suspend (Uuid) -> List<T>
) {
    safeExecute {
        val param = getUuidParameter(paramName) ?: return@safeExecute
        val entities = findFunction(param)
        respondWithList(entities)
    }
}

/**
 * Generic handler for find by string parameter operations that return lists
 */
suspend inline fun <reified T : Any> ApplicationCall.handleFindByStringParamList(
    paramName: String,
    crossinline findFunction: suspend (String) -> List<T>
) {
    safeExecute {
        val param = getStringParameter(paramName) ?: return@safeExecute
        val entities = findFunction(param)
        respondWithList(entities)
    }
}

/**
 * Generic handler for search operations
 */
suspend inline fun <reified T : Any> ApplicationCall.handleSearch(
    queryParamName: String = "q",
    crossinline searchFunction: suspend (String) -> List<T>
) {
    safeExecute {
        val query = getQueryParameter(queryParamName) ?: return@safeExecute
        val entities = searchFunction(query)
        respondWithList(entities)
    }
}

/**
 * Generic handler for find all operations
 */
suspend inline fun <reified T : Any> ApplicationCall.handleFindAll(
    crossinline findAllFunction: suspend () -> List<T>
) {
    safeExecute {
        val entities = findAllFunction()
        respondWithList(entities)
    }
}

/**
 * Generic handler for create operations
 */
suspend inline fun <reified T : Any> ApplicationCall.handleCreate(
    crossinline createFunction: suspend (T) -> T
) {
    safeExecute {
        val entity = safeReceive<T>() ?: return@safeExecute
        val createdEntity = createFunction(entity)
        respond(HttpStatusCode.Created, createdEntity)
    }
}

/**
 * Generic handler for update operations
 */
suspend inline fun <reified T : Any> ApplicationCall.handleUpdate(
    paramName: String = "id",
    crossinline updateFunction: suspend (Uuid, T) -> T?
) {
    safeExecute {
        val id = getUuidParameter(paramName) ?: return@safeExecute
        val entity = safeReceive<T>() ?: return@safeExecute
        val updatedEntity = updateFunction(id, entity)
        respondWithEntityOrNotFound(updatedEntity, "Entity not found or update failed")
    }
}

/**
 * Generic handler for delete operations
 */
suspend inline fun ApplicationCall.handleDelete(
    paramName: String = "id",
    crossinline deleteFunction: suspend (Uuid) -> Boolean
) {
    safeExecute {
        val id = getUuidParameter(paramName) ?: return@safeExecute
        val deleted = deleteFunction(id)
        if (deleted) {
            respond(HttpStatusCode.NoContent)
        } else {
            respond(HttpStatusCode.NotFound, mapOf("error" to "Entity not found"))
        }
    }
}
