import at.mocode.shared.enums.EventStatusE
import at.mocode.shared.enums.NennungsArtE
import at.mocode.shared.enums.RegelwerkTypE
import at.mocode.shared.enums.SparteE
import at.mocode.shared.serializers.KotlinInstantSerializer
import at.mocode.shared.serializers.KotlinLocalDateSerializer
import at.mocode.shared.serializers.KotlinLocalDateTimeSerializer
import at.mocode.shared.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * Repräsentiert ein spezifisches, vom zuständigen Verband (OEPS, FEI) genehmigtes Turnier
 * innerhalb eines Veranstaltungsrahmens. Enthält alle turnierspezifischen Informationen
 * gemäß Ausschreibung und die für den Turnierbericht relevanten Daten.
 *
 * @property turnierOepsId Eindeutiger interner Identifikator für dieses Turnier (UUID).
 * @property veranstaltungsRahmenId Fremdschlüssel zum übergeordneten `VeranstaltungsRahmen`.
 * @property oepsTurnierNr Die offizielle OEPS-Turniernummer (z.B. "25319"). Sollte eindeutig sein.
 * @property titel Der offizielle Titel des Turniers laut Ausschreibung
 * (z.B. "CSN-C NEU CSNP-C NEU NEUMARKT/M., OÖ").
 * @property untertitel Optionaler Untertitel oder Zusatzbezeichnung für das Turnier.
 * @property hauptsparte Die primäre Pferdesportsparte dieses Turniers (z.B. SPRINGEN, DRESSUR).
 * Auch wenn Bewerbe anderer Sparten stattfinden können, gibt es meist eine Hauptausrichtung.
 * @property oetoKategorieStammdatenIds Liste von Fremdschlüsseln zu `Sportfachliche_Stammdaten` (Typ BEWERBSKATEGORIE_OETO),
 * die die offiziellen ÖTO-Kategorien dieses Turniers definieren (z.B. ["CDN-C Neu", "CDNP-C Neu"]).
 * @property regelwerkTyp Das primär für dieses Turnier geltende Regelwerk (ÖTO, FEI).
 * @property datumVon Spezifisches Startdatum dieses Turniers.
 * @property datumBis Spezifisches Enddatum dieses Turniers.
 * @property nennschlussNenndatei Zeitlicher Nennschluss für die vom OEPS bereitgestellte Nenndatei (n2).
 * @property nennschlussOffiziell Offizieller Nennschluss laut Ausschreibung.
 * @property nennungsArtBevorzugt Die bevorzugte Art der Nennung für dieses Turnier (z.B. Online-Portal, ZNS).
 * @property nennungsHinweisText Wichtige Hinweise zur Nennung direkt aus der Ausschreibung.
 * @property pdfAusschreibungUrl URL oder Pfad zur offiziellen PDF-Ausschreibung.
 * @property kommentarIntern Interne Notizen oder Kommentare zum Turnier für die Meldestelle.
 * @property typNationalInternational Kennzeichnung, ob es sich um ein nationales oder internationales Turnier handelt.
 * @property spracheDefault Hauptsprache für Dokumente und Kommunikation dieses Turniers.
 * @property startnummernVerwenden Gibt an, ob Startnummern (zusätzlich zu Kopfnummern) verwendet werden.
 * @property nennungUeberZnsMoeglichOeps Info, ob dieses Turnier über das OEPS eZNS nennbar ist/war.
 * @property erhebeNachnennungsgebuehr Gibt an, ob eine Nachnenngebühr erhoben wird.
 * @property erhebeNennungstauschgebuehr Gibt an, ob eine Gebühr für Pferd-/Reitertausch erhoben wird.
 * @property logoTurnierUrl URL oder Pfad zu einem spezifischen Logo für dieses Turnier.
 * @property turnierleiterDomPersonId Fremdschlüssel zur `DomPerson` des Turnierleiters.
 * @property turnierbeauftragterDomPersonId Fremdschlüssel zur `DomPerson` des Turnierbeauftragten (TBA).
 * @property meldestelleTelefon Telefonnummer der Meldestelle für dieses Turnier.
 * @property meldestelleOeffnungszeiten Öffnungszeiten der Meldestelle.
 * @property startUndErgebnislistenUrl URL, unter der Start- und Ergebnislisten veröffentlicht werden.
 * @property istBoxenMoeglich Gibt an, ob eine Einstallung/Boxenreservierung möglich ist.
 * @property anmerkungBoxen Details zu den Boxen (Anzahl, Preis, Reservierung etc.).
 * @property defaultDauerProStarterSek Standarddauer pro Starter in Sekunden (kann von Bewerb/Abteilung überschrieben werden).
 * @property defaultUmbauzeitMin Standard-Umbauzeit zwischen Bewerben in Minuten.
 * @property defaultBesichtigungszeitMin Standard-Besichtigungszeit (z.B. Parcours) in Minuten.
 * @property defaultStechzeitMin Standard-Zusatzzeit für ein Stechen in Minuten.
 * @property statusTurnier Aktueller Status des Turniers (analog zu EventStatusE, aber für das Turnier).
 * @property berichtAnmerkungDopingkontrollen Textfeld für den Turnierbericht.
 * @property berichtDopingboxVorhanden Boolean für den Turnierbericht.
 * @property berichtPferdepasskontrollenBeiBewerb Textfeld für den Turnierbericht.
 * @property berichtPferdepasskontrollenBeanstandungen Textfeld für den Turnierbericht.
 * @property berichtAnmerkungMeldestelleFunktion Textfeld für den Turnierbericht.
 * @property berichtAnmerkungZeitnehmungFunktion Textfeld für den Turnierbericht.
 * @property berichtAnzeigetafelInfo Textfeld für den Turnierbericht.
 * @property berichtZuschauertribueneInfo Textfeld für den Turnierbericht.
 * @property berichtStallungenInfo Textfeld für den Turnierbericht.
 * @property berichtAblaufBesonderheitenUnfaelleProteste Textfeld für den Turnierbericht.
 * @property berichtBesondereVorkommnisse Textfeld für den Turnierbericht.
 * @property berichtVerbesserungsvorschlaege Textfeld für den Turnierbericht.
 * @property berichtGesamteindruck Textfeld für den Turnierbericht.
 * @property berichtNennlisteMitSperrlisteGeprueft Boolean für den Turnierbericht.
 * @property berichtNennlisteBeanstandungen Textfeld für den Turnierbericht.
 * @property createdAt Zeitstempel der Erstellung dieses Datensatzes.
 * @property updatedAt Zeitstempel der letzten Aktualisierung dieses Datensatzes.
 */
