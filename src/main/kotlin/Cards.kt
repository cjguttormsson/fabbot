import com.willowtreeapps.fuzzywuzzy.diffutils.FuzzySearch
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

/** Database bindings for cards.db, which contains the table of cards used to respond to queries */
object Cards : IdTable<String>() {
    val setCode = char("set_code", 3)
    val setIndex = integer("set_index").check("CHECK_SET_INDEX") { it.greaterEq(0) }
    val name = text("name").index("INDEX_NAME") // with pitch value suffix (eg. "(3)") removed
    val pitchValue = integer("pitch_value").check("CHECK_PITCH") { it.between(1, 3) }.nullable()
    val imageId = text("image_id")

    override val primaryKey = PrimaryKey(setCode, setIndex)
    override val id: Column<EntityID<String>> = imageId.entityId()

    init {
        Database.connect("jdbc:sqlite:file:cards.db", "org.sqlite.JDBC")
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        transaction {
            SchemaUtils.createMissingTablesAndColumns(Cards)
        }
    }

    // All unique card names (cards with multiple pitch values only appear once)
    val allNames by lazy { transaction { selectAll().map { it[name] } }.toSet() }
}

/** A Flesh and Blood card, as stored in the database defined by Cards. */
class Card(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, Card>(Cards) {
        // Finds the card with the closest name to the `query` string, and optionally with the
        // specified pitch value.
        fun search(query: String, pitch: Int? = null) = transaction {
            find {
                (Cards.name eq (FuzzySearch.extractOne(query, Cards.allNames).string
                    ?: "")) and (booleanLiteral(pitch == null) or (Cards.pitchValue eq pitch))
            }.first()
        }
    }

    val setCode by Cards.setCode // eg. "EVR"
    val setIndex by Cards.setIndex // eg. 50
    val name by Cards.name // eg. "Wax On"
    val pitchValue by Cards.pitchValue // eg. 1
    val imageId by Cards.imageId // eg. "EVR050"

    // The official image source from LSS
    val imageUrl by lazy { "https://storage.googleapis.com/fabmaster/media/images/$imageId.width-450.png" }

    // Some cards specify a pitch value even though they only come in one, eg. MON007: Herald of Judgement
    fun hasOtherPitchValues() =
        pitchValue != null && transaction { Cards.select { Cards.name eq name }.count() > 1 }

    // Returns the set of cards with the same name, but a different pitch value
    fun pitchVariations() = transaction {
        Card.find { (Cards.name eq name) and (Cards.pitchValue neq pitchValue) }.toSet()
    }

    // A name that includes the set code and pitch, eg. "EVR050: Wax On (1)" or "MON002: Prism"
    override fun toString() =
        "$setCode${"%03d".format(setIndex)}: $name${this.pitchValue?.let { " (%d)".format(it) } ?: ""}"
}