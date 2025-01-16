package si.bob.zpmobileapp.ui.imageprocessor

import android.Manifest
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import si.bob.zpmobileapp.BuildConfig
import si.bob.zpmobileapp.MyApp
import si.bob.zpmobileapp.R
import si.bob.zpmobileapp.databinding.FragmentImageprocessorBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ImageProcessorFragment : Fragment() {

    private var _binding: FragmentImageprocessorBinding? = null
    private val binding get() = _binding!!

    private var photoUri: Uri? = null
    private var routeId: String? = null // For sending seat occupancy data
    private var trainType: String? = null // For sending seat occupancy data
    private var numOfPeople: Int? = null
    private var numOfSeats: Int? = null
    private var guessedOccupancyRate: Double? = null

    private lateinit var app: MyApp

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        app = requireActivity().application as MyApp

        _binding = FragmentImageprocessorBinding.inflate(inflater, container, false)
        val root: View = binding.root

        checkUserCredentials()

        return root
    }


    private fun checkUserCredentials() {
        val username = app.sharedPrefs.getString(MyApp.USERNAME_KEY, null)
        val token = app.sharedPrefs.getString(MyApp.TOKEN_KEY, null)

        if (!username.isNullOrEmpty() && !token.isNullOrEmpty()) {
            // Both username and token are present --> show the profile screen
            showSubmitImage()
            setupUI()
            fetchAndPopulateRoutes()
        } else {
            // Clear username and token if they are not valid
            app.sharedPrefs.edit().apply {
                remove(MyApp.USERNAME_KEY)
                remove(MyApp.TOKEN_KEY)
                apply()
            }
            showLoginLayout()
            setupUIMinimal()
        }
    }

    private fun setupUI() {
        binding.loadImageButton.setOnClickListener {
            checkStoragePermissionAndOpenGallery()
        }

        binding.takePictureButton.setOnClickListener {
            checkCameraPermissionAndLaunchCamera()
        }

        binding.submitButton.setOnClickListener {
            photoUri?.let {
                sendImageToServer()
            } ?: Toast.makeText(requireContext(), getString(R.string.no_image_selected), Toast.LENGTH_SHORT).show()
        }

        binding.textImageprocessor.text = getString(R.string.image_processor_top_text)

        binding.buttonSubmitOccupancy.setOnClickListener {
            submitOccupancy()
        }
    }

    private fun setupUIMinimal() {
        binding.navigateToLoginButton.setOnClickListener {
            val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
            bottomNavigationView.selectedItemId = R.id.navigation_profile
        }
    }

    private fun toggleLoading(isLoading: Boolean) {
        binding.loadingCircle.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun fetchAndPopulateRoutes() {
        val baseUrl = app.sharedPrefs.getString(MyApp.BACKEND_URL_KEY, null) ?: run {
            Toast.makeText(requireContext(), "Base URL not found!", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "$baseUrl/routes"

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to fetch routes: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { jsonString ->
                        try {
                            val routesList = mutableListOf<Pair<String, String>>() // Pair of `_id` and display string
                            val jsonArray = JSONArray(jsonString)

                            for (i in 0 until jsonArray.length()) {
                                val routeObject = jsonArray.getJSONObject(i)
                                val trainNumber = routeObject.getInt("trainNumber")
                                val trainType = routeObject.getString("trainType")
                                val id = routeObject.getString("_id")

                                // Format: trainNumber (trainType)
                                val displayString = "$trainNumber ($trainType)"
                                routesList.add(Pair(id, displayString))
                            }

                            requireActivity().runOnUiThread {
                                populateDropdown(routesList)
                            }
                        } catch (e: Exception) {
                            requireActivity().runOnUiThread {
                                Toast.makeText(requireContext(), "Error parsing routes data", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Failed to fetch routes: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun populateDropdown(routes: List<Pair<String, String>>) {
        val spinner = binding.routesSpinner // Replace with your Spinner ID
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            routes.map { it.second } // Extract only the display strings for the dropdown
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                routeId = routes[position].first // Access the `_id` for the selected item
                trainType = routes[position].second.substringAfter("(").substringBefore(")") // "LP", "IC", ... Retrieving number of seats
                // Toast.makeText(requireContext(), "Train Type: $trainType", Toast.LENGTH_SHORT).show()
                // Toast.makeText(requireContext(), "Route ID: $routeId", Toast.LENGTH_SHORT).show()
                getNumberOfWagons()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Nothing
            }
        }
    }

    private fun getNumberOfWagons() {
        val baseUrl = app.sharedPrefs.getString(MyApp.BACKEND_URL_KEY, null)
        val url = "$baseUrl/passengers/seats/$trainType"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()

                    // Parse the response JSON
                    val jsonResponse = responseBody?.let { JSONObject(it) }
                    val numberOfWagons = jsonResponse?.getInt("numberOfWagons")

                    // Now populate the wagon spinner
                    activity?.runOnUiThread {
                        // Now populate the wagon spinner
                        if (numberOfWagons != null) {
                            populateWagonSpinner(numberOfWagons)
                        }
                    }
                } else {
                    // Handle the failure (e.g., show a toast or log the error)
                    Log.e("Error", "Failed to retrieve number of wagons")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
                Log.e("Error", "Network error: ${e.message}")
            }
        })
    }

    private fun populateWagonSpinner(numberOfWagons: Int) {
        // Get the wagon spinner
        val wagonList = (1..numberOfWagons).map { "$it. wagon" } // Create a list of wagon numbers

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            wagonList // Use the generated list of numbers (1 to numberOfWagons)
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.wagonNumber.adapter = adapter
    }

    private fun sendImageToServer() {
        toggleLoading(true)
        binding.submitButton.isEnabled = false

        try {
            val imageFile = photoUri?.let { uriToFile(it) } ?: return
            val token = app.sharedPrefs.getString(MyApp.TOKEN_KEY, null)
            val username = app.sharedPrefs.getString(MyApp.USERNAME_KEY, null) ?: return
            val wagonNumber = binding.wagonNumber.selectedItem?.toString()?.substringBefore(".")?.toIntOrNull() ?: return
            // val trainType = binding.trainType.selectedItem?.toString() ?: return

            // Convert image to Base64
            val imageBase64 = Base64.encodeToString(imageFile.readBytes(), Base64.DEFAULT)

            val mqttClient = MqttClient("tcp://10.11.12.100:1883", MqttClient.generateClientId(), null)
            val options = MqttConnectOptions().apply {
                isCleanSession = true
            }

            mqttClient.connect(options)

            val requestPayload = JSONObject().apply {
                put("token", token)
                put("username", username)
                put("imageBase64", imageBase64)
                put("trainType", trainType)
                put("wagonNumber", wagonNumber)
            }

            val requestTopic = "app/passengers/count/request"
            val responseTopic = "app/passengers/count/response/$username"

            // Subscribe to the response topic
            mqttClient.subscribe(responseTopic)

            // Publish image to the server
            mqttClient.publish(requestTopic, MqttMessage(requestPayload.toString().toByteArray()))

            // Handle server response
            mqttClient.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    requireActivity().runOnUiThread {
                        //Toast.makeText(requireContext(), "MQTT connection lost: ${cause?.message}", Toast.LENGTH_SHORT).show()
                        toggleLoading(false)
                        binding.submitButton.isEnabled = true
                    }
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    if (topic == responseTopic) {
                        val response = message?.toString()
                        val jsonResponse = JSONObject(response ?: "{}")
                        val success = jsonResponse.optBoolean("success", false)

                        requireActivity().runOnUiThread {
                            if (success) {
                                numOfPeople = jsonResponse.optInt("numOfPeople", -1)
                                numOfSeats = jsonResponse.optInt("numOfSeats", -1)

                                // Toast.makeText(requireContext(), "Passengers detected: $numOfPeople, Seats available: $numOfSeats", Toast.LENGTH_SHORT).show()
                            } else {
                                val errorMessage = jsonResponse.optString("message", "Unknown error")
                                Toast.makeText(requireContext(), "Failed: $errorMessage", Toast.LENGTH_SHORT).show()
                            }

                            // Show results and reset UI
                            showOccupancyData()
                            toggleLoading(false)
                            binding.submitButton.isEnabled = true
                        }

                        mqttClient.disconnect()
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    // Optional: Handle delivery confirmation
                }
            })

        } catch (e: Exception) {
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                toggleLoading(false)
                binding.submitButton.isEnabled = true
            }
            Log.e("ImageProcessor", "Error", e)
        }
    }


    private fun showOccupancyData() {
        if (numOfPeople != null || numOfSeats != null) {
            guessedOccupancyRate = (numOfPeople?.times(100.0))?.div(numOfSeats!!.toDouble())

            binding.detectedPeopleValue.text = "$numOfPeople"
            binding.seatsOnWagonValue.text = "$numOfSeats"
            binding.percentageOccupiedValue.text = String.format("%.1f%%", guessedOccupancyRate)

            // Percentage spinner
            val percentages = (0..100 step 10).map { "$it%" }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, percentages)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.percentageSpinner.adapter = adapter

            binding.layoutSubmitOccupancy.visibility = View.VISIBLE
            binding.layoutSubmitImage.visibility = View.GONE
        } else {
            Toast.makeText(requireContext(), "Error: Data not available", Toast.LENGTH_SHORT).show()
            return
        }
    }

    private fun submitOccupancy() {
        val context = requireContext()

        // Check location permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            Toast.makeText(context, "Please grant location permissions", Toast.LENGTH_SHORT).show()
            return
        }

        // Get current location
        val locationProvider = LocationServices.getFusedLocationProviderClient(context)
        locationProvider.lastLocation.addOnSuccessListener { location: Location? ->
            if (location == null) {
                Toast.makeText(context, "Failed to get location", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            // Prepare data for MQTT request
            val realOccupancyRate = binding.percentageSpinner.selectedItem.toString().removeSuffix("%").toFloat()
            val timeOfRequest = DateTimeFormatter.ISO_INSTANT.format(java.time.Instant.now())
            val jsonBody = JSONObject().apply {
                put("timeOfRequest", timeOfRequest)
                put("coordinatesOfRequest", JSONObject().apply {
                    put("lat", location.latitude)
                    put("lng", location.longitude)
                })
                put("guessedOccupancyRate", guessedOccupancyRate)
                put("realOccupancyRate", realOccupancyRate)
                put("route", routeId)
            }

            // Get token and set up MQTT client
            val token = app.sharedPrefs.getString(MyApp.TOKEN_KEY, null)
            val username = app.sharedPrefs.getString(MyApp.USERNAME_KEY, null)
            val mqttClient = MqttClient("tcp://10.11.12.100:1883", MqttClient.generateClientId(), null)
            val options = MqttConnectOptions().apply {
                isCleanSession = true
            }

            mqttClient.connect(options)

            // Prepare MQTT request
            val requestPayload = JSONObject().apply {
                put("token", token)
                put("username", username)
                put("timeOfRequest", timeOfRequest)
                put("coordinatesOfRequest", jsonBody.getJSONObject("coordinatesOfRequest"))
                put("guessedOccupancyRate", guessedOccupancyRate)
                put("realOccupancyRate", realOccupancyRate)
                put("route", routeId)
            }

            val requestTopic = "app/passengers/create/request"
            val responseTopic = "app/passengers/create/response/$username"

            // Subscribe to the response topic
            mqttClient.subscribe(responseTopic)

            // Publish to the request topic
            mqttClient.publish(requestTopic, MqttMessage(requestPayload.toString().toByteArray()))

            // Handle server response
            mqttClient.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    requireActivity().runOnUiThread {
                        //Toast.makeText(requireContext(), "MQTT connection lost: ${cause?.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    Log.d("MQTT", "Message arrived on topic: $topic")
                    if (topic == responseTopic) {
                        val response = message?.toString()
                        val jsonResponse = JSONObject(response ?: "{}")
                        val success = jsonResponse.optBoolean("success", false)

                        requireActivity().runOnUiThread {
                            if (success) {
                                Toast.makeText(requireContext(), "Occupancy submitted successfully", Toast.LENGTH_SHORT).show()
                                reset()
                            } else {
                                val errorMessage = jsonResponse.optString("message", "Unknown error")
                                Toast.makeText(requireContext(), "Failed: $errorMessage", Toast.LENGTH_SHORT).show()
                            }
                        }

                        mqttClient.disconnect()
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    // Optional: Handle delivery confirmation
                }
            })
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Location error: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("OccupancySubmit", "Error getting location", e)
        }
    }


    private fun uriToFile(uri: Uri): File? {
        val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
        val file = File(requireContext().cacheDir, "selected_image.jpg")
        val outputStream = FileOutputStream(file)

        try {
            inputStream?.copyTo(outputStream)
        } catch (e: Exception) {
            Log.e("ImageProcessor", "File conversion failed", e)
            return null
        } finally {
            inputStream?.close()
            outputStream.close()
        }
        return file
    }

    private fun checkStoragePermissionAndOpenGallery() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (requireContext().checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES)
                == PackageManager.PERMISSION_GRANTED
            ) {
                openGallery()
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            openGallery()
        }
    }

    private fun checkCameraPermissionAndLaunchCamera() {
        if (requireContext().checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        try {
            val photoFile = createImageFile()
            photoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )
            takePictureLauncher.launch(photoUri)
        } catch (ex: IOException) {
            Toast.makeText(requireContext(), "Error: Unable to create image file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(null)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun loadImageFromUri(uri: Uri) {
        binding.imageView.setImageURI(uri)
        photoUri = uri
        // Enable submit button after loading the image
        binding.submitButton.isEnabled = true
    }

    private fun showSubmitImage() {
        binding.layoutSubmitImage.visibility = View.VISIBLE
        binding.layoutLogin.visibility = View.GONE
    }

    private fun showLoginLayout() {
        binding.layoutSubmitImage.visibility = View.GONE
        binding.layoutLogin.visibility = View.VISIBLE
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            loadImageFromUri(it)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            openGallery()
        } else {
            Toast.makeText(requireContext(), "Error: Storage permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            photoUri?.let {
                loadImageFromUri(it)
            }
        } else {
            Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(requireContext(), "Error: Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun reset() {
        binding.layoutSubmitImage.visibility = View.VISIBLE
        binding.layoutLogin.visibility = View.GONE
        binding.layoutSubmitOccupancy.visibility = View.GONE
        fetchAndPopulateRoutes()
        binding.imageView.setImageDrawable(null)
        photoUri = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}