import com.github.h0tk3y.betterParse.grammar.parseToEnd
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class QueryParserTest {
    @Test
    fun `parse single word query`() {
        val (cardName, pitch) = QueryParser.parseToEnd("pummel")
        assertEquals("pummel", cardName)
        assertNull(pitch)
    }

    @Test
    fun `parse multi-word query`() {
        val (cardName, pitch) = QueryParser.parseToEnd("wax on")
        assertEquals("wax on", cardName)
        assertNull(pitch)
    }

    @Test
    fun `parse query with pitch`() {
        val (cardName, pitch) = QueryParser.parseToEnd("wax on yellow")
        assertEquals("wax on", cardName)
        assertEquals(2, pitch)
    }
}