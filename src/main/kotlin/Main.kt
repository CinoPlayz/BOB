import java.io.File
import java.io.OutputStreamWriter


fun main(args: Array<String>) {

    val input = File(args[0])
    //printTokens(Scanner(RailwayAutomaton, input.readText().byteInputStream()))

    val parser = Parser(Scanner(RailwayAutomaton, input.readText().byteInputStream()))
    val status = parser.parse()
    if (status.first) {
        println("accept")
        println(status.second.eval(mutableMapOf()))
    } else {
        println("reject")
    }
}