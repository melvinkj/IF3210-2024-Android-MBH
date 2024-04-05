package com.example.transactionmanagementsystem.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.transactionmanagementsystem.database.TransactionDao
import com.example.transactionmanagementsystem.models.Transaction

@Database(
    entities = [Transaction::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class TransactionDatabase: RoomDatabase() {
//    abstract val dao: TransactionDao
    abstract fun getTransactionDao(): TransactionDao

    companion object{
        @Volatile // ensures changes made by one thread are visible to other threads
        private var instance: TransactionDatabase? = null
        private val LOCK = Any()  // ensures only one thread can execute at a time

        //singleton method
        operator fun invoke(context: Context) = instance ?:
        synchronized(LOCK){
            instance?:
            createDatabase(context).also {
                instance=it
            }

        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                TransactionDatabase::class.java,
                "transaction_management_system_db"
            ).build()
    }
}