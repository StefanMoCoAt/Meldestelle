package at.mocode.repositories

import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Base repository class that provides common database operations
 * and eliminates code duplication across repository implementations.
 */
abstract class BaseRepository<T, TTable : Table>(
    protected val table: TTable
) {

    /**
     * Abstract method to map a database row to the domain model
     */
    protected abstract fun rowToModel(row: ResultRow): T

    /**
     * Abstract method to get the ID column for the table
     */
    protected abstract fun getIdColumn(): Column<Uuid>

    /**
     * Abstract method to populate insert statement with model data
     */
    protected abstract fun populateInsert(statement: UpdateBuilder<Number>, model: T, now: Instant)

    /**
     * Abstract method to populate update statement with model data
     */
    protected abstract fun populateUpdate(statement: UpdateBuilder<Int>, model: T, now: Instant)

    /**
     * Abstract method to update the model's timestamp
     */
    protected abstract fun updateModelTimestamp(model: T, timestamp: Instant): T

    /**
     * Abstract method to update the model's ID and timestamp
     */
    protected abstract fun updateModelIdAndTimestamp(model: T, id: Uuid, timestamp: Instant): T

    /**
     * Optimized findAll - uses select instead of selectAll for better performance
     */
    protected open suspend fun findAll(): List<T> = transaction {
        table.selectAll().map { rowToModel(it) }
    }

    /**
     * Optimized findById - uses select with where clause directly
     */
    protected open suspend fun findById(id: Uuid): T? = transaction {
        table.select { getIdColumn() eq id }
            .map { rowToModel(it) }
            .singleOrNull()
    }

    /**
     * Generic find by column with single result
     */
    protected suspend fun <V> findByColumn(column: Column<V>, value: V): T? = transaction {
        table.select { column eq value }
            .map { rowToModel(it) }
            .singleOrNull()
    }

    /**
     * Generic find by column with multiple results
     */
    protected suspend fun <V> findByColumnList(column: Column<V>, value: V): List<T> = transaction {
        table.select { column eq value }
            .map { rowToModel(it) }
    }

    /**
     * Safe LIKE search that prevents SQL injection (nullable string)
     */
    protected suspend fun findByLikeSearch(column: Column<String?>, searchTerm: String): List<T> = transaction {
        val sanitizedTerm = searchTerm.replace("%", "\\%").replace("_", "\\_")
        table.select { column like "%$sanitizedTerm%" }
            .map { rowToModel(it) }
    }

    /**
     * Safe LIKE search that prevents SQL injection (non-nullable string)
     */
    protected suspend fun findByLikeSearchNonNull(column: Column<String>, searchTerm: String): List<T> = transaction {
        val sanitizedTerm = searchTerm.replace("%", "\\%").replace("_", "\\_")
        table.select { column like "%$sanitizedTerm%" }
            .map { rowToModel(it) }
    }

    /**
     * Multi-column LIKE search with OR conditions
     */
    protected suspend fun findByMultiColumnLikeSearch(
        columns: List<Column<String?>>,
        searchTerm: String
    ): List<T> = transaction {
        val sanitizedTerm = searchTerm.replace("%", "\\%").replace("_", "\\_")
        var combinedCondition: Op<Boolean>? = null

        for (column in columns) {
            val condition = column like "%$sanitizedTerm%"
            combinedCondition = if (combinedCondition == null) {
                condition
            } else {
                combinedCondition or condition
            }
        }

        table.select { combinedCondition!! }
            .map { rowToModel(it) }
    }

    /**
     * Generic create method
     */
    protected open suspend fun create(model: T): T = transaction {
        val now = Clock.System.now()
        table.insert { statement ->
            populateInsert(statement, model, now)
        }
        updateModelTimestamp(model, now)
    }

    /**
     * Generic update method
     */
    protected open suspend fun update(id: Uuid, model: T): T? = transaction {
        val now = Clock.System.now()
        val updateCount = table.update({ getIdColumn() eq id }) { statement ->
            populateUpdate(statement, model, now)
        }
        if (updateCount > 0) {
            updateModelIdAndTimestamp(model, id, now)
        } else {
            null
        }
    }

    /**
     * Generic delete method
     */
    protected open suspend fun delete(id: Uuid): Boolean = transaction {
        table.deleteWhere { getIdColumn() eq id } > 0
    }

    /**
     * Find by boolean column (e.g., active status)
     */
    protected suspend fun findByBooleanColumn(column: Column<Boolean>, value: Boolean): List<T> = transaction {
        table.select { column eq value }
            .map { rowToModel(it) }
    }

    /**
     * Find by integer column
     */
    protected suspend fun findByIntColumn(column: Column<Int>, value: Int): List<T> = transaction {
        table.select { column eq value }
            .map { rowToModel(it) }
    }

    /**
     * Find by nullable integer column
     */
    protected suspend fun findByNullableIntColumn(column: Column<Int?>, value: Int): List<T> = transaction {
        table.select { column eq value }
            .map { rowToModel(it) }
    }
}
