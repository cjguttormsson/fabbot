import com.willowtreeapps.fuzzywuzzy.diffutils.FuzzySearch
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

// Database bindings for cards.db, which contains the table of cards used to respond to queries
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

    val allNames by lazy { transaction { selectAll().map { it[name] } } }
}

class Card(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, Card>(Cards) {
        fun searchByName(query: String) = transaction {
            find {
                Cards.name eq (FuzzySearch.extractOne(query, Cards.allNames).string ?: "")
            }.firstOrNull()
        }
    }

    val setCode by Cards.setCode
    val setIndex by Cards.setIndex
    val name by Cards.name
    val pitchValue by Cards.pitchValue
    val imageId by Cards.imageId

    val imageUrl by lazy { "https://storage.googleapis.com/fabmaster/media/images/$imageId.width-450.png" }

    override fun toString() =
        "$setCode$setIndex: $name${this.pitchValue?.let { " (%d)".format(it) } ?: ""}"
}