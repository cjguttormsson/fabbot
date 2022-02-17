import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

fun main() {
    Cards.init()
    transaction {
        Cards.select { Cards.name eq "Wax On" }.forEach(::println)
    }
}