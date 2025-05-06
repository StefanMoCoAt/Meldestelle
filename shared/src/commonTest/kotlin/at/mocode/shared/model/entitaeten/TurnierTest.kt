package at.mocode.shared.model.entitaeten

import at.mocode.shared.model.enums.NennungsArt
import at.mocode.shared.model.enums.PlatzTyp
import at.mocode.shared.model.serializers.BigDecimalSerializer
import at.mocode.shared.model.serializers.KotlinInstantSerializer
import at.mocode.shared.model.serializers.KotlinLocalDateSerializer
import at.mocode.shared.model.serializers.KotlinLocalDateTimeSerializer
import at.mocode.shared.model.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TurnierTest {

    @Test
    fun testCreateTurnier() {
        // Create a Turnier with minimal required parameters
        val veranstaltungId = uuid4()
        val datumVon = LocalDate(2023, 6, 1)
        val datumBis = LocalDate(2023, 6, 3)

        val turnier = Turnier(
            veranstaltungId = veranstaltungId,
            oepsTurnierNr = "T12345",
            titel = "Test Turnier",
            untertitel = null,
            datumVon = datumVon,
            datumBis = datumBis,
            nennungsschluss = null,
            nennungsHinweis = null,
            eigenesNennsystemUrl = null,
            nenngeld = null,
            startgeldStandard = null,
            turnierleiterId = null,
            turnierbeauftragterId = null,
            tierarztInfos = null,
            hufschmiedInfo = null,
            meldestelleVerantwortlicherId = null,
            meldestelleTelefon = null,
            meldestelleOeffnungszeiten = null,
            ergebnislistenUrl = null
        )

        // Verify required fields
        assertEquals(veranstaltungId, turnier.veranstaltungId)
        assertEquals("T12345", turnier.oepsTurnierNr)
        assertEquals("Test Turnier", turnier.titel)
        assertEquals(datumVon, turnier.datumVon)
        assertEquals(datumBis, turnier.datumBis)

        // Verify default values
        assertNotNull(turnier.id)
        assertEquals(null, turnier.untertitel)
        assertEquals(null, turnier.nennungsschluss)
        assertTrue(turnier.nennungsArt.isEmpty())
        assertEquals(null, turnier.nennungsHinweis)
        assertEquals(null, turnier.eigenesNennsystemUrl)
        assertEquals(null, turnier.nenngeld)
        assertEquals(null, turnier.startgeldStandard)
        assertTrue(turnier.austragungsplaetze.isEmpty())
        assertTrue(turnier.vorbereitungsplaetze.isEmpty())
        assertEquals(null, turnier.turnierleiterId)
        assertEquals(null, turnier.turnierbeauftragterId)
        assertTrue(turnier.richterIds.isEmpty())
        assertTrue(turnier.parcoursbauerIds.isEmpty())
        assertTrue(turnier.parcoursAssistentIds.isEmpty())
        assertEquals(null, turnier.tierarztInfos)
        assertEquals(null, turnier.hufschmiedInfo)
        assertEquals(null, turnier.meldestelleVerantwortlicherId)
        assertEquals(null, turnier.meldestelleTelefon)
        assertEquals(null, turnier.meldestelleOeffnungszeiten)
        assertEquals(null, turnier.ergebnislistenUrl)
        assertTrue(turnier.verfuegbareArtikel.isEmpty())
        assertTrue(turnier.meisterschaftRefs.isEmpty())
        assertNotNull(turnier.createdAt)
        assertNotNull(turnier.updatedAt)
    }

    @Test
    fun testCreateTurnierWithAllParameters() {
        // Create a Turnier with all parameters
        val id = uuid4()
        val veranstaltungId = uuid4()
        val datumVon = LocalDate(2023, 7, 15)
        val datumBis = LocalDate(2023, 7, 17)
        val nennungsschluss = LocalDateTime(2023, 7, 10, 23, 59, 59)
        val turnierleiterId = uuid4()
        val turnierbeauftragterId = uuid4()
        val meldestelleVerantwortlicherId = uuid4()
        val now = Clock.System.now()

        // Create some test objects for lists
        val nennungsArt = listOf(NennungsArt.OEPS_ZNS, NennungsArt.EIGENES_ONLINE)
        val austragungsplatz = Platz(name = "Hauptplatz", dimension = "60x20m", boden = "Sand", typ = PlatzTyp.AUSTRAGUNG)
        val vorbereitungsplatz = Platz(name = "Abreiteplatz", dimension = "40x20m", boden = "Sand", typ = PlatzTyp.VORBEREITUNG)
        val richterIds = listOf(uuid4(), uuid4())
        val parcoursbauerIds = listOf(uuid4())
        val parcoursAssistentIds = listOf(uuid4())
        val artikel = Artikel(bezeichnung = "Startgebühr", preis = BigDecimal.parseString("25.00"), einheit = "Start")
        val meisterschaftRef = MeisterschaftReferenz(
            meisterschaftId = uuid4(),
            name = "Landesmeisterschaft",
            betrifftBewerbNummern = listOf(1, 2, 3),
            berechnungsstrategie = "Standard",
            reglementUrl = null
        )

        val turnier = Turnier(
            id = id,
            veranstaltungId = veranstaltungId,
            oepsTurnierNr = "T67890",
            titel = "Vollständiges Turnier",
            untertitel = "Mit allen Details",
            datumVon = datumVon,
            datumBis = datumBis,
            nennungsschluss = nennungsschluss,
            nennungsArt = nennungsArt,
            nennungsHinweis = "Bitte rechtzeitig nennen",
            eigenesNennsystemUrl = "https://example.com/nennung",
            nenngeld = BigDecimal.parseString("50.00"),
            startgeldStandard = BigDecimal.parseString("25.00"),
            austragungsplaetze = listOf(austragungsplatz),
            vorbereitungsplaetze = listOf(vorbereitungsplatz),
            turnierleiterId = turnierleiterId,
            turnierbeauftragterId = turnierbeauftragterId,
            richterIds = richterIds,
            parcoursbauerIds = parcoursbauerIds,
            parcoursAssistentIds = parcoursAssistentIds,
            tierarztInfos = "Dr. Vet, Tel: 12345",
            hufschmiedInfo = "Hans Schmidt, Tel: 67890",
            meldestelleVerantwortlicherId = meldestelleVerantwortlicherId,
            meldestelleTelefon = "+43 123 456789",
            meldestelleOeffnungszeiten = "8-18 Uhr",
            ergebnislistenUrl = "https://example.com/ergebnisse",
            verfuegbareArtikel = listOf(artikel),
            meisterschaftRefs = listOf(meisterschaftRef),
            createdAt = now,
            updatedAt = now
        )

        // Verify all fields
        assertEquals(id, turnier.id)
        assertEquals(veranstaltungId, turnier.veranstaltungId)
        assertEquals("T67890", turnier.oepsTurnierNr)
        assertEquals("Vollständiges Turnier", turnier.titel)
        assertEquals("Mit allen Details", turnier.untertitel)
        assertEquals(datumVon, turnier.datumVon)
        assertEquals(datumBis, turnier.datumBis)
        assertEquals(nennungsschluss, turnier.nennungsschluss)
        assertEquals(nennungsArt, turnier.nennungsArt)
        assertEquals("Bitte rechtzeitig nennen", turnier.nennungsHinweis)
        assertEquals("https://example.com/nennung", turnier.eigenesNennsystemUrl)
        assertEquals(BigDecimal.parseString("50.00"), turnier.nenngeld)
        assertEquals(BigDecimal.parseString("25.00"), turnier.startgeldStandard)
        assertEquals(1, turnier.austragungsplaetze.size)
        assertEquals(austragungsplatz.name, turnier.austragungsplaetze[0].name)
        assertEquals(1, turnier.vorbereitungsplaetze.size)
        assertEquals(vorbereitungsplatz.name, turnier.vorbereitungsplaetze[0].name)
        assertEquals(turnierleiterId, turnier.turnierleiterId)
        assertEquals(turnierbeauftragterId, turnier.turnierbeauftragterId)
        assertEquals(richterIds, turnier.richterIds)
        assertEquals(parcoursbauerIds, turnier.parcoursbauerIds)
        assertEquals(parcoursAssistentIds, turnier.parcoursAssistentIds)
        assertEquals("Dr. Vet, Tel: 12345", turnier.tierarztInfos)
        assertEquals("Hans Schmidt, Tel: 67890", turnier.hufschmiedInfo)
        assertEquals(meldestelleVerantwortlicherId, turnier.meldestelleVerantwortlicherId)
        assertEquals("+43 123 456789", turnier.meldestelleTelefon)
        assertEquals("8-18 Uhr", turnier.meldestelleOeffnungszeiten)
        assertEquals("https://example.com/ergebnisse", turnier.ergebnislistenUrl)
        assertEquals(1, turnier.verfuegbareArtikel.size)
        assertEquals(artikel.bezeichnung, turnier.verfuegbareArtikel[0].bezeichnung)
        assertEquals(1, turnier.meisterschaftRefs.size)
        assertEquals(meisterschaftRef.name, turnier.meisterschaftRefs[0].name)
        assertEquals(now, turnier.createdAt)
        assertEquals(now, turnier.updatedAt)
    }

    @Test
    fun testModifyTurnier() {
        // Create a Turnier
        val veranstaltungId = uuid4()
        val datumVon = LocalDate(2023, 8, 1)
        val datumBis = LocalDate(2023, 8, 3)

        val turnier = Turnier(
            veranstaltungId = veranstaltungId,
            oepsTurnierNr = "T12345",
            titel = "Original Turnier",
            untertitel = null,
            datumVon = datumVon,
            datumBis = datumBis,
            nennungsschluss = null,
            nennungsHinweis = null,
            eigenesNennsystemUrl = null,
            nenngeld = null,
            startgeldStandard = null,
            turnierleiterId = null,
            turnierbeauftragterId = null,
            tierarztInfos = null,
            hufschmiedInfo = null,
            meldestelleVerantwortlicherId = null,
            meldestelleTelefon = null,
            meldestelleOeffnungszeiten = null,
            ergebnislistenUrl = null
        )

        val originalUpdatedAt = turnier.updatedAt
        val newVeranstaltungId = uuid4()
        val newDatumVon = LocalDate(2023, 9, 1)
        val newDatumBis = LocalDate(2023, 9, 3)
        val newNennungsschluss = LocalDateTime(2023, 8, 25, 23, 59, 59)
        val newTurnierleiterId = uuid4()
        val newTurnierbeauftragterId = uuid4()
        val newMeldestelleVerantwortlicherId = uuid4()

        // Create some test objects for lists
        val nennungsArt = listOf(NennungsArt.DIREKT_VERANSTALTER_EMAIL)
        val austragungsplatz = Platz(name = "Neuer Hauptplatz", dimension = "70x30m", boden = "Gras", typ = PlatzTyp.AUSTRAGUNG)
        val richterIds = listOf(uuid4())
        val artikel = Artikel(bezeichnung = "Neue Startgebühr", preis = BigDecimal.parseString("30.00"), einheit = "Start")

        // Modify properties
        turnier.veranstaltungId = newVeranstaltungId
        turnier.oepsTurnierNr = "T54321"
        turnier.titel = "Geändertes Turnier"
        turnier.untertitel = "Mit Untertitel"
        turnier.datumVon = newDatumVon
        turnier.datumBis = newDatumBis
        turnier.nennungsschluss = newNennungsschluss
        turnier.nennungsArt = nennungsArt
        turnier.nennungsHinweis = "Neuer Hinweis"
        turnier.eigenesNennsystemUrl = "https://example.com/neues-system"
        turnier.nenngeld = BigDecimal.parseString("60.00")
        turnier.startgeldStandard = BigDecimal.parseString("35.00")
        turnier.austragungsplaetze = listOf(austragungsplatz)
        turnier.turnierleiterId = newTurnierleiterId
        turnier.turnierbeauftragterId = newTurnierbeauftragterId
        turnier.richterIds = richterIds
        turnier.tierarztInfos = "Neuer Tierarzt"
        turnier.hufschmiedInfo = "Neuer Hufschmied"
        turnier.meldestelleVerantwortlicherId = newMeldestelleVerantwortlicherId
        turnier.meldestelleTelefon = "+43 987 654321"
        turnier.meldestelleOeffnungszeiten = "9-17 Uhr"
        turnier.ergebnislistenUrl = "https://example.com/neue-ergebnisse"
        turnier.verfuegbareArtikel = listOf(artikel)
        turnier.updatedAt = Clock.System.now()

        // Verify modifications
        assertEquals(newVeranstaltungId, turnier.veranstaltungId)
        assertEquals("T54321", turnier.oepsTurnierNr)
        assertEquals("Geändertes Turnier", turnier.titel)
        assertEquals("Mit Untertitel", turnier.untertitel)
        assertEquals(newDatumVon, turnier.datumVon)
        assertEquals(newDatumBis, turnier.datumBis)
        assertEquals(newNennungsschluss, turnier.nennungsschluss)
        assertEquals(nennungsArt, turnier.nennungsArt)
        assertEquals("Neuer Hinweis", turnier.nennungsHinweis)
        assertEquals("https://example.com/neues-system", turnier.eigenesNennsystemUrl)
        assertEquals(BigDecimal.parseString("60.00"), turnier.nenngeld)
        assertEquals(BigDecimal.parseString("35.00"), turnier.startgeldStandard)
        assertEquals(1, turnier.austragungsplaetze.size)
        assertEquals("Neuer Hauptplatz", turnier.austragungsplaetze[0].name)
        assertEquals(newTurnierleiterId, turnier.turnierleiterId)
        assertEquals(newTurnierbeauftragterId, turnier.turnierbeauftragterId)
        assertEquals(richterIds, turnier.richterIds)
        assertEquals("Neuer Tierarzt", turnier.tierarztInfos)
        assertEquals("Neuer Hufschmied", turnier.hufschmiedInfo)
        assertEquals(newMeldestelleVerantwortlicherId, turnier.meldestelleVerantwortlicherId)
        assertEquals("+43 987 654321", turnier.meldestelleTelefon)
        assertEquals("9-17 Uhr", turnier.meldestelleOeffnungszeiten)
        assertEquals("https://example.com/neue-ergebnisse", turnier.ergebnislistenUrl)
        assertEquals(1, turnier.verfuegbareArtikel.size)
        assertEquals("Neue Startgebühr", turnier.verfuegbareArtikel[0].bezeichnung)
        assertNotEquals(originalUpdatedAt, turnier.updatedAt)
    }

    @Test
    fun testSerializationDeserialization() {
        // Create a simplified Turnier for serialization testing
        val veranstaltungId = uuid4()
        val datumVon = LocalDate(2023, 10, 1)
        val datumBis = LocalDate(2023, 10, 3)
        val turnierleiterId = uuid4()

        val turnier = Turnier(
            veranstaltungId = veranstaltungId,
            oepsTurnierNr = "T12345",
            titel = "Serialisiertes Turnier",
            untertitel = "Für Test",
            datumVon = datumVon,
            datumBis = datumBis,
            nennungsschluss = null,
            nennungsArt = listOf(NennungsArt.OEPS_ZNS),
            nennungsHinweis = "Hinweis",
            eigenesNennsystemUrl = null,
            nenngeld = BigDecimal.parseString("40.00"),
            startgeldStandard = null,
            turnierleiterId = turnierleiterId,
            turnierbeauftragterId = null,
            tierarztInfos = "Tierarzt Info",
            hufschmiedInfo = null,
            meldestelleVerantwortlicherId = null,
            meldestelleTelefon = "+43 123 456789",
            meldestelleOeffnungszeiten = null,
            ergebnislistenUrl = null
        )

        // Serialize to JSON
        val json = Json {
            prettyPrint = true
            encodeDefaults = true
        }
        val jsonString = json.encodeToString(turnier)

        // Verify JSON contains expected fields
        assertTrue(jsonString.contains("\"veranstaltungId\""), "JSON should contain veranstaltungId field")
        assertTrue(jsonString.contains(veranstaltungId.toString()), "JSON should contain veranstaltungId value")
        assertTrue(jsonString.contains("\"oepsTurnierNr\""), "JSON should contain oepsTurnierNr field")
        assertTrue(jsonString.contains("\"T12345\""), "JSON should contain oepsTurnierNr value")
        assertTrue(jsonString.contains("\"titel\""), "JSON should contain titel field")
        assertTrue(jsonString.contains("\"Serialisiertes Turnier\""), "JSON should contain titel value")
        assertTrue(jsonString.contains("\"datumVon\""), "JSON should contain datumVon field")
        assertTrue(jsonString.contains("\"2023-10-01\""), "JSON should contain datumVon value")
        assertTrue(jsonString.contains("\"nennungsArt\""), "JSON should contain nennungsArt field")
        assertTrue(jsonString.contains("\"OEPS_ZNS\""), "JSON should contain nennungsArt value")
        assertTrue(jsonString.contains("\"nenngeld\""), "JSON should contain nenngeld field")
        assertTrue(jsonString.contains("40"), "JSON should contain nenngeld value")
        assertTrue(jsonString.contains("\"turnierleiterId\""), "JSON should contain turnierleiterId field")
        assertTrue(jsonString.contains(turnierleiterId.toString()), "JSON should contain turnierleiterId value")

        // Deserialize from JSON
        val deserializedTurnier = json.decodeFromString<Turnier>(jsonString)

        // Verify deserialized object matches original
        assertEquals(turnier.id, deserializedTurnier.id)
        assertEquals(turnier.veranstaltungId, deserializedTurnier.veranstaltungId)
        assertEquals(turnier.oepsTurnierNr, deserializedTurnier.oepsTurnierNr)
        assertEquals(turnier.titel, deserializedTurnier.titel)
        assertEquals(turnier.untertitel, deserializedTurnier.untertitel)
        assertEquals(turnier.datumVon, deserializedTurnier.datumVon)
        assertEquals(turnier.datumBis, deserializedTurnier.datumBis)
        assertEquals(turnier.nennungsArt, deserializedTurnier.nennungsArt)
        assertEquals(turnier.nennungsHinweis, deserializedTurnier.nennungsHinweis)
        assertEquals(turnier.nenngeld, deserializedTurnier.nenngeld)
        assertEquals(turnier.turnierleiterId, deserializedTurnier.turnierleiterId)
        assertEquals(turnier.tierarztInfos, deserializedTurnier.tierarztInfos)
        assertEquals(turnier.meldestelleTelefon, deserializedTurnier.meldestelleTelefon)
        assertEquals(turnier.createdAt, deserializedTurnier.createdAt)
        assertEquals(turnier.updatedAt, deserializedTurnier.updatedAt)
    }

    @Test
    fun testCopyTurnier() {
        // Create a Turnier
        val veranstaltungId = uuid4()
        val datumVon = LocalDate(2023, 11, 1)
        val datumBis = LocalDate(2023, 11, 3)

        val original = Turnier(
            veranstaltungId = veranstaltungId,
            oepsTurnierNr = "T12345",
            titel = "Original Turnier",
            untertitel = "Original Untertitel",
            datumVon = datumVon,
            datumBis = datumBis,
            nennungsschluss = null,
            nennungsHinweis = "Original Hinweis",
            eigenesNennsystemUrl = null,
            nenngeld = BigDecimal.parseString("50.00"),
            startgeldStandard = null,
            turnierleiterId = null,
            turnierbeauftragterId = null,
            tierarztInfos = null,
            hufschmiedInfo = null,
            meldestelleVerantwortlicherId = null,
            meldestelleTelefon = null,
            meldestelleOeffnungszeiten = null,
            ergebnislistenUrl = null
        )

        val newVeranstaltungId = uuid4()
        val newDatumVon = LocalDate(2023, 12, 1)
        val newDatumBis = LocalDate(2023, 12, 3)

        // Create a copy with some modified properties
        val copy = original.copy(
            veranstaltungId = newVeranstaltungId,
            titel = "Kopiertes Turnier",
            datumVon = newDatumVon,
            datumBis = newDatumBis,
            nenngeld = BigDecimal.parseString("60.00")
        )

        // Verify copied properties
        assertEquals(original.id, copy.id)
        assertEquals(original.oepsTurnierNr, copy.oepsTurnierNr)
        assertEquals(original.untertitel, copy.untertitel)
        assertEquals(original.nennungsschluss, copy.nennungsschluss)
        assertEquals(original.nennungsArt, copy.nennungsArt)
        assertEquals(original.nennungsHinweis, copy.nennungsHinweis)
        assertEquals(original.eigenesNennsystemUrl, copy.eigenesNennsystemUrl)
        assertEquals(original.startgeldStandard, copy.startgeldStandard)
        assertEquals(original.austragungsplaetze, copy.austragungsplaetze)
        assertEquals(original.vorbereitungsplaetze, copy.vorbereitungsplaetze)
        assertEquals(original.turnierleiterId, copy.turnierleiterId)
        assertEquals(original.turnierbeauftragterId, copy.turnierbeauftragterId)
        assertEquals(original.richterIds, copy.richterIds)
        assertEquals(original.parcoursbauerIds, copy.parcoursbauerIds)
        assertEquals(original.parcoursAssistentIds, copy.parcoursAssistentIds)
        assertEquals(original.tierarztInfos, copy.tierarztInfos)
        assertEquals(original.hufschmiedInfo, copy.hufschmiedInfo)
        assertEquals(original.meldestelleVerantwortlicherId, copy.meldestelleVerantwortlicherId)
        assertEquals(original.meldestelleTelefon, copy.meldestelleTelefon)
        assertEquals(original.meldestelleOeffnungszeiten, copy.meldestelleOeffnungszeiten)
        assertEquals(original.ergebnislistenUrl, copy.ergebnislistenUrl)
        assertEquals(original.verfuegbareArtikel, copy.verfuegbareArtikel)
        assertEquals(original.meisterschaftRefs, copy.meisterschaftRefs)
        assertEquals(original.createdAt, copy.createdAt)
        assertEquals(original.updatedAt, copy.updatedAt)

        // Verify modified properties
        assertEquals(newVeranstaltungId, copy.veranstaltungId)
        assertEquals("Kopiertes Turnier", copy.titel)
        assertEquals(newDatumVon, copy.datumVon)
        assertEquals(newDatumBis, copy.datumBis)
        assertEquals(BigDecimal.parseString("60.00"), copy.nenngeld)
    }
}
