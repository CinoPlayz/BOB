import java.io.InputStream

enum class Symbol {
    ARROW, ASSIGN, BEND, BOX, BRIDGE, CIRC, COMMA, CROSSING, DEFINE, // let
    DIVIDES, EOF, INFRASTRUCTURE, INTDIVIDES, LCURLY, LINE, LPAREN, LSQUARE, MINUS, NAME, NULL, PERON, PLATFORM, PLUS, POW, RCURLY, REAL, RPAREN, RSQUARE, SEMICOLON, SKIP, STATION, SWITCH, TIMES, TRACK, TRAIN, TUNNEL, VAR,
}

const val EOF = -1
const val ERROR_STATE = 0
const val NEWLINE = '\n'.code


interface DFA {
    val states: Set<Int>
    val alphabet: IntRange
    fun next(state: Int, code: Int): Int
    fun symbol(state: Int): Symbol
    val startState: Int
    val finalStates: Set<Int>
}

object RailwayAutomaton : DFA {
    override val states = (1..103).toSet()
    override val alphabet = 0..255
    override val startState = 1


    override val finalStates = setOf(
        5,
        7,
        12,
        16,
        23,
        37,
        40,
        43,
        50,
        55,
        60,
        62,
        67,
        68,
        72,
        73,
        74,
        75,
        77,
        78,
        79,
        80,
        81,
        82,
        83,
        84,
        85,
        86,
        87,
        88,
        89,
        90,
        91,
        92,
        94,
        95,
        103
    )

    private val numberOfStates = states.max() + 1 // plus the ERROR_STATE
    private val numberOfCodes = alphabet.max() + 2 // plus the EOF
    private val transitions = Array(numberOfStates) { IntArray(numberOfCodes) }
    private val values = Array(numberOfStates) { Symbol.SKIP }
    private val alphabetLowerCharSet: Set<Char> = ('a'..'z').toSet()
    private val alphabetUpperCharSet: Set<Char> = ('A'..'Z').toSet()
    private val alphabetCharSet: Set<Char> = alphabetLowerCharSet + alphabetUpperCharSet
    private val numberCharSet: Set<Char> = ('0'..'9').toSet()
    private val alphaNumberCharSet: Set<Char> = alphabetCharSet + numberCharSet

    private fun setTransition(from: Int, chr: Char, to: Int) {
        transitions[from][chr.code + 1] = to
    }

    private fun setTransitionElseVariable(from: Int, chr: Char, to: Int) {
        alphaNumberCharSet.forEach {
            transitions[from][it.code + 1] = 68 // + 1 because EOF is -1 and the array starts at 0
        }
        transitions[from][chr.code + 1] = to
    }

    private fun setTransitionElseVariable(from: Int, chr: Char, to: Int, skipCharSet: Set<Char>) {
        alphaNumberCharSet.forEach {
            if (!skipCharSet.contains(it)) {
                transitions[from][it.code + 1] = 68 // + 1 because EOF is -1 and the array starts at 0
            }
        }
        transitions[from][chr.code + 1] = to
    }

    private fun setTransition(from: Int, code: Int, to: Int) {
        transitions[from][code + 1] = to
    }

    private fun setTransition(from: Int, charSet: Set<Char>, to: Int) {
        charSet.forEach {
            transitions[from][it.code + 1] = to // + 1 because EOF is -1 and the array starts at 0
        }
    }

    private fun setSymbol(state: Int, symbol: Symbol) {
        values[state] = symbol
    }

    override fun next(state: Int, code: Int): Int {
        assert(states.contains(state))
        assert(alphabet.contains(code))
        return transitions[state][code + 1]
    }

    override fun symbol(state: Int): Symbol {
        assert(states.contains(state))
        return values[state]
    }

