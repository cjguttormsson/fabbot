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
        println("${"%20s".format(it)} -> ${Card.search(it)}")
    }
    listOf(null, 1, 2, 3).forEach { println("${"%20s".format(it)} -> ${Card.search("wax on", it)}") }
    println(Card.search("Bingo")?.hasOtherPitchValues())
}