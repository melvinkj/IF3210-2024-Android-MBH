package com.example.transactionmanagementsystem.fragments

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.lifecycle.Lifecycle
import com.example.transactionmanagementsystem.GraphFragment
import com.example.transactionmanagementsystem.R
import com.example.transactionmanagementsystem.ScanFragment
import com.example.transactionmanagementsystem.SettingsFragment
import com.example.transactionmanagementsystem.TwibbonFragment
import com.example.transactionmanagementsystem.TransactionFragment
import com.example.transactionmanagementsystem.databinding.FragmentNavbarBinding

class NavbarFragment : Fragment(R.layout.fragment_navbar) {

    private var navbarBinding: FragmentNavbarBinding? = null
    private val binding get() = navbarBinding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        navbarBinding = FragmentNavbarBinding.inflate(inflater, container, false)

        replaceFragment(TransactionListFragment())

        binding.navbar.setOnItemSelectedListener{
            when(it.itemId){
                R.id.transaction -> replaceFragment(TransactionListFragment())
                R.id.scan -> replaceFragment(ScanFragment())
                R.id.graph -> replaceFragment(GraphFragment())
                R.id.settings -> replaceFragment(SettingsFragment())
                R.id.twibbon -> replaceFragment(TwibbonFragment())
                else -> {}
            }
            true

        }

        binding.navbar.setBackgroundColor(0xFFFFFFF.toInt())

        return binding.root
    }
    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.commit()
    }


    override fun onDestroy() {
        super.onDestroy()
        navbarBinding = null
    }
}