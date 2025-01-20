package si.bob.zpmobileapp.utils.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class LocationHelper(private val context: Context) {
    private val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission") // Suppress warning if permissions are already checked
    fun getCurrentLocation(onLocationReceived: (Location?) -> Unit) {
        // Check if permissions are granted
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {

            onLocationReceived(null) // Permission not granted
            return
        }

        // Request the current location
        fusedLocationProviderClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->
            onLocationReceived(location) // Pass location to the callback
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
            onLocationReceived(null)
        }
    }
}
