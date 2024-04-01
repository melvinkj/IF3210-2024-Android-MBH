package com.example.transactionmanagementsystem

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.example.transactionmanagementsystem.databinding.ActivityMainBinding
import java.io.File

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the button by its ID
        val sendButton = view.findViewById<Button>(R.id.sendBtn)

        // Set OnClickListener on the button
        sendButton.setOnClickListener {
            // Perform your task here when the button is clicked
            sendEmail()
        }
    }

    protected fun sendEmail() {
        val address = "13521052@std.stei.itb.ac.id"
        val subject = "Test Subject"
        val message = "Test Message"

        val intent = Intent(Intent.ACTION_SENDTO).apply{
            data = Uri.parse("mailto:$address")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, message)
        }
//        intent.type = "text/plain"

        if(intent.resolveActivity(requireActivity().packageManager) != null){
            startActivity(intent)
        }else{
            println(intent.resolveActivity(requireActivity().packageManager) != null)
            Toast.makeText( requireContext(),"Required App is not installed", Toast.LENGTH_SHORT).show()
        }
    }

}