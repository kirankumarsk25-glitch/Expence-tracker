package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey
    val categoryId: String,
    val monthlyLimit: Double,
    val currencyCode: String = "USD",
    val alertThresholdPercent: Int = 80,
    val isAlertEnabled: Boolean = true
)