    init {
        //Skip
        setTransition(1, '\n', 86)
        setTransition(1, '\r', 86)
        setTransition(1, ' ', 86)
        setTransition(1, '\t', 86)

        //.2 - IS OK
        setTransition(1, '.', 75)
        setTransition(75, '.', 76)

        //0-9
        setTransition(1, numberCharSet, 75)
        setTransition(75, numberCharSet, 75)
        setTransition(76, numberCharSet, 77)
        setTransition(77, numberCharSet, 77)

        //a-z A-Z
        setTransition(1, alphabetCharSet, 68)
        setTransition(68, alphabetCharSet, 68)


        //Reserved words

        //b
        setTransition(1, 'b', 2)

        //bend
        setTransitionElseVariable(2, 'e', 3, setOf('e', 'o', 'r'))
        setTransitionElseVariable(3, 'n', 4)
        setTransitionElseVariable(4, 'd', 5)

        //box
        setTransitionElseVariable(2, 'o', 6, setOf('e', 'o', 'r'))
        setTransitionElseVariable(6, 'x', 7)

        //bridge
        setTransitionElseVariable(2, 'r', 8, setOf('e', 'o', 'r'))
        setTransitionElseVariable(8, 'i', 9)
        setTransitionElseVariable(9, 'd', 10)
        setTransitionElseVariable(10, 'g', 11)
        setTransitionElseVariable(11, 'e', 12)


        //c
        setTransition(1, 'c', 13)

        //circ
        setTransitionElseVariable(13, 'i', 14, setOf('i', 'r'))
        setTransitionElseVariable(14, 'r', 15)
        setTransitionElseVariable(15, 'c', 16)

        //crossing
        setTransitionElseVariable(13, 'r', 17, setOf('i', 'r'))
        setTransitionElseVariable(17, 'o', 18)
        setTransitionElseVariable(18, 's', 19)
        setTransitionElseVariable(19, 's', 20)
        setTransitionElseVariable(20, 'i', 21)
        setTransitionElseVariable(21, 'n', 22)
        setTransitionElseVariable(22, 'g', 23)


        //i - infrastructure
        setTransition(1, 'i', 24)
        setTransitionElseVariable(24, 'n', 25)
        setTransitionElseVariable(25, 'f', 26)
        setTransitionElseVariable(26, 'r', 27)
        setTransitionElseVariable(27, 'a', 28)
        setTransitionElseVariable(28, 's', 29)
        setTransitionElseVariable(29, 't', 30)
        setTransitionElseVariable(30, 'r', 31)
        setTransitionElseVariable(31, 'u', 32)
        setTransitionElseVariable(32, 'c', 33)
        setTransitionElseVariable(33, 't', 34)
        setTransitionElseVariable(34, 'u', 35)
        setTransitionElseVariable(35, 'r', 36)
        setTransitionElseVariable(36, 'e', 37)


        //l
        setTransition(1, 'l', 38)

        //let
        setTransitionElseVariable(38, 'e', 39, setOf('e', 'i'))
        setTransitionElseVariable(39, 't', 40)

        //line
        setTransitionElseVariable(38, 'i', 41, setOf('e', 'i'))
        setTransitionElseVariable(41, 'n', 42)
        setTransitionElseVariable(42, 'e', 43)

        //p - platform
        setTransition(1, 'p', 96)
        setTransitionElseVariable(96, 'l', 97)
        setTransitionElseVariable(97, 'a', 98)
        setTransitionElseVariable(98, 't', 99)
        setTransitionElseVariable(99, 'f', 100)
        setTransitionElseVariable(100, 'o', 101)
        setTransitionElseVariable(101, 'r', 102)
        setTransitionElseVariable(102, 'm', 103)


        //s
        setTransition(1, 's', 44)

        //station
        setTransitionElseVariable(44, 't', 45, setOf('t', 'w'))
        setTransitionElseVariable(45, 'a', 46)
        setTransitionElseVariable(46, 't', 47)
        setTransitionElseVariable(47, 'i', 48)
        setTransitionElseVariable(48, 'o', 49)
        setTransitionElseVariable(49, 'n', 50)

        //switch
        setTransitionElseVariable(44, 'w', 51, setOf('t', 'w'))
        setTransitionElseVariable(51, 'i', 52)
        setTransitionElseVariable(52, 't', 53)
        setTransitionElseVariable(53, 'c', 54)
        setTransitionElseVariable(54, 'h', 55)


        //t
        setTransition(1, 't', 56)

        //tra
        setTransitionElseVariable(56, 'r', 57, setOf('r', 'u'))
        setTransitionElseVariable(57, 'a', 58)

        //track
        setTransitionElseVariable(58, 'c', 59, setOf('c', 'i'))
        setTransitionElseVariable(59, 'k', 60)

        //train
        setTransitionElseVariable(58, 'i', 61, setOf('c', 'i'))
        setTransitionElseVariable(61, 'n', 62)

        //tunnel
        setTransitionElseVariable(56, 'u', 63, setOf('r', 'u'))
        setTransitionElseVariable(63, 'n', 64)
        setTransitionElseVariable(64, 'n', 65)
        setTransitionElseVariable(65, 'e', 66)
        setTransitionElseVariable(66, 'l', 67)


        //n - null
        setTransition(1, 'n', 69)
        setTransitionElseVariable(69, 'u', 70)
        setTransitionElseVariable(70, 'l', 71)
        setTransitionElseVariable(71, 'l', 72)



        setTransition(1, '"', 93)
        for (n in 0..255) { //Any character inside "
            if (n != '"'.code) {
                setTransition(93, n, 93)
            }
        }
        setTransition(93, '"', 94)


        setTransition(1, '-', 73)
        setTransition(73, '>', 74)
        setTransition(1, '/', 78)
        setTransition(78, '/', 79)
        setTransition(1, '+', 80)
        setTransition(1, '*', 81)

        setTransition(1, '^', 82)
        setTransition(1, '=', 83)
        setTransition(1, ';', 84)
        setTransition(1, ',', 85)

        setTransition(1, '(', 87)
        setTransition(1, ')', 88)
        setTransition(1, '}', 89)
        setTransition(1, '{', 90)
        setTransition(1, '[', 91)
        setTransition(1, ']', 92)


        setTransition(1, numberCharSet, 75)
        setTransition(75, numberCharSet, 75)
        setTransition(75, '.', 76)
        setTransition(76, numberCharSet, 77)
        setTransition(77, numberCharSet, 77)

        // Končna stanja
        setSymbol(75, Symbol.REAL)
        setSymbol(77, Symbol.REAL)

        setTransition(1, EOF, 95)

        setSymbol(5, Symbol.BEND)
        setSymbol(7, Symbol.BOX)
        setSymbol(12, Symbol.BRIDGE)
        setSymbol(16, Symbol.CIRC)
        setSymbol(23, Symbol.CROSSING)
        setSymbol(37, Symbol.INFRASTRUCTURE)
        setSymbol(40, Symbol.DEFINE)
        setSymbol(43, Symbol.LINE)
        setSymbol(50, Symbol.STATION)
        setSymbol(55, Symbol.SWITCH)
        setSymbol(60, Symbol.TRACK)
        setSymbol(62, Symbol.TRAIN)
        setSymbol(67, Symbol.TUNNEL)

        setSymbol(68, Symbol.VAR)
        setSymbol(72, Symbol.NULL)
        setSymbol(73, Symbol.MINUS)
        setSymbol(74, Symbol.ARROW)
        setSymbol(75, Symbol.REAL)
        setSymbol(77, Symbol.REAL)
        setSymbol(78, Symbol.DIVIDES)
        setSymbol(79, Symbol.INTDIVIDES)
        setSymbol(80, Symbol.PLUS)
        setSymbol(81, Symbol.TIMES)
        setSymbol(82, Symbol.POW)
        setSymbol(83, Symbol.ASSIGN)

        setSymbol(84, Symbol.SEMICOLON)
        setSymbol(85, Symbol.COMMA)
        setSymbol(86, Symbol.SKIP) //Doesn't print

        setSymbol(87, Symbol.LPAREN)
        setSymbol(88, Symbol.RPAREN)
        setSymbol(89, Symbol.RCURLY)
        setSymbol(90, Symbol.LCURLY)
        setSymbol(91, Symbol.LSQUARE)
        setSymbol(92, Symbol.RSQUARE)
        setSymbol(94, Symbol.NAME)
        setSymbol(95, Symbol.EOF)

        setSymbol(103, Symbol.PLATFORM)
    }
}

