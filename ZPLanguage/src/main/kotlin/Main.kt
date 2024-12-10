import java.io.File


fun main(args: Array<String>) {

    val input = File(args[0])
    val output = File(args[1])
    //printTokens(Scanner(RailwayAutomaton, input.readText().byteInputStream()))

    val parser = Parser(Scanner(RailwayAutomaton, input.readText().byteInputStream()))
    val status = parser.parse()
    if (status.first) {
        println("accept")
        val geoJson = status.second.eval(mutableMapOf())
        output.writeText(geoJson)
    } else {
        println("reject")
    }
}