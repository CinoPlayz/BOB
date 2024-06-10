import kotlin.math.*

class RailwayAST {

    interface Arithmetic : Expr
    interface Shape : Expr {
        val shapeCoordinate: Coordinates
    }

    interface Var: Expr

    enum class RailwayTypes{
        INFRASTRUCTURE,
        STATION,
        PLATFORM,
        TRACK,
        SHAPESMUL,
        BOX,
        LINE,
        ERRORSHAPE,
        COORDINATES,
        ARITHMETIC,
        BEND,
        CIRC


    }

    interface Expr {
        val type: RailwayTypes
        fun eval(variables: MutableMap<String, RailwayTypesData>): String
    }

    class Infrastructure(private val name: String, vararg val expr: Expr) : Expr {
        override val type: RailwayTypes
            get() = RailwayTypes.INFRASTRUCTURE

        override fun eval(variables: MutableMap<String, RailwayTypesData>): String {
            val stringBuilderComponents = StringBuilder()

            expr.forEachIndexed { index, item ->
                stringBuilderComponents.append(item.eval(variables))
                if(index != expr.size - 1){
                    stringBuilderComponents.append(",")
                }
            }

            return """{
                  "type": "FeatureCollection",
                  "features": [${stringBuilderComponents}],
                  "metadata": {
                    "name": "$name"
                  }
                }"""
        }
    }

    class Station(private val name: String, private val shapes: ShapesMul, vararg val platformIn: Platform) : Expr {
        val platforms = mutableMapOf<String, Platform>()
        override val type: RailwayTypes
            get() = RailwayTypes.STATION

        init {
            //Sets coordinates of tracks outside the platform
            platformIn.forEachIndexed { index, platform ->
                for (i in 1..platform.countOfTracks.toInt()) {

                    val platformClone = platform.clone()

                    if(i == 1){
                        platformClone.coordinates = Coordinates(platform.coordinates.lat + 0.2f, platform.coordinates.lng + 0.2f)
                    }
                    else {
                        platformClone.coordinates = Coordinates(platform.coordinates.lat - 0.2f, platform.coordinates.lng - 0.2f)
                    }



                    platforms["${platform.number}.$i"] = platformClone

                    //println("platform ${platforms["${platform.number}.$i"]!!.coordinates.lat} ${platforms["${platform.number}.$i"]!!.coordinates.lng}")
                }
            }
        }

        override fun eval(variables: MutableMap<String, RailwayTypesData>): String {
            val platformsGeoJson: StringBuilder = StringBuilder()

            //Gets GeoJson of each platform
            platformIn.forEachIndexed { index, platform ->
                platformsGeoJson.append(platform.eval(variables))
                if(index != platformIn.size - 1){
                    platformsGeoJson.append(",")
                }

            }

            val stationGeoJson = """
                {
                  "type": "Feature",
                  "properties": {
                    "type": "station",
                    "name": "$name"
                  }, 
                  ${shapes.eval(variables)}
                }
            """.trimIndent()

            return if(platformIn.isNotEmpty()){
                "$stationGeoJson,$platformsGeoJson"
            }
            else {
                stationGeoJson
            }
        }

    }

    class Platform(val number: String, val countOfTracks: String, val stationName: String, private val shapes: ShapesMul) : Expr {
        var coordinates = shapes.lastCoordinates
        override val type: RailwayTypes
            get() = RailwayTypes.PLATFORM

        override fun eval(variables: MutableMap<String, RailwayTypesData>): String {


            return """
                {
                  "type": "Feature",
                  "properties": {
                    "type": "platform",
                    "number": "$number",
                    "countOfTracks": "$countOfTracks",
                    "stationName": "$stationName"
                  }, 
                  ${shapes.eval(variables)}
                }
            """.trimIndent()
        }

        fun clone(): Platform{
            return Platform(number, countOfTracks, stationName, shapes)
        }

    }


    class Track(val name: String, private val shapes: ShapesMul, val platform1: Platform, val platform2: Platform) : Expr {
        override val type: RailwayTypes
            get() = RailwayTypes.TRACK

        override fun eval(variables: MutableMap<String, RailwayTypesData>): String {

           return """
                {
                  "type": "Feature",
                  "properties": {
                    "type": "track",
                    "name": "$name",
                    "startPlatformStationName": "${platform1.stationName}",
                    "startPlatformNumber": "${platform1.number}",
                    "startPlatformCountOfTracks": "${platform1.countOfTracks}",
                    "endPlatformStationName": "${platform2.stationName}",
                    "endPlatformNumber": "${platform2.number}",
                    "endPlatformCountOfTracks": "${platform2.countOfTracks}"
                  }, 
                  ${shapes.eval(variables)}
                }
            """.trimIndent()
        }

    }

    class Switch(private val shapes: ShapesMul, val track1: Track, val track2: Track) : Expr {
        override val type: RailwayTypes
            get() = RailwayTypes.TRACK

