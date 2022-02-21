import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.use
import com.github.h0tk3y.betterParse.combinators.zeroOrMore
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.lexer.regexToken
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

                try {
                    val card = Card.search(name, pitch)
                    val reply = message.reply { content = card.imageUrl }

                    // Add reactions for other pitch values of the same card, if there are any
                    card.pitchVariations().forEach { variant ->
                        reply.addReaction(pitch_value_to_reaction_emoji[variant.pitchValue]!!)
                    }
                } catch (e: NoSuchElementException) {
                    message.reply {
                        content =
                            "I couldn't find anything matching the name \"$name\" with pitch value $pitch"
                    }
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
data class SearchQuery(val cardName: String, val pitch: Int?)

object QueryParser : Grammar<SearchQuery>() {
    private val space by regexToken("\\s+")
    private val word by regexToken("[^\\s]+")

    private val firstWords by zeroOrMore(word and space) use { joinToString("") { it.t1.text + it.t2.text } }
    private val lastWord by word use { text }

    override val rootParser by firstWords and lastWord use { makeQuery(t1, t2) }

    // If the last word in the query is a pitch value specifier, extract it. Otherwise, add the last
    // word back to the rest of the words.
    private fun makeQuery(firstWords: String, lastWords: String) = when (lastWords.lowercase()) {
        in query_pitch_values.keys -> SearchQuery(
            firstWords.trim(), query_pitch_values[lastWords.lowercase()]
        )
        else -> SearchQuery(firstWords + lastWords, null as Int?)
    }
}