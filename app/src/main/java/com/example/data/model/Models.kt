package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CreditGreen
import com.example.ui.theme.DebitRed

enum class TransactionType(val displayName: String) {
    CREDIT("Credit / Income"),
    DEBIT("Debit / Expense")
}

enum class ExpenseCategory(
    val id: String,
    val title: String,
    val iconName: String,
    val colorHex: Long,
    val isCredit: Boolean = false
) {
    // Credit Categories
    SALARY("salary", "Salary", "Payments", 0xFF10B981, isCredit = true),
    TRANSFER_OTHER("transfer_other", "Transfer by other", "Work", 0xFF0D9488, isCredit = true),
    OTHER_CREDIT("other_credit", "Other", "TrendingUp", 0xFF8B5CF6, isCredit = true),

    // Debit Categories
    SHOPPING("shopping", "Shopping", "ShoppingBag", 0xFFEC4899),
    PROVISION("provision", "Provision", "Store", 0xFFF59E0B),
    BAKERY("bakery", "Bakery", "Cake", 0xFFF97316),
    HOTEL("hotel", "Hotel", "Hotel", 0xFF6366F1),
    FOODS("foods", "Foods", "Restaurant", 0xFFEF4444),
    BILLS("bills", "Bills", "Receipt", 0xFF3B82F6),
    STATIONARY("stationary", "Stationary", "Edit", 0xFF14B8A6),
    PRINTING("printing", "Printing", "Print", 0xFF8B5CF6),
    XEROX("xerox", "Xerox", "ContentCopy", 0xFF06B6D4),
    HEALTH_HOSPITAL("health_hospital", "Health & Hospital", "LocalHospital", 0xFF10B981),
    MEDICAL_STORE("medical_store", "Medical Store", "MedicalServices", 0xFFE11D48),
    FUEL("fuel", "Fuel", "LocalGasStation", 0xFFD97706),
    PUBLIC_TRANSPORT("public_transport", "Public Transport", "DirectionsBus", 0xFF2563EB),
    CABS("cabs", "Cabs", "LocalTaxi", 0xFF7C3AED),
    GENERAL_MISC("general_misc", "General & Miscellaneous", "MoreHoriz", 0xFF64748B);

    companion object {
        val creditCategories = entries.filter { it.isCredit }
        val debitCategories = entries.filter { !it.isCredit }

        fun fromId(id: String): ExpenseCategory {
            val exact = entries.find { it.id.equals(id, ignoreCase = true) }
            if (exact != null) return exact

            // Backward compatibility for legacy IDs
            return when (id.lowercase()) {
                "dining" -> FOODS
                "transport" -> PUBLIC_TRANSPORT
                "healthcare" -> HEALTH_HOSPITAL
                "freelance" -> TRANSFER_OTHER
                "investment" -> OTHER_CREDIT
                "entertainment" -> GENERAL_MISC
                "others" -> GENERAL_MISC
                else -> GENERAL_MISC
            }
        }
    }
}

data class SupportedCurrency(
    val code: String,
    val symbol: String,
    val name: String,
    val country: String,
    val rateToUsd: Double
) {
    companion object {
        val INR = SupportedCurrency("INR", "₹", "Indian Rupee", "India", 83.50)
        val USD = SupportedCurrency("USD", "$", "US Dollar", "United States", 1.0)
        val EUR = SupportedCurrency("EUR", "€", "Euro", "European Union", 0.92)
        val GBP = SupportedCurrency("GBP", "£", "British Pound", "United Kingdom", 0.79)
        val AED = SupportedCurrency("AED", "AED", "UAE Dirham", "United Arab Emirates", 3.67)
        val JPY = SupportedCurrency("JPY", "¥", "Japanese Yen", "Japan", 155.20)
        val CAD = SupportedCurrency("CAD", "CA$", "Canadian Dollar", "Canada", 1.36)
        val AUD = SupportedCurrency("AUD", "A$", "Australian Dollar", "Australia", 1.51)
        val SGD = SupportedCurrency("SGD", "S$", "Singapore Dollar", "Singapore", 1.35)

        val ALL = listOf(INR, USD, EUR, GBP, AED, JPY, CAD, AUD, SGD)

        fun find(code: String): SupportedCurrency =
            ALL.find { it.code.equals(code, ignoreCase = true) } ?: INR
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

data class BankDetail(
    val id: String = java.util.UUID.randomUUID().toString(),
    val bankName: String,
    val accountNumber: String,
    val ifscCode: String,
    val accountType: String = "Savings Account"
)

data class StoredDocument(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val docNumber: String,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
