package com.example.transactionmanagementsystem

// For dummy transaction
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.transactionmanagementsystem.models.Transaction
import com.example.transactionmanagementsystem.viewmodel.TransactionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

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
        val saveXlsxButton = view.findViewById<Button>(R.id.save1Btn)
        val saveXlsButton = view.findViewById<Button>(R.id.save2Btn)
        val sendButton = view.findViewById<Button>(R.id.sendBtn)
        val logoutButton = view.findViewById<Button>(R.id.logout)
        transactionViewModel = (activity as MainActivity).transactionViewModel
        // Set OnClickListener on the button

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
            CoroutineScope(Dispatchers.Main).launch {
                val transactions = transactionViewModel.getTransactionsOrderedById()
                withContext(Dispatchers.Main) {
                    createAndSaveExcelFile(transactions)
                }

            }
            Toast.makeText(requireContext(), "Saving Excel...", Toast.LENGTH_LONG).show()
            Toast.makeText(requireContext(), "File Saved", Toast.LENGTH_LONG).show()
        }

        // Logout
        logoutButton.setOnClickListener {
            logout()
        }
    }

    companion object {
        const val CREATE_FILE_REQUEST_CODE = 1
    }

//     Dummy transactions for saving file
    fun getDummyTransactions(): List<Transaction> {
        // Replace with your own dummy data
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = format.parse("2023-08-01")

        return listOf(
            Transaction(1, "Banana", "Fruit", 10000.0, date, "", 20.0, 10.0),
            Transaction(2, "Carrot", "Veggies", 5000.0, date, "", 20.0, 10.0)
        )
    }
    private suspend fun createAndSaveExcelFile(transactions: Flow<List<Transaction>>) {

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
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "kontolodon.xlsx")
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
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            val transactions : List<Transaction> = convertFlowToList(transactionViewModel.getTransactionsOrderedById())
//            data?.data?.also { uri ->
//                lifecycleScope.launch {
//                    saveFile(transactions, uri)
//                }
//            }
//        }
//    }

}