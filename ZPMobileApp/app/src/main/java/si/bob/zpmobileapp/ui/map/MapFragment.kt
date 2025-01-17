package si.bob.zpmobileapp.ui.map

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.TilesOverlay
import org.osmdroid.util.GeoPoint
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import si.bob.zpmobileapp.MyApp
import si.bob.zpmobileapp.R
import si.bob.zpmobileapp.databinding.FragmentMapBinding
import java.text.SimpleDateFormat
import java.util.Locale

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var mapView: MapView
    private lateinit var app: MyApp
    private lateinit var uuid: String
    val trainLocHistoryList = mutableListOf<TrainLocHistory>()
    val passengersList = mutableListOf<Passenger>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        app = requireActivity().application as MyApp
        uuid = app.sharedPrefs.getString(MyApp.UUID_KEY, null) ?: ""

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        drawMap()
        getTrainData()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Disable hardware acceleration for the MapView only --> custom marker icon is placed correctly
        mapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    private fun getTrainData() {
        val startDate = "2024-05-24"
        val endDate = "2024-05-25"

        try {
            val mqttClient = MqttClient("tcp://164.8.215.37:1883", MqttClient.generateClientId(), null)
            val options = MqttConnectOptions().apply {
                isCleanSession = true
            }

            mqttClient.connect(options)

            val requestPayload = JSONObject().apply {
                put("startDate", startDate)
                put("endDate", endDate)
                put("uuid", uuid)
            }

            val requestTopic = "app/trains/trainHistoryByDateRange/request"
            val responseTopic = "app/trains/trainHistoryByDateRange/response/$uuid"

            mqttClient.subscribe(responseTopic)

            mqttClient.publish(requestTopic, MqttMessage(requestPayload.toString().toByteArray()))

            mqttClient.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    requireActivity().runOnUiThread {
                        //Toast.makeText(requireContext(), "MQTT connection lost: ${cause?.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    if (topic == responseTopic) {
                        val response = message?.toString()
                        val jsonResponse = JSONObject(response ?: "{}")
                        val success = jsonResponse.optBoolean("success", false)

                        if (success) {
                            requireActivity().runOnUiThread {
                                //Toast.makeText(requireContext(), "Train data fetched successfully", Toast.LENGTH_SHORT).show()
                            }

                            val trainHistoryJsonArray = jsonResponse.optJSONArray("trainHistory")
                            val passengersJsonArray = jsonResponse.optJSONArray("occupancy")
                            //Log.d("TrainHistory", "Fetched train history: trainHistoryJsonArray")

                            if (trainHistoryJsonArray != null) {
                                // Create a new list to avoid concurrent modification
                                val newTrainLocHistoryList = mutableListOf<TrainLocHistory>()
                                for (i in 0 until trainHistoryJsonArray.length()) {
                                    val trainData = trainHistoryJsonArray.getJSONObject(i)
                                    val coordinatesJson = trainData.getJSONObject("coordinates")
                                    val trainLocHistory = TrainLocHistory(
                                        timeOfRequest = trainData.getString("timeOfRequest"),
                                        trainType = trainData.getString("trainType"),
                                        trainNumber = trainData.getString("trainNumber"),
                                        routeFrom = trainData.getString("routeFrom"),
                                        routeTo = trainData.getString("routeTo"),
                                        routeStartTime = trainData.getString("routeStartTime"),
                                        nextStation = trainData.getString("nextStation"),
                                        delay = trainData.getInt("delay"),
                                        coordinates = Coordinates(
                                            lat = coordinatesJson.getDouble("lat"),
                                            lng = coordinatesJson.getDouble("lng")
                                        )
                                    )
                                    newTrainLocHistoryList.add(trainLocHistory)
                                }
                                trainLocHistoryList.clear() // Clear the original list
                                trainLocHistoryList.addAll(newTrainLocHistoryList) // Add the new data
                                Log.d("TrainHistory", "Train History Size: ${trainLocHistoryList.size}")
                            }

                            if (passengersJsonArray != null) {
                                val newPassengersList = mutableListOf<Passenger>()
                                for (i in 0 until passengersJsonArray.length()) {

                                    /*val timeOfRequest = passengerData.getString("timeOfRequest")
                                    val coordinates = Coordinates(
                                        lat = coordinatesJson.getDouble("lat"),
                                        lng = coordinatesJson.getDouble("lng")
                                    )
                                    val guessedOccupancyRate = passengerData.getDouble("guessedOccupancyRate")
                                    val realOccupancyRate = passengerData.optDouble("realOccupancyRate").takeIf { !passengerData.isNull("realOccupancyRate") }
                                    val route = passengerData.getString("route")
                                    val postedByUserId = passengerData.getString("postedByUser")

                                    Log.d("OccupancyHistory", "Passenger #$i:")
                                    Log.d("OccupancyHistory", "  Time of Request: $timeOfRequest")
                                    Log.d("OccupancyHistory", "  Coordinates: lat=${coordinates.lat}, lng=${coordinates.lng}")
                                    Log.d("OccupancyHistory", "  Guessed Occupancy Rate: $guessedOccupancyRate")
                                    Log.d("OccupancyHistory", "  Real Occupancy Rate: ${realOccupancyRate ?: "null"}")
                                    Log.d("OccupancyHistory", "  Route ID: $route")
                                    Log.d("OccupancyHistory", "  Posted By User ID: $postedByUserId")*/

                                    val passengerData = passengersJsonArray.getJSONObject(i)
                                    val coordinatesJson = passengerData.getJSONObject("coordinatesOfRequest")
                                    val passenger = Passenger(
                                        timeOfRequest = passengerData.getString("timeOfRequest"),
                                        coordinates = Coordinates(
                                            lat = coordinatesJson.getDouble("lat"),
                                            lng = coordinatesJson.getDouble("lng")
                                        ),
                                        guessedOccupancyRate = passengerData.getDouble("guessedOccupancyRate"),
                                        realOccupancyRate = passengerData.optDouble("realOccupancyRate").takeIf { !passengerData.isNull("realOccupancyRate") },
                                        route = passengerData.getString("route"),
                                        postedByUserId = passengerData.getString("postedByUser")
                                    )
                                    newPassengersList.add(passenger)
                                }
                                passengersList.clear() // Clear the original list
                                passengersList.addAll(newPassengersList) // Add the new data
                                Log.d("OccupancyHistory", "Occupancy History Size: ${passengersList.size}")
                                Log.d("OccupancyHistory", "Occupancy History: $passengersList")
                            }

                            requireActivity().runOnUiThread {
                                showTrainsOnMap()
                            }
                        } else {
                            requireActivity().runOnUiThread {
                                //Toast.makeText(requireContext(), "Failed to fetch train data", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                }
            })

        } catch (e: Exception) {
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            Log.e("Map", "Error", e)
        }
    }

    private fun showTrainsOnMap() {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

        val startDate = "2024-05-24T07:22:00.000Z"
        val endDate = "2024-05-24T07:23:00.000Z"

        val start = formatter.parse(startDate)
        val end = formatter.parse(endDate)

        // Create a copy of the trainLocHistoryList to avoid concurrent modification
        val trainLocHistoryListCopy = trainLocHistoryList.toList()

        // Filter one snapshot of train history --> for presentation
        val trainLocHistoryListFiltered = trainLocHistoryListCopy.filter { train ->
            val trainDate = formatter.parse(train.timeOfRequest)
            trainDate.after(start) && trainDate.before(end)
        }

        // Calculate and Update Occupancy
        val passengersByRoute = passengersList.groupBy { it.route }

        val routeOccupancyMap = passengersByRoute.mapValues { entry ->
            val passengers = entry.value

            val avgGuessedOccupancy = passengers.map { it.guessedOccupancyRate }.average()
            val avgRealOccupancy = passengers.mapNotNull { it.realOccupancyRate }.average().takeIf { it.isFinite() }

            Pair(avgGuessedOccupancy, avgRealOccupancy)
        }

        /*Log.d("TrainHistoryFiltered", "Train History Filtered Size: ${trainLocHistoryListFiltered.size}")
        Log.d("TrainHistoryFiltered", "Train History Filtered: $trainLocHistoryListFiltered")

        val groupedByTime = trainLocHistoryList.groupBy { it.timeOfRequest }

        groupedByTime.forEach { (time, trains) ->
            Log.d("GroupedTrainData", "Time: $time | Number of Trains: ${trains.size}")
        }*/

        val updatedTrainLocHistoryList = trainLocHistoryListFiltered.map { train ->
            val occupancy = routeOccupancyMap[train.trainNumber]
            if (occupancy != null) {
                train.copy(
                    estimatedOccupancy = occupancy.first,
                    realOccupancy = occupancy.second
                )
            } else {
                train
            }
        }

        updatedTrainLocHistoryList.forEach { train ->
            val geoPoint = GeoPoint(train.coordinates.lat, train.coordinates.lng)

            // Determine occupancy levels
            val estimatedOccupancyLevel = getOccupancyLevel(train.estimatedOccupancy)
            val realOccupancyLevel = getOccupancyLevel(train.realOccupancy)

            // Build title with occupancy information
            val titleBuilder = StringBuilder().apply {
                append("${train.trainType} ${train.trainNumber}\n")
                append("Next Station: ${train.nextStation}\n")
                append("Delay: ${train.delay} min\n")
                append("Est. Occupancy: $estimatedOccupancyLevel\n")
                append("Real Occupancy: $realOccupancyLevel")
            }

            // Create marker with updated title
            val marker = Marker(mapView).apply {
                position = geoPoint
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = titleBuilder.toString()

                val originalIcon = ContextCompat.getDrawable(requireContext(), R.drawable.train_icon)
                val scaledIcon = Bitmap.createScaledBitmap(
                    (originalIcon as BitmapDrawable).bitmap,
                    32, 37,  // Adjust size if necessary
                    true
                )
                icon = BitmapDrawable(resources, scaledIcon)
            }

            mapView.overlays.add(marker)
        }

        /*if (trainLocHistoryListFiltered.isNotEmpty()) {
            val firstTrain = trainLocHistoryListFiltered.first()
            mapView.controller.setZoom(12.0)
            mapView.controller.setCenter(GeoPoint(firstTrain.coordinates.lat, firstTrain.coordinates.lng))
        }*/

        mapView.invalidate()
    }

    private fun drawMap() {
        Configuration.getInstance().load(requireContext(), androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext()))

        mapView = binding.map
        mapView.setTileSource(TileSourceFactory.MAPNIK) // OpenStreetMap base layer

        // OpenRailwayMap tile source
        val railwayTileSource: ITileSource = XYTileSource(
            "OpenRailwayMap",
            0,
            19,
            256,
            ".png",
            arrayOf("https://tiles.openrailwaymap.org/standard/"),
            "© OpenRailwayMap contributors"
        )

        val railwayTileProvider = MapTileProviderBasic(requireContext())
        railwayTileProvider.setTileSource(railwayTileSource)
        val railwayOverlay = TilesOverlay(railwayTileProvider, requireContext())
        railwayOverlay.loadingBackgroundColor = android.graphics.Color.TRANSPARENT

        mapView.overlays.add(railwayOverlay)

        mapView.setMultiTouchControls(true)

        mapView.controller.setZoom(10.5)
        // mapView.controller.setCenter(GeoPoint(46.5547, 15.6459)) // Maribor
        // mapView.controller.setCenter(GeoPoint(46.0569, 14.5058)) // Ljubljana
        mapView.controller.setCenter(GeoPoint(46.2397, 15.2677)) // Celje

        // Add touch listener to close popups
        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                // Close all marker popups when tapping on empty space
                mapView.overlays.filterIsInstance<Marker>().forEach { it.closeInfoWindow() }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        }

        // Add the event overlay to the map
        val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
        mapView.overlays.add(mapEventsOverlay)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        //trainLocHistoryList.clear()
        //passengersList.clear()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
