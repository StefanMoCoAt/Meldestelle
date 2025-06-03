package at.mocode.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import at.mocode.model.Turnier as TurnierModel
import at.mocode.model.Bewerb as BewerbModel

/**
 * DAO for Turnier (Tournament) entity
 */
class TurnierEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TurnierEntity>(Turniere)

    var name by Turniere.name
    var datum by Turniere.datum
    var number by Turniere.number
    val bewerbe by BewerbEntity referrersOn Bewerbe.turnierId

    /**
     * Converts this entity to a domain model
     */
    fun toModel(): TurnierModel {
        return TurnierModel(
            name = name,
            datum = datum,
            number = number,
            bewerbe = bewerbe.map { it.toModel() }
        )
    }
}

/**
 * DAO for Bewerb (Competition) entity
 */
class BewerbEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<BewerbEntity>(Bewerbe)

    var nummer by Bewerbe.nummer
    var titel by Bewerbe.titel
    var klasse by Bewerbe.klasse
    var task by Bewerbe.task
    var turnier by TurnierEntity referencedOn Bewerbe.turnierId

    /**
     * Converts this entity to a domain model
     */
    fun toModel(): BewerbModel {
        return BewerbModel(
            nummer = nummer,
            titel = titel,
            klasse = klasse,
            task = task
        )
    }
}
