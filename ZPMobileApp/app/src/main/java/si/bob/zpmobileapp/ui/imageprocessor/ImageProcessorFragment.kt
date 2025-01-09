package si.bob.zpmobileapp.ui.imageprocessor

import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import si.bob.zpmobileapp.BuildConfig
import si.bob.zpmobileapp.MyApp
import si.bob.zpmobileapp.R
import si.bob.zpmobileapp.databinding.FragmentImageprocessorBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ImageProcessorFragment : Fragment() {

    private var _binding: FragmentImageprocessorBinding? = null
    private val binding get() = _binding!!

    private var photoUri: Uri? = null
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
                sendImageToServer(it)
            } ?: Toast.makeText(requireContext(), getString(R.string.no_image_selected), Toast.LENGTH_SHORT).show()
        }

        binding.textImageprocessor.text = getString(R.string.image_processor_top_text)
    }

    private fun setupUIMinimal() {
        binding.navigateToLoginButton.setOnClickListener {
            val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
            bottomNavigationView.selectedItemId = R.id.navigation_profile
        }
    }

    private fun sendImageToServer(imageUri: Uri) {
        try {
            val imageFile = uriToFile(imageUri) ?: return
            val baseUrl = BuildConfig.BASE_URL
            val token = BuildConfig.AUTH_TOKEN

            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "image",
                    imageFile.name,
                    imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                )
                .build()

            val request = Request.Builder()
                .url("$baseUrl/passengers/countPassengers")
                .addHeader("Authorization", "Bearer $token")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Request failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("ImageProcessor", "Request failed", e)
                    }
                }

                override fun onResponse(call: okhttp3.Call, response: Response) {
                    requireActivity().runOnUiThread {
                        if (response.isSuccessful) {
                            val responseJson = response.body?.string()
                            Toast.makeText(requireContext(), "Success: $responseJson", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Failed: ${response.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            })

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("ImageProcessor", "Error", e)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}