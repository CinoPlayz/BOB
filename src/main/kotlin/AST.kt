import java.io.OutputStreamWriter

class RailwayAST {

    interface Arithmetic : Expr
    interface Shape : Expr {
        val shapeCoordinate: Coordinates
    }

    interface Expr {
        fun eval(variables: MutableMap<String, RailwayTypes>): String
    }

    class Infrastructure(private val name: String, vararg val expr: Expr) : Expr {
        override fun eval(variables: MutableMap<String, RailwayTypes>): String {
            val stringBuilderComponents = StringBuilder()

            for (item in expr) {
                stringBuilderComponents.append(item.eval(variables))
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
        val platforms = mutableMapOf<String, Coordinates>()
        override fun eval(variables: MutableMap<String, RailwayTypes>): String {

            //Sets coordinates of tracks outside the platform
            platformIn.forEachIndexed { index, platform ->
                for (i in 1..platform.countOfTracks.toInt()) {
                    platforms["${platform.number}.$i"] = Coordinates(
                        platform.coordinates.lat.plus(0.2f),
                        platform.coordinates.lng.plus(0.2f)
                    )
                }

            }

            return """
                {
                  "type": "FeatureCollection",
                  "features": [${shapes.eval(variables)}],
                  "metadata": {
                    "name": "$name"
                  }
                }
           """.trimIndent()
        }

    }

    class Platform(val number: String, val countOfTracks: String, private val shapes: ShapesMul) : Expr {
        val coordinates = shapes.lastCoordinates
        override fun eval(variables: MutableMap<String, RailwayTypes>): String {
            return """
                {
                  "type": "FeatureCollection",
                  "features": [${shapes.eval(variables)}],
                  "metadata": {
                    "number": "$number",
                    "countOfTracks": "$countOfTracks"
                  }
                }
            """.trimIndent()
        }

    }

    class ShapesMul(vararg val shape: Shape) : Expr {
        var lastCoordinates = Coordinates(0.0f, 0.0f)
        override fun eval(variables: MutableMap<String, RailwayTypes>): String {
            val stringBuilder = StringBuilder()

            shape.forEachIndexed { index, shapeInner ->
                stringBuilder.append(shapeInner.eval(variables))
                if (index != shape.count() - 1) {
                    stringBuilder.append(",")
                    lastCoordinates = shapeInner.shapeCoordinate
                }
            }
            return stringBuilder.toString()
        }
    }

    class ErrorShapesMul() : Expr {
        override fun eval(variables: MutableMap<String, RailwayTypes>): String {
            return ""
        }
    }


    class Box(val cord1: Coordinates, val cord2: Coordinates) : Shape {
        override val shapeCoordinate: Coordinates
            get() = cord1

        override fun eval(variables: MutableMap<String, RailwayTypes>): String {
            return """
                {
                  "type": "Feature",
                  "geometry": {
                     "type": "Polygon",
                     "coordinates": [                      
                        ${cord1.eval(variables)},
                        ${cord2.eval(variables)}                      
                     ]
                  }
                }     
                """.trimIndent()
        }
    }

    //Used to represent syntax error
    class ErrorShape : Shape {
        override val shapeCoordinate: Coordinates
            get() = Coordinates(0.0f, 0.0f)

        override fun eval(variables: MutableMap<String, RailwayTypes>): String {
            return ""
        }
    }

    class Coordinates(val lat: Float, val lng: Float) : Expr {
        override fun eval(variables: MutableMap<String, RailwayTypes>): String {
            return "[$lat, $lng]"
        }

    }
}

typealias RailwayTypes = RailwayAST.Station