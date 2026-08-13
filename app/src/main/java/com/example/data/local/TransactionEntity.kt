package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "CREDIT", "DEBIT", "TRANSFER"
    val categoryId: String,
    val currencyCode: String = "USD",
    val amountInBaseUsd: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val paymentMethod: String = "Bank Account",
    val syncState: String = "SYNCED"
)
