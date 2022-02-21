import com.github.h0tk3y.betterParse.grammar.parseToEnd
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class QueryParserTest {
    @Test
    fun `parse single word query`() {
        assertEquals(SearchQuery("pummel", null), QueryParser.parseToEnd("pummel"))
    }

    @Test
    fun `parse multi-word query`() {
        assertEquals(SearchQuery("wax on", null), QueryParser.parseToEnd("wax on"))
    }

    @Test
    fun `parse query with full word pitch`() {
        assertEquals(SearchQuery("wax on", 2), QueryParser.parseToEnd("wax on yellow"))
    }

    @Test
    fun `parse query with abbreviated pitch`() {
        assertEquals(SearchQuery("wax on", 3), QueryParser.parseToEnd("wax on b"))
    }

    @Test
    fun `parse query with card name that contains a pitch word`() {
        assertEquals(
            SearchQuery("red in the ledger", null), QueryParser.parseToEnd("red in the ledger")
        )
    }

    @Test
    fun `parse query with card name ending in pitch token`() {
        // Ensure that the 'r' isn't picked off the end, ie. SearchQuery("shimme", 1)
        assertEquals(SearchQuery("shimmer", null), QueryParser.parseToEnd("shimmer"))
    }
}