package at.mocode.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Column

// Definiert die Struktur der Tabelle "turniere" in der Datenbank
object TurniereTable : Table("turniere") { // "turniere" ist der Name der Tabelle in PostgreSQL

    // Spaltendefinitionen - wir mappen die Felder unserer data class Turnier
    // wir wählen hier passende SQL-Datentypen aus.

    // id: Wir nehmen VARCHAR(36) an, falls wir UUIDs als Strings speichern.
    // uniqueIndex() sorgt für Eindeutigkeit und ist gut für Primärschlüssel.
    val id: Column<String> = varchar("id", 36).uniqueIndex()

    // name: Ein Textfeld, max. 255 Zeichen
    val name: Column<String> = varchar("name", 255)

    // datum: Vorerst einfacher Text, max. 100 Zeichen
    val datum: Column<String> = varchar("datum", 100)

    // logoUrl: Textfeld, max. 500 Zeichen, kann NULL sein (.nullable())
    val logoUrl: Column<String?> = varchar("logo_url", 500).nullable()

    // ausschreibungUrl: Textfeld, max. 500 Zeichen, kann NULL sein
    val ausschreibungUrl: Column<String?> = varchar("ausschreibung_url", 500).nullable()

    // Definiert die Spalte 'id' als Primärschlüssel für diese Tabelle
    override val primaryKey = PrimaryKey(id)
}

// Hier können später weitere Table-Objekte für Nennung, Prüfung etc. hinzukommen.
