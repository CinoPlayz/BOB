import java.io.File
import java.io.InputStream

const val ERROR_STATE = 0

const val EOF = -1
const val BOX = 0
const val PERON = 1
const val DEFINE = 2 // let
const val VAR = 3
const val REAL = 4
const val PLUS = 5
const val MINUS = 6
const val TIMES = 7
const val DIVIDES = 8
const val INTDIVIDES = 9
const val POW = 10
const val LPAREN = 11
const val RPAREN = 12
const val ASSIGN = 13
const val SKIP = 14
const val CIRC = 15
const val INFRASTRUCTURE = 16
const val TUNEL = 17
const val MOST = 18
const val PREHOD = 19
const val VLAK = 20
const val TIR = 21
const val POSTAJA = 22
const val KRETNICA = 23
const val SEMICOLON = 24
const val COMMA = 25
const val LCURLY = 26
const val RCURLY = 27
const val NAME = 28
const val NULL = 29
const val DOT = 30
const val ARROW = 31
const val BEND = 32


const val NEWLINE = '\n'.code

interface DFA {
    val states: Set<Int>
    val alphabet: IntRange
    fun next(state: Int, code: Int): Int
    fun symbol(state: Int): Int
    val startState: Int
    val finalStates: Set<Int>
}

object LeksAutomaton : DFA {
    override val states = (1..140).toSet()
    override val alphabet = 0..255
    override val startState = 1


    override val finalStates =
        setOf(
            2,
            4,
            5,
            6,
            7,
            8,
            9,
            10,
            11,
            12,
            13,
            14,
            15,
            16,
            17,
            18,
            19,
            20,
            21,
            22,
            23,
            24,
            25,
            26,
            27,
            28,
            29,
            30,
            31,
            32,
            33,
            34,
            35,
            36,
            37,
            38,
            39,
            40,
            41,
            42,
            43,
            44,
            45,
            46,
            47,
            48,
            49,
            50,
            51,
            52,
            53,
            54,
            55,
            56,
            57,
            58,
            59,
            60,
            61,
            62,
            63,
            64,
            65,
            66,
            67,
            68,
            69,
            70,
            71,
            72,
            73,
            74,
            75,
            76,
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
            93,
            94,
            95,
            96,
            97,
            98,
            99,
            100,
            101,
            102,
            103,
            104,
            105,
            106,
            107,
            108,
            109,
            110,
            111,
            112,
            113,
            114,
            115,
            116,
            117,
            118,
            119,
            120,
            121,
            122,
            123,
            124,
            125,
            126,
            127,
            128,
            129,
            130,
            131,
            132,
            133,
            134,
            135,
            136,
            137,
            138
        )

    private val numberOfStates = states.max() + 1 // plus the ERROR_STATE
    private val numberOfCodes = alphabet.max() + 2 // plus the EOF
    private val transitions = Array(numberOfStates) { IntArray(numberOfCodes) }
    private val values = Array(numberOfStates) { SKIP }

    private fun setTransition(from: Int, chr: Char, to: Int) {
        transitions[from][chr.code + 1] = to
    }

    private fun setTransition(from: Int, code: Int, to: Int) {
        transitions[from][code + 1] = to
    }

    private fun setSymbol(state: Int, symbol: Int) {
        values[state] = symbol
    }

    override fun next(state: Int, code: Int): Int {
        assert(states.contains(state))
        assert(alphabet.contains(code))
        return transitions[state][code + 1]
    }

    override fun symbol(state: Int): Int {
        assert(states.contains(state))
        return values[state]
    }

