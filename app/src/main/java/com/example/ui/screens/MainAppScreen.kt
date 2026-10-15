package com.example.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.data.local.BudgetEntity
import com.example.data.local.TransactionEntity
import com.example.data.model.BankDetail
import com.example.data.model.ExpenseCategory
import com.example.data.model.FinancialSummary
import com.example.data.model.StoredDocument
import com.example.data.model.SupportedCurrency
import com.example.data.model.TransactionType
import com.example.ui.components.AddTransactionBottomSheet
import com.example.ui.components.ChangeProfilePhotoBottomSheet
import com.example.ui.components.HeroNetBalanceCard
import com.example.ui.components.ProfileAvatarView
import com.example.ui.components.SetBudgetBottomSheet
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

// 5 Core Tabs - No Stats tab as requested
enum class AppTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    LOANS("Loans", Icons.Default.AccountBalance),
    INVESTMENTS("Invest", Icons.Default.TrendingUp),
    BUDGETS("Budgets", Icons.Default.Wallet),
    SETTINGS("Settings", Icons.Default.Person)
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
                    modifier = Modifier.size(90.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Vault Locked",
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
                    text = "Authenticate with Biometrics to access financial records",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        val activity = context as? FragmentActivity
                        if (activity != null) {
                            viewModel.biometricHelper.promptBiometricAuth(
                                activity = activity,
                                onSuccess = { viewModel.unlockVaultWithBiometrics() },
                                onError = { err ->
                                    Toast.makeText(context, "Authentication: $err", Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            viewModel.unlockVaultWithBiometrics()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(50.dp)
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
            val profilePhotoUri by viewModel.userProfilePhotoUri.collectAsState()

            HeaderBar(
                userName = userName,
                profilePhotoUri = profilePhotoUri,
                userLocation = userLocation,
                userLat = userLat,
                userLng = userLng,
                onLocationClick = {
                    viewModel.autoSyncGpsLocation(12.9716, 77.5946, "Bengaluru, India")
                    Toast.makeText(context, "Location Auto-Synced: 12.9716° N, 77.5946° E", Toast.LENGTH_SHORT).show()
                },
                onAvatarClick = { activeTab = AppTab.SETTINGS }
            )

            when (activeTab) {
                AppTab.HOME -> HomeTabContent(
                    viewModel = viewModel,
                    summary = summary,
                    transactions = transactions,
                    syncState = syncState,
                    onSyncClick = { viewModel.triggerBackgroundSyncSimulation() },
                    onSeeAllTransactions = { activeTab = AppTab.BUDGETS }
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

                AppTab.SETTINGS -> SettingsTabContent(viewModel = viewModel)
            }
        }
    }

    if (showAddTxSheet) {
        AddTransactionBottomSheet(
            lockedCurrency = summary.activeCurrency,
            onDismiss = { showAddTxSheet = false },
            onSubmit = { title, amount, type, category, currency, note, paymentMethod ->
                viewModel.addTransaction(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    currency = currency,
                    note = note,
                    paymentMethod = paymentMethod
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
}

@Composable
fun HeaderBar(
    userName: String,
    profilePhotoUri: String? = null,
    userLocation: String,
    userLat: Double,
    userLng: Double,
    onLocationClick: () -> Unit,
    onAvatarClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = if (onAvatarClick != null) Modifier.clickable { onAvatarClick() } else Modifier
        ) {
            ProfileAvatarView(
                avatarUri = profilePhotoUri,
                userName = userName,
                size = 42.dp
            )

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
    summary: FinancialSummary,
    transactions: List<TransactionEntity>,
    syncState: com.example.data.model.SyncState,
    onSyncClick: () -> Unit,
    onSeeAllTransactions: () -> Unit
) {
    val context = LocalContext.current
    var showAllTransactions by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // Vault Balance, Credit & Debit Summary Box (Budget tracking card removed as requested)
        item {
            HeroNetBalanceCard(
                summary = summary,
                syncState = syncState,
                onCurrencyClick = {
                    Toast.makeText(
                        context,
                        "Locked Currency: ${summary.activeCurrency.name} (${summary.activeCurrency.symbol})",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onSyncClick = onSyncClick,
                onLockClick = { viewModel.lockVault() }
            )
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
                    text = if (showAllTransactions) "All Transactions (${transactions.size})" else "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PolishOnBackground
                )

                TextButton(onClick = { showAllTransactions = !showAllTransactions }) {
                    Text(
                        text = if (showAllTransactions) "Show Less" else "See All",
                        color = PolishPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        val displayedTransactions = if (showAllTransactions) transactions else transactions.take(8)

        if (displayedTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PolishSurface),
                    border = BorderStroke(1.dp, PolishOutline)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Transactions Recorded Yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PolishOnBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the '+' button below to record your first credit or debit.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            items(displayedTransactions) { tx ->
                TransactionRowItem(
                    tx = tx,
                    currency = summary.activeCurrency,
                    onDelete = { viewModel.deleteTransaction(tx.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsTabContent(
    viewModel: FinanceViewModel,
    summary: FinancialSummary,
    budgets: List<BudgetEntity>,
    transactions: List<TransactionEntity>,
    onAddBudget: () -> Unit
) {
    var selectedCategoryForDetails by remember { mutableStateOf<ExpenseCategory?>(null) }

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
                        text = "Click any category budget to see its individual transactions",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = PolishPrimary,
                    modifier = Modifier.clickable { onAddBudget() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Set Budget",
                        tint = Color.White,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        // Show debit expense categories for budgeting
        items(ExpenseCategory.debitCategories) { category ->
            val budget = budgets.find { it.categoryId == category.id }
            val limitUsd = budget?.monthlyLimit ?: 0.0
            val spentUsd = transactions
                .filter { it.categoryId == category.id && it.type == TransactionType.DEBIT.name }
                .sumOf { it.amountInBaseUsd }

            val ratio = if (limitUsd > 0) (spentUsd / limitUsd).toFloat().coerceIn(0f, 1f) else 0f
            val percent = (ratio * 100).toInt()
            val txCount = transactions.count { it.categoryId == category.id }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { selectedCategoryForDetails = category },
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
                                    .size(38.dp)
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
                                    text = "$txCount transactions • Tap to view",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PolishPrimary,
                                    fontWeight = FontWeight.Bold
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
                                    color = Color.Black,
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
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$percent% of monthly budget",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                            if (percent >= 100) {
                                Text(
                                    text = "Budget Exceeded!",
                                    style = MaterialTheme.typography.labelSmall,
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
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Modal Sheet showing specific transactions for selected budget category
    if (selectedCategoryForDetails != null) {
        val cat = selectedCategoryForDetails!!
        val catTransactions = transactions.filter { it.categoryId == cat.id }
        val catSpent = catTransactions.sumOf { it.amountInBaseUsd * summary.activeCurrency.rateToUsd }

        ModalBottomSheet(
            onDismissRequest = { selectedCategoryForDetails = null },
            containerColor = PolishBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${cat.title} Transactions",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = PolishOnBackground
                        )
                        Text(
                            text = "Total Spent: ${summary.activeCurrency.symbol}%.2f (${catTransactions.size} records)".format(catSpent),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = PolishPrimary
                        )
                    }

                    TextButton(onClick = { selectedCategoryForDetails = null }) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (catTransactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No transactions logged in ${cat.title} yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                    ) {
                        items(catTransactions) { tx ->
                            TransactionRowItem(
                                tx = tx,
                                currency = summary.activeCurrency,
                                onDelete = { viewModel.deleteTransaction(tx.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsTabContent(viewModel: FinanceViewModel) {
    val context = LocalContext.current

    val currentName by viewModel.userName.collectAsState()
    val currentDob by viewModel.userDob.collectAsState()
    val currentBloodGroup by viewModel.userBloodGroup.collectAsState()
    val currentEmail by viewModel.userEmail.collectAsState()
    val currentPhone by viewModel.userPhone.collectAsState()
    val currentSecPhone by viewModel.userSecondaryPhone.collectAsState()
    val currentOfficeAddr by viewModel.userOfficeAddress.collectAsState()
    val currentPermAddr by viewModel.userPermanentAddress.collectAsState()
    val currentTempAddr by viewModel.userTemporaryAddress.collectAsState()
    val currentPan by viewModel.userPanNumber.collectAsState()
    val currentAadhar by viewModel.userAadharNumber.collectAsState()
    val currentDl by viewModel.userDlNumber.collectAsState()
    val currentCarNo by viewModel.userCarNumber.collectAsState()
    val currentBikeNo by viewModel.userBikeNumber.collectAsState()
    val profilePhotoUri by viewModel.userProfilePhotoUri.collectAsState()

    val bankAccounts by viewModel.bankAccounts.collectAsState()
    val storedDocuments by viewModel.storedDocuments.collectAsState()

    val isBioEnabled by viewModel.isBiometricEnabled.collectAsState()
    val lockedCurrency by viewModel.selectedCurrency.collectAsState()
    val syncCode by viewModel.deviceSyncCode.collectAsState()

    // Form inputs state
    var nameInput by remember(currentName) { mutableStateOf(currentName) }
    var dobInput by remember(currentDob) { mutableStateOf(currentDob) }
    var bloodGroupInput by remember(currentBloodGroup) { mutableStateOf(currentBloodGroup) }
    var emailInput by remember(currentEmail) { mutableStateOf(currentEmail) }
    var phoneInput by remember(currentPhone) { mutableStateOf(currentPhone) }
    var secPhoneInput by remember(currentSecPhone) { mutableStateOf(currentSecPhone) }
    var officeAddrInput by remember(currentOfficeAddr) { mutableStateOf(currentOfficeAddr) }
    var permAddrInput by remember(currentPermAddr) { mutableStateOf(currentPermAddr) }
    var tempAddrInput by remember(currentTempAddr) { mutableStateOf(currentTempAddr) }
    var panInput by remember(currentPan) { mutableStateOf(currentPan) }
    var aadharInput by remember(currentAadhar) { mutableStateOf(currentAadhar) }
    var dlInput by remember(currentDl) { mutableStateOf(currentDl) }
    var carNoInput by remember(currentCarNo) { mutableStateOf(currentCarNo) }
    var bikeNoInput by remember(currentBikeNo) { mutableStateOf(currentBikeNo) }

    // Dialogs state for Bank Account and Documents
    var showAddBankDialog by remember { mutableStateOf(false) }
    var showAddDocDialog by remember { mutableStateOf(false) }

    // Profile Photo Change Modal State
    var showChangePhotoSheet by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Account Profile & Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = PolishOnBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Personal details, identification, multiple bank accounts & export",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Profile Photo & Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PolishPrimaryContainer),
                border = BorderStroke(1.dp, PolishOutline)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfileAvatarView(
                        avatarUri = profilePhotoUri,
                        userName = currentName,
                        size = 72.dp,
                        showCameraBadge = true,
                        onClick = { showChangePhotoSheet = true }
                    )

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
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PolishPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Tap photo to change avatar",
                                style = MaterialTheme.typography.labelSmall,
                                color = PolishPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section 1: Personal & Residence Details
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
                        text = "Personal & Residence Information",
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

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = dobInput,
                            onValueChange = { dobInput = it },
                            label = { Text("DOB (Date of Birth)") },
                            leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null, tint = PolishPrimary) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        OutlinedTextField(
                            value = bloodGroupInput,
                            onValueChange = { bloodGroupInput = it },
                            label = { Text("Blood Group") },
                            placeholder = { Text("O+, A+, etc.") },
                            singleLine = true,
                            modifier = Modifier.weight(0.7f)
                        )
                    }

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
                        label = { Text("Primary Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PolishPrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = secPhoneInput,
                        onValueChange = { secPhoneInput = it },
                        label = { Text("Secondary / Alternate Phone Number") },
                        placeholder = { Text("+91 ...") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PolishPrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = permAddrInput,
                        onValueChange = { permAddrInput = it },
                        label = { Text("Permanent Residence Address") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = PolishPrimary) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = tempAddrInput,
                        onValueChange = { tempAddrInput = it },
                        label = { Text("Temporary Residence Address") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = PolishPrimary) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = officeAddrInput,
                        onValueChange = { officeAddrInput = it },
                        label = { Text("Office Address") },
                        leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = PolishPrimary) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            viewModel.updateFullProfile(
                                name = nameInput,
                                dob = dobInput,
                                bloodGroup = bloodGroupInput,
                                email = emailInput,
                                phone = phoneInput,
                                secPhone = secPhoneInput,
                                officeAddr = officeAddrInput,
                                permAddr = permAddrInput,
                                tempAddr = tempAddrInput,
                                pan = panInput,
                                aadhar = aadharInput,
                                dl = dlInput,
                                carNo = carNoInput,
                                bikeNo = bikeNoInput
                            )
                            Toast.makeText(context, "Personal Details Saved Successfully!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Personal Details", fontWeight = FontWeight.Bold, color = PolishOnPrimary)
                    }
                }
            }
        }

        // Section 2: Identification & Vehicle Numbers
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
                        text = "Official Identification & Vehicles",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PolishOnBackground
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = panInput,
                        onValueChange = { panInput = it.uppercase() },
                        label = { Text("PAN Number") },
                        placeholder = { Text("ABCDE1234F") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = PolishPrimary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = aadharInput,
                        onValueChange = { aadharInput = it },
                        label = { Text("Aadhaar Number") },
                        placeholder = { Text("12-digit Aadhaar") },
                        leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, tint = PolishPrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = dlInput,
                        onValueChange = { dlInput = it },
                        label = { Text("Driving License (DL) Number") },
                        leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = PolishPrimary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = carNoInput,
                            onValueChange = { carNoInput = it.uppercase() },
                            label = { Text("Car Number") },
                            placeholder = { Text("KA-01-AB-1234") },
                            leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = PolishPrimary) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        OutlinedTextField(
                            value = bikeNoInput,
                            onValueChange = { bikeNoInput = it.uppercase() },
                            label = { Text("Bike Number") },
                            placeholder = { Text("KA-05-CD-5678") },
                            leadingIcon = { Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = PolishPrimary) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            viewModel.updateFullProfile(
                                name = nameInput,
                                dob = dobInput,
                                bloodGroup = bloodGroupInput,
                                email = emailInput,
                                phone = phoneInput,
                                secPhone = secPhoneInput,
                                officeAddr = officeAddrInput,
                                permAddr = permAddrInput,
                                tempAddr = tempAddrInput,
                                pan = panInput,
                                aadhar = aadharInput,
                                dl = dlInput,
                                carNo = carNoInput,
                                bikeNo = bikeNoInput
                            )
                            Toast.makeText(context, "ID & Vehicle Numbers Saved!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Identification & Vehicles", fontWeight = FontWeight.Bold, color = PolishOnPrimary)
                    }
                }
            }
        }

        // Section 3: Bank Details (Multiple Banks)
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
                        Column {
                            Text(
                                text = "Bank Accounts (${bankAccounts.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PolishOnBackground
                            )
                            Text(
                                text = "Multiple bank account details for transfers & ledger",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = { showAddBankDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Bank", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (bankAccounts.isEmpty()) {
                        Text(
                            text = "No bank accounts added yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black
                        )
                    } else {
                        bankAccounts.forEach { bank ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = PolishSurfaceVariant,
                                border = BorderStroke(0.5.dp, PolishOutline),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = bank.bankName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = PolishOnBackground
                                        )
                                        Text(
                                            text = "A/C: ${bank.accountNumber} • IFSC: ${bank.ifscCode}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.Black
                                        )
                                        Text(
                                            text = bank.accountType,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PolishPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    IconButton(onClick = { viewModel.deleteBankAccount(bank.id) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Bank",
                                            tint = PolishDebitRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Document Vault
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
                        Column {
                            Text(
                                text = "Document Storage Vault (${storedDocuments.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PolishOnBackground
                            )
                            Text(
                                text = "Store document numbers, references & records",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = { showAddDocDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Doc", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (storedDocuments.isEmpty()) {
                        Text(
                            text = "No documents stored in vault yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black
                        )
                    } else {
                        storedDocuments.forEach { doc ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = PolishSurfaceVariant,
                                border = BorderStroke(0.5.dp, PolishOutline),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = doc.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = PolishOnBackground
                                        )
                                        Text(
                                            text = "ID: ${doc.docNumber}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                        if (doc.notes.isNotBlank()) {
                                            Text(
                                                text = doc.notes,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF49454F)
                                            )
                                        }
                                    }

                                    IconButton(onClick = { viewModel.deleteStoredDocument(doc.id) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Document",
                                            tint = PolishDebitRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 5: Locked Currency & Biometrics
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
                    // Locked currency
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Registered Currency",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Locked to ${lockedCurrency.country}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PolishPrimaryContainer
                        ) {
                            Text(
                                text = "${lockedCurrency.code} (${lockedCurrency.symbol})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PolishPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Biometric Lock
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Biometric Lock",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Require Fingerprint to open app",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Switch(
                            checked = isBioEnabled,
                            onCheckedChange = { viewModel.toggleBiometricEnabled(it) }
                        )
                    }
                }
            }
        }

        // Section 6: EXPORT EVERYTHING IN EXCEL SHEET OPTION (Prominent Bottom Button)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = PolishPrimaryContainer),
                border = BorderStroke(1.5.dp, PolishPrimary)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = "Excel Export",
                            tint = PolishPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Comprehensive Ledger Export",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PolishPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Exports all Profile Details, Banks, Documents, Transactions, Budgets, Loans & Investment portfolio page-wise into an Excel-compatible spreadsheet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.exportEverythingToExcel(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export to Excel",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Export Everything in Excel Sheet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Section 7: Sign Out
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
                            Toast.makeText(context, "Signed out of account", Toast.LENGTH_SHORT).show()
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

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Add Bank Account Dialog
    if (showAddBankDialog) {
        var bankName by remember { mutableStateOf("") }
        var accNumber by remember { mutableStateOf("") }
        var ifsc by remember { mutableStateOf("") }
        var accType by remember { mutableStateOf("Savings Account") }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                focusManager.clearFocus()
                keyboardController?.hide()
                showAddBankDialog = false
            },
            title = { Text("Add Bank Account", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("Bank Name") },
                        placeholder = { Text("e.g. State Bank of India, HDFC") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = accNumber,
                        onValueChange = { accNumber = it },
                        label = { Text("Account Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ifsc,
                        onValueChange = { ifsc = it.uppercase() },
                        label = { Text("IFSC Code") },
                        placeholder = { Text("SBIN0001234") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = accType,
                        onValueChange = { accType = it },
                        label = { Text("Account Type") },
                        placeholder = { Text("Savings, Salary, Current") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        if (bankName.isNotBlank() && accNumber.isNotBlank()) {
                            viewModel.addBankAccount(bankName, accNumber, ifsc, accType)
                            showAddBankDialog = false
                            Toast.makeText(context, "Bank Account Added!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary)
                ) {
                    Text("Add Bank")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        showAddBankDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Stored Document Dialog
    if (showAddDocDialog) {
        var docTitle by remember { mutableStateOf("") }
        var docNumber by remember { mutableStateOf("") }
        var docNotes by remember { mutableStateOf("") }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                focusManager.clearFocus()
                keyboardController?.hide()
                showAddDocDialog = false
            },
            title = { Text("Add Document to Vault", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = docTitle,
                        onValueChange = { docTitle = it },
                        label = { Text("Document Title") },
                        placeholder = { Text("e.g. Passport, RC, Insurance, Lease") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = docNumber,
                        onValueChange = { docNumber = it },
                        label = { Text("Document / ID Number") },
                        placeholder = { Text("e.g. Z1234567") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = docNotes,
                        onValueChange = { docNotes = it },
                        label = { Text("Notes / Reference (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        if (docTitle.isNotBlank() && docNumber.isNotBlank()) {
                            viewModel.addStoredDocument(docTitle, docNumber, docNotes)
                            showAddDocDialog = false
                            Toast.makeText(context, "Document Added to Vault!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary)
                ) {
                    Text("Save Document")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        showAddDocDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Change Profile Photo Bottom Sheet
    if (showChangePhotoSheet) {
        ChangeProfilePhotoBottomSheet(
            currentUri = profilePhotoUri,
            userName = currentName,
            onDismiss = { showChangePhotoSheet = false },
            onPhotoSelected = { newUri ->
                viewModel.setProfilePhoto(newUri)
            }
        )
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
                        color = Color.Black,
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
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = PolishDebitRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
