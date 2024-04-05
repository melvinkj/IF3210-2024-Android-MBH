package com.example.transactionmanagementsystem.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.Date

@Entity(tableName = "transactions")
@Parcelize
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val category: String,
    val amount: Double,
    val date: Date,
    val address: String,
    val latitude: Double,
    val longitude: Double
): Parcelable
