package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class InvestmentCategory(val label: String, val colorHex: Long) {
    STOCKS("Stocks & Equities", 0xFF2563EB),
    MUTUAL_FUNDS("Mutual Funds / ETFs", 0xFF7C3AED),
    CRYPTO("Crypto Assets", 0xFFD97706),
    REAL_ESTATE("Real Estate", 0xFF059669),
    FIXED_DEPOSIT("Fixed Deposit / Bonds", 0xFF0284C7),
    GOLD("Gold & Commodities", 0xFFEAB308),
    OTHER("Other Assets", 0xFF6B7280);

    companion object {
        fun fromName(name: String): InvestmentCategory {
            return entries.find { it.name == name } ?: OTHER
        }
    }
}

@Entity(tableName = "investments")
data class InvestmentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String, // InvestmentCategory enum name
    val investedAmountUsd: Double,
    val currentValueUsd: Double,
    val purchaseDate: Long = System.currentTimeMillis(),
    val notes: String = ""
)
