package si.bob.zpmobileapp.ui.imageprocessor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import si.bob.zpmobileapp.databinding.ActivityPickLocationBinding
import si.bob.zpmobileapp.ui.map.Coordinates

class PickLocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPickLocationBinding
    private lateinit var mapView: MapView
    private var initialLocation: Coordinates? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPickLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize initial location from Intent extras
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)
        initialLocation = Coordinates(latitude, longitude)

        mapView = binding.mapView
        mapView.setTileSource(TileSourceFactory.MAPNIK)

        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        val startLocation = if (initialLocation != null) {
            GeoPoint(initialLocation!!.lat, initialLocation!!.lng)
        } else {
            GeoPoint(46.5547, 15.6459) // Maribor
        }

        mapView.controller.setCenter(startLocation)
        mapView.controller.setZoom(13)

        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                // On map click, set the location to the picked coordinates
                p?.let {
                    val pickedLocation = Coordinates(it.latitude, it.longitude)

                    // Return to the previous fragment with the picked location
                    val resultIntent = Intent().apply {
                        putExtra("latitude", pickedLocation.lat)
                        putExtra("longitude", pickedLocation.lng)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish() // Close the activity
                }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false // Ignore long press events
            }
        }

        // Attach the MapEventsReceiver to the MapView
        val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
        mapView.overlays.add(mapEventsOverlay)
    }
}