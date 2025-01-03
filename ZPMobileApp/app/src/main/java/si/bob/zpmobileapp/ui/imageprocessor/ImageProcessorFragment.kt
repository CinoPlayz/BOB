package si.bob.zpmobileapp.ui.imageprocessor

import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import si.bob.zpmobileapp.databinding.FragmentImageprocessorBinding
import java.io.File
import java.io.IOException
import java.util.Date
import java.util.Locale

class ImageProcessorFragment : Fragment() {

    private var _binding: FragmentImageprocessorBinding? = null

    private val binding get() = _binding!!

    private lateinit var imageView: ImageView
    private lateinit var loadImageButton: Button
    private lateinit var takePictureButton: Button
    private var photoUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                loadImageFromUri(it)
            }
        }

    // Register for permission request
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                openGallery()
            } else {
                Toast.makeText(requireContext(), "Error: Storage permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                photoUri?.let {
                    loadImageFromUri(it)
                }
            } else {
                Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show()
            }
        }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                launchCamera()
            } else {
                Toast.makeText(requireContext(), "Error: Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val imageprocessorViewModel =
            ViewModelProvider(this)[ImageProcessorViewModel::class.java]

        _binding = FragmentImageprocessorBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize UI elements
        imageView = binding.imageView
        loadImageButton = binding.loadImageButton
        takePictureButton = binding.takePictureButton

        loadImageButton.setOnClickListener {
            checkStoragePermissionAndOpenGallery()
        }

        takePictureButton.setOnClickListener {
            checkCameraPermissionAndLaunchCamera()
        }

        val textView: TextView = binding.textImageprocessor
        imageprocessorViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }


        return root
    }

    private fun checkStoragePermissionAndOpenGallery() {
        // Android 13+ (API 33+) --> check for the new permission for images
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (requireContext().checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES)
                == PackageManager.PERMISSION_GRANTED
            ) {
                openGallery()
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            // Below Android 13 --> legacy READ_EXTERNAL_STORAGE or scoped storage
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
        imageView.setImageURI(uri)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