    init {


//         skipped
        setTransition(1, '\n', 87)
        setTransition(1, '\r', 87)
        setTransition(1, ' ', 87)
        setTransition(87, '\n', 87)
        setTransition(87, '\r', 87)
        setTransition(87, ' ', 87)
        setTransition(1, '\t', 87)
        setTransition(87, '\t', 87)


        for (n in '0'..'9') {
            setTransition(1, n, 71)
            setTransition(71, n, 71)
            setTransition(72, n, 73)
            setTransition(73, n, 73)
            setTransition(69, n, 70)
            setTransition(70, n, 70)
        }


        for (n in 'a'..'z') {
            setTransition(1, n, 69)
            setTransition(69, n, 69)
            //setTransition(70, n, 70)
        }

        for (n in 'A'..'Z') {
            setTransition(1, n, 69)
            setTransition(69, n, 69)
            //setTransition(70, n, 70)
        }

        // rezervirane besede
        for (n in ('a'..'z') + ('A'..'Z') + ('0'..'9')) {
            for (m in 2..68) {
                if (m == 2 && n == 'o')//box
                    setTransition(m, n, 3)
                else if (m == 3 && n == 'x')//box
                    setTransition(m, n, 4)
                else if (m == 5 && n == 'e')//peron
                    setTransition(m, n, 6)
                else if (m == 6 && n == 'r')
                    setTransition(m, n, 7)
                else if (m == 7 && n == 'o')
                    setTransition(m, n, 8)
                else if (m == 8 && n == 'n')//peron
                    setTransition(m, n, 9)
                else if (m == 10 && n == 'e')//let
                    setTransition(m, n, 11)
                else if (m == 11 && n == 't')
                    setTransition(m, n, 12)//let
                else if (m == 13 && n == 'i')//circ
                    setTransition(m, n, 14)
                else if (m == 14 && n == 'r')
                    setTransition(m, n, 15)
                else if (m == 15 && n == 'c')
                    setTransition(m, n, 16)
                else if (m == 17 && n == 'n') // infrastruktura
                    setTransition(m, n, 18)
                else if (m == 18 && n == 'f')
                    setTransition(m, n, 19)
                else if (m == 19 && n == 'r')
                    setTransition(m, n, 20)
                else if (m == 20 && n == 'a')
                    setTransition(m, n, 21)
                else if (m == 21 && n == 's')
                    setTransition(m, n, 22)
                else if (m == 22 && n == 't')
                    setTransition(m, n, 23)
                else if (m == 23 && n == 'r')
                    setTransition(m, n, 24)
                else if (m == 24 && n == 'u')
                    setTransition(m, n, 25)
                else if (m == 25 && n == 'k')
                    setTransition(m, n, 26)
                else if (m == 26 && n == 't')
                    setTransition(m, n, 27)
                else if (m == 27 && n == 'u')
                    setTransition(m, n, 28)
                else if (m == 28 && n == 'r')
                    setTransition(m, n, 29)
                else if (m == 29 && n == 'a')
                    setTransition(m, n, 30)
                else if (m == 5 && n == 'o') // postaja
                    setTransition(m, n, 31)
                else if (m == 31 && n == 's')
                    setTransition(m, n, 32)
                else if (m == 32 && n == 't')
                    setTransition(m, n, 33)
                else if (m == 33 && n == 'a')
                    setTransition(m, n, 34)
                else if (m == 34 && n == 'j')
                    setTransition(m, n, 35)
                else if (m == 35 && n == 'a')
                    setTransition(m, n, 36) // postaja
                else if (m == 37 && n == 'r') // kretnica
                    setTransition(m, n, 38)
                else if (m == 38 && n == 'e')
                    setTransition(m, n, 39)
                else if (m == 39 && n == 't')
                    setTransition(m, n, 40)
                else if (m == 40 && n == 'n')
                    setTransition(m, n, 41)
                else if (m == 41 && n == 'i')
                    setTransition(m, n, 42)
                else if (m == 42 && n == 'c')
                    setTransition(m, n, 43)
                else if (m == 43 && n == 'a')
                    setTransition(m, n, 44) // kretnica
                else if (m == 45 && n == 'u') // tunel
                    setTransition(m, n, 46)
                else if (m == 46 && n == 'n')
                    setTransition(m, n, 47)
                else if (m == 47 && n == 'e')
                    setTransition(m, n, 48)
                else if (m == 48 && n == 'l')
                    setTransition(m, n, 49) // tunel
                else if (m == 50 && n == 'o') // most
                    setTransition(m, n, 51)
                else if (m == 51 && n == 's')
                    setTransition(m, n, 52)
                else if (m == 52 && n == 't')
                    setTransition(m, n, 53) // most
                else if (m == 5 && n == 'r') // prehod
                    setTransition(m, n, 54)
                else if (m == 54 && n == 'e')
                    setTransition(m, n, 55)
                else if (m == 55 && n == 'h')
                    setTransition(m, n, 56)
                else if (m == 56 && n == 'o')
                    setTransition(m, n, 57)
                else if (m == 57 && n == 'd')
                    setTransition(m, n, 58) // prehod
                else if (m == 59 && n == 'l') // vlak
                    setTransition(m, n, 60)
                else if (m == 60 && n == 'a')
                    setTransition(m, n, 61)
                else if (m == 61 && n == 'k')
                    setTransition(m, n, 62) // vlak
                else if (m == 45 && n == 'i') // tir
                    setTransition(m, n, 63)
                else if (m == 63 && n == 'r')
                    setTransition(m, n, 64) // tir
                else if (m == 65 && n == 'u')
                    setTransition(m, n, 66) // null
                else if (m == 66 && n == 'l')
                    setTransition(m, n, 67)
                else if (m == 67 && n == 'l')
                    setTransition(m, n, 68) // null
                else {
                    setTransition(m, n, 69)
                    setSymbol(m, VAR)
                }
            }
        }


        for (n in ('a'..'z') + ('A'..'Z') + ('0'..'9')) {
            for (m in 2..96) {
                if (m == 2 && n == 'e')
                    setTransition(m, n, 93)
                else if (m == 93 && n == 'n')
                    setTransition(m, n, 94)
                else if (m == 94 && n == 'd')
                    setTransition(m, n, 95)
            }
        }

        setTransition(1, '"', 89)
        for (n in 0..255) { // kateri koli znak znotraj narekovajev
            if (n != '"'.code) {
                setTransition(89, n, 89)
            }
        }
        setTransition(89, '"', 90)

        // začetne tranzicije rezerviranih besed
        setTransition(1, 'b', 2) // box
        setTransition(1, 'p', 5) // peron
        setTransition(1, 'l', 10) // let
        setTransition(1, 'c', 13) // circ
        setTransition(1, 'i', 17) // infrastruktura
        setTransition(1, 'k', 37) // kretnica
        setTransition(1, 't', 45) // tunel
        setTransition(1, 'm', 50) // most
        setTransition(1, 'v', 59) // vlak
        setTransition(1, 'n', 65)


        setTransition(71, '.', 72)//za realno število
        setTransition(1, '+', 74)
        setTransition(1, '-', 75)
        setTransition(1, '*', 76)
        setTransition(1, '/', 77)
        setTransition(77, '/', 78)
        setTransition(1, '^', 79)
        setTransition(1, '(', 80)
        setTransition(1, ')', 81)
        setTransition(1, '=', 82)
        setTransition(1, ';', 83)
        setTransition(1, ',', 84)
        setTransition(1, '{', 85)
        setTransition(1, '}', 86)
        setTransition(1, '.', 91)
        setTransition(75, '>', 92)

        setTransition(1, EOF, 88)
        setSymbol(4, BOX)
        setSymbol(9, PERON)
        setSymbol(12, DEFINE)
        setSymbol(16, CIRC)
        setSymbol(30, INFRASTRUCTURE)
        setSymbol(36, POSTAJA)
        setSymbol(44, KRETNICA)
        setSymbol(49, TUNEL)
        setSymbol(53, MOST)
        setSymbol(58, PREHOD)
        setSymbol(62, VLAK)
        setSymbol(64, TIR)
        setSymbol(68, NULL)

        setSymbol(69, VAR)
        setSymbol(70, VAR)
        setSymbol(71, REAL)
        setSymbol(73, REAL)
        setSymbol(74, PLUS)
        setSymbol(75, MINUS)
        setSymbol(76, TIMES)
        setSymbol(77, DIVIDES)
        setSymbol(78, INTDIVIDES)
        setSymbol(79, POW)
        setSymbol(80, LPAREN)
        setSymbol(81, RPAREN)
        setSymbol(82, ASSIGN)
        setSymbol(83, SEMICOLON)
        setSymbol(84, COMMA)
        setSymbol(85, LCURLY)
        setSymbol(86, RCURLY)
        setSymbol(87, SKIP)//se ne izpiše
        setSymbol(88, EOF)
        setSymbol(90, NAME)
        setSymbol(91, DOT)
        setSymbol(92, ARROW)
        setSymbol(95, BEND)


    }
}

