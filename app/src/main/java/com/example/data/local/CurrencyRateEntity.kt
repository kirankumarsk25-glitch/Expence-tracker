package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currency_rates")
data class CurrencyRateEntity(
    @PrimaryKey
    val code: String,
    val symbol: String,
    val name: String,
    val rateToUsd: Double,
    val lastUpdated: Long = System.currentTimeMillis()
)
