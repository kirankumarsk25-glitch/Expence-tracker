package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CreditGreen
import com.example.ui.theme.DebitRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.PinkCategory
import com.example.ui.theme.PurpleCategory
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.WarningOrange

enum class TransactionType(val displayName: String) {
    CREDIT("Credit / Income"),
    DEBIT("Debit / Expense"),
    TRANSFER("Transfer")
}

enum class ExpenseCategory(
    val id: String,
    val title: String,
    val iconName: String,
    val colorHex: Long
) {
    SALARY("salary", "Salary & Income", "Payments", 0xFF10B981),
    FREELANCE("freelance", "Freelance & Business", "Work", 0xFF0D9488),
    INVESTMENT("investment", "Investments & Dividends", "TrendingUp", 0xFF8B5CF6),
    SHOPPING("shopping", "Shopping & Retail", "ShoppingBag", 0xFFEC4899),
    DINING("dining", "Dining & Food", "Restaurant", 0xFFF59E0B),
    BILLS("bills", "Bills & Utilities", "Receipt", 0xFF3B82F6),
    ENTERTAINMENT("entertainment", "Entertainment & Leisure", "Movie", 0xFFF97316),
    HEALTHCARE("healthcare", "Health & Wellness", "MedicalServices", 0xFFEF4444),
    TRANSPORT("transport", "Transport & Fuel", "DirectionsCar", 0xFF6366F1),
    OTHERS("others", "General & Miscellaneous", "MoreHoriz", 0xFF64748B);

    companion object {
        fun fromId(id: String): ExpenseCategory =
            entries.find { it.id.equals(id, ignoreCase = true) } ?: OTHERS
    }
}

data class SupportedCurrency(
    val code: String,
    val symbol: String,
    val name: String,
    val rateToUsd: Double
) {
    companion object {
        val USD = SupportedCurrency("USD", "$", "US Dollar", 1.0)
        val EUR = SupportedCurrency("EUR", "€", "Euro", 0.92)
        val GBP = SupportedCurrency("GBP", "£", "British Pound", 0.79)
        val INR = SupportedCurrency("INR", "₹", "Indian Rupee", 83.50)
        val JPY = SupportedCurrency("JPY", "¥", "Japanese Yen", 155.20)
        val CAD = SupportedCurrency("CAD", "CA$", "Canadian Dollar", 1.36)
        val AUD = SupportedCurrency("AUD", "A$", "Australian Dollar", 1.51)
        val SGD = SupportedCurrency("SGD", "S$", "Singapore Dollar", 1.35)

        val ALL = listOf(USD, EUR, GBP, INR, JPY, CAD, AUD, SGD)

        fun find(code: String): SupportedCurrency =
            ALL.find { it.code.equals(code, ignoreCase = true) } ?: USD
    }
}

enum class SyncState {
    SYNCED,
    PENDING_UPLOAD,
    SYNCING,
    OFFLINE
}

data class FinancialSummary(
    val totalCredit: Double,
    val totalDebit: Double,
    val netBalance: Double,
    val monthlySavingsRate: Double,
    val activeCurrency: SupportedCurrency
)
