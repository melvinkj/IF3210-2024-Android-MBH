package com.example.transactionmanagementsystem

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat.isLocationEnabled
import androidx.navigation.findNavController
import com.example.transactionmanagementsystem.api.ApiInterface
import com.example.transactionmanagementsystem.api.RetrofitInstance
import com.example.transactionmanagementsystem.databinding.FragmentAddTransactionBinding
import com.example.transactionmanagementsystem.databinding.FragmentScanBinding
import com.example.transactionmanagementsystem.models.Transaction
import com.example.transactionmanagementsystem.viewmodel.TransactionViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
//import kotlinx.coroutines.DefaultExecutor.enqueue
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.properties.Delegates

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ScanFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class   ScanFragment : Fragment() {

    private lateinit var imageCapture: ImageCapture
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var viewFinder  : PreviewView

    private lateinit var transactionViewModel: TransactionViewModel
    private var scanBinding: FragmentScanBinding? = null

    private val binding get() = scanBinding!!

    private lateinit var  scanView: View

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        scanBinding = FragmentScanBinding.inflate(inflater, container, false)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        return binding.root
//        return inflater.inflate(R.layout.fragment_scan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(checkPermissionsCamera()){
            val captureButton = view.findViewById<FloatingActionButton>(R.id.captureButton)
            val pickImageButton = view.findViewById<FloatingActionButton>(R.id.pickImageButton)
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

            outputDirectory = getOutputDirectory()
            cameraExecutor = Executors.newSingleThreadExecutor()

            if (allPermissionsGranted()) {
                startCamera()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    REQUIRED_PERMISSIONS,
                    REQUEST_CAMERA_PERMISSIONS
                )
            }
            captureButton.setOnClickListener { takePhoto() }
            pickImageButton.setOnClickListener { openGallery() }
        }else{
            requestPermissionsCamera()
        }

        activity?.title = "Scan"

    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
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

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun getOutputDirectory(): File {
        val mediaDir = requireContext().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return mediaDir ?: requireContext().filesDir
    }

    private fun takePhoto() {
        // Create a timestamped output file
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )
        // Set up the output options for ImageCapture
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Capture the photo
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    Toast.makeText(requireContext(), "Photo Captured", Toast.LENGTH_SHORT).show()
                    sendToServer(savedUri)
                    Log.d(TAG, "Photo saved: $savedUri")

                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Error capturing photo: ${exception.message}", exception)
                    // Handle the error (e.g., show an error message)
                }
            }
        )
    }

    private fun openGallery(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun uriToFile(uri: Uri, context: Context): File? {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("temp", ".jpg")

        inputStream?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation(callback: (Boolean, String, Double, Double) -> Unit) {
        var address: String = "ITB"
        var latitude: Double = -6.8915
        var longitude: Double = 107.6107
        Log.d(TAG, "getLocation: asedekon sama lu semua")
        if (!locationAccessPreviouslyDenied()) {
            if (checkPermissions()) {
                if (isLocationEnabled()) {
                    mFusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                        val location: Location? = task.result
                        if (location != null) {
                            val geocoder = Geocoder(requireContext(), Locale.getDefault())
                            val list: MutableList<Address>? =
                                geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            binding.apply {
                                latitude = list?.get(0)?.latitude!!.toDouble()
                                longitude = list?.get(0)?.longitude!!.toDouble()
                                address = list?.get(0)?.getAddressLine(0).toString()
                            }
                            callback(true, address, latitude, longitude)
                        } else {
                            callback(false, address, latitude, longitude)
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Please turn on location", Toast.LENGTH_LONG)
                        .show()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                    callback(false, address, latitude, longitude)
                }
            } else {
                requestPermissions()
                callback(false, address, latitude, longitude)
            }
        } else {
            callback(true, address, latitude, longitude)
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }
    private fun requestPermissions() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) ||
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
        ) {
            // Show a rationale for why the permissions are needed
            Toast.makeText(
                requireContext(),
                "Location permission is required for this feature",
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
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }

    private fun checkPermissionsCamera(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissionsCamera() {
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

    private fun locationAccessPreviouslyDenied(): Boolean {
        val deniedPreviously = ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION )
                && ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION )
        return deniedPreviously
    }

    private fun sendToServer(selectedImageUri: Uri){
        Toast.makeText(requireContext(), "Sending...", Toast.LENGTH_LONG).show()
        val token = requireActivity().getSharedPreferences("UserToken", Service.MODE_PRIVATE).getString("token", null)
        val files = uriToFile(selectedImageUri, requireContext())
        val requestFile = RequestBody.create(MediaType.parse("image/jpeg"), files)
        val file = MultipartBody.Part.createFormData("file", files?.name, requestFile)
        val retIn = RetrofitInstance.getRetrofitInstance().create(ApiInterface::class.java)

        retIn.uploadBill("Bearer $token", file).enqueue(object: retrofit2.Callback <ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    t.message,
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>){
                if(response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Success Sending Image",
                        Toast.LENGTH_SHORT
                    ).show()
                    val responseBody = response.body()?.string()

                    val jsonObject = JSONObject(responseBody)
                    val itemsObject = jsonObject.getJSONObject("items")
                    val itemsArray = itemsObject.getJSONArray("items")

                    var amount = 0.0

                    for (i in 0 until itemsArray.length()) {
                        val itemObject = itemsArray.getJSONObject(i)
                        val qty = itemObject.getInt("qty")
                        val price = itemObject.getDouble("price")
                        amount += (qty * price)
                    }

                    val date = Date()
                    val category = "EXPENSE"
                    val title = "Transaction " + date.toString()

                    getLocation { success, address, latitude, longitude ->

                        if (success) {
                            transactionViewModel = (activity as MainActivity).transactionViewModel

                            val newTransaction = Transaction(
                                0,
                                title,
                                category,
                                amount,
                                date,
                                address,
                                latitude,
                                longitude
                            )
                            transactionViewModel.addTransaction(newTransaction)
                            Toast.makeText(
                                requireContext(),
                                "Transaction Saved!",
                                Toast.LENGTH_SHORT
                            ).show()

                        } else {
                            Toast.makeText(requireContext(), "Failed Sending To Server!", Toast.LENGTH_SHORT).show()
                        }

                    }
                }else if(response.code() == 401){
                    Toast.makeText(requireContext(), "Session Expired! Relogin", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(requireContext(), "Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
        })

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null){
            val selectedImageUri = data.data!!
            sendToServer(selectedImageUri)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission has been granted, proceed with camera operations
                startCamera()
            } else {
                // Camera permission has been denied, show a message or handle the situation
                Toast.makeText(requireContext(), "Camera permission is required to use the camera.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val TAG = "ScanFragment"
        private const val PICK_IMAGE_REQUEST = 102
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CAMERA_PERMISSIONS = 101
    }

}