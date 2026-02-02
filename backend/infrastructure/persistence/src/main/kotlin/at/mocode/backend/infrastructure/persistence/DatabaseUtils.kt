package at.mocode.backend.infrastructure.persistence

import at.mocode.core.domain.model.ErrorCodes
import at.mocode.core.domain.model.ErrorDto
import at.mocode.core.domain.model.PagedResponse
import at.mocode.core.utils.Result
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.statements.BatchInsertStatement
import org.jetbrains.exposed.v1.core.statements.StatementType
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
    // Wir nutzen hier explizit Exposed JDBC Transaktionen.
    // Der Cast ist sicher, solange wir nur JDBC Databases verwenden (was wir tun).
    val result = transaction(database) {
      this.block()
    }
    Result.success(result)
  } catch (_: SQLTimeoutException) {
    Result.failure(
      ErrorDto(
        code = ErrorCodes.DATABASE_TIMEOUT,
        message = "Datenbank-Operation wegen Timeout fehlgeschlagen"
      )
    )
  } catch (e: SQLException) {
    // Robustere Fehlerbehandlung über SQLSTATE (Postgres)
    val mapped = when (e.sqlState) {
      "23505" -> ErrorCodes.DUPLICATE_ENTRY
      "23503" -> ErrorCodes.FOREIGN_KEY_VIOLATION
      "23514" -> ErrorCodes.CHECK_VIOLATION
      "40001" -> ErrorCodes.DATABASE_ERROR // serialization_failure / deadlock
      "08000", "08003", "08006" -> ErrorCodes.DATABASE_ERROR // connection errors
      else -> ErrorCodes.DATABASE_ERROR
    }

    Result.failure(
      ErrorDto(
        code = mapped,
        message = "Datenbank-Operation fehlgeschlagen: ${e.message}"
      )
    )
  } catch (e: Exception) {
    Result.failure(
      ErrorDto(
        code = ErrorCodes.TRANSACTION_ERROR,
        message = "Transaktion fehlgeschlagen: ${e.message}"
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
  require(page >= 0) { "Page number must be non-negative" }
  require(size > 0) { "Page size must be positive" }

  val totalCount = this.count()

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

  val totalPages = ((totalCount + size - 1) / size).toInt()

  if (page >= totalPages) {
    return PagedResponse.create(
      content = emptyList(),
      page = page,
      size = size,
      totalElements = totalCount,
      totalPages = totalPages,
      hasNext = false,
      hasPrevious = totalPages > 0
    )
  }

  val content = this.paginate(page, size).map(transform)

  return PagedResponse.create(
    content = content,
    page = page,
    size = size,
    totalElements = totalCount,
    totalPages = totalPages,
    hasNext = page < totalPages - 1,
    hasPrevious = page > 0
  )
}

object DatabaseUtils {

  fun tableExists(tableName: String, database: Database? = null): Boolean {
    return transactionResult(database) {
      val valid = tableName.trim()
      if (!valid.matches(Regex("^[A-Za-z_][A-Za-z0-9_]*$"))) return@transactionResult false

      this.exec("SELECT to_regclass('$valid')", explicitStatementType = StatementType.SELECT) { rs ->
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
      fun quoteIdent(name: String): String {
        require(name.matches(Regex("^[A-Za-z_][A-Za-z0-9_]*$"))) { "Ungültiger Identifier: $name" }
        return "\"$name\""
      }

      val uniqueStr = if (unique) "UNIQUE" else ""
      val qTable = quoteIdent(tableName)
      val qIndex = quoteIdent(indexName)
      val cols = columns.map { quoteIdent(it) }.joinToString(", ")
      val sql = "CREATE $uniqueStr INDEX IF NOT EXISTS $qIndex ON $qTable ($cols)"

      this.exec(sql, explicitStatementType = StatementType.CREATE)
      Unit
    }
  }

  fun executeRawSql(sql: String, database: Database? = null): Result<Unit> = transactionResult(database) {
    this.exec(sql, explicitStatementType = StatementType.OTHER)
    Unit
  }

  fun executeUpdate(sql: String, database: Database? = null): Result<Int> = transactionResult(database) {
    // Exposed 1.0.0: prepareStatement returns PreparedStatementApi which is NOT AutoCloseable
    // and executeUpdate() might be missing on the interface or requires casting.
    // We use the safe way via try-finally and closeIfPossible()
    val stmt = this.connection.prepareStatement(sql, false)
    try {
      stmt.executeUpdate()
    } finally {
      stmt.closeIfPossible()
    }
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
      // Spalten ignorieren
    }
  }
  return result
}