        override fun eval(variables: MutableMap<String, RailwayTypesData>): String {

            return """
                {
                  "type": "Feature",
                  "properties": {
                    "type": "switch",
                    "trackOneName": "${track1.name}",
                    "trackTwoName": "${track2.name}"
                  }, 
                  ${shapes.eval(variables)}
                }
            """.trimIndent()
        }

    }

    class ShapesMul(vararg val shape: Shape) : Expr {
        var lastCoordinates = Coordinates(0.0f, 0.0f)
        override val type: RailwayTypes
            get() = RailwayTypes.SHAPESMUL

        init {
            if(shape.isNotEmpty()){
                lastCoordinates = shape.last().shapeCoordinate
            }

        }

        override fun eval(variables: MutableMap<String, RailwayTypesData>): String {
            val stringBuilder = StringBuilder()

            shape.forEachIndexed { index, shapeInner ->
                stringBuilder.append(shapeInner.eval(variables))
                if (index != shape.size - 1) {
                    stringBuilder.append(",")
                    lastCoordinates = shapeInner.shapeCoordinate
                }
            }


            return if(shape.size != 1){
                """
                    "geometry": {
                        "type": "GeometryCollection",
                        "geometries": [$stringBuilder]
                    }
                  """
            } else {
                """
                    "geometry": $stringBuilder
                """.trimIndent()
            }
        }
    }

    class ErrorShapesMul() : Expr {
        override val type: RailwayTypes
            get() = RailwayTypes.ERRORSHAPE

        override fun eval(variables: MutableMap<String, RailwayTypesData>): String {
            return ""
        }
    }


    class Box(val cord1: Coordinates, val cord2: Coordinates) : Shape {
        override val shapeCoordinate: Coordinates
            get() = cord1
        override val type: RailwayTypes
            get() = RailwayTypes.BOX

        override fun eval(variables: MutableMap<String, RailwayTypesData>): String {
            return """
                  {
                     "type": "Polygon",
                     "coordinates": [[                      
                        ${cord1.eval(variables)},
                        [${cord2.lng}, ${cord1.lat}],
                        ${cord2.eval(variables)},          
                        [${cord1.lng}, ${cord2.lat}],
                        ${cord1.eval(variables)}
                     ]]
                  }                     
                """.trimIndent()
        }
    }


    class Line(val cord1: Coordinates, val cord2: Coordinates) : Shape {
        override val shapeCoordinate: Coordinates
            get() = cord2
        override val type: RailwayTypes
            get() = RailwayTypes.LINE

        override fun eval(variables: MutableMap<String, RailwayTypesData>): String {
            return """
                  {
                     "type": "LineString",
                     "coordinates": [                      
                        ${cord1.eval(variables)},                       
                        ${cord2.eval(variables)}
                     ]
                  }                     
                """.trimIndent()
        }
    }

    class Circ(val cord1: Coordinates, val radius: Float) : Shape {
        override val shapeCoordinate: Coordinates
            get() = cord1
        override val type: RailwayTypes
            get() = RailwayTypes.CIRC

        override fun eval(variables: MutableMap<String, RailwayTypesData>): String {

            val circleStringBuilder = StringBuilder()
            var first = ""
            for (i in 1..360){
                val angle = i * Math.PI.toFloat() / 180f
                val ptx = cord1.lat + radius * cos(angle)
                val pty = cord1.lng + radius * sin(angle)

                if(first == ""){
                    first = "[$pty, $ptx]"
                }
                circleStringBuilder.append("[$pty, $ptx],")
            }


            return """
                  {
                    "type": "Polygon",
                    "coordinates": [[ 
                        $circleStringBuilder
                        $first
                     ]]
                  }                     
                """.trimIndent()
        }
    }

    class Bend(val cord1: Coordinates, val cord2: Coordinates, val angle: Float) : Shape {
        override val shapeCoordinate: Coordinates
            get() = cord2
        override val type: RailwayTypes
            get() = RailwayTypes.BEND

        override fun eval(variables: MutableMap<String, RailwayTypesData>): String {
            val latlng1 = arrayOf(cord1.lat, cord1.lng)
            val latlng2 =  arrayOf(cord2.lat, cord2.lng)

            val offsetX = latlng2[1] - latlng1[1]
            val offsetY = latlng2[0] - latlng1[0]

            val r = sqrt( offsetX.pow(2) + offsetY.pow(2) )
            val theta = atan2(offsetY, offsetX)

            val thetaOffset = (angle * 3.14/ 180f)

            val r2 = (r/2)/(cos(thetaOffset))
            val theta2 = theta + thetaOffset

            val midpointX = (r2 * cos(theta2)) + latlng1[1]
            val midpointY = (r2 * sin(theta2)) + latlng1[0]

            val x = midpointX
            val y = midpointY

            //Bezier-curve
            var t = 0.05f
            val listOfBezierPoints = mutableListOf<Coordinates>()

            while(t < 1f){
                val bezierPointX = (1f- t).pow(2) * latlng1[0] + 2f * t * (1f- t) * x + t.pow(2f) * latlng2[0]
                val bezierPointY = (1f- t).pow(2) * latlng1[1] + 2f * t * (1f- t) * y + t.pow(2f) * latlng2[1]

                listOfBezierPoints.add(Coordinates(bezierPointX.toFloat(), bezierPointY.toFloat()))
                t += 0.05f
            }

            val bezierpointStringBuilder: StringBuilder = StringBuilder()
            listOfBezierPoints.forEach {
                bezierpointStringBuilder.appendLine("${it.eval(variables)},")
            }




            return """
                  {
                     "type": "LineString",
                     "coordinates": [                      
                        ${cord1.eval(variables)},   
                        $bezierpointStringBuilder
                        ${cord2.eval(variables)}
                        
                     ]
                  }                     
                """.trimIndent()
        }
    }

