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

class AddTransactionFragment : Fragment(R.layout.fragment_add_transaction), MenuProvider {

    private var addTransactionBinding: FragmentAddTransactionBinding? = null
    private val binding get() = addTransactionBinding!!

    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var  addTransactionView: View

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2
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
    }

    private fun saveTransaction(view: View, transactionLocation: List<Any>){
        val transactionTitle = binding.addTransactionTitle.text.toString().trim()
        val transactionCategory = binding.addTransactionCategory.text.toString().trim()
        val transactionAmountStr = binding.addTransactionAmount.text.toString().trim()
        val transactionDate = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant())

        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 100)
        }

        val address = transactionLocation.get(0).toString()
        val latitude = transactionLocation.get(1).toString().toDouble()
        val longitude = transactionLocation.get(2).toString().toDouble()


        if (transactionTitle.isEmpty()) {
            Toast.makeText(addTransactionView.context, "Title must be filled", Toast.LENGTH_SHORT).show()
            return
        } else{}
        if (transactionCategory.isEmpty()) {
            Toast.makeText(addTransactionView.context, "Category must be filled", Toast.LENGTH_SHORT).show()
        }
        val transactionAmount: Double = try {
            transactionAmountStr.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(addTransactionView.context, "Amount must be filled with a number", Toast.LENGTH_SHORT).show()
            return
        }
        if (transactionTitle.isNotEmpty() && transactionCategory.isNotEmpty() && transactionAmountStr.isNotEmpty()) {
            val transaction = Transaction(0, transactionTitle, transactionCategory, transactionAmount, transactionDate, address, latitude, longitude)
            transactionViewModel.addTransaction(transaction)

            Toast.makeText(addTransactionView.context, "Transaction saved", Toast.LENGTH_SHORT).show()
            view.findNavController().popBackStack(R.id.transactionListFragment, false)
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.add_transaction_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when(menuItem.itemId){
            R.id.saveMenu -> {
                getLocation()
                true
            }
            else -> false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        addTransactionBinding = null
    }
//
//    fun getLocation() {
//        val location = fusedLocationProviderClient.lastLocation
//        var latitude: Double = -6.8915
//        var longitude: Double = 107.6107
//        var address: String = "ITB"
//
//        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
//            != PackageManager.PERMISSION_GRANTED &&
//            ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
//            != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 100)
////            saveTransaction(addTransactionView, listOf(address, latitude, longitude))
////            return
//        }
//
//
//        location.addOnSuccessListener {
//            if (it!=null) {
//                latitude = it.latitude
//                longitude = it.longitude
//                val geocoder = Geocoder(requireContext(), Locale.getDefault())
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                    geocoder.getFromLocation(latitude, longitude, 1) { list ->
//                        if (list.size != 0) {
//                            address = list[0].getAddressLine(0)
//                        }
//                    }
//
//                } else {
//                    try {
//                        val list = geocoder.getFromLocation(latitude, longitude, 1)
//                        if (list != null && list.size != 0) {
//                            address = list[0].getAddressLine(0)
//                        }
//                    } catch (e: IOException) {
//                        e.printStackTrace()
//                    }
//                }
//                saveTransaction(addTransactionView, listOf(address, latitude, longitude))
//            } else {
//                address = "hello"
//                saveTransaction(addTransactionView, listOf(address, latitude, longitude))
//            }
//        }
//    }
    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        var latitude: Double = -6.8915
        var longitude: Double = 107.6107
        var address: String = "ITB"

        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        val geocoder = Geocoder(requireContext(), Locale.getDefault())
                        val list: MutableList<Address>? =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        binding.apply {
//                            tvLocality.text = "Locality\n${list[0].locality}"
//                            tvAddress.text = "Address\n${list[0].getAddressLine(0)}"
                            latitude = list?.get(0)?.latitude!!.toDouble()
                            longitude = list?.get(0)?.longitude!!.toDouble()
                            address = list?.get(0)?.getAddressLine(0).toString()
                            saveTransaction(addTransactionView, listOf(address, latitude, longitude))
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
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
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }
    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }


}