data class Token(val symbol: Int, val lexeme: String, val startRow: Int, val startColumn: Int)

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

        var code = last ?: stream.read()//zadnji znak ki ni bil obdelan ali pa prebere novega
        var state = automaton.startState
        while (true) {
            val nextState = automaton.next(state, code)//prehodi med stanji
            if (nextState == ERROR_STATE) break //trenutni znak ne ustreza nobenemmu vzorcu

            state = nextState
            updatePosition(code)
            buffer.add(code.toChar())
            code = stream.read()
        }
        //Po izhodu iz zanke se zadnji prebrani znak, ki ni bil del trenutnega leksema (ker je povzročil prekinitev zanke), shrani v last.
        last = code // The code following the current lexeme is the first code of the next lexeme


        //preverja, ali je trenutno stanje (state) eno izmed končnih stanj avtomata (DFA).
        //Končna stanja so tista, ki predstavljajo uspešno prepoznane lekseme.
        if (automaton.finalStates.contains(state)) {
            val symbol = automaton.symbol(state)//določa, kateri vrsti tokena pripada prepoznani leksem.
            return if (symbol == SKIP) {
                getToken()
            } else {
                val lexeme = String(buffer.toCharArray())//sestavi se leksem iz znakov v buffer
                Token(symbol, lexeme, startRow, startColumn)//vrne se kot rezultat getToken
            }
        } else {
            throw Error("Invalid pattern at ${row}:${column}")
        }
    }

}


