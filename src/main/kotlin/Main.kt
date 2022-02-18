import Cards.allNames
import com.willowtreeapps.fuzzywuzzy.diffutils.FuzzySearch

fun main() {
    println(FuzzySearch.extractOne("wax on", allNames))

    println(FuzzySearch.extractOne("Ride the Tailwind", allNames))
    println(FuzzySearch.extractOne("Ride the Tail Wind", allNames))
    println(FuzzySearch.extractOne("Ride the Tailwinds", allNames))

    println(FuzzySearch.extractOne("young Ira", allNames))
    println(FuzzySearch.extractOne("young Benji", allNames))
    println(FuzzySearch.extractOne("young Lexi", allNames))
    println(FuzzySearch.extractOne("young Bravo", allNames))
    println(FuzzySearch.extractOne("t-bone", allNames))
    println(FuzzySearch.extractOne("arclight sentinel", allNames))
    println(FuzzySearch.extractOne("runechant", allNames))
    println(FuzzySearch.extractOne("pulse of c", allNames))
    println(FuzzySearch.extractOne("tailwinds", allNames))
    println(FuzzySearch.extractOne("tome of", allNames))
}