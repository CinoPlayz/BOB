package si.bob.zpmobileapp.ui.imageprocessor

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
import android.media.Image
import android.media.ImageReader
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import si.bob.zpmobileapp.R
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AutoCaptureActivity : Activity() {

    private lateinit var cameraManager: CameraManager
    private var cameraDevice: CameraDevice? = null
    private lateinit var cameraCaptureSession: CameraCaptureSession
    private lateinit var previewSurface: Surface
    private lateinit var captureExecutor: ExecutorService
    private lateinit var surfaceView: SurfaceView
    private lateinit var imageReader: ImageReader
    private val cameraId: String = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_capture)

        surfaceView = findViewById(R.id.previewSurfaceView)
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        captureExecutor = Executors.newSingleThreadExecutor()

        imageReader = ImageReader.newInstance(1920, 1080, ImageFormat.JPEG, 1) // Choose appropriate resolution

        openCamera()
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        try {
            val handler = Handler(Looper.getMainLooper())

            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    startPreview()
                }

                override fun onDisconnected(camera: CameraDevice) {
                    cameraDevice?.close()
                    cameraDevice = null
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    Log.e(TAG, "Camera error: $error")
                    cameraDevice?.close()
                    cameraDevice = null
                }
            }, handler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Failed to open camera", e)
        }
    }


    private fun startPreview() {
        try {
            val surfaceHolder: SurfaceHolder = surfaceView.holder
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
            previewSurface = surfaceHolder.surface

            cameraDevice!!.createCaptureSession(
                listOf(previewSurface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        cameraCaptureSession = session
                        captureImage(session)  // Capture image when ready
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Toast.makeText(this@AutoCaptureActivity, "Configuration failed", Toast.LENGTH_SHORT).show()
                    }
                },
                Handler(Looper.getMainLooper()))
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Error starting preview", e)
        }
    }

    private fun captureImage(session: CameraCaptureSession) {
        try {
            val captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureRequestBuilder.addTarget(previewSurface)

            session.capture(captureRequestBuilder.build(), object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                    super.onCaptureCompleted(session, request, result)
                    Log.d(TAG, "Image capture successful")

                    val bitmap = captureBitmapFromSurface()

                    val photoUri = saveCapturedImageToStorage(bitmap)

                    val resultIntent = Intent().apply {
                        putExtra("photoUri", photoUri.toString())
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
            }, Handler(Looper.getMainLooper()))
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Error capturing image", e)
        }
    }

    private fun captureBitmapFromSurface(): Bitmap {
        val bitmap = Bitmap.createBitmap(surfaceView.width, surfaceView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        surfaceView.draw(canvas)

        return bitmap
    }

    private fun saveCapturedImageToStorage(bitmap: Bitmap): Uri {
        val timestamp = System.currentTimeMillis()
        val displayName = "captured_image_$timestamp.jpg" // Unique name using timestamp

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }

            Log.d(TAG, "Image saved at: $uri")
        }

        return uri ?: Uri.EMPTY
    }

    companion object {
        private const val TAG = "AutoCaptureActivity"
    }
}