package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.LoanEntity
import com.example.data.local.LoanPaymentEntity
import com.example.data.local.LoanType
import androidx.compose.material.icons.filled.Calculate
import com.example.ui.components.AddLoanBottomSheet
import com.example.ui.components.LoanCalculatorBottomSheet
import com.example.ui.components.RecordLoanPaymentBottomSheet
import com.example.ui.theme.PolishBackground
import com.example.ui.theme.PolishCreditGreen
import com.example.ui.theme.PolishDebitRed
import com.example.ui.theme.PolishOnBackground
import com.example.ui.theme.PolishOnPrimary
import com.example.ui.theme.PolishOutline
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishSurfaceVariant
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LoansTabContent(viewModel: FinanceViewModel) {
    val loans by viewModel.loans.collectAsState()
    val allPayments by viewModel.loanPayments.collectAsState()
    val summary by viewModel.financialSummary.collectAsState()
    val currency = summary.activeCurrency

    var showAddLoanSheet by remember { mutableStateOf(false) }
    var showCalculatorSheet by remember { mutableStateOf(false) }
    var selectedLoanForPayment by remember { mutableStateOf<LoanEntity?>(null) }

    // Summary calculations
    val totalLoansPrincipal = loans.sumOf { it.totalPrincipalUsd }
    val totalSentSoFar = allPayments.sumOf { it.amountUsd }
    val totalRemainingBalance = (totalLoansPrincipal - totalSentSoFar).coerceAtLeast(0.0)

    val loansTakenForOthers = loans.filter { it.type == LoanType.ON_BEHALF_OF_OTHER.name }
    val othersPrincipal = loansTakenForOthers.sumOf { it.totalPrincipalUsd }
    val othersSent = allPayments
        .filter { p -> loansTakenForOthers.any { it.id == p.loanId } }
        .sumOf { it.amountUsd }
    val balanceOthersOwe = (othersPrincipal - othersSent).coerceAtLeast(0.0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Loan & Debt Tracker",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = PolishOnBackground
                    )
                    Text(
                        text = "Track loans taken for others, monthly payments sent & remaining balances",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black, // High contrast black subtext
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = PolishPrimaryContainer,
                        modifier = Modifier.clickable { showCalculatorSheet = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Loan Calculator",
                                tint = PolishPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Calculator",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = PolishPrimary
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = PolishPrimary,
                        modifier = Modifier.clickable { showAddLoanSheet = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Loan",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "New Loan",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Summary Hero Card for Loans
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PolishPrimaryContainer),
                border = BorderStroke(1.dp, PolishOutline)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "OVERALL LOAN PORTFOLIO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black // Solid black for readability
                    )

                    Text(
                        text = "${currency.symbol}%.2f".format(totalLoansPrincipal * currency.rateToUsd),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = PolishPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PolishSurface
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Others Have To Give Me",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "${currency.symbol}%.2f".format(balanceOthersOwe * currency.rateToUsd),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PolishCreditGreen
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Total Repayments Sent",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "${currency.symbol}%.2f".format(totalSentSoFar * currency.rateToUsd),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PolishPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total Balance Left on Loans:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = "${currency.symbol}%.2f".format(totalRemainingBalance * currency.rateToUsd),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PolishDebitRed
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Active Loan Accounts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PolishOnBackground,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (loans.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PolishSurface),
                    border = BorderStroke(1.dp, PolishOutline)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Loans or Debt Records",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PolishOnBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Track loans taken on behalf of others or personal borrowing.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showAddLoanSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Loan",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Add Loan / Debt Record",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        } else {
            items(loans) { loan ->
                val loanPayments = allPayments.filter { it.loanId == loan.id }
                val totalSentForLoan = loanPayments.sumOf { it.amountUsd }
                val remainingLoanBalance = (loan.totalPrincipalUsd - totalSentForLoan).coerceAtLeast(0.0)

                LoanCardItem(
                    loan = loan,
                    payments = loanPayments,
                    totalSent = totalSentForLoan,
                    remainingBalance = remainingLoanBalance,
                    currencySymbol = currency.symbol,
                    currencyRate = currency.rateToUsd,
                    onRecordPayment = { selectedLoanForPayment = loan },
                    onDeleteLoan = { viewModel.deleteLoan(loan.id) },
                    onDeletePayment = { paymentId -> viewModel.deleteLoanPayment(paymentId) }
                )
            }
        }
    }

    if (showAddLoanSheet) {
        AddLoanBottomSheet(
            onDismiss = { showAddLoanSheet = false },
            onSubmit = { title, personName, type, principal, monthly, notes ->
                viewModel.addLoan(title, personName, type, principal, monthly, notes)
            }
        )
    }

    if (showCalculatorSheet) {
        LoanCalculatorBottomSheet(
            onDismiss = { showCalculatorSheet = false },
            onAddLoanToTracker = { title, personName, type, principal, monthly, notes ->
                viewModel.addLoan(title, personName, type, principal, monthly, notes)
            }
        )
    }

    selectedLoanForPayment?.let { loan ->
        RecordLoanPaymentBottomSheet(
            loan = loan,
            onDismiss = { selectedLoanForPayment = null },
            onSubmit = { amount, note, payerName ->
                viewModel.recordLoanPayment(loan.id, amount, note, payerName)
                selectedLoanForPayment = null
            }
        )
    }
}