fun name(symbol: Int) =
    when (symbol) {
        REAL -> "real"
        VAR -> "variable"
        PLUS -> "plus"
        MINUS -> "minus"
        TIMES -> "times"
        DIVIDES -> "divides"
        INTDIVIDES -> "integer-divides"
        POW -> "pow"
        LPAREN -> "lparen"
        RPAREN -> "rparen"
        ASSIGN -> "assign"
        DEFINE -> "define" //let
        BOX -> "box"
        PERON -> "peron"
        CIRC -> "circ"
        INFRASTRUCTURE -> "infrastructure"
        KRETNICA -> "kretnica"
        TUNEL -> "tunel"
        MOST -> "most"
        PREHOD -> "prehod"
        VLAK -> "vlak"
        TIR -> "tir"
        POSTAJA -> "postaja"
        LCURLY -> "lcurly"
        RCURLY -> "rcurly"
        COMMA -> "comma"
        SEMICOLON -> "semicolon"
        NAME -> "name"
        NULL -> "null"
        DOT -> "dot"
        ARROW -> "arrow"
        BEND -> "bend"
        else -> throw Error("Invalid symbol")
    }


fun printTokens(scanner: Scanner) {
    val tmpToken = scanner.getToken()
    if (tmpToken.symbol != EOF) {
        print("${name(tmpToken.symbol)}(\"${tmpToken.lexeme}\") ")
        printTokens(scanner)
    }
}

fun main(args: Array<String>) {

    val input = File(args[0])
    printTokens(Scanner(LeksAutomaton, input.readText().byteInputStream()))
}