data class Token(val symbol: Symbol, val lexeme: String, val startRow: Int, val startColumn: Int)

class Scanner(private val automaton: DFA, private val stream: InputStream) {
    private var last: Int? = null
    private var row = 1
    private var column = 1

    private fun updatePosition(code: Int) {
        if (code == NEWLINE) {
            row += 1
            column = 1
        } else {
            column += 1
        }
    }

    fun getToken(): Token {
        val startRow = row
        val startColumn = column
        val buffer = mutableListOf<Char>()

        var code = last ?: stream.read()//Last character that was processed or reads new one
        var state = automaton.startState
        while (true) {
            val nextState = automaton.next(state, code)//Transition between states
            if (nextState == ERROR_STATE) break //Current character doesn't belong to any state

            state = nextState
            updatePosition(code)
            buffer.add(code.toChar())
            code = stream.read()
        }

        //Last character that was read in loop which wasn't part of current lexeme (this causes loop break)
        last = code // The code following the current lexeme is the first code of the next lexeme


        //Checks if the current state is final state if not error
        if (automaton.finalStates.contains(state)) {
            val symbol = automaton.symbol(state) //Gets which symbol belongs to this state
            return if (symbol == Symbol.SKIP) {
                getToken()
            } else {
                val lexeme = String(buffer.toCharArray()).replace("\"", "") //Creates lexeme from buffer
                Token(symbol, lexeme, startRow, startColumn)
            }
        } else {
            throw Error("Invalid pattern at ${row}:${column}")
        }
    }

}