@Composable
fun LoanCardItem(
    loan: LoanEntity,
    payments: List<LoanPaymentEntity>,
    totalSent: Double,
    remainingBalance: Double,
    currencySymbol: String,
    currencyRate: Double,
    onRecordPayment: () -> Unit,
    onDeleteLoan: () -> Unit,
    onDeletePayment: (String) -> Unit
) {
    var expandedHistory by remember { mutableStateOf(false) }

    val loanType = LoanType.entries.find { it.name == loan.type } ?: LoanType.ON_BEHALF_OF_OTHER
    val ratio = if (loan.totalPrincipalUsd > 0) (totalSent / loan.totalPrincipalUsd).toFloat().coerceIn(0f, 1f) else 0f
    val percentPaid = (ratio * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PolishSurface),
        border = BorderStroke(1.dp, PolishOutline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (loanType) {
                            LoanType.ON_BEHALF_OF_OTHER -> PolishPrimaryContainer
                            LoanType.I_BORROWED -> PolishSurfaceVariant
                            LoanType.I_LENT -> PolishSurfaceVariant
                        }
                    ) {
                        Text(
                            text = loanType.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PolishPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = loan.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PolishOnBackground
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = PolishPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Person / Party: ${loan.borrowerOrLenderName}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black // Solid black for readability
                        )
                    }
                }

                IconButton(onClick = onDeleteLoan) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Loan",
                        tint = PolishDebitRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Repayment Progress ($percentPaid% Paid)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "${payments.size} Payments Logged",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = PolishPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape),
                color = if (ratio >= 1.0f) PolishCreditGreen else PolishPrimary,
                trackColor = PolishSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Main Financial Details Grid
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PolishSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Total Loan Principal",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "$currencySymbol%.2f".format(loan.totalPrincipalUsd * currencyRate),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PolishOnBackground
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Monthly EMI Expected",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "$currencySymbol%.2f / mo".format(loan.monthlyInstallmentUsd * currencyRate),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PolishPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (loanType == LoanType.ON_BEHALF_OF_OTHER) "Amount Sent By ${loan.borrowerOrLenderName}" else "Total Repaid",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "$currencySymbol%.2f".format(totalSent * currencyRate),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PolishCreditGreen
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (loanType == LoanType.ON_BEHALF_OF_OTHER) "Balance ${loan.borrowerOrLenderName} Has To Give Me" else "Remaining Loan Balance",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "$currencySymbol%.2f".format(remainingBalance * currencyRate),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (remainingBalance > 0) PolishDebitRed else PolishCreditGreen
                            )
                        }
                    }
                }
            }

            if (loan.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Note: ${loan.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { expandedHistory = !expandedHistory }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (expandedHistory) "Hide Repayment History" else "View Payment Log (${payments.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = PolishPrimary
                        )
                        Icon(
                            imageVector = if (expandedHistory) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = PolishPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Button(
                    onClick = onRecordPayment,
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = PolishOnPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Log Payment",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PolishOnPrimary
                    )
                }
            }

            // Payment Log Expansion
            if (expandedHistory) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PolishSurfaceVariant)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Monthly Payment Log",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PolishOnBackground
                    )

                    if (payments.isEmpty()) {
                        Text(
                            text = "No payments logged yet for this loan account.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black
                        )
                    } else {
                        payments.forEach { payment ->
                            val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(payment.paymentDate))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = PolishCreditGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = payment.note.ifBlank { "Monthly Payment" },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = PolishOnBackground
                                        )
                                        Text(
                                            text = "$dateStr • Payer: ${payment.payerOrPayeeName}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Black
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "+$currencySymbol%.2f".format(payment.amountUsd * currencyRate),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PolishCreditGreen
                                    )
                                    IconButton(
                                        onClick = { onDeletePayment(payment.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = PolishDebitRed,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
