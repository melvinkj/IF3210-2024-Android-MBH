package com.example.transactionmanagementsystem

// For dummy transaction
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import android.content.Context.MODE_PRIVATE
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.transactionmanagementsystem.models.Transaction
import com.example.transactionmanagementsystem.viewmodel.TransactionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File

class SettingsFragment : Fragment() {

    private lateinit var transactionViewModel : TransactionViewModel
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
        transactionViewModel = (activity as MainActivity).transactionViewModel
        // Set OnClickListener on the button
        randomizeButton.setOnClickListener {
            // Trigger the broadcast
            LocalBroadcastManager.getInstance(requireContext())
                .sendBroadcast(Intent("RANDOMIZE_TRANSACTION"))
        }

        val transactions = transactionViewModel.getTransactionsOrderedById()

        // Save as XLSX file
        saveXlsxButton.setOnClickListener {
//            val format = ".xlsx"
//            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
//                addCategory(Intent.CATEGORY_OPENABLE)
//                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
//                putExtra(Intent.EXTRA_TITLE, "transactions$format")
//            }
//            startActivityForResult(intent, CREATE_FILE_REQUEST_CODE)
            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.Main){
                    createAndSaveExcelFile(transactions, "xlsx")
                }
            }
            Toast.makeText(requireContext(), "Exporting Excel...", Toast.LENGTH_LONG).show()
            Toast.makeText(requireContext(), "Excel Saved to Downloads!", Toast.LENGTH_SHORT).show()
        }

        // Save as XLS file
        saveXlsButton.setOnClickListener {
//            val format = ".xls"
//            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
//                addCategory(Intent.CATEGORY_OPENABLE)
//                type = "application/vnd.ms-excel"
//                putExtra(Intent.EXTRA_TITLE, "transactions$format")
//            }
//            startActivityForResult(intent, CREATE_FILE_REQUEST_CODE)
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.IO){
                    createAndSaveExcelFile(transactions, "xls")
                }
            }
            Toast.makeText(requireContext(), "Exporting Excel...", Toast.LENGTH_LONG).show()
            Toast.makeText(requireContext(), "Excel Saved to Downloads!", Toast.LENGTH_SHORT).show()

        }

        // Send Email
        sendButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.IO){
                    createAndSaveExcelFile(transactions, "xlsx")
                }
            }
            Toast.makeText(requireContext(), "Exporting Excel...", Toast.LENGTH_LONG).show()
            Toast.makeText(requireContext(), "Excel Saved!", Toast.LENGTH_SHORT).show()
            pickFile()
        }

        // Logout
        logoutButton.setOnClickListener {
            logout()
        }
        activity?.title = "Settings"
    }

    companion object {
        const val CREATE_FILE_REQUEST_CODE = 1
        const val PICK_IMAGE_REQUEST = 2
        const val PICK_FILE_REQUEST_CODE = 3
    }

    private suspend fun createAndSaveExcelFile(transactions: Flow<List<Transaction>>, format: String) {

        withContext(Dispatchers.IO){
            val workbook = XSSFWorkbook()

            // Create a blank sheet
            val sheet = workbook.createSheet("Transactions")

            // Create some data rows
            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("Date")
            headerRow.createCell(1).setCellValue("Transaction Name")
            headerRow.createCell(2).setCellValue("Category")
            headerRow.createCell(3).setCellValue("Amount")
            headerRow.createCell(4).setCellValue("Address")
            headerRow.createCell(5).setCellValue("Latitude")
            headerRow.createCell(6).setCellValue("Longitude")
            convertFlowToList{listTransaction ->
                listTransaction.forEachIndexed(){ index, transaction ->
                    val row : Row = sheet.createRow(index+1)
                    row.createCell(0).setCellValue(transaction.date.toString())
                    row.createCell(1).setCellValue(transaction.title.toString())
                    row.createCell(2).setCellValue(transaction.category.toString())
                    row.createCell(3).setCellValue(transaction.amount.toDouble())
                    row.createCell(4).setCellValue(transaction.address.toString())
                    row.createCell(5).setCellValue(transaction.latitude.toString())
                    row.createCell(6).setCellValue(transaction.longitude.toString())

                }
                val currentDateTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                val datetime =  currentDateTime.format(formatter)

                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Transactions$datetime.$format" )
                val fileOutputStream = FileOutputStream(file)
                workbook.write(fileOutputStream)
                fileOutputStream.close()
                Log.d("Excel", "Excel file saved successfully at: ${file.absolutePath}")
            }
        }
    }

    private suspend fun convertFlowToList(callback: (List<Transaction>) -> Unit) {
        val flow = transactionViewModel.getTransactionsOrderedById()
        val resultList = mutableListOf<Transaction>() // Initialize an empty mutable list
        flow.collect { transactions -> // Collect elements emitted by the flow
            resultList.addAll(transactions) // Add each list of transactions to the result list
            callback(resultList)
        }
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
    @SuppressLint("IntentReset")
    protected fun sendEmail(uri: Uri) {
        val address = requireActivity().getSharedPreferences("Email", MODE_PRIVATE).getString("email","")
        val subject = "Daftar Transaksi"
        val message = "Daftar Transaksi"

        val email = Intent(Intent.ACTION_SEND)
        email.type = "application/octet-stream"
        email.data = Uri.parse("mailto:$address")
        email.putExtra(Intent.EXTRA_SUBJECT, subject)
        email.putExtra(Intent.EXTRA_TEXT, message)
        email.putExtra(Intent.EXTRA_STREAM, uri)
        requireContext().startActivity(Intent.createChooser(email, "choose"))

    }
    private fun openGallery(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }
    private fun pickFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*" // Set the MIME type to specify the type of file you want to pick
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false) // Set to true to allow picking multiple files
        }
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null){
            val selectedImageUri = data.data!!
            sendEmail(selectedImageUri)
        }

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // File picked successfully
            val uri = data?.data
            if(uri != null){
                sendEmail(uri)
            }else{
                Toast.makeText(requireContext(), "Pick the file you want to attach on your email!", Toast.LENGTH_SHORT).show()
            }
        }
    }

}