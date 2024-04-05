package com.example.transactionmanagementsystem

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.Toast
import android.provider.Settings

class TwibbonFragment : Fragment() {
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    private val REQUEST_CODE_PERMISSIONS = 101

    private lateinit var imageCapture: ImageCapture
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var viewFinder  : PreviewView
    private lateinit var twibbonImageView: ImageView
    private lateinit var capturedImageView: ImageView

    private val permissionId = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_twibbon, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (checkPermissions()) {
            twibbonImageView = view.findViewById(R.id.twibbonImageView)
            twibbonImageView.visibility = View.GONE
            capturedImageView = requireView().findViewById<ImageView>(R.id.capturedImageView)

            val retakeButton = view.findViewById<ImageButton>(R.id.retakeButton)
            retakeButton.setOnClickListener {
                // Set the visibility of the viewFinder to VISIBLE
                viewFinder.visibility = View.VISIBLE

                // Set the visibility of the capturedImageView to GONE
                val capturedImageView = requireView().findViewById<ImageView>(R.id.capturedImageView)
                capturedImageView.visibility = View.GONE
            }
            outputDirectory = getOutputDirectory()

            if (allPermissionsGranted()) {
                startCamera()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    REQUIRED_PERMISSIONS,
                    REQUEST_CODE_PERMISSIONS
                )
            }

            cameraExecutor = Executors.newSingleThreadExecutor()
            val captureButton = view.findViewById<ImageButton>(R.id.captureButton)

            captureButton.setOnClickListener {
                takePhoto()
            }
        } else {
            requestPermissions()
        }
        activity?.title = "Twibbon"

    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        if (checkPermissions()) {
            // If permissions are granted, start the camera
            val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                viewFinder = requireView().findViewById(R.id.viewFinder)
                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(viewFinder.surfaceProvider)
                    }

                imageCapture = ImageCapture.Builder()
                    .build()

                // Add a variable to hold the current camera selector
                var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // Add button to switch camera
                val switchCameraButton = requireView().findViewById<ImageButton>(R.id.switchCamera)
                switchCameraButton.setOnClickListener {
                    cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                    } catch (exc: Exception) {
                        Log.e(TAG, "Use case binding failed", exc)
                    }
                }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                } catch (exc: Exception) {
                    Log.e(TAG, "Use case binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(requireContext()))
        } else {
            requestPermissions()
        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = requireContext().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return mediaDir ?: requireContext().filesDir
    }

    private fun takePhoto() {
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        // Set up the output options for ImageCapture
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.targetRotation = viewFinder.display.rotation

        // Capture photo
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    Log.d(TAG, "Photo saved: $savedUri")

                    // Convert the savedUri to a Bitmap
                    val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, savedUri)

                    // Correct the orientation of the bitmap
                    val exif = ExifInterface(photoFile.absolutePath)
                    val rotation = when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270
                        else -> 0
                    }
                    val matrix = Matrix()
                    matrix.postRotate(rotation.toFloat())
                    val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                    val twibbonBitmap = (twibbonImageView.drawable as BitmapDrawable).bitmap

                    // Calculate the scale for the twibbon image based on the height
                    val scale = rotatedBitmap.height.toFloat() / twibbonBitmap.height

                    // Calculate the position for the twibbon image
                    val left = (rotatedBitmap.width - twibbonBitmap.width * scale) / 2

                    // Create a new matrix for the twibbon image
                    val twibbonMatrix = Matrix()
                    twibbonMatrix.postScale(scale, scale)
                    twibbonMatrix.postTranslate(left, 0f)

                    // Overlay the twibbon onto the photo
                    val resultBitmap = Bitmap.createBitmap(rotatedBitmap.width, rotatedBitmap.height, rotatedBitmap.config)
                    val canvas = Canvas(resultBitmap)
                    canvas.drawBitmap(rotatedBitmap, 0f, 0f, null)
                    canvas.drawBitmap(twibbonBitmap, twibbonMatrix, null)

                    // Display the result bitmap in an ImageView
                    capturedImageView.setImageBitmap(resultBitmap)

                    // Set the visibility of the twibbonImageView to VISIBLE
                    twibbonImageView.visibility = View.VISIBLE

                    // Set the visibility of the capturedImageView to VISIBLE
                    capturedImageView.visibility = View.VISIBLE

                    // Set the visibility of the viewFinder to GONE
                    viewFinder.visibility = View.GONE
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Error capturing photo: ${exception.message}", exception)
                }
            }
        )
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            // Show a rationale for why the permissions are needed
            Toast.makeText(
                requireContext(),
                "Camera permission is required for this feature",
                Toast.LENGTH_LONG
            ).show()
        } else {
            // Direct the user to app settings to enable permissions manually
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", requireActivity().packageName, null)
            intent.data = uri
            startActivity(intent)
        }

        // Request permissions regardless
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            permissionId
        )
    }

    companion object {
        private const val TAG = "CameraFragment"
    }
}