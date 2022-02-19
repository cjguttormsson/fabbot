import com.willowtreeapps.fuzzywuzzy.diffutils.FuzzySearch

fun main() {
    listOf(
        "wax on",

        "Ride the Tailwind",
        "Ride the Tail Wind",
        "Ride the Tailwinds",

        "Ira",
        "Benji",
        "young Lexi",
        "young Bravo",
        "t-bone",
        "arclight sentinel",
        "runechant",
        "pulse of c",
        "tailwinds",
        "tome of"
    ).forEach {
        println("$it\t-> ${FuzzySearch.extractOne(it, Cards.allNames)}")
    }
}