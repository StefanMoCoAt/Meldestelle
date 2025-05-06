package at.mocode.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date // Für kotlinx-datetime LocalDate
import org.jetbrains.exposed.sql.kotlin.datetime.datetime // Für kotlinx-datetime LocalDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp // Für kotlinx-datetime Instant

// Annahme: Es gibt bereits oder wird geben:
// object VeranstaltungenTable : Table("veranstaltungen") { val id = uuid("id") /* ... */ }
// object PersonenTable : Table("personen") { val id = uuid("id") /* ... */ }
// Diese sind für die Foreign Key Constraints notwendig.

/**
 * Exposed Table Definition für die Turnier-Entität.
 * Spiegelt die Struktur von shared/.../Turnier.kt wider.
 */
object TurniereTable : Table("turniere") { // Name der Tabelle in PostgreSQL

    // Primärschlüssel (KMP Uuid -> DB UUID)
    val id = uuid("id") // Exposed bietet uuid() für UUIDs

    // Foreign Key zur Veranstaltungstabelle
    val veranstaltungId = uuid("veranstaltung_id").references(VeranstaltungenTable.id)

    // OEPS Turniernummer (kann Buchstaben enthalten? Besser Varchar)
    val oepsTurnierNr = varchar("oeps_turnier_nr", 15).uniqueIndex() // Eindeutig machen?

    // Titel und Untertitel
    val titel = varchar("titel", 255)
    val untertitel = varchar("untertitel", 500).nullable()

    // Datumswerte (kotlinx -> DB Date/Timestamp)
    val datumVon = date("datum_von")
    val datumBis = date("datum_bis")
    val nennungsschluss = datetime("nennungsschluss").nullable()

    // NennungsArt Liste -> Einfache Speicherung als CSV-String für den Anfang
    // Bessere Lösung später: Eigene Zwischentabelle (TurnierNennungsArtMapping)
    val nennungsArtCsv = text("nennungs_art_csv").nullable() // Z.B. "EIGENES_ONLINE,DIREKT_VERANSTALTER_TELEFON"

    val nennungsHinweis = text("nennungs_hinweis").nullable()
    val eigenesNennsystemUrl = varchar("eigenes_nennsystem_url", 500).nullable()

    // Geldwerte (KMP BigDecimal -> DB Varchar)
    // Konvertierung muss im Code (Service-Schicht) erfolgen!
    // Alternative: decimal("nenngeld", 10, 2).nullable() - erfordert Konvertierungslogik KMP<->JVM BigDecimal
    val nenngeld = varchar("nenngeld", 50).nullable()
    val startgeldStandard = varchar("startgeld_standard", 50).nullable()

    // Plätze (List<Platz>) -> Besser in eigener Tabelle "PlaetzeTable" mit FK zu Turnier.
    // Hier *nicht* direkt speichern.

    // Personen-Referenzen (FKs)
    val turnierleiterId = uuid("turnierleiter_id").references(PersonenTable.id).nullable()
    val turnierbeauftragterId = uuid("turnierbeauftragter_id").references(PersonenTable.id).nullable()

    // Listen von Personen-IDs -> Einfache Speicherung als CSV-String für den Anfang
    // Bessere Lösung später: Eigene Zwischentabellen (TurnierRichterMapping, TurnierParcoursbauerMapping etc.)
    val richterIdsCsv = text("richter_ids_csv").nullable()             // z.B. "uuid1,uuid2,uuid3"
    val parcoursbauerIdsCsv = text("parcoursbauer_ids_csv").nullable()
    val parcoursAssistentIdsCsv = text("parcours_assistent_ids_csv").nullable()

    // Info-Texte
    val tierarztInfos = text("tierarzt_infos").nullable()
    val hufschmiedInfo = text("hufschmied_info").nullable()

    // Meldestelle
    val meldestelleVerantwortlicherId = uuid("meldestelle_verantwortlicher_id").references(PersonenTable.id).nullable()
    val meldestelleTelefon = varchar("meldestelle_telefon", 50).nullable()
    val meldestelleOeffnungszeiten = varchar("meldestelle_oeffnungszeiten", 255).nullable()
    val ergebnislistenUrl = varchar("ergebnislisten_url", 500).nullable()

    // Komplexe Listen -> Besser eigene Tabellen oder JSONB (PostgreSQL)
    // Hier *nicht* direkt speichern:
    // - verfuegbareArtikel: List<Artikel> -> Eigene Tabelle TurnierArtikelMapping
    // - meisterschaftRefs: List<MeisterschaftReferenz> -> Eigene Tabelle TurnierMeisterschaftMapping

    // Timestamps (kotlinx Instant -> DB Timestamp mit Zeitzone)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    // Primärschlüssel definieren
    override val primaryKey = PrimaryKey(id)
}
