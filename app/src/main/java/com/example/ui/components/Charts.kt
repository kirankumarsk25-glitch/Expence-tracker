package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TransactionEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.SupportedCurrency
import com.example.data.model.TransactionType
import com.example.ui.theme.PolishCreditGreen
import com.example.ui.theme.PolishDebitRed
import com.example.ui.theme.PolishOnBackground
import com.example.ui.theme.PolishOutline
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishSurface
import kotlin.math.atan2

data class CategorySliceData(
    val category: ExpenseCategory,
    val totalAmount: Double,
    val percentage: Float,
    val color: Color
)

@Composable
fun SpendingCategoryDonutChart(
    transactions: List<TransactionEntity>,
    currency: SupportedCurrency,
    modifier: Modifier = Modifier
) {
    val debitTxs = transactions.filter { it.type == TransactionType.DEBIT.name }
    val totalDebitUsd = debitTxs.sumOf { it.amountInBaseUsd }

    val categoryMap = debitTxs.groupBy { it.categoryId }
    val slices = categoryMap.map { (catId, txs) ->
        val cat = ExpenseCategory.fromId(catId)
        val sumUsd = txs.sumOf { it.amountInBaseUsd }
        val pct = if (totalDebitUsd > 0) (sumUsd / totalDebitUsd * 100).toFloat() else 0f
        CategorySliceData(
            category = cat,
            totalAmount = sumUsd * currency.rateToUsd,
            percentage = pct,
            color = Color(cat.colorHex)
        )
    }.sortedByDescending { it.totalAmount }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PolishSurface),
        border = BorderStroke(1.dp, PolishOutline)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Expenditure Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PolishOnBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (slices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No expenses logged yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF49454F)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(180.dp)
                            .pointerInput(slices) {
                                detectTapGestures { tapOffset ->
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val dx = tapOffset.x - center.x
                                    val dy = tapOffset.y - center.y
                                    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                    if (angle < 0) angle += 360f

                                    var currentAngle = 270f
                                    slices.forEachIndexed { index, slice ->
                                        val sweep = (slice.percentage / 100f) * 360f
                                        val start = currentAngle % 360f
                                        val end = (start + sweep) % 360f

                                        val isInside = if (start < end) {
                                            angle >= start && angle <= end
                                        } else {
                                            angle >= start || angle <= end
                                        }

                                        if (isInside) {
                                            selectedIndex = if (selectedIndex == index) null else index
                                            return@detectTapGestures
                                        }
                                        currentAngle += sweep
                                    }
                                }
                            }
                    ) {
                        var startAngle = 270f
                        val strokeWidth = 32.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                        val drawSize = Size(diameter, diameter)

                        slices.forEachIndexed { index, slice ->
                            val sweepAngle = (slice.percentage / 100f) * 360f
                            val isSelected = selectedIndex == index
                            val stroke = if (isSelected) strokeWidth + 12.dp.toPx() else strokeWidth

                            drawArc(
                                color = slice.color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle - 2f, // gap
                                useCenter = false,
                                topLeft = topLeft,
                                size = drawSize,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                            startAngle += sweepAngle
                        }
                    }

                    // Center text overlay
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val activeSlice = selectedIndex?.let { slices.getOrNull(it) }
                        if (activeSlice != null) {
                            Text(
                                text = activeSlice.category.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF49454F),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${currency.symbol}%.2f".format(activeSlice.totalAmount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = activeSlice.color
                            )
                            Text(
                                text = "%.1f%%".format(activeSlice.percentage),
                                style = MaterialTheme.typography.labelSmall,
                                color = PolishPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "TOTAL DEBIT",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF49454F),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${currency.symbol}%.2f".format(totalDebitUsd * currency.rateToUsd),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PolishOnBackground
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Legend List
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    slices.take(5).forEachIndexed { index, slice ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedIndex = if (selectedIndex == index) null else index
                                }
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(slice.color)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = slice.category.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PolishOnBackground,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "%.1f%%".format(slice.percentage),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF49454F),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${currency.symbol}%.2f".format(slice.totalAmount),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PolishOnBackground
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CashFlowBarChart(
    transactions: List<TransactionEntity>,
    currency: SupportedCurrency,
    modifier: Modifier = Modifier
) {
    val totalCredit = transactions
        .filter { it.type == TransactionType.CREDIT.name }
        .sumOf { it.amountInBaseUsd } * currency.rateToUsd

    val totalDebit = transactions
        .filter { it.type == TransactionType.DEBIT.name }
        .sumOf { it.amountInBaseUsd } * currency.rateToUsd

    val maxAmount = maxOf(totalCredit, totalDebit, 1.0)

    val creditRatio = (totalCredit / maxAmount).toFloat().coerceIn(0.05f, 1f)
    val debitRatio = (totalDebit / maxAmount).toFloat().coerceIn(0.05f, 1f)

    val animatedCredit by animateFloatAsState(
        targetValue = creditRatio,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
    )
    val animatedDebit by animateFloatAsState(
        targetValue = debitRatio,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PolishSurface),
        border = BorderStroke(1.dp, PolishOutline)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Credit vs Debit Comparison",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PolishOnBackground
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Credit Bar
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = "${currency.symbol}%.0f".format(totalCredit),
                            style = MaterialTheme.typography.labelSmall,
                            color = PolishCreditGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height((120 * animatedCredit).dp)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(PolishCreditGreen)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Credits",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF49454F),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Debit Bar
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = "${currency.symbol}%.0f".format(totalDebit),
                            style = MaterialTheme.typography.labelSmall,
                            color = PolishDebitRed,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height((120 * animatedDebit).dp)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(PolishDebitRed)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Debits",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF49454F),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
