package at.mocode.backend.infrastructure.persistence

import at.mocode.core.domain.model.ErrorCodes
import at.mocode.core.domain.model.ErrorDto
import at.mocode.core.domain.model.PagedResponse
import at.mocode.core.utils.Result
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.statements.BatchInsertStatement
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.sql.SQLException
import java.sql.SQLTimeoutException

/**
 * JVM-specific database utilities for the Backend.
 * Provides common database operations and configurations using Exposed.
 *
 * Moved from core:core-utils to avoid polluting the KMP frontend build.
 */

inline fun <T> transactionResult(
  database: Database? = null,
  crossinline block: JdbcTransaction.() -> T
): Result<T> {
  return try {
    val result = transaction(database) { block() }
    Result.success(result)
  } catch (e: SQLTimeoutException) {
    Result.failure(
      ErrorDto(
        code = ErrorCodes.DATABASE_TIMEOUT,
        message = "Datenbank-Operation wegen Timeout fehlgeschlagen"
      )
    )
  } catch (e: SQLException) {
    // Robustere Fehlerbehandlung über SQLSTATE (Postgres)
    val mapped = when (e.sqlState) {
      // unique_violation
      "23505" -> ErrorCodes.DUPLICATE_ENTRY
      // foreign_key_violation
      "23503" -> ErrorCodes.FOREIGN_KEY_VIOLATION
      // check_violation
      "23514" -> ErrorCodes.CHECK_VIOLATION
      else -> ErrorCodes.DATABASE_ERROR
    }

    Result.failure(
      ErrorDto(
        code = mapped,
        message = "Datenbank-Operation fehlgeschlagen"
      )
    )
  } catch (e: Exception) {
    Result.failure(
      ErrorDto(
        code = ErrorCodes.TRANSACTION_ERROR,
        message = "Transaktion fehlgeschlagen"
      )
    )
  }
}

inline fun <T> writeTransaction(
  database: Database? = null,
  crossinline block: JdbcTransaction.() -> T
): Result<T> = transactionResult(database, block)

inline fun <T> readTransaction(
  database: Database? = null,
  crossinline block: JdbcTransaction.() -> T
): Result<T> = transactionResult(database, block)

fun Query.paginate(page: Int, size: Int): Query {
  require(page >= 0) { "Page number must be non-negative" }
  require(size > 0) { "Page size must be positive" }

  return limit(size).offset(start = (page * size).toLong())
}

fun <T> Query.toPagedResponse(
  page: Int,
  size: Int,
  transform: (ResultRow) -> T
): PagedResponse<T> {
  // Validate input parameters
  require(page >= 0) { "Page number must be non-negative" }
  require(size > 0) { "Page size must be positive" }

  // Calculate the total count first (executes a COUNT query)
  val totalCount = this.count()

  // If there are no results, return an empty page
  if (totalCount == 0L) {
    return PagedResponse.create(
      content = emptyList(),
      page = page,
      size = size,
      totalElements = 0,
      totalPages = 0,
      hasNext = false,
      hasPrevious = page > 0
    )
  }

  // Calculate total pages - use ceil division to ensure we round up
  val totalPages = ((totalCount + size - 1) / size).toInt()

  // Ensure the requested page exists (if page is beyond available pages, return the last page)
  val adjustedPage = if (page >= totalPages) (totalPages - 1).coerceAtLeast(0) else page

  // Then apply pagination and transform results
  val content = this.paginate(adjustedPage, size).map(transform)

  return PagedResponse.create(
    content = content,
    page = adjustedPage,
    size = size,
    totalElements = totalCount,
    totalPages = totalPages,
    hasNext = adjustedPage < totalPages - 1,
    hasPrevious = adjustedPage > 0
  )
}

object DatabaseUtils {

  fun tableExists(tableName: String, database: Database? = null): Boolean {
    return transactionResult(database) {
      // Postgres-spezifischer, robuster Ansatz über to_regclass
      val valid = tableName.trim()
      if (!valid.matches(Regex("^[A-Za-z_][A-Za-z0-9_]*$"))) return@transactionResult false
      exec("SELECT to_regclass('$valid')") { rs ->
        if (rs.next()) rs.getString(1) else null
      } != null
    }.fold(
      onSuccess = { it },
      onFailure = { false }
    )
  }

  @JvmName("createIndexIfNotExistsArray")
  fun createIndexIfNotExists(
    tableName: String,
    indexName: String,
    columns: Array<String>,
    unique: Boolean = false,
    database: Database? = null
  ): Result<Unit> = createIndexIfNotExists(tableName, indexName, *columns, unique = unique, database = database)

  @JvmName("createIndexIfNotExistsVararg")
  fun createIndexIfNotExists(
    tableName: String,
    indexName: String,
    vararg columns: String,
    unique: Boolean = false,
    database: Database? = null
  ): Result<Unit> {
    return transactionResult(database) {
      // Einfache Sanitization + Quoting der Identifier
      fun quoteIdent(name: String): String {
        require(name.matches(Regex("^[A-Za-z_][A-Za-z0-9_]*$"))) { "Ungültiger Identifier: $name" }
        return "\"$name\""
      }

      val uniqueStr = if (unique) "UNIQUE" else ""
      val qTable = quoteIdent(tableName)
      val qIndex = quoteIdent(indexName)
      val cols = columns.map { quoteIdent(it) }.joinToString(", ")
      val sql = "CREATE $uniqueStr INDEX IF NOT EXISTS $qIndex ON $qTable ($cols)"
      exec(sql)
      Unit
    }
  }

  fun executeRawSql(sql: String, database: Database? = null): Result<Unit> = transactionResult(database) {
    exec(sql)
    Unit
  }

  fun executeUpdate(sql: String, database: Database? = null): Result<Int> = transactionResult(database) {
    // Nutzt Exposed PreparedStatementApi, kein AutoCloseable
    val ps = this.connection.prepareStatement(sql, false)
    ps.executeUpdate()
  }

  inline fun <T> batchInsert(
    table: Table,
    data: Iterable<T>,
    crossinline body: BatchInsertStatement.(T) -> Unit
  ): Result<List<ResultRow>> {
    return transactionResult {
      table.batchInsert(data) { item ->
        body(item)
      }
    }
  }
}

fun <T> ResultRow.getOrNull(column: Column<T>): T? {
  return try {
    this[column]
  } catch (e: Exception) {
    null
  }
}

fun ResultRow.toMap(): Map<String, Any?> {
  val result = mutableMapOf<String, Any?>()
  this.fieldIndex.forEach { (expression, _) ->
    try {
      when (expression) {
        is Column<*> -> result[expression.name] = this[expression]
        else -> result[expression.toString()] = this[expression]
      }
    } catch (e: Exception) {
      // Ignore columns that can't be read and log the error if needed
      // You could add logging here in a production environment
    }
  }
  return result
}
