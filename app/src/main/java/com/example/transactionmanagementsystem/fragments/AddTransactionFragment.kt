package com.example.transactionmanagementsystem.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import com.example.transactionmanagementsystem.MainActivity
import com.example.transactionmanagementsystem.R
import com.example.transactionmanagementsystem.databinding.FragmentAddTransactionBinding
import com.example.transactionmanagementsystem.models.Transaction
import com.example.transactionmanagementsystem.viewmodel.TransactionViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import java.util.Random

class AddTransactionFragment : Fragment(R.layout.fragment_add_transaction), MenuProvider {

    private var addTransactionBinding: FragmentAddTransactionBinding? = null
    private val binding get() = addTransactionBinding!!

    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var  addTransactionView: View

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 3
    private var launchSource: String? = null
    companion object {
        // Define a method to create a new instance of the fragment with the launch source argument
        fun newInstance(source: String): AddTransactionFragment {
            val fragment = AddTransactionFragment()
            val args = Bundle().apply {
                putString("launch_source", source)
            }
            fragment.arguments = args
            return fragment
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve the launch source argument
        launchSource = arguments?.getString("launch_source")
    }

    private fun randomize() {
        val random = Random()
        val value = (random.nextInt(1000) * 1000).toString()
        binding.addTransactionAmount.setText(value)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        addTransactionBinding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        transactionViewModel = (activity as MainActivity).transactionViewModel
        addTransactionView = view

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val categoryOptions = arrayOf("Income", "Expense")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.addTransactionCategory.adapter = adapter

        binding.buttonSave.setOnClickListener {
            saveTransaction()
        }

        if (launchSource == "broadcast") {
            randomize()
        }
        activity?.title = "Add Transaction"
    }

    private fun areAllFieldsFilled(transactionTitle: String, transactionCategory: String, transactionAmountStr: String): Boolean {
        if (transactionTitle.isEmpty()) {
            Toast.makeText(addTransactionView.context, "Title must be filled", Toast.LENGTH_SHORT).show()
        }
        if (transactionCategory.isEmpty()) {
            Toast.makeText(addTransactionView.context, "Category must be filled", Toast.LENGTH_SHORT).show()
        }
        val transactionAmount: Double = try {
            transactionAmountStr.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(addTransactionView.context, "Amount must be filled with a number", Toast.LENGTH_SHORT).show()
            return false
        }
        if (transactionTitle.isNotEmpty() && transactionCategory.isNotEmpty() && transactionAmountStr.isNotEmpty()) {
            return true
        } else {
            return false
        }
    }

    private fun saveTransaction() {
        getLocation { success, address, latitude, longitude ->
            if (success) {
                // Location fetched successfully, proceed with saving the transaction

            } else {
                // Failed to fetch location, handle accordingly
                Toast.makeText(requireContext(), "Failed to fetch location, will use default value", Toast.LENGTH_SHORT).show()
            }
            val transactionTitle = binding.addTransactionTitle.text.toString().trim()
            val transactionCategory = binding.addTransactionCategory.selectedItem.toString().trim().uppercase()
            val transactionAmountStr = binding.addTransactionAmount.text.toString().trim()
            val transactionDate = Date()

            if (areAllFieldsFilled(transactionTitle, transactionCategory, transactionAmountStr)) {
                val transaction = Transaction(
                    0,
                    transactionTitle,
                    transactionCategory,
                    transactionAmountStr.toDouble(),
                    transactionDate,
                    address,
                    latitude,
                    longitude
                )
                transactionViewModel.addTransaction(transaction)

                Toast.makeText(addTransactionView.context, "Transaction saved", Toast.LENGTH_SHORT)
                    .show()
                addTransactionView.findNavController().popBackStack(R.id.navbarFragment, false)
            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.add_transaction_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when(menuItem.itemId){
            R.id.saveMenu -> {
                saveTransaction()
                true
            }
            else -> false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        addTransactionBinding = null
    }
    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation(callback: (Boolean, String, Double, Double) -> Unit) {
        var address: String = "ITB"
        var latitude: Double = -6.8915
        var longitude: Double = 107.6107

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
                    Toast.makeText(requireContext(), "Please turn on location", Toast.LENGTH_LONG).show()
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

    private fun locationAccessPreviouslyDenied(): Boolean {
        val deniedPreviously = ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION )
                && ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION )
        return deniedPreviously
    }


    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            return true
        }
        return false
    }
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }
//    @SuppressLint("MissingSuperCall")
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        if (requestCode == permissionId) {
//            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//            }
//        }
//    }
}