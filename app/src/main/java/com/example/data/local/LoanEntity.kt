package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class LoanType(val label: String, val description: String) {
    ON_BEHALF_OF_OTHER(
        "Taken for Someone Else",
        "Loan taken on behalf of a third party who sends monthly repayments to cover it"
    ),
    I_BORROWED(
        "Direct Personal Borrowing",
        "Loan taken from a bank/lender that you pay back directly"
    ),
    I_LENT(
        "Money Lent to Someone",
        "Personal money lent directly to someone that they pay back"
    )
}

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val borrowerOrLenderName: String, // E.g., "John Doe" or "HDFC Bank"
    val type: String, // LoanType enum name
    val totalPrincipalUsd: Double,
    val monthlyInstallmentUsd: Double,
    val startDate: Long = System.currentTimeMillis(),
    val notes: String = "",
    val currencyCode: String = "USD"
)
