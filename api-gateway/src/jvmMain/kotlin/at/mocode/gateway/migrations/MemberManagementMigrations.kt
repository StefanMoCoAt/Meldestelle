package at.mocode.gateway.migrations

import at.mocode.shared.database.Migration
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp

/**
 * Migration zur Erstellung der Mitgliederverwaltung-Tabellen.
 */
class MemberManagementTablesCreation : Migration(2, "Create member management tables") {
    override fun up() {
        // Person-Tabelle
        SchemaUtils.create(PersonTable)

        // Verein-Tabelle
        SchemaUtils.create(VereinTable)

        // Mitgliedschaft-Tabelle
        SchemaUtils.create(MitgliedschaftTable)

        // Adresse-Tabelle
        SchemaUtils.create(AdresseTable)
    }
}

// Definition der Tabellen
object PersonTable : Table("person") {
    val id = uuid("id").autoGenerate()
    val vorname = varchar("vorname", 50)
    val nachname = varchar("nachname", 50)
    val email = varchar("email", 100).uniqueIndex()
    val telefon = varchar("telefon", 20).nullable()
    val geburtsdatum = date("geburtsdatum").nullable()
    val aktiv = bool("aktiv").default(true)
    val erstelltAm = timestamp("erstellt_am").defaultExpression(CurrentTimestamp)
    val geaendertAm = timestamp("geaendert_am").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}

object VereinTable : Table("verein") {
    val id = uuid("id").autoGenerate()
    val name = varchar("name", 100)
    val vereinsNummer = varchar("vereins_nummer", 20).uniqueIndex()
    val landCode = varchar("land_code", 2).references(LandTable.code)
    val bundeslandCode = varchar("bundesland_code", 5).nullable()
    val aktiv = bool("aktiv").default(true)
    val erstelltAm = timestamp("erstellt_am").defaultExpression(CurrentTimestamp)
    val geaendertAm = timestamp("geaendert_am").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)

    init {
        foreignKey(
            bundeslandCode to LandTable.code,
            landCode to BundeslandTable.landCode
        )
    }
}

object MitgliedschaftTable : Table("mitgliedschaft") {
    val personId = uuid("person_id").references(PersonTable.id)
    val vereinId = uuid("verein_id").references(VereinTable.id)
    val aktiv = bool("aktiv").default(true)
    val erstelltAm = timestamp("erstellt_am").defaultExpression(CurrentTimestamp)
    val geaendertAm = timestamp("geaendert_am").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(personId, vereinId)
}

object AdresseTable : Table("adresse") {
    val id = uuid("id").autoGenerate()
    val personId = uuid("person_id").references(PersonTable.id).nullable()
    val vereinId = uuid("verein_id").references(VereinTable.id).nullable()
    val strasse = varchar("strasse", 100)
    val hausnummer = varchar("hausnummer", 10)
    val plz = varchar("plz", 10)
    val ort = varchar("ort", 100)
    val landCode = varchar("land_code", 2).references(LandTable.code)
    val bundeslandCode = varchar("bundesland_code", 5).nullable()
    val aktiv = bool("aktiv").default(true)
    val erstelltAm = timestamp("erstellt_am").defaultExpression(CurrentTimestamp)
    val geaendertAm = timestamp("geaendert_am").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)

    init {
        foreignKey(
            bundeslandCode to LandTable.code,
            landCode to BundeslandTable.landCode
        )
        check("address_owner_check") {
            (personId.isNotNull() and vereinId.isNull()) or
            (personId.isNull() and vereinId.isNotNull())
        }
    }
}
