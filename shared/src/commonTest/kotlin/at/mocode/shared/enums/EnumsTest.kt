package at.mocode.shared.enums

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class EnumsTest {

    @Test
    fun testVeranstalterTypEnum() {
        // Test all enum values
        val values = VeranstalterTyp.entries.toTypedArray()
        assertEquals(5, values.size)

        // Test specific enum values
        assertEquals(VeranstalterTyp.VEREIN, values[0])
        assertEquals(VeranstalterTyp.FIRMA, values[1])
        assertEquals(VeranstalterTyp.PRIVATPERSON, values[2])
        assertEquals(VeranstalterTyp.SONSTIGE, values[3])
        assertEquals(VeranstalterTyp.UNBEKANNT, values[4])

        // Test serialization and deserialization
        testEnumSerialization(VeranstalterTyp.VEREIN)
        testEnumSerialization(VeranstalterTyp.FIRMA)
        testEnumSerialization(VeranstalterTyp.PRIVATPERSON)
        testEnumSerialization(VeranstalterTyp.SONSTIGE)
        testEnumSerialization(VeranstalterTyp.UNBEKANNT)

        // Test comparison
        assertEquals(VeranstalterTyp.VEREIN, VeranstalterTyp.VEREIN)
        assertNotEquals(VeranstalterTyp.VEREIN, VeranstalterTyp.FIRMA)
    }

    @Test
    fun testPlatzTypEnum() {
        // Test all enum values
        val values = PlatzTyp.entries.toTypedArray()
        assertEquals(4, values.size)

        // Test specific enum values
        assertEquals(PlatzTyp.AUSTRAGUNG, values[0])
        assertEquals(PlatzTyp.VORBEREITUNG, values[1])
        assertEquals(PlatzTyp.LONGIEREN, values[2])
        assertEquals(PlatzTyp.SONSTIGES, values[3])

        // Test serialization and deserialization
        testEnumSerialization(PlatzTyp.AUSTRAGUNG)
        testEnumSerialization(PlatzTyp.VORBEREITUNG)
        testEnumSerialization(PlatzTyp.LONGIEREN)
        testEnumSerialization(PlatzTyp.SONSTIGES)

        // Test comparison
        assertEquals(PlatzTyp.AUSTRAGUNG, PlatzTyp.AUSTRAGUNG)
        assertNotEquals(PlatzTyp.AUSTRAGUNG, PlatzTyp.VORBEREITUNG)
    }

    @Test
    fun testNennungsArtEnum() {
        // Test all enum values
        val values = NennungsArt.entries.toTypedArray()
        assertEquals(6, values.size)

        // Test specific enum values
        assertEquals(NennungsArt.OEPS_ZNS, values[0])
        assertEquals(NennungsArt.EIGENES_ONLINE, values[1])
        assertEquals(NennungsArt.DIREKT_VERANSTALTER_EMAIL, values[2])
        assertEquals(NennungsArt.DIREKT_VERANSTALTER_TELEFON, values[3])
        assertEquals(NennungsArt.DIREKT_VERANSTALTER_WHATSAPP, values[4])
        assertEquals(NennungsArt.SONSTIGE, values[5])

        // Test serialization and deserialization
        testEnumSerialization(NennungsArt.OEPS_ZNS)
        testEnumSerialization(NennungsArt.EIGENES_ONLINE)
        testEnumSerialization(NennungsArt.DIREKT_VERANSTALTER_EMAIL)
        testEnumSerialization(NennungsArt.DIREKT_VERANSTALTER_TELEFON)
        testEnumSerialization(NennungsArt.DIREKT_VERANSTALTER_WHATSAPP)
        testEnumSerialization(NennungsArt.SONSTIGE)

        // Test comparison
        assertEquals(NennungsArt.OEPS_ZNS, NennungsArt.OEPS_ZNS)
        assertNotEquals(NennungsArt.OEPS_ZNS, NennungsArt.EIGENES_ONLINE)
    }

    @Test
    fun testSparteEnum() {
        // Test all enum values
        val values = SparteE.entries.toTypedArray()
        assertEquals(12, values.size)

        // Test specific enum values
        assertEquals(SparteE.DRESSUR, values[0])
        assertEquals(SparteE.SPRINGEN, values[1])
        assertEquals(SparteE.VIELSEITIGKEIT, values[2])
        assertEquals(SparteE.FAHREN, values[3])
        assertEquals(SparteE.VOLTIGIEREN, values[4])
        assertEquals(SparteE.WESTERN, values[5])
        assertEquals(SparteE.DISTANZ, values[6])
        assertEquals(SparteE.ISLAND, values[7])
        assertEquals(SparteE.PFERDESPORT_SPIEL, values[8])
        assertEquals(SparteE.BASIS, values[9])
        assertEquals(SparteE.KOMBINIERT, values[10])
        assertEquals(SparteE.SONSTIGES, values[11])

        // Test serialization and deserialization
        testEnumSerialization(SparteE.DRESSUR)
        testEnumSerialization(SparteE.SPRINGEN)
        testEnumSerialization(SparteE.VIELSEITIGKEIT)
        testEnumSerialization(SparteE.FAHREN)
        testEnumSerialization(SparteE.VOLTIGIEREN)
        testEnumSerialization(SparteE.WESTERN)
        testEnumSerialization(SparteE.DISTANZ)
        testEnumSerialization(SparteE.ISLAND)
        testEnumSerialization(SparteE.PFERDESPORT_SPIEL)
        testEnumSerialization(SparteE.BASIS)
        testEnumSerialization(SparteE.KOMBINIERT)
        testEnumSerialization(SparteE.SONSTIGES)

        // Test comparison
        assertEquals(SparteE.DRESSUR, SparteE.DRESSUR)
        assertNotEquals(SparteE.DRESSUR, SparteE.SPRINGEN)
    }

    @Test
    fun testBewerbStatusEnum() {
        // Test all enum values
        val values = BewerbStatus.entries.toTypedArray()
        assertEquals(6, values.size)

        // Test specific enum values
        assertEquals(BewerbStatus.GEPLANT, values[0])
        assertEquals(BewerbStatus.OFFEN_FUER_NENNUNG, values[1])
        assertEquals(BewerbStatus.GESCHLOSSEN_FUER_NENNUNG, values[2])
        assertEquals(BewerbStatus.LAEUFT, values[3])
        assertEquals(BewerbStatus.ABGESCHLOSSEN, values[4])
        assertEquals(BewerbStatus.ABGESAGT, values[5])

        // Test serialization and deserialization
        testEnumSerialization(BewerbStatus.GEPLANT)
        testEnumSerialization(BewerbStatus.OFFEN_FUER_NENNUNG)
        testEnumSerialization(BewerbStatus.GESCHLOSSEN_FUER_NENNUNG)
        testEnumSerialization(BewerbStatus.LAEUFT)
        testEnumSerialization(BewerbStatus.ABGESCHLOSSEN)
        testEnumSerialization(BewerbStatus.ABGESAGT)

        // Test comparison
        assertEquals(BewerbStatus.GEPLANT, BewerbStatus.GEPLANT)
        assertNotEquals(BewerbStatus.GEPLANT, BewerbStatus.ABGESAGT)
    }

    @Test
    fun testBedingungstypEnum() {
        // Test all enum values
        val values = Bedingungstyp.entries.toTypedArray()
        assertEquals(9, values.size)

        // Test specific enum values
        assertEquals(Bedingungstyp.LIZENZ_REITER, values[0])
        assertEquals(Bedingungstyp.LIZENZ_FAHRER, values[1])
        assertEquals(Bedingungstyp.ALTER_PFERD, values[2])
        assertEquals(Bedingungstyp.ALTER_REITER, values[3])
        assertEquals(Bedingungstyp.RASSE_PFERD, values[4])
        assertEquals(Bedingungstyp.GESCHLECHT_PFERD, values[5])
        assertEquals(Bedingungstyp.GESCHLECHT_REITER, values[6])
        assertEquals(Bedingungstyp.STARTKARTE, values[7])
        assertEquals(Bedingungstyp.SONSTIGES, values[8])

        // Test serialization and deserialization
        testEnumSerialization(Bedingungstyp.LIZENZ_REITER)
        testEnumSerialization(Bedingungstyp.LIZENZ_FAHRER)
        testEnumSerialization(Bedingungstyp.ALTER_PFERD)
        testEnumSerialization(Bedingungstyp.ALTER_REITER)
        testEnumSerialization(Bedingungstyp.RASSE_PFERD)
        testEnumSerialization(Bedingungstyp.GESCHLECHT_PFERD)
        testEnumSerialization(Bedingungstyp.GESCHLECHT_REITER)
        testEnumSerialization(Bedingungstyp.STARTKARTE)
        testEnumSerialization(Bedingungstyp.SONSTIGES)

        // Test comparison
        assertEquals(Bedingungstyp.LIZENZ_REITER, Bedingungstyp.LIZENZ_REITER)
        assertNotEquals(Bedingungstyp.LIZENZ_REITER, Bedingungstyp.LIZENZ_FAHRER)
    }

    @Test
    fun testOperatorEnum() {
        // Test all enum values
        val values = Operator.entries.toTypedArray()
        assertEquals(7, values.size)

        // Test specific enum values
        assertEquals(Operator.GLEICH, values[0])
        assertEquals(Operator.UNGLEICH, values[1])
        assertEquals(Operator.MINDESTENS, values[2])
        assertEquals(Operator.MAXIMAL, values[3])
        assertEquals(Operator.ZWISCHEN, values[4])
        assertEquals(Operator.IN_LISTE, values[5])
        assertEquals(Operator.NICHT_IN_LISTE, values[6])

        // Test serialization and deserialization
        testEnumSerialization(Operator.GLEICH)
        testEnumSerialization(Operator.UNGLEICH)
        testEnumSerialization(Operator.MINDESTENS)
        testEnumSerialization(Operator.MAXIMAL)
        testEnumSerialization(Operator.ZWISCHEN)
        testEnumSerialization(Operator.IN_LISTE)
        testEnumSerialization(Operator.NICHT_IN_LISTE)

        // Test comparison
        assertEquals(Operator.GLEICH, Operator.GLEICH)
        assertNotEquals(Operator.GLEICH, Operator.UNGLEICH)
    }

    @Test
    fun testFunktionaerRolleEnum() {
        // Test all enum values
        val values = FunktionaerRolle.entries.toTypedArray()
        assertEquals(12, values.size)

        // Test specific enum values
        assertEquals(FunktionaerRolle.RICHTER, values[0])
        assertEquals(FunktionaerRolle.PARCOURSBAUER, values[1])
        assertEquals(FunktionaerRolle.PARCOURSBAU_ASSISTENT, values[2])
        assertEquals(FunktionaerRolle.TECHN_DELEGIERTER, values[3])
        assertEquals(FunktionaerRolle.TURNIERBEAUFTRAGTER, values[4])
        assertEquals(FunktionaerRolle.STEWARD, values[5])
        assertEquals(FunktionaerRolle.ZEITNEHMER, values[6])
        assertEquals(FunktionaerRolle.SCHREIBER, values[7])
        assertEquals(FunktionaerRolle.VERANSTALTER_KONTAKT, values[8])
        assertEquals(FunktionaerRolle.TURNIERLEITER, values[9])
        assertEquals(FunktionaerRolle.HELFER, values[10])
        assertEquals(FunktionaerRolle.SONSTIGE, values[11])

        // Test serialization and deserialization
        testEnumSerialization(FunktionaerRolle.RICHTER)
        testEnumSerialization(FunktionaerRolle.PARCOURSBAUER)
        testEnumSerialization(FunktionaerRolle.PARCOURSBAU_ASSISTENT)
        testEnumSerialization(FunktionaerRolle.TECHN_DELEGIERTER)
        testEnumSerialization(FunktionaerRolle.TURNIERBEAUFTRAGTER)
        testEnumSerialization(FunktionaerRolle.STEWARD)
        testEnumSerialization(FunktionaerRolle.ZEITNEHMER)
        testEnumSerialization(FunktionaerRolle.SCHREIBER)
        testEnumSerialization(FunktionaerRolle.VERANSTALTER_KONTAKT)
        testEnumSerialization(FunktionaerRolle.TURNIERLEITER)
        testEnumSerialization(FunktionaerRolle.HELFER)
        testEnumSerialization(FunktionaerRolle.SONSTIGE)

        // Test comparison
        assertEquals(FunktionaerRolle.RICHTER, FunktionaerRolle.RICHTER)
        assertNotEquals(FunktionaerRolle.RICHTER, FunktionaerRolle.PARCOURSBAUER)
    }

    @Test
    fun testRichterPositionEnum() {
        // Test all enum values
        val values = RichterPosition.entries.toTypedArray()
        assertEquals(8, values.size)

        // Test specific enum values
        assertEquals(RichterPosition.BEI_C, values[0])
        assertEquals(RichterPosition.BEI_E, values[1])
        assertEquals(RichterPosition.BEI_H, values[2])
        assertEquals(RichterPosition.BEI_M, values[3])
        assertEquals(RichterPosition.BEI_B, values[4])
        assertEquals(RichterPosition.VORSITZ, values[5])
        assertEquals(RichterPosition.SEITENRICHTER, values[6])
        assertEquals(RichterPosition.SONSTIGE, values[7])

        // Test serialization and deserialization
        testEnumSerialization(RichterPosition.BEI_C)
        testEnumSerialization(RichterPosition.BEI_E)
        testEnumSerialization(RichterPosition.BEI_H)
        testEnumSerialization(RichterPosition.BEI_M)
        testEnumSerialization(RichterPosition.BEI_B)
        testEnumSerialization(RichterPosition.VORSITZ)
        testEnumSerialization(RichterPosition.SEITENRICHTER)
        testEnumSerialization(RichterPosition.SONSTIGE)

        // Test comparison
        assertEquals(RichterPosition.BEI_C, RichterPosition.BEI_C)
        assertNotEquals(RichterPosition.BEI_C, RichterPosition.BEI_E)
    }

    @Test
    fun testGeschlechtEnum() {
        // Test all enum values
        val values = Geschlecht.entries.toTypedArray()
        assertEquals(4, values.size)

        // Test specific enum values
        assertEquals(Geschlecht.M, values[0])
        assertEquals(Geschlecht.W, values[1])
        assertEquals(Geschlecht.D, values[2])
        assertEquals(Geschlecht.UNBEKANNT, values[3])

        // Test serialization and deserialization
        testEnumSerialization(Geschlecht.M)
        testEnumSerialization(Geschlecht.W)
        testEnumSerialization(Geschlecht.D)
        testEnumSerialization(Geschlecht.UNBEKANNT)

        // Test comparison
        assertEquals(Geschlecht.M, Geschlecht.M)
        assertNotEquals(Geschlecht.M, Geschlecht.W)
    }

    @Test
    fun testLizenzTypEnum() {
        // Test all enum values
        val values = LizenzTyp.entries.toTypedArray()
        assertEquals(15, values.size)

        // Test specific enum values
        assertEquals(LizenzTyp.REITER, values[0])
        assertEquals(LizenzTyp.FAHRER, values[1])
        assertEquals(LizenzTyp.VOLTIGIERER, values[2])
        assertEquals(LizenzTyp.WESTERN, values[3])
        assertEquals(LizenzTyp.WORKING_EQUITATION, values[4])
        assertEquals(LizenzTyp.POLO, values[5])
        assertEquals(LizenzTyp.STARTKARTE_ALLG, values[6])
        assertEquals(LizenzTyp.STARTKARTE_VOLTIGIEREN, values[7])
        assertEquals(LizenzTyp.STARTKARTE_WESTERN, values[8])
        assertEquals(LizenzTyp.STARTKARTE_ISLAND, values[9])
        assertEquals(LizenzTyp.STARTKARTE_FAHREN_JUGEND, values[10])
        assertEquals(LizenzTyp.STARTKARTE_HORSEBALL, values[11])
        assertEquals(LizenzTyp.STARTKARTE_POLO, values[12])
        assertEquals(LizenzTyp.PARAEQUESTRIAN, values[13])
        assertEquals(LizenzTyp.SONSTIGE, values[14])

        // Test serialization and deserialization
        testEnumSerialization(LizenzTyp.REITER)
        testEnumSerialization(LizenzTyp.FAHRER)
        testEnumSerialization(LizenzTyp.VOLTIGIERER)
        testEnumSerialization(LizenzTyp.WESTERN)
        testEnumSerialization(LizenzTyp.WORKING_EQUITATION)
        testEnumSerialization(LizenzTyp.POLO)
        testEnumSerialization(LizenzTyp.STARTKARTE_ALLG)
        testEnumSerialization(LizenzTyp.STARTKARTE_VOLTIGIEREN)
        testEnumSerialization(LizenzTyp.STARTKARTE_WESTERN)
        testEnumSerialization(LizenzTyp.STARTKARTE_ISLAND)
        testEnumSerialization(LizenzTyp.STARTKARTE_FAHREN_JUGEND)
        testEnumSerialization(LizenzTyp.STARTKARTE_HORSEBALL)
        testEnumSerialization(LizenzTyp.STARTKARTE_POLO)
        testEnumSerialization(LizenzTyp.PARAEQUESTRIAN)
        testEnumSerialization(LizenzTyp.SONSTIGE)

        // Test comparison
        assertEquals(LizenzTyp.REITER, LizenzTyp.REITER)
        assertNotEquals(LizenzTyp.REITER, LizenzTyp.FAHRER)
    }

    @Test
    fun testGeschlechtPferdEnum() {
        // Test all enum values
        val values = GeschlechtPferd.entries.toTypedArray()
        assertEquals(4, values.size)

        // Test specific enum values
        assertEquals(GeschlechtPferd.HENGST, values[0])
        assertEquals(GeschlechtPferd.STUTE, values[1])
        assertEquals(GeschlechtPferd.WALLACH, values[2])
        assertEquals(GeschlechtPferd.UNBEKANNT, values[3])

        // Test serialization and deserialization
        testEnumSerialization(GeschlechtPferd.HENGST)
        testEnumSerialization(GeschlechtPferd.STUTE)
        testEnumSerialization(GeschlechtPferd.WALLACH)
        testEnumSerialization(GeschlechtPferd.UNBEKANNT)

        // Test comparison
        assertEquals(GeschlechtPferd.HENGST, GeschlechtPferd.HENGST)
        assertNotEquals(GeschlechtPferd.HENGST, GeschlechtPferd.STUTE)
    }

    @Test
    fun testEnumCollections() {
        // Test using enums in collections
        val enumSet = setOf(
            Geschlecht.M,
            Geschlecht.W,
            FunktionaerRolle.RICHTER,
            FunktionaerRolle.PARCOURSBAUER
        )

        assertTrue(enumSet.contains(Geschlecht.M))
        assertTrue(enumSet.contains(FunktionaerRolle.RICHTER))
        assertEquals(4, enumSet.size)

        // Test serialization of collections with enums
        @Serializable
        data class TestClass(
            val geschlecht: Geschlecht,
            val rollen: Set<FunktionaerRolle>
        )

        val testObject = TestClass(
            geschlecht = Geschlecht.M,
            rollen = setOf(FunktionaerRolle.RICHTER, FunktionaerRolle.PARCOURSBAUER)
        )

        val json = Json { prettyPrint = true }
        val jsonString = json.encodeToString(testObject)

        // Verify serialization
        assertTrue(jsonString.contains("\"geschlecht\""))
        assertTrue(jsonString.contains("\"M\""))
        assertTrue(jsonString.contains("\"rollen\""))
        assertTrue(jsonString.contains("\"RICHTER\""))
        assertTrue(jsonString.contains("\"PARCOURSBAUER\""))

        // Verify deserialization
        val deserializedObject = json.decodeFromString<TestClass>(jsonString)
        assertEquals(testObject.geschlecht, deserializedObject.geschlecht)
        assertEquals(testObject.rollen, deserializedObject.rollen)
    }

    // Test serialization for a specific enum value
    private fun testEnumSerialization(value: VeranstalterTyp) {
        val json = Json { prettyPrint = true }
        val jsonString = json.encodeToString(value)

        // Verify serialization
        assertEquals("\"${value.name}\"", jsonString)

        // Verify deserialization
        val deserializedValue = json.decodeFromString<VeranstalterTyp>(jsonString)
        assertEquals(value, deserializedValue)
    }

    // Test serialization for a specific enum value
    private fun testEnumSerialization(value: PlatzTyp) {
        val json = Json { prettyPrint = true }
        val jsonString = json.encodeToString(value)

        // Verify serialization
        assertEquals("\"${value.name}\"", jsonString)

        // Verify deserialization
        val deserializedValue = json.decodeFromString<PlatzTyp>(jsonString)
        assertEquals(value, deserializedValue)
    }

    // Test serialization for a specific enum value
    private fun testEnumSerialization(value: NennungsArt) {
        val json = Json { prettyPrint = true }
        val jsonString = json.encodeToString(value)

        // Verify serialization
        assertEquals("\"${value.name}\"", jsonString)

        // Verify deserialization
        val deserializedValue = json.decodeFromString<NennungsArt>(jsonString)
        assertEquals(value, deserializedValue)
    }

    // Test serialization for a specific enum value
    private fun testEnumSerialization(value: SparteE) {
        val json = Json { prettyPrint = true }
        val jsonString = json.encodeToString(value)

        // Verify serialization
        assertEquals("\"${value.name}\"", jsonString)

        // Verify deserialization
        val deserializedValue = json.decodeFromString<SparteE>(jsonString)
        assertEquals(value, deserializedValue)
    }

    // Test serialization for a specific enum value
    private fun testEnumSerialization(value: BewerbStatus) {
        val json = Json { prettyPrint = true }
        val jsonString = json.encodeToString(value)

        // Verify serialization
        assertEquals("\"${value.name}\"", jsonString)

        // Verify deserialization
        val deserializedValue = json.decodeFromString<BewerbStatus>(jsonString)
        assertEquals(value, deserializedValue)
    }

    // Test serialization for a specific enum value
    private fun testEnumSerialization(value: Bedingungstyp) {
        val json = Json { prettyPrint = true }
        val jsonString = json.encodeToString(value)

        // Verify serialization
        assertEquals("\"${value.name}\"", jsonString)

        // Verify deserialization
        val deserializedValue = json.decodeFromString<Bedingungstyp>(jsonString)
        assertEquals(value, deserializedValue)
    }

    // Test serialization for a specific enum value
    private fun testEnumSerialization(value: Operator) {
        val json = Json { prettyPrint = true }
        val jsonString = json.encodeToString(value)

        // Verify serialization
        assertEquals("\"${value.name}\"", jsonString)

        // Verify deserialization
        val deserializedValue = json.decodeFromString<Operator>(jsonString)
        assertEquals(value, deserializedValue)
    }

    // Test serialization for a specific enum value
    private fun testEnumSerialization(value: FunktionaerRolle) {
        val json = Json { prettyPrint = true }
        val jsonString = json.encodeToString(value)

        // Verify serialization
        assertEquals("\"${value.name}\"", jsonString)

        // Verify deserialization
        val deserializedValue = json.decodeFromString<FunktionaerRolle>(jsonString)
        assertEquals(value, deserializedValue)
    }

    // Test serialization for a specific enum value
    private fun testEnumSerialization(value: RichterPosition) {
        val json = Json { prettyPrint = true }
        val jsonString = json.encodeToString(value)

        // Verify serialization
        assertEquals("\"${value.name}\"", jsonString)

        // Verify deserialization
        val deserializedValue = json.decodeFromString<RichterPosition>(jsonString)
        assertEquals(value, deserializedValue)
    }

    // Test serialization for a specific enum value
    private fun testEnumSerialization(value: Geschlecht) {
        val json = Json { prettyPrint = true }
        val jsonString = json.encodeToString(value)

        // Verify serialization
        assertEquals("\"${value.name}\"", jsonString)

        // Verify deserialization
        val deserializedValue = json.decodeFromString<Geschlecht>(jsonString)
        assertEquals(value, deserializedValue)
    }

    // Test serialization for a specific enum value
    private fun testEnumSerialization(value: LizenzTyp) {
        val json = Json { prettyPrint = true }
        val jsonString = json.encodeToString(value)

        // Verify serialization
        assertEquals("\"${value.name}\"", jsonString)

        // Verify deserialization
        val deserializedValue = json.decodeFromString<LizenzTyp>(jsonString)
        assertEquals(value, deserializedValue)
    }

    // Test serialization for a specific enum value
    private fun testEnumSerialization(value: GeschlechtPferd) {
        val json = Json { prettyPrint = true }
        val jsonString = json.encodeToString(value)

        // Verify serialization
        assertEquals("\"${value.name}\"", jsonString)

        // Verify deserialization
        val deserializedValue = json.decodeFromString<GeschlechtPferd>(jsonString)
        assertEquals(value, deserializedValue)
    }
}
