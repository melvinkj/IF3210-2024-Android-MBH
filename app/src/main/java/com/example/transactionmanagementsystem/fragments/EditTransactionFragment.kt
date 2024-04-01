package com.example.transactionmanagementsystem.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.location.LocationManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.transactionmanagementsystem.MainActivity
import com.example.transactionmanagementsystem.R
import com.example.transactionmanagementsystem.databinding.FragmentEditTransactionBinding
import com.example.transactionmanagementsystem.models.Transaction
import com.example.transactionmanagementsystem.viewmodel.TransactionViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale

class EditTransactionFragment : Fragment(R.layout.fragment_edit_transaction), MenuProvider {

    private var editTransactionBinding: FragmentEditTransactionBinding? = null
    private val binding get() = editTransactionBinding!!

    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var  currentTransaction: Transaction
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2

    private val args: EditTransactionFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        editTransactionBinding = FragmentEditTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        transactionViewModel = (activity as MainActivity).transactionViewModel

        currentTransaction = args.transaction!!

        binding.editTransactionTitle.setText(currentTransaction.title)
        binding.editTransactionCategory.setText(currentTransaction.category)
        binding.editTransactionAmount.setText(currentTransaction.amount.toString())
        binding.editTransactionDate.setText(currentTransaction.date.toString())
        binding.editTransactionAddress.setText(currentTransaction.address)
        binding.editTransactionLatitude.setText(currentTransaction.latitude.toString())
        binding.editTransactionLongitude.setText(currentTransaction.longitude.toString())


        binding.buttonSave.setOnClickListener {
            val transactionTitle = binding.editTransactionTitle.text.toString().trim()
            val transactionCategory = binding.editTransactionCategory.text.toString().trim()
            val transactionAmountStr = binding.editTransactionAmount.text.toString().trim()
            val address = binding.editTransactionAddress.text.toString().trim()
            var transactionAmount: Double? = null
            if (transactionTitle.isEmpty()) {
                Toast.makeText(context, "Title must be filled", Toast.LENGTH_SHORT).show()
            }
            if (transactionCategory.isEmpty()) {
                Toast.makeText(context, "Category must be filled", Toast.LENGTH_SHORT).show()
            }
            if (transactionAmountStr.isEmpty()) {
                Toast.makeText(context, "Category must be filled", Toast.LENGTH_SHORT).show()
            } else {
                transactionAmount = transactionAmountStr.toDouble()

            }

            if (transactionTitle.isNotEmpty() && transactionCategory.isNotEmpty() && transactionAmount != null) {
                val transaction = Transaction(currentTransaction.id, transactionTitle, transactionCategory, transactionAmount, currentTransaction.date, address, currentTransaction.latitude, currentTransaction.longitude)
                transactionViewModel.editTransaction(transaction)

                Toast.makeText(context, "Update saved", Toast.LENGTH_SHORT).show()
                view.findNavController().popBackStack(R.id.transactionListFragment, false)
            }
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.buttonChangeLocation.setOnClickListener {
            getLocation()
        }
    }
    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
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
                            editTransactionLatitude.text = list?.get(0)?.latitude.toString()
                            editTransactionLongitude.text = list?.get(0)?.longitude.toString()
                            editTransactionAddress.text = list?.get(0)?.getAddressLine(0).toString()
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

    private fun deleteTransaction() {
        AlertDialog.Builder(activity).apply{
            setTitle("Delete Transaction")
            setMessage("Are you sure want to delete this transaction?")
            setPositiveButton("Delete"){_,_ ->
                transactionViewModel.deleteTransaction(currentTransaction)
                Toast.makeText(context, "Transaction Deleted", Toast.LENGTH_SHORT).show()
                view?.findNavController()?.popBackStack(R.id.transactionListFragment, false)
            }
            setNegativeButton("Cancel", null)
        }.create().show()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.edit_transaction_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when(menuItem.itemId){
            R.id.deleteMenu -> {
                deleteTransaction()
                true
            } else -> false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        editTransactionBinding = null
    }

}