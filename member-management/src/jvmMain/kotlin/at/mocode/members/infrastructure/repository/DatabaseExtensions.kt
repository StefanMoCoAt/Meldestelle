package at.mocode.members.infrastructure.repository

import kotlinx.datetime.*
import kotlinx.datetime.toJavaInstant as kotlinxToJavaInstant
import kotlinx.datetime.toKotlinInstant as javaToKotlinInstant
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager

/**
 * Extension function to convert Kotlin Instant to LocalDateTime for database storage.
 */
fun Instant.toLocalDateTime(): LocalDateTime = this.toLocalDateTime(TimeZone.UTC)

/**
 * Extension function to convert LocalDateTime to Kotlin Instant.
 */
fun LocalDateTime.toInstant(): Instant = this.toInstant(TimeZone.UTC)

/**
 * Extension function for upsert (insert or update) operation on tables.
 * If a record with the given key exists, it updates it; otherwise, it inserts a new record.
 */
fun <T : Table> T.insertOrUpdate(
    vararg keys: Column<*>,
    body: T.(InsertStatement<Number>) -> Unit
) = InsertOrUpdate<Number>(this, keys = keys).apply {
    body(this)
}.execute(this)

/**
 * Custom InsertOrUpdate statement implementation for PostgreSQL.
 */
class InsertOrUpdate<Key : Any>(
    table: Table,
    isIgnore: Boolean = false,
    private vararg val keys: Column<*>
) : InsertStatement<Key>(table, isIgnore) {

    override fun prepareSQL(transaction: Transaction, prepared: Boolean): String {
        val tm = TransactionManager.current()
        val updateSetter = (table.columns - keys.toSet()).joinToString { "${tm.identity(it)} = EXCLUDED.${tm.identity(it)}" }
        val keyColumns = keys.joinToString { tm.identity(it) }
        val insertSQL = super.prepareSQL(transaction, prepared)
        return "$insertSQL ON CONFLICT ($keyColumns) DO UPDATE SET $updateSetter"
    }
}

/**
 * Extension function to execute the InsertOrUpdate statement.
 */
fun InsertOrUpdate<*>.execute(table: Table): InsertOrUpdate<*> {
    TransactionManager.current().exec(this)
    return this
}
