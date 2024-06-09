class Parser(private val scanner: Scanner) {
    private var currentToken: Token? = null

    fun parse(): Pair<Boolean, RailwayAST.Expr> {
        currentToken = scanner.getToken()
        val program = program()
        return Pair(program.first && currentToken?.symbol == Symbol.EOF, program.second)
    }

    private fun program(): Pair<Boolean, RailwayAST.Expr> {
        return infra()
    }

    private fun infra(): Pair<Boolean, RailwayAST.Expr> {
        if (currentToken?.symbol == Symbol.INFRASTRUCTURE) {
            currentToken = scanner.getToken()
            if (currentToken?.symbol == Symbol.NAME) {
                val name = currentToken?.lexeme
                currentToken = scanner.getToken()
                if (currentToken?.symbol == Symbol.LCURLY) {
                    currentToken = scanner.getToken()
                    val exprs = exprs()
                    if (exprs.first && currentToken?.symbol == Symbol.RCURLY) {
                        currentToken = scanner.getToken()
                        return Pair(true, RailwayAST.Infrastructure(name ?: "", *exprs.second.toTypedArray()))
                    }
                }
            }
        }
        return Pair(false, RailwayAST.Coordinates(0.0f, 0.0f))
    }

    private fun exprs(): Pair<Boolean, List<RailwayAST.Expr>> {
        var expr = expr()
        val listOfExpr = mutableListOf<RailwayAST.Expr>()
        if (expr.first) {
            while (expr.first) {
                //Loop to check if there are more expr
                listOfExpr.add(expr.second)
                expr = expr()

            }
            return Pair(true, listOfExpr)
        }
        return Pair(false, listOfExpr)
    }

    private fun expr(): Pair<Boolean, RailwayAST.Expr> {
        if (currentToken?.symbol == Symbol.DEFINE) {
            currentToken = scanner.getToken()
            if (currentToken?.symbol == Symbol.VAR) {
                currentToken = scanner.getToken()
                if (currentToken?.symbol == Symbol.ASSIGN) {
                    currentToken = scanner.getToken()
                    var tmp = comp()
                    return tmp
                }
            }
        }
        return comp()

    }

    private fun comp(): Pair<Boolean, RailwayAST.Expr> {
        //return station().first || track() || switch() || tunnel() || bridge() || crossing() || train() || additive()
        val station = station()
        return Pair(station.first, station.second)
    }

    private fun station(): Pair<Boolean, RailwayAST.Expr> {
        if (currentToken?.symbol == Symbol.STATION) {
            currentToken = scanner.getToken()
            if (currentToken?.symbol == Symbol.NAME) {
                val name = currentToken?.lexeme

                currentToken = scanner.getToken()
                if (currentToken?.symbol == Symbol.LCURLY) {
                    currentToken = scanner.getToken()
                    val shapesStatus = shapesmul(0.0f)
                    val platformStatus = platformmul(name ?: "")
                    if (shapesStatus.first && platformStatus.first) {

                        while (currentToken?.symbol != Symbol.RCURLY) {
                            if (!shapesmul(0.0f).first && !platformmul(name ?: "").first) {
                                break
                            }
                        }

                        if (currentToken?.symbol == Symbol.RCURLY) {
                            currentToken = scanner.getToken()
                            return Pair(true, RailwayAST.Station(name ?: "", shapesStatus.second, *platformStatus.second.toTypedArray()))
                        }
                    }
                }
            }
        }
        return Pair(false, RailwayAST.Coordinates(0.0f, 0.0f))
    }

    private fun platformmul(stationName: String): Pair<Boolean, List<RailwayAST.Platform>> {
        var platform = platform(stationName)
        val listOfPlatforms = mutableListOf<RailwayAST.Platform>()

        if (platform.first) {
            while (platform.first) {
                //Loop for checking if there are more platforms
                listOfPlatforms.add(platform.second)
                platform = platform(stationName)
            }
            return Pair(true, listOfPlatforms)
        }
        return Pair(false, listOf())
    }

    private fun platform(stationName: String): Pair<Boolean, RailwayAST.Platform> {
        if (currentToken?.symbol == Symbol.PLATFORM) {
            currentToken = scanner.getToken()
            if (currentToken?.symbol == Symbol.REAL) {
                val number = currentToken?.lexeme
                currentToken = scanner.getToken()
                if (currentToken?.symbol == Symbol.COMMA) {
                    currentToken = scanner.getToken()
                    if (currentToken?.symbol == Symbol.REAL) {
                        val countOfTrack = currentToken?.lexeme
                        currentToken = scanner.getToken()
                        if (currentToken?.symbol == Symbol.LCURLY) {
                            currentToken = scanner.getToken()

                            val shapesMul = shapesmul(0.0f)
                            if (shapesMul.first) {
                                if (currentToken?.symbol == Symbol.RCURLY) {
                                    currentToken = scanner.getToken()
                                    return Pair(true, RailwayAST.Platform(number ?: "1", countOfTrack ?: "1", stationName, shapesMul.second))
                                }
                            }
                        }
                    }
                }
            }
        }
        return Pair(false, RailwayAST.Platform("", "", stationName, errorshapesmul().second))
    }

    private fun shapesmul(inLinkValue: Float): Pair<Boolean, RailwayAST.ShapesMul> {
        var shape = shapes(inLinkValue)
        val listOfShapes = mutableListOf<RailwayAST.Shape>()
        if (shape.first) {
            while (shape.first) {
                //Loop to check if there are more shapes
                listOfShapes.add(shape.second)
                shape = shapes(inLinkValue)
            }
            return Pair(true, RailwayAST.ShapesMul(*listOfShapes.toTypedArray()))
        }
        return errorshapesmul()
    }

    private fun errorshapesmul(): Pair<Boolean, RailwayAST.ShapesMul> {
        return Pair(false, RailwayAST.ShapesMul())
    }

    private fun shapes(inLinkValue: Float): Pair<Boolean, RailwayAST.Shape> {
        return when (currentToken?.symbol) {

            Symbol.BOX -> {
                currentToken = scanner.getToken()
                return box(inLinkValue)
            }

            /*Symbol.CIRC -> {
                currentToken = scanner.getToken()
                return circ(inLinkValue)
            }*/

            else -> errorShape()    //shapes1d()
        }
    }

    private fun errorShape(): Pair<Boolean, RailwayAST.Shape> {
        return Pair(false, RailwayAST.ErrorShape())
    }

    private fun box(inLinkValue: Float): Pair<Boolean, RailwayAST.Shape> {
        if (currentToken?.symbol == Symbol.LPAREN) {
            currentToken = scanner.getToken()
            val cord1 = cord(inLinkValue)
            if (cord1.first) {
                if (currentToken?.symbol == Symbol.COMMA) {
                    currentToken = scanner.getToken()
                    val cord2 = cord(inLinkValue)
                    if (cord2.first) {
                        if (currentToken?.symbol == Symbol.RPAREN) {
                            currentToken = scanner.getToken()
                            if (currentToken?.symbol == Symbol.SEMICOLON) {
                                currentToken = scanner.getToken()

                                //Checks if lat or lng are the same (if they are then they cannot be a polygon, they are a line)
                                val boxCord1 = cord1.second as RailwayAST.Coordinates
                                val boxCord2 = cord2.second as RailwayAST.Coordinates
                                if(boxCord1.lat == boxCord2.lat){
                                    return Pair(false, RailwayAST.Box(RailwayAST.Coordinates(0.0f, 0.0f), RailwayAST.Coordinates(0.0f, 0.0f)))
                                }

                                if(boxCord1.lng == boxCord2.lng){
                                    return Pair(false, RailwayAST.Box(RailwayAST.Coordinates(0.0f, 0.0f), RailwayAST.Coordinates(0.0f, 0.0f)))
                                }

                                return Pair(true, RailwayAST.Box(cord1.second as RailwayAST.Coordinates, cord2.second as RailwayAST.Coordinates))
                            }
                        }
                    }
                }
            }
        }
        return Pair(false, RailwayAST.Box(RailwayAST.Coordinates(0.0f, 0.0f), RailwayAST.Coordinates(0.0f, 0.0f)))
    }


    /*private fun circ(inLinkValue: Float): Boolean {
        if (currentToken?.symbol == Symbol.LPAREN) {
            currentToken = scanner.getToken()
            if (cord()) {
                if (currentToken?.symbol == Symbol.COMMA) {
                    currentToken = scanner.getToken()
                    if (currentToken?.symbol == Symbol.REAL) {
                        currentToken = scanner.getToken()
                        if (currentToken?.symbol == Symbol.RPAREN) {
                            currentToken = scanner.getToken()
                            if (currentToken?.symbol == Symbol.SEMICOLON) {
                                currentToken = scanner.getToken()
                                return true
                            }
                        }
                    }
                }
            }
        }
        return false
    }*/

    private fun cord(inLinkValue: Float): Pair<Boolean, RailwayAST.Expr> {
        if (currentToken?.symbol == Symbol.LPAREN) {
            currentToken = scanner.getToken()

            val real1: String?

            if (currentToken?.symbol == Symbol.REAL) {
                real1 = currentToken?.lexeme
                currentToken = scanner.getToken()
            }
            else if (currentToken?.symbol == Symbol.NULL) {
                real1 = null
                currentToken = scanner.getToken()
            }
            else {
                return Pair(false, RailwayAST.Coordinates(0.0f, 0.0f))
            }

            val real2: String?

            if (currentToken?.symbol == Symbol.COMMA) {
                currentToken = scanner.getToken()

                if (currentToken?.symbol == Symbol.REAL) {
                    real2 = currentToken?.lexeme
                    currentToken = scanner.getToken()
                }
                else if(currentToken?.symbol == Symbol.NULL){
                    real2 = null
                    currentToken = scanner.getToken()
                }
                else {
                    return Pair(false, RailwayAST.Coordinates(0.0f, 0.0f))
                }

                if (currentToken?.symbol == Symbol.RPAREN) {
                    currentToken = scanner.getToken()
                    if (real1 != null &&  real2 != null) {
                        return Pair(true, RailwayAST.Coordinates(real1.toFloat(), real2.toFloat()))
                    }
                    else if(real1 == null && real2 != null){
                        return Pair(true, RailwayAST.Coordinates(inLinkValue, real2.toFloat()))
                    }
                    else if(real1 != null && real2 == null){
                        return Pair(true, RailwayAST.Coordinates(real1.toFloat(), inLinkValue))
                    }
                    else {
                        return Pair(false, RailwayAST.Coordinates(0.0f, 0.0f))
                    }
                }

            }
        }

        return Pair(false, RailwayAST.Coordinates(0.0f, 0.0f))
    }


    /*private fun shapes1d(): Boolean {
        return when (currentToken?.symbol) {
            Symbol.LINE -> {
                currentToken = scanner.getToken()
                return line()
            }

            Symbol.BEND -> {
                currentToken = scanner.getToken()
                return bend()
            }

            else -> {
                false
            }
        }
    }

    private fun line(inLinkValue: Float): Pair<Boolean, RailwayAST.Expr> {
        if (currentToken?.symbol == Symbol.LPAREN) {
            currentToken = scanner.getToken()
            val cord1 = cord(inLinkValue)
            if (cord1.first) {
                if (currentToken?.symbol == Symbol.COMMA) {
                    currentToken = scanner.getToken()
                    val cord2 = cord(inLinkValue)
                    if (cord2.first) {
                        if (currentToken?.symbol == Symbol.RPAREN) {
                            currentToken = scanner.getToken()
                            if (currentToken?.symbol == Symbol.SEMICOLON) {
                                currentToken = scanner.getToken()
                                return Pair(true, )
                            }
                        }
                    }
                }
            }
        }
        return false
    }


    private fun bend(): Boolean {
        if (currentToken?.symbol == Symbol.LPAREN) {
            currentToken = scanner.getToken()
            if (cord()) {
                if (currentToken?.symbol == Symbol.COMMA) {
                    currentToken = scanner.getToken()
                    if (cord()) {
                        if (currentToken?.symbol == Symbol.COMMA) {
                            currentToken = scanner.getToken()
                            if (currentToken?.symbol == Symbol.REAL) {
                                currentToken = scanner.getToken()
                                if (currentToken?.symbol == Symbol.RPAREN) {
                                    currentToken = scanner.getToken()
                                    if (currentToken?.symbol == Symbol.SEMICOLON) {
                                        currentToken = scanner.getToken()
                                        return true
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false
    }

    private fun track(): Boolean {
        if (currentToken?.symbol == Symbol.TRACK) {
            currentToken = scanner.getToken()
            if (currentToken?.symbol == Symbol.NAME) {
                currentToken = scanner.getToken()
                if (currentToken?.symbol == Symbol.COMMA) {
                    currentToken = scanner.getToken()
                    if (trackTmp()) {
                        if (currentToken?.symbol == Symbol.COMMA) {
                            currentToken = scanner.getToken()
                            if (trackTmp()) {
                                if (currentToken?.symbol == Symbol.LCURLY) {
                                    currentToken = scanner.getToken()
                                    if (shapes1dMul()) {
                                        if (currentToken?.symbol == Symbol.RCURLY) {
                                            currentToken = scanner.getToken()
                                            return true
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false
    }

    private fun trackTmp(): Boolean {
        if (currentToken?.symbol == Symbol.VAR) {
            currentToken = scanner.getToken()
            if (currentToken?.symbol == Symbol.ARROW) {
                currentToken = scanner.getToken()
                if (currentToken?.symbol == Symbol.REAL) {
                    currentToken = scanner.getToken()
                    if (currentToken?.symbol == Symbol.ARROW) {
                        currentToken = scanner.getToken()
                        if (currentToken?.symbol == Symbol.REAL) {
                            currentToken = scanner.getToken()
                            return true
                        }
                    }
                }
            }
        }
        return false
    }


    private fun shapes1dMul(): Boolean {
        if (shapes1d()) {
            while (shapes1d()) {
                // Zanka za preverjanje več shapes1d
            }
            return true
        }
        return false
    }

    private fun switch(): Boolean {
        if (currentToken?.symbol == Symbol.SWITCH) {
            currentToken = scanner.getToken()
            if (currentToken?.symbol == Symbol.VAR) {
                currentToken = scanner.getToken()
                if (currentToken?.symbol == Symbol.COMMA) {
                    currentToken = scanner.getToken()
                    if (currentToken?.symbol == Symbol.VAR) {
                        currentToken = scanner.getToken()
                        if (currentToken?.symbol == Symbol.LCURLY) {
                            currentToken = scanner.getToken()
                            if (shapes1dMulOpt()) {
                                if (currentToken?.symbol == Symbol.RCURLY) {
                                    currentToken = scanner.getToken()
                                    return true
                                }
                            }
                        }
                    }
                }
            }
        }
        return false
    }

    private fun shapes1dMulOpt(): Boolean {
        if (!shapes1d())
            return true
        while (shapes1d()) {
            // Zanka za preverjanje več shapes1d
        }
        return true
    }

    private fun tunnel(): Boolean {
        if (currentToken?.symbol == Symbol.TUNNEL) {
            currentToken = scanner.getToken()
            if (tbcTemplate())
                return true
        }
        return false
    }

    private fun array(): Boolean {
        if (currentToken?.symbol == Symbol.LSQUARE) {
            currentToken = scanner.getToken()
            if (currentToken?.symbol == Symbol.VAR) {
                currentToken = scanner.getToken()
                if (array1()) {
                    if (currentToken?.symbol == Symbol.RSQUARE) {
                        currentToken = scanner.getToken()
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun array1(): Boolean {
        while (currentToken?.symbol == Symbol.COMMA) {
            currentToken = scanner.getToken()
            if (currentToken?.symbol == Symbol.VAR) {
                currentToken = scanner.getToken()
            } else {
                return false
            }
        }
        return true
    }


    private fun bridge(): Boolean {
        if (currentToken?.symbol == Symbol.BRIDGE) {
            currentToken = scanner.getToken()
            if (tbcTemplate()) {
                return true
            }
        }
        return false
    }


    private fun crossing(): Boolean {
        if (currentToken?.symbol == Symbol.CROSSING) {
            currentToken = scanner.getToken()
            if (tbcTemplate()) {
                return true
            }
        }
        return false
    }

    private fun tbcTemplate(): Boolean {
        if (currentToken?.symbol == Symbol.NAME) {
            currentToken = scanner.getToken()
            if (currentToken?.symbol == Symbol.COMMA) {
                currentToken = scanner.getToken()
                if (array()) {
                    if (currentToken?.symbol == Symbol.LCURLY) {
                        currentToken = scanner.getToken()
                        if (shapesmul()) {
                            if (currentToken?.symbol == Symbol.RCURLY) {
                                currentToken = scanner.getToken()
                                return true
                            }
                        }
                    }
                }
            }
        }
        return false
    }

    private fun train(): Boolean {
        if (currentToken?.symbol == Symbol.TRAIN) {
            currentToken = scanner.getToken()
            if (currentToken?.symbol == Symbol.NAME) {
                currentToken = scanner.getToken()
                if (currentToken?.symbol == Symbol.COMMA) {
                    currentToken = scanner.getToken()
                    if (currentToken?.symbol == Symbol.VAR) {
                        currentToken = scanner.getToken()
                        if (currentToken?.symbol == Symbol.COMMA) {
                            currentToken = scanner.getToken()
                            if (currentToken?.symbol == Symbol.REAL) {
                                currentToken = scanner.getToken()
                                if (currentToken?.symbol == Symbol.LCURLY) {
                                    currentToken = scanner.getToken()
                                    if (shapesmul()) {
                                        if (currentToken?.symbol == Symbol.RCURLY) {
                                            currentToken = scanner.getToken()
                                            return true
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false
    }

    private fun additive(): Boolean {
        return multiplicative() && additiveTwo()
    }

    private fun additiveTwo(): Boolean {
        return when (currentToken?.symbol) {
            Symbol.PLUS -> {
                currentToken = scanner.getToken()
                multiplicative() && additiveTwo()
            }

            Symbol.MINUS -> {
                currentToken = scanner.getToken()
                multiplicative() && additiveTwo()
            }

            else -> true
        }
    }

    private fun multiplicative(): Boolean {
        return exponential() && multiplicativeTwo()
    }

    private fun multiplicativeTwo(): Boolean {
        return when (currentToken?.symbol) {
            Symbol.TIMES -> {
                currentToken = scanner.getToken()
                exponential() && multiplicativeTwo()
            }

            Symbol.DIVIDES -> {
                currentToken = scanner.getToken()
                exponential() && multiplicativeTwo()
            }

            Symbol.INTDIVIDES -> {
                currentToken = scanner.getToken()
                exponential() && multiplicativeTwo()
            }

            else -> true
        }
    }

    private fun exponential(): Boolean {
        return unary() && exponentialTwo()
    }

    private fun exponentialTwo(): Boolean {
        return when (currentToken?.symbol) {
            Symbol.POW -> {
                currentToken = scanner.getToken()
                unary() && exponentialTwo()
            }

            else -> true
        }
    }

    private fun unary(): Boolean {
        return when (currentToken?.symbol) {
            Symbol.PLUS -> {
                currentToken = scanner.getToken()
                primary()
            }

            Symbol.MINUS -> {
                currentToken = scanner.getToken()
                primary()
            }

            else -> primary()
        }
    }

    private fun primary(): Boolean {
        return when (currentToken?.symbol) {
            Symbol.REAL, Symbol.VAR -> {
                currentToken = scanner.getToken()
                true
            }

            Symbol.LPAREN -> {
                currentToken = scanner.getToken()
                if (additive()) {
                    if (currentToken?.symbol == Symbol.RPAREN) {
                        currentToken = scanner.getToken()
                        true
                    } else false
                } else false
            }

            else -> false
        }
    }*/

}