    //Used to represent syntax error
    class ErrorShape : Shape {
        override val shapeCoordinate: Coordinates
            get() = Coordinates(0.0f, 0.0f)
        override val type: RailwayTypes
            get() = RailwayTypes.ERRORSHAPE

        override fun eval(variables: MutableMap<String, RailwayTypesData>): String {
            return ""
        }
    }

    class Coordinates(val lat: Float, val lng: Float) : Expr {
        override val type: RailwayTypes
            get() = RailwayTypes.COORDINATES

        override fun eval(variables: MutableMap<String, RailwayTypesData>): String {
            return "[$lng, $lat]"
        }

    }

    class Plus(private val arithmetic1: Arithmetic, private val arithmetic2: Arithmetic) : Arithmetic {
        override val type: RailwayTypes
            get() = RailwayTypes.ARITHMETIC

        override fun eval( variables: MutableMap<String, RailwayTypesData>): String {
            return (arithmetic1.eval(variables).toFloat() + arithmetic2.eval(variables).toFloat()).toString()
        }

    }

    class Minus(private val arithmetic1: Arithmetic, private val arithmetic2: Arithmetic) : Arithmetic {
        override val type: RailwayTypes
            get() = RailwayTypes.ARITHMETIC

        override fun eval( variables: MutableMap<String, RailwayTypesData>): String {
            return (arithmetic1.eval(variables).toFloat() - arithmetic2.eval(variables).toFloat()).toString()
        }

    }

    class Times(private val arithmetic1: Arithmetic, private val arithmetic2: Arithmetic) : Arithmetic {
        override val type: RailwayTypes
            get() = RailwayTypes.ARITHMETIC

        override fun eval( variables: MutableMap<String, RailwayTypesData>): String {
            return (arithmetic1.eval(variables).toFloat() * arithmetic2.eval(variables).toFloat()).toString()
        }

    }

    class Divides(private val arithmetic1: Arithmetic, private val arithmetic2: Arithmetic) : Arithmetic {
        override val type: RailwayTypes
            get() = RailwayTypes.ARITHMETIC

        override fun eval( variables: MutableMap<String, RailwayTypesData>): String {
            return (arithmetic1.eval(variables).toFloat() / arithmetic2.eval(variables).toFloat()).toString()
        }

    }

    class IntegerDivides(private val arithmetic1: Arithmetic, private val arithmetic2: Arithmetic) : Arithmetic {
        override val type: RailwayTypes
            get() = RailwayTypes.ARITHMETIC

        override fun eval( variables: MutableMap<String, RailwayTypesData>): String {
            val resultInt =  arithmetic1.eval(variables).toFloat().roundToInt() / arithmetic2.eval(variables).toFloat().roundToInt()
            return resultInt.toString()
        }

    }

    class Pow(private val arithmetic1: Arithmetic, private val arithmetic2: Arithmetic) : Arithmetic {
        override val type: RailwayTypes
            get() = RailwayTypes.ARITHMETIC

        override fun eval( variables: MutableMap<String, RailwayTypesData>): String {
            return (arithmetic1.eval(variables).toFloat().pow(arithmetic2.eval(variables).toFloat())).toString()
        }

    }

    class UnaryPlus(private val arithmetic: Arithmetic) : Arithmetic {
        override val type: RailwayTypes
            get() = RailwayTypes.ARITHMETIC

        override fun eval( variables: MutableMap<String, RailwayTypesData>): String {
            return arithmetic.eval(variables)
        }

    }

    class UnaryMinus(private val arithmetic: Arithmetic) : Arithmetic {
        override val type: RailwayTypes
            get() = RailwayTypes.ARITHMETIC

        override fun eval( variables: MutableMap<String, RailwayTypesData>): String {
            return (-1 * arithmetic.eval(variables).toFloat()).toString()
        }

    }

    class Real(private val real: Float) : Arithmetic {
        override val type: RailwayTypes
            get() = RailwayTypes.ARITHMETIC

        override fun eval( variables: MutableMap<String, RailwayTypesData>): String {
            return real.toString()
        }

    }
}

//interface RailwayTypes : RailwayAST.Station, RailwayTypes.

data class RailwayTypesData(val station: RailwayAST.Station? = null, val track: RailwayAST.Track? = null, val platform: RailwayAST.Platform? = null)

//typealias RailwayTypes = RailwayAST.Station|Rail