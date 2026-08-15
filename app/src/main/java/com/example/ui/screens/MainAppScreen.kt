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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.data.local.TransactionEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.SupportedCurrency
import com.example.data.model.TransactionType
import com.example.ui.components.AddTransactionBottomSheet
import com.example.ui.components.CashFlowBarChart
import com.example.ui.components.HeroNetBalanceCard
import com.example.ui.components.SetBudgetBottomSheet
import com.example.ui.components.SpendingCategoryDonutChart
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

import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.unit.sp
import android.widget.Toast

enum class AppTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    LOANS("Loans", Icons.Default.AccountBalance),
    INVESTMENTS("Invest", Icons.Default.TrendingUp),
    BUDGETS("Budgets", Icons.Default.Wallet),
    STATS("Stats", Icons.Default.ShowChart),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun MainAppContainer(viewModel: FinanceViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    if (!isLoggedIn) {
        LoginScreen(viewModel = viewModel)
        return
    }

    var activeTab by remember { mutableStateOf(AppTab.HOME) }

    val userName by viewModel.userName.collectAsState()
    val isLocked by viewModel.isBiometricLocked.collectAsState()
    val summary by viewModel.financialSummary.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val budgets by viewModel.budgets.collectAsState()
    val syncState by viewModel.syncState.collectAsState()

    var showAddTxSheet by remember { mutableStateOf(false) }
    var showAddBudgetSheet by remember { mutableStateOf(false) }
    var currencyPickerOpen by remember { mutableStateOf(false) }

    val context = LocalContext.current

    if (isLocked) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = PolishBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = PolishPrimaryContainer,
                    modifier = Modifier.size(96.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = PolishPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Vault Locked",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = PolishOnBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Authentication required to access financial records",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                FloatingActionButton(
                    onClick = {
                        (context as? FragmentActivity)?.let { act ->
                            viewModel.biometricHelper.promptBiometricAuth(
                                activity = act,
                                onSuccess = { viewModel.unlockVaultWithBiometrics() },
                                onError = { /* handle */ }
                            )
                        } ?: viewModel.unlockVaultWithBiometrics()
                    },
                    containerColor = PolishPrimary,
                    contentColor = PolishOnPrimary,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unlock Vault", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    Scaffold(
        containerColor = PolishBackground,
        bottomBar = {
            NavigationBar(
                containerColor = PolishSurfaceVariant,
                tonalElevation = 8.dp
            ) {
                AppTab.entries.forEach { tab ->
                    val isSelected = activeTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { activeTab = tab },
                        icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PolishOnPrimary,
                            selectedTextColor = PolishPrimary,
                            indicatorColor = PolishPrimary,
                            unselectedIconColor = Color(0xFF49454F),
                            unselectedTextColor = Color(0xFF49454F)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (activeTab == AppTab.HOME || activeTab == AppTab.BUDGETS) {
                FloatingActionButton(
                    onClick = { showAddTxSheet = true },
                    containerColor = PolishPrimary,
                    contentColor = PolishOnPrimary,
                    shape = CircleShape
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val userLocation by viewModel.userLocation.collectAsState()
            val userLat by viewModel.userLatitude.collectAsState()
            val userLng by viewModel.userLongitude.collectAsState()

            HeaderBar(
                userName = userName,
                userLocation = userLocation,
                userLat = userLat,
                userLng = userLng,
                onLocationClick = {
                    viewModel.autoSyncGpsLocation(12.9716, 77.5946, "Bengaluru, India")
                    Toast.makeText(context, "Location Auto-Synced: 12.9716° N, 77.5946° E", Toast.LENGTH_SHORT).show()
                }
            )

            when (activeTab) {
                AppTab.HOME -> HomeTabContent(
                    viewModel = viewModel,
                    summary = summary,
                    transactions = transactions,
                    budgets = budgets,
                    syncState = syncState,
                    onCurrencyClick = { currencyPickerOpen = true },
                    onSyncClick = { viewModel.triggerBackgroundSyncSimulation() },
                    onSeeAllTransactions = { activeTab = AppTab.STATS }
                )

                AppTab.LOANS -> LoansTabContent(
                    viewModel = viewModel
                )

                AppTab.INVESTMENTS -> InvestmentsTabContent(
                    viewModel = viewModel
                )

                AppTab.BUDGETS -> BudgetsTabContent(
                    viewModel = viewModel,
                    summary = summary,
                    budgets = budgets,
                    transactions = transactions,
                    onAddBudget = { showAddBudgetSheet = true }
                )

                AppTab.STATS -> StatsTabContent(
                    viewModel = viewModel,
                    summary = summary,
                    transactions = transactions
                )

                AppTab.SETTINGS -> SettingsTabContent(viewModel = viewModel)
            }
        }
    }

    if (showAddTxSheet) {
        AddTransactionBottomSheet(
            onDismiss = { showAddTxSheet = false },
            onSubmit = { title, amount, type, category, currency, note, paymentMethod ->
                viewModel.addTransaction(
                    title, amount, type, category, currency, note, paymentMethod
                )
            }
        )
    }

    if (showAddBudgetSheet) {
        SetBudgetBottomSheet(
            onDismiss = { showAddBudgetSheet = false },
            onSubmit = { catId, limit, isAlert ->
                viewModel.setBudget(catId, limit, isAlert)
            }
        )
    }

    if (currencyPickerOpen) {
        CurrencyPickerBottomSheet(
            activeCurrency = summary.activeCurrency,
            onDismiss = { currencyPickerOpen = false },
            onSelect = { curr ->
                viewModel.setSelectedCurrency(curr)
                currencyPickerOpen = false
            }
        )
    }
}

@Composable
fun HeaderBar(
    userName: String,
    userLocation: String,
    userLat: Double,
    userLng: Double,
    onLocationClick: () -> Unit
) {
    val initials = userName.split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .take(2)
        .joinToString("")
        .ifEmpty { "VE" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = PolishPrimaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PolishPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PolishOnBackground
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PolishSurfaceVariant,
            border = BorderStroke(1.dp, PolishOutline),
            modifier = Modifier.clickable { onLocationClick() }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Auto Location",
                    tint = PolishPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = userLocation.split("(").first().trim(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PolishOnBackground
                    )
                    Text(
                        text = "${String.format("%.2f", userLat)}°, ${String.format("%.2f", userLng)}° GPS",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = PolishCreditGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun HomeTabContent(
    viewModel: FinanceViewModel,
    summary: com.example.data.model.FinancialSummary,
    transactions: List<TransactionEntity>,
    budgets: List<com.example.data.local.BudgetEntity>,
    syncState: com.example.data.model.SyncState,
    onCurrencyClick: () -> Unit,
    onSyncClick: () -> Unit,
    onSeeAllTransactions: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            HeroNetBalanceCard(
                summary = summary,
                syncState = syncState,
                onCurrencyClick = onCurrencyClick,
                onSyncClick = onSyncClick,
                onLockClick = { viewModel.lockVault() }
            )
        }

        item {
            val totalDebitUsd = transactions
                .filter { it.type == TransactionType.DEBIT.name }
                .sumOf { it.amountInBaseUsd }
            val totalBudgetUsd = budgets.sumOf { it.monthlyLimit }.coerceAtLeast(1.0)
            val overallRatio = (totalDebitUsd / totalBudgetUsd).toFloat().coerceIn(0f, 1f)
            val percentUsed = (overallRatio * 100).toInt()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                border = BorderStroke(1.dp, PolishOutline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = PolishPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Budget Tracking",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PolishOnBackground
                            )
                        }

                        Text(
                            text = "$percentUsed% Used",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (percentUsed > 80) PolishDebitRed else PolishPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { overallRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = if (percentUsed > 80) PolishDebitRed else PolishPrimary,
                        trackColor = PolishSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Used: ${summary.activeCurrency.symbol}%.2f".format(totalDebitUsd * summary.activeCurrency.rateToUsd),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF49454F),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Limit: ${summary.activeCurrency.symbol}%.2f".format(totalBudgetUsd * summary.activeCurrency.rateToUsd),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF49454F),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PolishOnBackground
                )

                TextButton(onClick = onSeeAllTransactions) {
                    Text("See all", color = PolishPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(transactions.take(8)) { tx ->
            TransactionRowItem(
                tx = tx,
                currency = summary.activeCurrency,
                onDelete = { viewModel.deleteTransaction(tx.id) }
            )
        }
    }
}

@Composable
fun BudgetsTabContent(
    viewModel: FinanceViewModel,
    summary: com.example.data.model.FinancialSummary,
    budgets: List<com.example.data.local.BudgetEntity>,
    transactions: List<TransactionEntity>,
    onAddBudget: () -> Unit
) {
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
                        text = "Category Budgets",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = PolishOnBackground
                    )
                    Text(
                        text = "Real-time notifications trigger when thresholds are reached",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF49454F),
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = PolishPrimary,
                    modifier = Modifier.clickable { onAddBudget() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Budget",
                        tint = Color.White,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        items(ExpenseCategory.entries) { category ->
            val budget = budgets.find { it.categoryId == category.id }
            val limitUsd = budget?.monthlyLimit ?: 0.0
            val spentUsd = transactions
                .filter { it.categoryId == category.id && it.type == TransactionType.DEBIT.name }
                .sumOf { it.amountInBaseUsd }

            val ratio = if (limitUsd > 0) (spentUsd / limitUsd).toFloat().coerceIn(0f, 1f) else 0f
            val percent = (ratio * 100).toInt()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                border = BorderStroke(1.dp, PolishOutline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(category.colorHex).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    tint = Color(category.colorHex),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = category.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PolishOnBackground
                                )
                                Text(
                                    text = if (limitUsd > 0) "$percent% of monthly budget" else "No budget set",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF49454F),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${summary.activeCurrency.symbol}%.2f".format(spentUsd * summary.activeCurrency.rateToUsd),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (percent >= 80) PolishDebitRed else PolishOnBackground
                            )
                            if (limitUsd > 0) {
                                Text(
                                    text = "/ ${summary.activeCurrency.symbol}%.2f".format(limitUsd * summary.activeCurrency.rateToUsd),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF49454F),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    if (limitUsd > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = if (percent >= 80) PolishDebitRed else Color(category.colorHex),
                            trackColor = PolishSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatsTabContent(
    viewModel: FinanceViewModel,
    summary: com.example.data.model.FinancialSummary,
    transactions: List<TransactionEntity>
) {
    val searchQuery by viewModel.searchQuery.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            SpendingCategoryDonutChart(
                transactions = transactions,
                currency = summary.activeCurrency
            )
        }

        item {
            CashFlowBarChart(
                transactions = transactions,
                currency = summary.activeCurrency
            )
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search transactions, notes...", color = Color(0xFF79747E)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PolishPrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = PolishSurface,
                        unfocusedContainerColor = PolishSurface,
                        focusedBorderColor = PolishPrimary,
                        unfocusedBorderColor = PolishOutline
                    )
                )
            }
        }

        items(transactions) { tx ->
            TransactionRowItem(
                tx = tx,
                currency = summary.activeCurrency,
                onDelete = { viewModel.deleteTransaction(tx.id) }
            )
        }
    }
}

@Composable
fun SettingsTabContent(viewModel: FinanceViewModel) {
    val context = LocalContext.current

    val isBioEnabled by viewModel.isBiometricEnabled.collectAsState()
    val syncCode by viewModel.deviceSyncCode.collectAsState()

    val currentName by viewModel.userName.collectAsState()
    val currentEmail by viewModel.userEmail.collectAsState()
    val currentPhone by viewModel.userPhone.collectAsState()
    val currentDob by viewModel.userDob.collectAsState()
    val currentLocation by viewModel.userLocation.collectAsState()

    var nameInput by remember(currentName) { mutableStateOf(currentName) }
    var emailInput by remember(currentEmail) { mutableStateOf(currentEmail) }
    var phoneInput by remember(currentPhone) { mutableStateOf(currentPhone) }
    var dobInput by remember(currentDob) { mutableStateOf(currentDob) }
    var locationInput by remember(currentLocation) { mutableStateOf(currentLocation) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Account Settings & Profile",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = PolishOnBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Manage your credentials, profile options & security settings",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Google Account Status Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PolishPrimaryContainer),
                border = BorderStroke(1.dp, PolishOutline)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = currentName.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("").ifEmpty { "U" },
                                fontWeight = FontWeight.Bold,
                                color = PolishPrimary,
                                fontSize = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PolishOnBackground
                        )
                        Text(
                            text = currentEmail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = PolishCreditGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Google Account Connected",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PolishCreditGreen
                            )
                        }
                    }
                }
            }
        }

        // Personal Profile Options Form
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                border = BorderStroke(1.dp, PolishOutline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Profile Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PolishOnBackground
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PolishPrimary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email ID") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PolishPrimary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PolishPrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = dobInput,
                        onValueChange = { dobInput = it },
                        label = { Text("Date of Birth (DOB)") },
                        leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null, tint = PolishPrimary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = locationInput,
                        onValueChange = { locationInput = it },
                        label = { Text("Location (Auto-saved)") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = PolishPrimary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.updateUserProfile(nameInput, emailInput, phoneInput, dobInput, locationInput)
                            Toast.makeText(context, "Profile & location updated in database!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save Profile",
                            modifier = Modifier.size(18.dp),
                            tint = PolishOnPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save Profile Changes",
                            fontWeight = FontWeight.Bold,
                            color = PolishOnPrimary
                        )
                    }
                }
            }
        }

        // Biometrics Card
        item {
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = PolishPrimary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Biometric Lock",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Require Fingerprint / Face ID to open app",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        androidx.compose.material3.Switch(
                            checked = isBioEnabled,
                            onCheckedChange = { viewModel.toggleBiometricEnabled(it) }
                        )
                    }
                }
            }
        }

        // Multi-Device Cloud Sync Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                border = BorderStroke(1.dp, PolishOutline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = PolishPrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Cross-Device Cloud Sync",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Pair with tablet or second mobile device",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PolishSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Device Pairing Code: $syncCode",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = PolishPrimary
                            )

                            TextButton(onClick = { viewModel.generateNewSyncCode() }) {
                                Text("Regenerate")
                            }
                        }
                    }
                }
            }
        }

        // Sign Out Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                border = BorderStroke(1.dp, PolishOutline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedButton(
                        onClick = {
                            viewModel.logout()
                            Toast.makeText(context, "Signed out of Google account", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PolishDebitRed),
                        border = BorderStroke(1.dp, PolishDebitRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = PolishDebitRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sign Out of Account",
                            fontWeight = FontWeight.Bold,
                            color = PolishDebitRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionRowItem(
    tx: TransactionEntity,
    currency: SupportedCurrency,
    onDelete: () -> Unit
) {
    val category = ExpenseCategory.fromId(tx.categoryId)
    val isCredit = tx.type == TransactionType.CREDIT.name
    val formattedDate = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(Date(tx.timestamp))

    val displayAmount = tx.amountInBaseUsd * currency.rateToUsd

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PolishSurface),
        border = BorderStroke(0.5.dp, PolishOutline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCredit) PolishCreditGreen.copy(alpha = 0.15f)
                            else Color(category.colorHex).copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = if (isCredit) PolishCreditGreen else Color(category.colorHex),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = tx.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = PolishOnBackground
                    )
                    Text(
                        text = "${category.title} • $formattedDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF49454F),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (isCredit) "+" else "-"}${currency.symbol}%.2f".format(displayAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCredit) PolishCreditGreen else PolishDebitRed
                    )
                    Text(
                        text = tx.paymentMethod,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF49454F),
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFF79747E),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CurrencyPickerBottomSheet(
    activeCurrency: SupportedCurrency,
    onDismiss: () -> Unit,
    onSelect: (SupportedCurrency) -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Currency") },
        text = {
            Column {
                SupportedCurrency.ALL.forEach { curr ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(curr) }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${curr.code} (${curr.symbol})",
                            fontWeight = if (curr.code == activeCurrency.code) FontWeight.Bold else FontWeight.Normal,
                            color = if (curr.code == activeCurrency.code) PolishPrimary else PolishOnBackground
                        )
                        Text(
                            text = curr.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
