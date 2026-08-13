package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "loan_payments")
data class LoanPaymentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val loanId: String,
    val amountUsd: Double,
    val paymentDate: Long = System.currentTimeMillis(),
    val note: String = "Monthly Installment Repayment",
    val payerOrPayeeName: String = ""
)
