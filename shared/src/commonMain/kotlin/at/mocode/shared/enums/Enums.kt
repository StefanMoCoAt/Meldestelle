package at.mocode.shared.enums

import kotlinx.serialization.Serializable

@Serializable
enum class RegelwerkTypE { OETO, FEI, SONSTIGE }

@Serializable
enum class DatenQuelleE { OEPS_ZNS, MANUELL }

@Serializable
enum class VerbandE { OEPS, FEI, SONSTIGE }

@Serializable
enum class CupSerieTypE { CUP_SERIE }


@Serializable
enum class LizenzKategorieE { REITERLIZENZ, FAHRERLIZENZ, STARTKARTE }
@Serializable
enum class LizenzTypE { REITER, FAHRER, VOLTIGIERER, WESTERN, WORKING_EQUITATION, POLO, STARTKARTE_ALLG, STARTKARTE_VOLTIGIEREN, STARTKARTE_WESTERN, STARTKARTE_ISLAND, STARTKARTE_FAHREN_JUGEND, STARTKARTE_HORSEBALL, STARTKARTE_POLO, PARAEQUESTRIAN, SONSTIGE }


@Serializable
enum class SportfachStammdatenTypE { DRESSURAUFGABE, WERTUNGSVERFAHREN_SPRINGEN, WERTUNGSVERFAHREN_DRESSUR, BEWERBSKLASSE, BEWERBSKATEGORIE_OETO }

@Serializable
enum class PruefungsViereckE { VIERECK_20X40, VIERECK_20X60 }

@Serializable
enum class RichtverfahrenModusE { GM, GT }

@Serializable
enum class ArtDesStechensE { EINFACHES_STECHEN }

@Serializable
enum class PferdeGeschlechtE {
    HENGST, STUTE, WALLACH, UNBEKANNT
}

@Serializable
enum class EventStatusE { IN_PLANUNG, AKTIV, BEENDET }

@Serializable
enum class NennungsArtE {
    ONLINE_PORTAL, EZNS_OEPS, EMAIL, TELEFON, FAX, VOR_ORT
}

@Serializable
enum class VeranstalterTypE { VEREIN, FIRMA, PRIVATPERSON, SONSTIGE, UNBEKANNT }
@Serializable
enum class PlatzTypE { AUSTRAGUNG, VORBEREITUNG, LONGIEREN, SONSTIGES }

@Serializable
enum class SparteE { DRESSUR, SPRINGEN, VIELSEITIGKEIT, FAHREN, VOLTIGIEREN, WESTERN, DISTANZ, ISLAND, PFERDESPORT_SPIEL, BASIS, KOMBINIERT, SONSTIGES }

@Serializable
enum class BewerbStatus { GEPLANT, OFFEN_FUER_NENNUNG, GESCHLOSSEN_FUER_NENNUNG, LAEUFT, ABGESCHLOSSEN, ABGESAGT }
@Serializable
enum class Bedingungstyp { LIZENZ_REITER, LIZENZ_FAHRER, ALTER_PFERD, ALTER_REITER, RASSE_PFERD, GESCHLECHT_PFERD, GESCHLECHT_REITER, STARTKARTE, SONSTIGES }
@Serializable
enum class BeginnzeitTypE { FIX_UM, NACH_BEWERB, CA_UM, ANSCHLIESSEND }
@Serializable
enum class Operator { GLEICH, UNGLEICH, MINDESTENS, MAXIMAL, ZWISCHEN, IN_LISTE, NICHT_IN_LISTE }
@Serializable
enum class FunktionaerRolle { RICHTER, PARCOURSBAUER, PARCOURSBAU_ASSISTENT, TECHN_DELEGIERTER, TURNIERBEAUFTRAGTER, STEWARD, ZEITNEHMER, SCHREIBER, VERANSTALTER_KONTAKT, TURNIERLEITER, HELFER, SONSTIGE }

@Serializable
enum class RichterPositionE { C, E, H, M, B, VORSITZ, SEITENRICHTER, SONSTIGE }
@Serializable
enum class GeschlechtE { M, W, D, UNBEKANNT }
@Serializable
enum class GeschlechtPferdE { HENGST, STUTE, WALLACH, UNBEKANNT }
