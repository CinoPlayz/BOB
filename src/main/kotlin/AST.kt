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
        val platforms = mutableMapOf<String, Coordinates>()
        override fun eval(variables: MutableMap<String, RailwayTypes>): String {
            val platformsGeoJson: StringBuilder = StringBuilder()

            //Sets coordinates of tracks outside the platform
            platformIn.forEachIndexed { index, platform ->
                for (i in 1..platform.countOfTracks.toInt()) {
                    platforms["${platform.number}.$i"] = Coordinates(
                        platform.coordinates.lat.plus(0.2f),
                        platform.coordinates.lng.plus(0.2f)
                    )
                }

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

    class Platform(val number: String, val countOfTracks: String, private val stationName: String, private val shapes: ShapesMul) : Expr {
        val coordinates = shapes.lastCoordinates
        override fun eval(variables: MutableMap<String, RailwayTypes>): String {


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

    }

    class ShapesMul(vararg val shape: Shape) : Expr {
        var lastCoordinates = Coordinates(0.0f, 0.0f)
        override fun eval(variables: MutableMap<String, RailwayTypes>): String {
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
            return "[$lng, $lat]"
        }

    }
}

typealias RailwayTypes = RailwayAST.Station