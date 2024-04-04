package com.example.transactionmanagementsystem

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import android.content.Context.MODE_PRIVATE
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.transactionmanagementsystem.models.Transaction
import kotlinx.coroutines.launch
import org.apache.poi.xssf.usermodel.XSSFWorkbook
// For dummy transaction
import java.text.SimpleDateFormat
import java.util.Locale

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
        val randomizeButton = view.findViewById<Button>(R.id.randomizeBtn)
        val saveXlsxButton = view.findViewById<Button>(R.id.save1Btn)
        val saveXlsButton = view.findViewById<Button>(R.id.save2Btn)
        val sendButton = view.findViewById<Button>(R.id.sendBtn)
        val logoutButton = view.findViewById<Button>(R.id.logout)

        // Set OnClickListener on the button
        randomizeButton.setOnClickListener {
            // Trigger the broadcast
            LocalBroadcastManager.getInstance(requireContext())
                .sendBroadcast(Intent("RANDOMIZE_TRANSACTION"))
        }

        // Save as XLSX file
        saveXlsxButton.setOnClickListener {
            val format = ".xlsx"
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                putExtra(Intent.EXTRA_TITLE, "transactions$format")
            }
            startActivityForResult(intent, CREATE_FILE_REQUEST_CODE)
        }

        // Save as XLS file
        saveXlsButton.setOnClickListener {
            val format = ".xls"
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/vnd.ms-excel"
                putExtra(Intent.EXTRA_TITLE, "transactions$format")
            }
            startActivityForResult(intent, CREATE_FILE_REQUEST_CODE)
        }

        // Send Email
        sendButton.setOnClickListener {
            // Perform your task here when the button is clicked
            sendEmail()
        }

        // Logout
        logoutButton.setOnClickListener {
            logout()
        }
    }

    companion object {
        const val CREATE_FILE_REQUEST_CODE = 1
    }

    // Dummy transactions for saving file
    fun getDummyTransactions(): List<Transaction> {
        // Replace with your own dummy data
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = format.parse("2023-08-01")

        return listOf(
            Transaction(1, "Banana", "Fruit", 10000.0, date, "", 20.0, 10.0),
            Transaction(2, "Carrot", "Veggies", 5000.0, date, "", 20.0, 10.0)
        )
    }

    // Save file as XLSX or XLS
    private fun saveFile(transactions: List<Transaction>, uri: Uri) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Transactions")

        // Create a CellStyle object for date format
        val dateCellStyle = workbook.createCellStyle().apply {
            dataFormat = workbook.creationHelper.createDataFormat().getFormat("yyyy-MM-dd")
        }

        // Create header row
        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("Date")
        header.createCell(1).setCellValue("Category")
        header.createCell(2).setCellValue("Amount")

        // Add transactions
        transactions.forEachIndexed { index, transaction ->
            val row = sheet.createRow(index + 1)

            // Create a cell for date and apply the date format
            val dateCell = row.createCell(0)
            dateCell.setCellValue(transaction.date)
            dateCell.cellStyle = dateCellStyle

            row.createCell(1).setCellValue(transaction.category)
            row.createCell(2).setCellValue(transaction.amount)
        }

        // Write to file
        requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
            workbook.write(outputStream)
        }
        workbook.close()
    }

    // Send email function
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

        if(intent.resolveActivity(requireActivity().packageManager) != null){
            startActivity(intent)
        }else{
            println(intent.resolveActivity(requireActivity().packageManager) != null)
            Toast.makeText( requireContext(),"Required App is not installed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logout() {
        // Clear the saved token
        val token = activity?.getSharedPreferences("UserToken", MODE_PRIVATE)
        val editor = token?.edit()
        editor?.remove("token")
        editor?.apply()

        // Stop the TokenExpiryService
        val serviceIntent = Intent(context, TokenExpiryService::class.java)
        context?.stopService(serviceIntent)

        // Redirect to the login page
        val intent = Intent(context, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    // For triggering the saveFile function
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                lifecycleScope.launch {
                    saveFile(getDummyTransactions(), uri)
                }
            }
        }
    }

}