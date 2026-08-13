package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.LoanType
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
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanCalculatorBottomSheet(
    onDismiss: () -> Unit,
    onAddLoanToTracker: (
        title: String,
        personName: String,
        type: LoanType,
        totalPrincipal: Double,
        monthlyInstallment: Double,
        notes: String
    ) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var loanTitle by remember { mutableStateOf("Car Loan") }
    var principalText by remember { mutableStateOf("10000") }
    var interestRateText by remember { mutableStateOf("8.5") }
    var tenureYearsText by remember { mutableStateOf("3") }
    
    // Beneficiary selection: Self vs Someone Else
    var isForSomeoneElse by remember { mutableStateOf(false) }
    var beneficiaryName by remember { mutableStateOf("") }

    val principal = principalText.toDoubleOrNull() ?: 0.0
    val annualRatePercent = interestRateText.toDoubleOrNull() ?: 0.0
    val tenureYears = tenureYearsText.toDoubleOrNull() ?: 0.0

    // Calculation logic
    val totalMonths = (tenureYears * 12).toInt().coerceAtLeast(1)
    val monthlyRate = (annualRatePercent / 12) / 100

    val emi = if (principal > 0 && annualRatePercent > 0 && totalMonths > 0) {
        val factor = (1 + monthlyRate).pow(totalMonths)
        principal * monthlyRate * factor / (factor - 1)
    } else if (principal > 0 && totalMonths > 0) {
        principal / totalMonths
    } else 0.0

    val totalPayment = emi * totalMonths
    val totalInterest = (totalPayment - principal).coerceAtLeast(0.0)
    val interestPercent = if (totalPayment > 0) (totalInterest / totalPayment) * 100 else 0.0

    val reportText = buildString {
        append("📊 LOAN CALCULATOR ESTIMATE REPORT\n")
        append("----------------------------------------\n")
        append("Loan Purpose/Title: $loanTitle\n")
        append("Beneficiary: ${if (isForSomeoneElse) "On Behalf of ($beneficiaryName)" else "Self"}\n")
        append("Principal Amount: $%.2f\n".format(principal))
        append("Annual Interest Rate: %.2f%%\n".format(annualRatePercent))
        append("Tenure: %.1f years (%d months)\n".format(tenureYears, totalMonths))
        append("----------------------------------------\n")
        append("Estimated Monthly EMI: $%.2f\n".format(emi))
        append("Total Interest Payable: $%.2f\n".format(totalInterest))
        append("Total Amount Payable: $%.2f\n".format(totalPayment))
        append("----------------------------------------\n")
        append("Generated via Vault Finance App")
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PolishBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = PolishPrimaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = PolishPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Loan & EMI Calculator",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = PolishOnBackground
                    )
                    Text(
                        text = "Estimate monthly payments & export amortization summary",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Beneficiary Toggle Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, PolishOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Who is this loan for?",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (!isForSomeoneElse) PolishPrimaryContainer else PolishSurfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isForSomeoneElse = false }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = !isForSomeoneElse,
                                    onClick = { isForSomeoneElse = false },
                                    colors = RadioButtonDefaults.colors(selectedColor = PolishPrimary)
                                )
                                Text(
                                    text = "For Myself",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isForSomeoneElse) PolishPrimary else PolishOnBackground
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isForSomeoneElse) PolishPrimaryContainer else PolishSurfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isForSomeoneElse = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isForSomeoneElse,
                                    onClick = { isForSomeoneElse = true },
                                    colors = RadioButtonDefaults.colors(selectedColor = PolishPrimary)
                                )
                                Text(
                                    text = "For Someone Else",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isForSomeoneElse) PolishPrimary else PolishOnBackground
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = isForSomeoneElse) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            OutlinedTextField(
                                value = beneficiaryName,
                                onValueChange = { beneficiaryName = it },
                                label = { Text("Person Name (e.g. Alex Smith)") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = PolishPrimary)
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Inputs
            OutlinedTextField(
                value = loanTitle,
                onValueChange = { loanTitle = it },
                label = { Text("Loan Title / Purpose") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = principalText,
                onValueChange = { principalText = it },
                label = { Text("Principal Amount ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = interestRateText,
                    onValueChange = { interestRateText = it },
                    label = { Text("Interest Rate (% p.a.)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                OutlinedTextField(
                    value = tenureYearsText,
                    onValueChange = { tenureYearsText = it },
                    label = { Text("Tenure (Years)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Calculated Output Result Hero Box
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PolishPrimaryContainer),
                border = androidx.compose.foundation.BorderStroke(1.dp, PolishOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "ESTIMATED MONTHLY INSTALLMENT (EMI)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Text(
                        text = "$%.2f / mo".format(emi),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = PolishPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = PolishSurface
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Total Interest",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "$%.2f".format(totalInterest),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PolishDebitRed
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Total Repayable",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "$%.2f".format(totalPayment),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PolishOnBackground
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Interest vs Principal Ratio: %.1f%% Interest".format(interestPercent),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            LinearProgressIndicator(
                                progress = { (principal / totalPayment.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = PolishPrimary,
                                trackColor = PolishDebitRed
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons: Export Report & Add to Loan Tracker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Loan Calculation Report", reportText))

                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Loan EMI Calculation Report")
                            putExtra(Intent.EXTRA_TEXT, reportText)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Export Loan Report"))
                        Toast.makeText(context, "Report copied & ready to share!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Export Report",
                        modifier = Modifier.size(16.dp),
                        tint = PolishPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export Report", fontWeight = FontWeight.Bold, color = PolishPrimary)
                }

                Button(
                    onClick = {
                        val type = if (isForSomeoneElse) LoanType.ON_BEHALF_OF_OTHER else LoanType.I_BORROWED
                        val targetPerson = if (isForSomeoneElse) beneficiaryName.ifBlank { "Unspecified" } else "Self"
                        val notes = "Created via Loan Calculator (Rate: $annualRatePercent%%, Tenure: $tenureYears yrs)"
                        onAddLoanToTracker(loanTitle, targetPerson, type, principal, emi, notes)
                        Toast.makeText(context, "Added to active loan tracker!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1.2f),
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCard,
                        contentDescription = "Add Loan",
                        modifier = Modifier.size(16.dp),
                        tint = PolishOnPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save to Tracker", fontWeight = FontWeight.Bold, color = PolishOnPrimary)
                }
            }
        }
    }
}
