package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.ExpenseCategory
import com.example.data.model.SupportedCurrency
import com.example.data.model.TransactionType
import com.example.ui.theme.PolishBackground
import com.example.ui.theme.PolishCreditGreen
import com.example.ui.theme.PolishDebitRed
import com.example.ui.theme.PolishOnBackground
import com.example.ui.theme.PolishOnPrimary
import com.example.ui.theme.PolishOutline
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionBottomSheet(
    lockedCurrency: SupportedCurrency,
    onDismiss: () -> Unit,
    onSubmit: (
        title: String,
        amount: Double,
        type: TransactionType,
        category: ExpenseCategory,
        currency: SupportedCurrency,
        note: String,
        paymentMethod: String
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.DEBIT) }

    // Dynamic category and payment methods based on type
    val currentCategories = if (selectedType == TransactionType.CREDIT) {
        ExpenseCategory.creditCategories
    } else {
        ExpenseCategory.debitCategories
    }

    var selectedCategory by remember(selectedType) {
        mutableStateOf(currentCategories.first())
    }

    val creditPaymentMethods = listOf("Bank Transfer", "Cash", "UPI")
    val debitPaymentMethods = listOf("UPI", "Cash", "Bank Transfer", "Debit Card", "Credit Card")

    val currentPaymentMethods = if (selectedType == TransactionType.CREDIT) creditPaymentMethods else debitPaymentMethods

    var paymentMethod by remember(selectedType) {
        mutableStateOf(currentPaymentMethods.first())
    }

    var note by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PolishBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Log Transaction",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = PolishOnBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Transaction Type Selector Tabs (Only Credit and Debit - No Transfer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PolishSurfaceVariant)
                    .padding(4.dp)
            ) {
                // Debit Button
                val isDebit = selectedType == TransactionType.DEBIT
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDebit) PolishDebitRed else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            selectedType = TransactionType.DEBIT
                        }
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Debit (Expense)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDebit) Color.White else PolishOnBackground
                        )
                    }
                }

                // Credit Button
                val isCredit = selectedType == TransactionType.CREDIT
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCredit) PolishCreditGreen else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            selectedType = TransactionType.CREDIT
                        }
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Credit (Income)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isCredit) Color.White else PolishOnBackground
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Input with locked currency indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PolishSurfaceVariant,
                    border = BorderStroke(1.dp, PolishOutline)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${lockedCurrency.code} (${lockedCurrency.symbol})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PolishPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (${lockedCurrency.symbol})") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Transaction Title / Payee") },
                placeholder = {
                    Text(if (selectedType == TransactionType.CREDIT) "e.g., Monthly Salary, Client Transfer" else "e.g., Grocery Shopping, Petrol, Dinner")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Category Selection Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Category (${if (selectedType == TransactionType.CREDIT) "Credit" else "Debit"})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = PolishOnBackground
                )

                Text(
                    text = selectedCategory.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = PolishPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Categories LazyRow Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(currentCategories) { category ->
                    val isSelected = selectedCategory == category
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) Color(category.colorHex) else PolishSurfaceVariant,
                        border = if (isSelected) BorderStroke(1.5.dp, Color.White) else BorderStroke(1.dp, PolishOutline),
                        modifier = Modifier.clickable { selectedCategory = category }
                    ) {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else PolishOnBackground,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Payment Method Header
            Text(
                text = "Payment Method",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = PolishOnBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Payment Methods LazyRow
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(currentPaymentMethods) { method ->
                    val isSelected = paymentMethod == method
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) PolishPrimary else PolishSurfaceVariant,
                        border = BorderStroke(1.dp, if (isSelected) PolishPrimary else PolishOutline),
                        modifier = Modifier.clickable { paymentMethod = method }
                    ) {
                        Text(
                            text = method,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else PolishOnBackground,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Notes / Reference (Optional)") },
                placeholder = { Text("Bill number, item details, remarks...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = PolishOnBackground, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = {
                        val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
                        if (title.isNotBlank() && parsedAmount > 0) {
                            onSubmit(
                                title,
                                parsedAmount,
                                selectedType,
                                selectedCategory,
                                lockedCurrency,
                                note,
                                paymentMethod
                            )
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedType == TransactionType.CREDIT) PolishCreditGreen else PolishPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = if (selectedType == TransactionType.CREDIT) "+ Save Credit Transaction" else "- Save Debit Expense",
                        fontWeight = FontWeight.Bold,
                        color = PolishOnPrimary
                    )
                }
            }
        }
    }
}
