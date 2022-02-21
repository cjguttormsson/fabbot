import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.rightAssociative
import com.github.h0tk3y.betterParse.combinators.use
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.utils.Tuple2
import dev.kord.core.Kord
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.reply
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.on
import kotlinx.coroutines.flow.count
import java.lang.System.getenv

// Regexes used to parse incoming queries
val bracket_pattern = Regex("""\[\[([^]\[]+)]]""") // matches any text in [[double brackets]]
val query_pitch_values = mapOf("r" to 1, "red" to 1, "y" to 2, "yellow" to 2, "b" to 3, "blue" to 3)

// Maps used to handle pitch value reactions
val pitch_value_to_reaction_emoji = mapOf(
    1 to ReactionEmoji.Unicode("\uD83D\uDD34"),
    2 to ReactionEmoji.Unicode("\uD83D\uDFE1"),
    3 to ReactionEmoji.Unicode("\uD83D\uDD35")
)
val reaction_emoji_to_pitch_value =
    pitch_value_to_reaction_emoji.entries.associate { (k, v) -> v to k }

suspend fun main() {
    val client = Kord(getenv("DISCORD_BOT_KEY"))

    client.on<ReadyEvent> {
        println("Logged in as ${client.getSelf().username}")
        println("Currently in ${client.guilds.count()} server(s)")
    }

    // Handle incoming messages
    client.on<MessageCreateEvent> {
        // Don't respond to ourselves
        if (this.message.author?.id == client.selfId) return@on

        // Respond to up to three search terms in a message
        bracket_pattern.findAll(this.message.content).flatMap { it.groupValues.drop(1) }.take(3)
            .forEach {
                // Reply with the image of the card
                val (name, pitch) = QueryParser.parseToEnd(it)
                println("Searching name=$name pitch=$pitch")
                val card = Card.search(name, pitch)
                val reply = message.reply { content = card.imageUrl }

                // Add reactions for other pitch values of the same card, if there are any
                card.pitchVariations().forEach { variant ->
                    reply.addReaction(pitch_value_to_reaction_emoji[variant.pitchValue]!!)
                }
            }
    }

    // Handle reactions to the bot's messages
    client.on<ReactionAddEvent> {
        // Don't handle our own reactions, or reactions to messages other than ours
        if (this.userId == client.selfId || this.getMessage().author?.id != client.selfId) return@on

        // Handle pitch reactions
        if (this.emoji in reaction_emoji_to_pitch_value) {
            val targetCost = reaction_emoji_to_pitch_value[this.emoji]!!
            val currentCard = Card.fromImageUrl(getMessage().content)
            val targetCard = currentCard?.let { Card.search(it.name, targetCost) }

            // Update the message text to refer to the new card, and correct the reactions.
            targetCard?.let {
                this.getMessage().edit { content = it.imageUrl }
                this.getMessage().deleteReaction(this.emoji)
                this.getMessage()
                    .addReaction(pitch_value_to_reaction_emoji[currentCard.pitchValue]!!)
            }
        }
    }

    client.login()
}

// Parse a search query, which looks like a card name that is optionally followed by a letter or
// word to specify pitch value (eg. "wax on b")
object QueryParser : Grammar<Tuple2<String, Int?>>() {
    private val ws by regexToken("\\s+")

    private val cardNameTerm by regexToken("[^\\s]+")
    private val pitchTerm by regexToken(query_pitch_values.keys.joinToString("|"))
    private val eitherTerm by pitchTerm or cardNameTerm

    private val idk by rightAssociative(eitherTerm use { makeTup(text) },
        ws use { text }) { a, b, c -> combineTups(a, b, c) }

    override val rootParser: Parser<Tuple2<String, Int?>> by idk

    private fun makeTup(match: String) = when (match.lowercase()) {
        in query_pitch_values.keys -> Tuple2("", query_pitch_values[match.lowercase()])
        else -> Tuple2(match, null as Int?)
    }

    private fun combineTups(
        tup1: Tuple2<String, Int?>, sep: String, tup2: Tuple2<String, Int?>
    ): Tuple2<String, Int?> = when (tup2.t2) {
        null -> Tuple2(tup1.t1 + sep + tup2.t1, null)
        else -> when (tup2.t1) {
            "" -> Tuple2(tup1.t1, tup2.t2)
            else -> Tuple2(tup1.t1 + sep + tup2.t1, tup2.t2)
        }
    }
}