package at.mocode.model

//import kotlinx.serialization.Serializable
//
///**
// * Represents a tournament (Turnier) with its details and associated competitions (Bewerbe).
// * Each tournament can have one or more competitions.
// */
//@Serializable
//data class Turnier(
//    /** The name of the tournament, e.g. "CSN-C NEU CSNP-C NEU NEUMARKT/M., OÖ" */
//    val name: String,
//
//    /** The date of the tournament as a formatted string, e.g. "7.JUNI 2025" */
//    val datum: String,
//
//    /** Unique identifier for the tournament */
//    val number: Int,
//
//    /** List of competitions (Bewerbe) associated with this tournament */
//    var bewerbe: List<Bewerb>
//)
//
///**
// * Represents a competition (Bewerb) within a tournament.
// * A competition has specific details like number, title, class, and optional task.
// */
//@Serializable
//data class Bewerb(
//    /** Competition number, e.g. 1, 2, etc. */
//    val nummer: Int,
//
//    /** Title of the competition, e.g. "Stilspringprüfung" or "Dressurprüfung" */
//    val titel: String,
//
//    /** Class/level of the competition, e.g. "60 cm" or "Kl. A" */
//    val klasse: String,
//
//    /** Optional task identifier, e.g. "DRA 1" */
//    val task: String?
//)
