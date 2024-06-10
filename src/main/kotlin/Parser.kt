class Parser(private val scanner: Scanner) {
    private var currentToken: Token? = null
    private var variables: MutableMap<String, RailwayTypesData> = mutableMapOf()

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
                val varName = currentToken?.lexeme ?: ""
                currentToken = scanner.getToken()
                if (currentToken?.symbol == Symbol.ASSIGN) {
                    currentToken = scanner.getToken()

                    val component = comp()

                    //Sets variable
                    when (component.second.type) {
                        RailwayAST.RailwayTypes.STATION -> variables[varName] =
                            RailwayTypesData(station = component.second as RailwayAST.Station)

                        RailwayAST.RailwayTypes.PLATFORM -> variables[varName] =
                            RailwayTypesData(platform = component.second as RailwayAST.Platform)

                        RailwayAST.RailwayTypes.TRACK -> variables[varName] =
                            RailwayTypesData(track = component.second as RailwayAST.Track)

                        else -> {}
                    }


                    return component
                }
            }
        }
        return comp()

    }

    private fun comp(): Pair<Boolean, RailwayAST.Expr> {
        //return station().first || track() || switch() || tunnel() || bridge() || crossing() || train() || additive()
        val station = station()
        if (station.first) {
            return Pair(true, station.second)
        }

        val track = track()
        if (track.first) {
            return Pair(true, track.second)
        }

        val switch = switch()
        if (switch.first) {
            return Pair(true, switch.second)
        }

        return Pair(false, RailwayAST.Coordinates(0.0f, 0.0f))

    }

    private fun station(): Pair<Boolean, RailwayAST.Expr> {
        if (currentToken?.symbol == Symbol.STATION) {
            currentToken = scanner.getToken()
            if (currentToken?.symbol == Symbol.NAME) {
                val name = currentToken?.lexeme

                currentToken = scanner.getToken()
                if (currentToken?.symbol == Symbol.LCURLY) {
                    currentToken = scanner.getToken()

                    val shapesStatus = shapesmul(RailwayAST.Coordinates(0.0f, 0.0f))
                    val platformStatus = platformmul(name ?: "")
                    if (shapesStatus.first && platformStatus.first) {

                        while (currentToken?.symbol != Symbol.RCURLY) {
                            if (!shapesmul(RailwayAST.Coordinates(0.0f, 0.0f)).first && !platformmul(
                                    name ?: ""
                                ).first
                            ) {
                                break
                            }
                        }

                        if (currentToken?.symbol == Symbol.RCURLY) {
                            currentToken = scanner.getToken()

                            return Pair(
                                true,
                                RailwayAST.Station(
                                    name ?: "",
                                    shapesStatus.second,
                                    *platformStatus.second.toTypedArray()
                                )
                            )
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

                            val shapesMul = shapesmul(RailwayAST.Coordinates(0.0f, 0.0f))
                            if (shapesMul.first) {
                                if (currentToken?.symbol == Symbol.RCURLY) {
                                    currentToken = scanner.getToken()
                                    return Pair(
                                        true,
                                        RailwayAST.Platform(
                                            number ?: "1",
                                            countOfTrack ?: "1",
                                            stationName,
                                            shapesMul.second
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        return Pair(false, RailwayAST.Platform("", "", stationName, errorshapesmul().second))
    }

    private fun track(): Pair<Boolean, RailwayAST.Expr> {
        if (currentToken?.symbol == Symbol.TRACK) {
            currentToken = scanner.getToken()
            if (currentToken?.symbol == Symbol.NAME) {
                val trackName = currentToken?.lexeme ?: ""
                currentToken = scanner.getToken()

                if (currentToken?.symbol == Symbol.COMMA) {
                    currentToken = scanner.getToken()
                    val trackPlatform1 = trackPlatform()

                    if (trackPlatform1.first) {
                        if (currentToken?.symbol == Symbol.COMMA) {
                            currentToken = scanner.getToken()

                            val trackPlatform2 = trackPlatform()

                            if (trackPlatform2.first) {
                                if (currentToken?.symbol == Symbol.LCURLY) {
                                    currentToken = scanner.getToken()

                                    //Link track to platform
                                    val shapesMul = shapes1dMul(
                                        trackPlatform1.second.coordinates,
                                        trackPlatform2.second.coordinates
                                    )

                                    if (shapesMul.first) {
                                        if (currentToken?.symbol == Symbol.RCURLY) {
                                            currentToken = scanner.getToken()
                                            return Pair(
                                                true,
                                                RailwayAST.Track(
                                                    trackName,
                                                    shapesMul.second,
                                                    trackPlatform1.second,
                                                    trackPlatform2.second
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return Pair(false, RailwayAST.Coordinates(0.0f, 0.0f))
    }

    private fun trackPlatform(): Pair<Boolean, RailwayAST.Platform> {
        if (currentToken?.symbol == Symbol.VAR) {
            val varName = currentToken?.lexeme ?: ""
            currentToken = scanner.getToken()

            if (currentToken?.symbol == Symbol.ARROW) {
                currentToken = scanner.getToken()

                if (currentToken?.symbol == Symbol.REAL) {
                    val number = currentToken?.lexeme ?: ""
                    currentToken = scanner.getToken()

                    if (currentToken?.symbol == Symbol.ARROW) {
                        currentToken = scanner.getToken()

                        if (currentToken?.symbol == Symbol.REAL) {
                            val trackNumber = currentToken?.lexeme ?: ""
                            currentToken = scanner.getToken()

                            //Checks if variable was saved
                            if (variables[varName] == null) {
                                return Pair(false, RailwayAST.Platform("0", "0", "0", errorshapesmul().second))
                            }

                            //println("$number.$trackNumber")

                            if (variables[varName]!!.station?.platforms?.get("$number.$trackNumber") == null) {
                                return Pair(false, RailwayAST.Platform("0", "0", "0", errorshapesmul().second))
                            }

                            return Pair(true, variables[varName]!!.station?.platforms?.get("$number.$trackNumber")!!)
                        }
                    }
                }
            }
        }
        return Pair(false, RailwayAST.Platform("0", "0", "0", errorshapesmul().second))
    }

    private fun switch(): Pair<Boolean, RailwayAST.Expr> {
        if (currentToken?.symbol == Symbol.SWITCH) {
            currentToken = scanner.getToken()
            if (currentToken?.symbol == Symbol.VAR) {
                val varName1 = currentToken?.lexeme ?: ""
                currentToken = scanner.getToken()

                if (variables[varName1] == null || variables[varName1]!!.track == null) {
                    return Pair(false, RailwayAST.Coordinates(0.0f, 0.0f))
                }
                val track1 = variables[varName1]!!.track!!

                if (currentToken?.symbol == Symbol.COMMA) {
                    currentToken = scanner.getToken()
                    if (currentToken?.symbol == Symbol.VAR) {
                        val varName2 = currentToken?.lexeme ?: ""
                        currentToken = scanner.getToken()

                        if (variables[varName2] == null || variables[varName2]!!.track == null) {
                            return Pair(false, RailwayAST.Coordinates(0.0f, 0.0f))
                        }
                        val track2 = variables[varName2]!!.track!!

                        if (currentToken?.symbol == Symbol.LCURLY) {
                            currentToken = scanner.getToken()

                            val shapes1dMulOpt = shapes1dMulOpt()
                            if (shapes1dMulOpt.first) {
                                if (currentToken?.symbol == Symbol.RCURLY) {
                                    currentToken = scanner.getToken()
                                    return Pair(true, RailwayAST.Switch(shapes1dMulOpt.second, track1, track2))
                                }
                            }
                        }
                    }
                }
            }
        }
        return Pair(false, RailwayAST.Coordinates(0.0f, 0.0f))
    }


    /*private fun tunnel(): Boolean {
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
    }*/

    private fun shapesmul(inLinkValue: RailwayAST.Coordinates): Pair<Boolean, RailwayAST.ShapesMul> {
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

    private fun shapes(inLinkValue: RailwayAST.Coordinates): Pair<Boolean, RailwayAST.Shape> {
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

    private fun shapes1d(vararg inLinkValue: RailwayAST.Coordinates): Pair<Boolean, RailwayAST.Shape> {
        return when (currentToken?.symbol) {
            Symbol.LINE -> {
                currentToken = scanner.getToken()
                return line(*inLinkValue)
            }

            Symbol.BEND -> {
                currentToken = scanner.getToken()
                return bend(*inLinkValue)
            }

            else -> {
                errorShape()
            }
        }
    }

    private fun shapes1dMul(vararg inLinkValue: RailwayAST.Coordinates): Pair<Boolean, RailwayAST.ShapesMul> {
        val listOfLinkValue = mutableListOf<RailwayAST.Coordinates>(*inLinkValue)

        var shape = shapes1d(*listOfLinkValue.toTypedArray())
        val listOfShapes = mutableListOf<RailwayAST.Shape>()
        if (shape.first) {
            listOfLinkValue.removeFirst()
            listOfLinkValue.addFirst(shape.second.shapeCoordinate)
            while (shape.first) {
                //Loop to check if there are more 1d shapes
                listOfShapes.add(shape.second)
                listOfLinkValue.removeFirst()
                listOfLinkValue.addFirst(shape.second.shapeCoordinate)
                shape = shapes1d(*listOfLinkValue.toTypedArray())
            }
            return Pair(true, RailwayAST.ShapesMul(*listOfShapes.toTypedArray()))
        }
        return errorshapesmul()
    }

    private fun shapes1dMulOpt(vararg inLinkValue: RailwayAST.Coordinates): Pair<Boolean, RailwayAST.ShapesMul> {
        var shape1d = shapes1d()

        if (!shape1d.first) {
            //Can be empty
            return Pair(true, errorshapesmul().second)
        }

        val listOfShapes = mutableListOf<RailwayAST.Shape>()
        while (shape1d.first) {
            //Loop to check if there are more 1d shapes
            listOfShapes.add(shape1d.second)
            shape1d = shapes1d()
        }
        return Pair(true, RailwayAST.ShapesMul(*listOfShapes.toTypedArray()))


    }

    private fun line(vararg inLinkValue: RailwayAST.Coordinates): Pair<Boolean, RailwayAST.Shape> {
        if (currentToken?.symbol == Symbol.LPAREN) {
            currentToken = scanner.getToken()


            val cord1 = if (inLinkValue.isNotEmpty()) cord(inLinkValue[0]) else cord(RailwayAST.Coordinates(0f, 0f))
            if (cord1.first) {

                if (currentToken?.symbol == Symbol.COMMA) {
                    currentToken = scanner.getToken()
                    val cord2 = if (inLinkValue.size > 1) cord(inLinkValue[1]) else cord(RailwayAST.Coordinates(0f, 0f))

                    if (cord2.first) {
                        if (currentToken?.symbol == Symbol.RPAREN) {
                            currentToken = scanner.getToken()
                            if (currentToken?.symbol == Symbol.SEMICOLON) {
                                currentToken = scanner.getToken()
                                return Pair(
                                    true,
                                    RailwayAST.Line(
                                        cord1.second as RailwayAST.Coordinates,
                                        cord2.second as RailwayAST.Coordinates
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        return Pair(false, RailwayAST.Line(RailwayAST.Coordinates(0.0f, 0.0f), RailwayAST.Coordinates(0.0f, 0.0f)))
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

    private fun bend(vararg inLinkValue: RailwayAST.Coordinates): Pair<Boolean, RailwayAST.Shape> {
       if (currentToken?.symbol == Symbol.LPAREN) {
           currentToken = scanner.getToken()
           val cord1 = if (inLinkValue.isNotEmpty()) cord(inLinkValue[0]) else cord(RailwayAST.Coordinates(0f, 0f))

           if (cord1.first) {
               if (currentToken?.symbol == Symbol.COMMA) {
                   currentToken = scanner.getToken()
                   val cord2 = if (inLinkValue.size > 1) cord(inLinkValue[1]) else cord(RailwayAST.Coordinates(0f, 0f))

                   if (cord2.first) {
                       if (currentToken?.symbol == Symbol.COMMA) {
                           currentToken = scanner.getToken()

                           val arith = add()
                           if (arith.first) {
                               val angle = arith.second.eval(variables)

                               if (currentToken?.symbol == Symbol.RPAREN) {
                                   currentToken = scanner.getToken()
                                   if (currentToken?.symbol == Symbol.SEMICOLON) {
                                       currentToken = scanner.getToken()
                                       return Pair(
                                           true,
                                           RailwayAST.Bend(
                                               cord1.second as RailwayAST.Coordinates,
                                               cord2.second as RailwayAST.Coordinates,
                                               angle.toFloat()
                                           )
                                       )
                                   }
                               }
                           }
                       }
                   }
               }
           }
       }
       return Pair(
           false,
           RailwayAST.Bend(
               RailwayAST.Coordinates(0f, 0f),
               RailwayAST.Coordinates(0f, 0f),
               20f
           )
       )
   }



    private fun box(inLinkValue: RailwayAST.Coordinates): Pair<Boolean, RailwayAST.Shape> {
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
                                if (boxCord1.lat == boxCord2.lat) {
                                    return Pair(
                                        false,
                                        RailwayAST.Box(
                                            RailwayAST.Coordinates(0.0f, 0.0f),
                                            RailwayAST.Coordinates(0.0f, 0.0f)
                                        )
                                    )
                                }

                                if (boxCord1.lng == boxCord2.lng) {
                                    return Pair(
                                        false,
                                        RailwayAST.Box(
                                            RailwayAST.Coordinates(0.0f, 0.0f),
                                            RailwayAST.Coordinates(0.0f, 0.0f)
                                        )
                                    )
                                }

                                return Pair(
                                    true,
                                    RailwayAST.Box(
                                        cord1.second as RailwayAST.Coordinates,
                                        cord2.second as RailwayAST.Coordinates
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        return Pair(false, RailwayAST.Box(RailwayAST.Coordinates(0.0f, 0.0f), RailwayAST.Coordinates(0.0f, 0.0f)))
    }



    private fun cord(inLinkValue: RailwayAST.Coordinates): Pair<Boolean, RailwayAST.Expr> {
        if (currentToken?.symbol == Symbol.LPAREN) {
            currentToken = scanner.getToken()

            val real1 = add()
            if (real1.first) {

                if (currentToken?.symbol == Symbol.COMMA) {
                    currentToken = scanner.getToken()

                    val real2 = add()
                    if (real2.first) {

                        if (currentToken?.symbol == Symbol.RPAREN) {
                            currentToken = scanner.getToken()
                            return Pair(true, RailwayAST.Coordinates(real1.second.eval(variables).toFloat(), real2.second.eval(variables).toFloat()))
                        }
                    }
                }
            }
        } else if (currentToken?.symbol == Symbol.NULL) {
            currentToken = scanner.getToken()
            return Pair(true, inLinkValue)
        }

        return Pair(false, RailwayAST.Coordinates(0.0f, 0.0f))
    }

    private fun add(): Pair<Boolean, RailwayAST.Arithmetic> {
        val mul = mul()
        val add2 = add2(mul.second)
        return Pair(mul.first && add2.first, add2.second)
    }

    private fun add2(inVal: RailwayAST.Arithmetic): Pair<Boolean, RailwayAST.Arithmetic> {
        if (currentToken?.symbol == Symbol.PLUS) {
            currentToken = scanner.getToken()
            val plus = RailwayAST.Plus(inVal, mul().second)
            return add2(plus)
        } else if (currentToken?.symbol == Symbol.MINUS) {
            currentToken = scanner.getToken()
            val minus = RailwayAST.Minus(inVal, mul().second)
            return add2(minus)
        } else {
            return Pair(true, inVal)
        }
    }

    private fun mul(): Pair<Boolean, RailwayAST.Arithmetic> {
        val expon = expon()
        val mul2 = mul2(expon.second)
        return Pair(expon.first && mul2.first, mul2.second)
    }

    private fun mul2(inVal: RailwayAST.Arithmetic): Pair<Boolean, RailwayAST.Arithmetic> {
        if (currentToken?.symbol == Symbol.TIMES) {
            currentToken = scanner.getToken()
            val times = RailwayAST.Times(inVal, expon().second)
            return mul2(times)
        } else if (currentToken?.symbol == Symbol.DIVIDES) {
            currentToken = scanner.getToken()
            val divides = RailwayAST.Divides(inVal, expon().second)
            return mul2(divides)
        } else if (currentToken?.symbol == Symbol.INTDIVIDES) {
            currentToken = scanner.getToken()
            val intDivides = RailwayAST.IntegerDivides(inVal, expon().second)
            return mul2(intDivides)
        } else {
            return Pair(true, inVal)
        }
    }

    private fun expon(): Pair<Boolean, RailwayAST.Arithmetic> {
        val unary = unary()
        val expon2 = expon2(unary.second)
        return Pair(unary.first && expon2.first, expon2.second)
    }

    private fun expon2(inVal: RailwayAST.Arithmetic): Pair<Boolean, RailwayAST.Arithmetic> {
        if (currentToken?.symbol == Symbol.POW) {
            currentToken = scanner.getToken()
            val exp = expon2(unary().second)
            val pow = RailwayAST.Pow(inVal, exp.second)
            return expon2(pow)
        } else {
            return Pair(true, inVal)
        }
    }

    private fun unary(): Pair<Boolean, RailwayAST.Arithmetic> {
        if (currentToken?.symbol == Symbol.PLUS) {
            currentToken = scanner.getToken()
            val primary = primary()
            return Pair(primary.first, RailwayAST.UnaryPlus(primary.second))
        } else if (currentToken?.symbol == Symbol.MINUS) {
            currentToken = scanner.getToken()
            val primary = primary()
            return Pair(primary.first, RailwayAST.UnaryMinus(primary.second))
        } else {
            return primary()
        }
    }

    private fun primary(): Pair<Boolean, RailwayAST.Arithmetic> {
        if (currentToken?.symbol == Symbol.REAL) {

            val real = RailwayAST.Real(currentToken?.lexeme?.toFloat() ?: 0.0f)
            currentToken = scanner.getToken()
            return Pair(true, real)
        }
        else if (currentToken?.symbol == Symbol.LPAREN) {
            currentToken = scanner.getToken()
            val add = add()
            if (add.first) {
                if (currentToken?.symbol == Symbol.RPAREN) {
                    currentToken = scanner.getToken()
                    return Pair(true, add.second)
                }
            }
        }

        return Pair(false, RailwayAST.Real(0.0f))
    }



}