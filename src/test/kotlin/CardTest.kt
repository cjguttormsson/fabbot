import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

/** Tests for the Card class. */
internal class CardTest {
    companion object {
        @JvmStatic
        fun allCardNames() = Cards.allNames

        @JvmStatic
        fun misspelledCardNames() = listOf(
            arguments("ride the tailwinds", "Ride the Tailwind"),
            arguments("ride the tail wind", "Ride the Tailwind"),
            arguments("tailwinds", "Ride the Tailwind"),
            arguments("arclight sentinel", "Arc Light Sentinel"),
            arguments("tbone", "T-Bone"),
            arguments("t bone", "T-Bone"),
            arguments("metacarpus nodes", "Metacarpus Node"),
            arguments("starvo", "Bravo, Star of the Show"),
            arguments("younghim", "Oldhim"),
            arguments("bravold", "Bravo")
        )

        @JvmStatic
        fun partialCardNames() = listOf(
            arguments("ride the tail", "Ride the Tailwind"),
            arguments("pulse of c", "Pulse of Candlehold"),
            arguments("ira", "Ira, Crimson Haze"),
            arguments("benji", "Benji, the Piercing Wind"),
            arguments("embodiment", "Embodiment of Earth"),
            arguments("gloomveil", "Swarming Gloomveil"),
            arguments("meat and", "Meat and Greet"),
            arguments("drowning", "Drowning Dire"),
            arguments("balance", "Talisman of Balance"),
            arguments("kav", "Kavdaen, Trader of Skins"),
            arguments("dori", "Dorinthea Ironsong")
        )
    }

    @ParameterizedTest
    @MethodSource("allCardNames")
    fun `search by exact name`(name: String) {
        assertEquals(name, Card.search(name)?.name)
    }

    @ParameterizedTest
    @MethodSource("misspelledCardNames")
    fun `search by misspelled card name`(query: String, expected: String) {
        assertEquals(expected, Card.search(query)?.name)
    }

    @ParameterizedTest
    @MethodSource("partialCardNames")
    fun `search by partial card name`(query: String, expected: String) {
        assertEquals(expected, Card.search(query)?.name)
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3])
    fun `search with pitch`(pitch: Int?) {
        assertEquals(pitch, Card.search("Wax On", pitch)?.pitchValue)
    }

    @Test
    fun `search without pitch`() {
        assertEquals(1, Card.search("Wax On")?.pitchValue)
    }

    @Test
    fun `toString() matches expected format`() {
        assertEquals("EVR051: Wax On (2)", Card.search("Wax On", 2)?.toString())
    }
}