@Serializable
data class Turnier_OEPS( // Behält deinen Namen bei
    @Serializable(with = UuidSerializer::class)
    val turnierOepsId: Uuid = uuid4(),

    @Serializable(with = UuidSerializer::class)
    val veranstaltungsRahmenId: Uuid,

    var oepsTurnierNr: String,
    var titel: String,
    var untertitel: String? = null,
    var hauptsparte: SparteE,

    // Hier verwenden wir direkt List<Uuid>. Der Serializer für Uuid selbst (@Serializable(with = UuidSerializer::class) an der Uuid-Klasse)
    // sollte es kotlinx.serialization ermöglichen, auch Listen davon zu behandeln.
    var oetoKategorieStammdatenIds: List<@Serializable(with = UuidSerializer::class) Uuid>,

    var regelwerkTyp: RegelwerkTypE = RegelwerkTypE.OETO,
    @Serializable(with = KotlinLocalDateSerializer::class)
    var datumVon: LocalDate,
    @Serializable(with = KotlinLocalDateSerializer::class)
    var datumBis: LocalDate,

    @Serializable(with = KotlinLocalDateTimeSerializer::class)
    var nennschlussOffiziell: LocalDateTime? = null, // Nur der offizielle Nennschluss lt. Ausschreibung

    var pdfAusschreibungUrl: String? = null,
    var kommentarIntern: String? = null,
    var typNationalInternational: String = "National",
    var spracheDefault: String = "Deutsch",
    var logoTurnierUrl: String? = null,

    @Serializable(with = UuidSerializer::class)
    var turnierleiterDomPersonId: Uuid? = null,
    @Serializable(with = UuidSerializer::class)
    var turnierbeauftragterDomPersonId: Uuid? = null,

    var meldestelleTelefon: String? = null,
    var meldestelleOeffnungszeiten: String? = null,
    var startUndErgebnislistenUrl: String? = null, // Kann auch dynamisch generiert werden

    var statusTurnier: EventStatusE = EventStatusE.IN_PLANUNG, // Wiederverwendung EventStatusE

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