fun name(symbol: Symbol) = when (symbol) {
    Symbol.ARROW -> "arrow"
    Symbol.ASSIGN -> "assign"
    Symbol.BEND -> "bend"
    Symbol.BOX -> "box"
    Symbol.BRIDGE -> "bridge"
    Symbol.CIRC -> "circ"
    Symbol.COMMA -> "comma"
    Symbol.CROSSING -> "crossing"
    Symbol.DEFINE -> "define" //let
    Symbol.DIVIDES -> "divides"
    Symbol.INFRASTRUCTURE -> "infrastructure"
    Symbol.INTDIVIDES -> "integer-divides"
    Symbol.LCURLY -> "lcurly"
    Symbol.LINE -> "line"
    Symbol.LPAREN -> "lparen"
    Symbol.LSQUARE -> "lsquare"
    Symbol.MINUS -> "minus"
    Symbol.NAME -> "name"
    Symbol.NULL -> "null"
    Symbol.PERON -> "peron"
    Symbol.PLATFORM -> "platform"
    Symbol.PLUS -> "plus"
    Symbol.POW -> "pow"
    Symbol.RCURLY -> "rcurly"
    Symbol.REAL -> "real"
    Symbol.RPAREN -> "rparen"
    Symbol.RSQUARE -> "rsquare"
    Symbol.SEMICOLON -> "semicolon"
    Symbol.STATION -> "station"
    Symbol.SWITCH -> "switch"
    Symbol.TIMES -> "times"
    Symbol.TRACK -> "track"
    Symbol.TRAIN -> "train"
    Symbol.TUNNEL -> "tunnel"
    Symbol.VAR -> "variable"
    else -> throw Error("Invalid symbol")
}

fun printTokens(scanner: Scanner) {
    val tmpToken = scanner.getToken()
    if (tmpToken.symbol != Symbol.EOF) {
        print("${name(tmpToken.symbol)}(\"${tmpToken.lexeme}\") ")
        printTokens(scanner)
    }
}