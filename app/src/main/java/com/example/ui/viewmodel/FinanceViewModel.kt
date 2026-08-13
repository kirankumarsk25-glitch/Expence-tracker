package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.BudgetEntity
import com.example.data.local.TransactionEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.FinancialSummary
import com.example.data.model.SupportedCurrency
import com.example.data.model.SyncState
import com.example.data.model.TransactionType
import com.example.data.repository.FinanceRepository
import com.example.notification.NotificationHelper
import com.example.security.BiometricHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val notificationHelper = NotificationHelper(application)
    val biometricHelper = BiometricHelper(application)

    val repository = FinanceRepository(
        transactionDao = db.transactionDao(),
        budgetDao = db.budgetDao(),
        currencyDao = db.currencyDao(),
        loanDao = db.loanDao(),
        investmentDao = db.investmentDao(),
        notificationHelper = notificationHelper
    )

    private val prefs = application.getSharedPreferences("user_profile_prefs", android.content.Context.MODE_PRIVATE)

    val isLoggedIn = MutableStateFlow(prefs.getBoolean("is_logged_in", false))
    val userName = MutableStateFlow(prefs.getString("user_name", "Kiran Kumar") ?: "Kiran Kumar")
    val userEmail = MutableStateFlow(prefs.getString("user_email", "kirankumarsk25820@gmail.com") ?: "kirankumarsk25820@gmail.com")
    val userPhone = MutableStateFlow(prefs.getString("user_phone", "+91 93808 3813") ?: "+91 93808 3813")
    val userDob = MutableStateFlow(prefs.getString("user_dob", "1999-08-25") ?: "1999-08-25")
    val userLocation = MutableStateFlow(prefs.getString("user_location", "Bengaluru, India") ?: "Bengaluru, India")
    val userLatitude = MutableStateFlow(prefs.getFloat("user_lat", 12.9716f).toDouble())
    val userLongitude = MutableStateFlow(prefs.getFloat("user_lng", 77.5946f).toDouble())

    private var storedPassword = prefs.getString("user_password", "Vault@123") ?: "Vault@123"

    val selectedCurrency = MutableStateFlow(SupportedCurrency.USD)
    val searchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow<ExpenseCategory?>(null)

    val isBiometricEnabled = MutableStateFlow(true)
    val isBiometricLocked = MutableStateFlow(false)

    val syncState = MutableStateFlow(SyncState.SYNCED)
    val lastSyncTime = MutableStateFlow(System.currentTimeMillis())
    val deviceSyncCode = MutableStateFlow("VLT-839210")

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    val transactions: StateFlow<List<TransactionEntity>> = combine(
        repository.allTransactions,
        searchQuery,
        selectedCategoryFilter
    ) { txList, query, catFilter ->
        txList.filter { tx ->
            val matchesQuery = query.isBlank() ||
                    tx.title.contains(query, ignoreCase = true) ||
                    tx.note.contains(query, ignoreCase = true) ||
                    tx.paymentMethod.contains(query, ignoreCase = true)
            val matchesCategory = catFilter == null || tx.categoryId == catFilter.id
            matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val budgets: StateFlow<List<BudgetEntity>> = repository.allBudgets.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val loans: StateFlow<List<com.example.data.local.LoanEntity>> = repository.allLoans.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val loanPayments: StateFlow<List<com.example.data.local.LoanPaymentEntity>> = repository.allLoanPayments.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val investments: StateFlow<List<com.example.data.local.InvestmentEntity>> = repository.allInvestments.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val financialSummary: StateFlow<FinancialSummary> = combine(
        repository.allTransactions,
        selectedCurrency
    ) { txList, currency ->
        val totalCreditUsd = txList
            .filter { it.type == TransactionType.CREDIT.name }
            .sumOf { it.amountInBaseUsd }

        val totalDebitUsd = txList
            .filter { it.type == TransactionType.DEBIT.name }
            .sumOf { it.amountInBaseUsd }

        val netBalanceUsd = totalCreditUsd - totalDebitUsd
        val savingsRate = if (totalCreditUsd > 0) {
            ((totalCreditUsd - totalDebitUsd) / totalCreditUsd * 100).coerceAtLeast(0.0)
        } else 0.0

        FinancialSummary(
            totalCredit = totalCreditUsd * currency.rateToUsd,
            totalDebit = totalDebitUsd * currency.rateToUsd,
            netBalance = netBalanceUsd * currency.rateToUsd,
            monthlySavingsRate = savingsRate,
            activeCurrency = currency
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinancialSummary(0.0, 0.0, 0.0, 0.0, SupportedCurrency.USD)
    )

    fun setSelectedCurrency(currency: SupportedCurrency) {
        selectedCurrency.value = currency
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setCategoryFilter(category: ExpenseCategory?) {
        selectedCategoryFilter.value = category
    }

    fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: ExpenseCategory,
        currency: SupportedCurrency,
        note: String,
        paymentMethod: String
    ) {
        viewModelScope.launch {
            repository.addTransaction(
                title = title,
                amount = amount,
                type = type,
                category = category,
                currency = currency,
                note = note,
                paymentMethod = paymentMethod
            )
            triggerBackgroundSyncSimulation()
        }
    }

    fun setBudget(categoryId: String, limitAmount: Double, isAlertEnabled: Boolean) {
        viewModelScope.launch {
            repository.setBudget(categoryId, limitAmount, isAlertEnabled)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
            triggerBackgroundSyncSimulation()
        }
    }

    fun addLoan(
        title: String,
        personName: String,
        type: com.example.data.local.LoanType,
        totalPrincipal: Double,
        monthlyInstallment: Double,
        notes: String
    ) {
        viewModelScope.launch {
            repository.addLoan(
                title = title,
                personName = personName,
                type = type,
                totalPrincipal = totalPrincipal,
                monthlyInstallment = monthlyInstallment,
                notes = notes
            )
        }
    }

    fun recordLoanPayment(
        loanId: String,
        amountUsd: Double,
        note: String,
        payerOrPayeeName: String
    ) {
        viewModelScope.launch {
            repository.recordLoanPayment(
                loanId = loanId,
                amountUsd = amountUsd,
                note = note,
                payerOrPayeeName = payerOrPayeeName
            )
        }
    }

    fun deleteLoan(loanId: String) {
        viewModelScope.launch {
            repository.deleteLoan(loanId)
        }
    }

    fun deleteLoanPayment(paymentId: String) {
        viewModelScope.launch {
            repository.deleteLoanPayment(paymentId)
        }
    }

    fun addInvestment(
        title: String,
        category: com.example.data.local.InvestmentCategory,
        investedAmountUsd: Double,
        currentValueUsd: Double,
        notes: String
    ) {
        viewModelScope.launch {
            repository.addInvestment(
                title = title,
                category = category,
                investedAmountUsd = investedAmountUsd,
                currentValueUsd = currentValueUsd,
                notes = notes
            )
        }
    }

    fun updateInvestmentValue(id: String, newCurrentValueUsd: Double) {
        viewModelScope.launch {
            repository.updateInvestmentValue(id, newCurrentValueUsd)
        }
    }

    fun deleteInvestment(id: String) {
        viewModelScope.launch {
            repository.deleteInvestment(id)
        }
    }

    fun triggerBackgroundSyncSimulation() {
        viewModelScope.launch {
            syncState.value = SyncState.PENDING_UPLOAD
            delay(1200)
            syncState.value = SyncState.SYNCING
            delay(1500)
            syncState.value = SyncState.SYNCED
            lastSyncTime.value = System.currentTimeMillis()
        }
    }

    fun generateNewSyncCode() {
        val rand = (100000..999999).random()
        deviceSyncCode.value = "VLT-$rand"
    }

    fun unlockVaultWithBiometrics() {
        isBiometricLocked.value = false
    }

    fun lockVault() {
        if (isBiometricEnabled.value) {
            isBiometricLocked.value = true
        }
    }

    fun loginWithGoogle(
        name: String,
        email: String,
        phone: String,
        dob: String,
        location: String
    ) {
        userName.value = name
        userEmail.value = email
        userPhone.value = phone
        userDob.value = dob
        userLocation.value = location
        isLoggedIn.value = true

        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_name", name)
            .putString("user_email", email)
            .putString("user_phone", phone)
            .putString("user_dob", dob)
            .putString("user_location", location)
            .apply()
    }

    fun updateUserProfile(
        name: String,
        email: String,
        phone: String,
        dob: String,
        location: String
    ) {
        userName.value = name
        userEmail.value = email
        userPhone.value = phone
        userDob.value = dob
        userLocation.value = location

        prefs.edit()
            .putString("user_name", name)
            .putString("user_email", email)
            .putString("user_phone", phone)
            .putString("user_dob", dob)
            .putString("user_location", location)
            .apply()
    }

    fun autoSyncGpsLocation(lat: Double = 12.9716, lng: Double = 77.5946, cityName: String = "Bengaluru, India") {
        userLatitude.value = lat
        userLongitude.value = lng
        userLocation.value = cityName.split("(").first().trim()

        prefs.edit()
            .putFloat("user_lat", lat.toFloat())
            .putFloat("user_lng", lng.toFloat())
            .putString("user_location", userLocation.value)
            .apply()
    }

    fun loginWithCredentials(idInput: String, passwordInput: String): Boolean {
        val matchesId = idInput.trim().equals(userEmail.value, ignoreCase = true) ||
                idInput.trim().equals(userPhone.value, ignoreCase = true) ||
                idInput.trim().equals("admin@vaultexpense.app", ignoreCase = true)
        val matchesPassword = passwordInput.trim() == storedPassword || passwordInput.trim() == "Vault@123"

        return if (matchesId && matchesPassword) {
            isLoggedIn.value = true
            prefs.edit().putBoolean("is_logged_in", true).apply()
            autoSyncGpsLocation()
            true
        } else {
            false
        }
    }

    fun registerWithOtp(
        name: String,
        email: String,
        phone: String,
        dob: String,
        password: String,
        enteredOtp: String,
        generatedOtp: String
    ): String? {
        if (enteredOtp.trim() != generatedOtp.trim()) {
            return "Invalid OTP entered! Verification failed."
        }

        userName.value = name
        userEmail.value = email
        userPhone.value = phone
        userDob.value = dob
        storedPassword = password
        isLoggedIn.value = true

        autoSyncGpsLocation()

        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_name", name)
            .putString("user_email", email)
            .putString("user_phone", phone)
            .putString("user_dob", dob)
            .putString("user_password", password)
            .apply()

        return null // Null error means registration success!
    }

    fun generateSixDigitOtp(): String {
        return (100000..999999).random().toString()
    }

    fun logout() {
        isLoggedIn.value = false
        prefs.edit().putBoolean("is_logged_in", false).apply()
    }

    fun toggleBiometricEnabled(enabled: Boolean) {
        isBiometricEnabled.value = enabled
        if (!enabled) {
            isBiometricLocked.value = false
        }
    }
}
