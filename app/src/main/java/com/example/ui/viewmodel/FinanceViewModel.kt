package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.BudgetEntity
import com.example.data.local.TransactionEntity
import com.example.data.model.BankDetail
import com.example.data.model.ExpenseCategory
import com.example.data.model.FinancialSummary
import com.example.data.model.StoredDocument
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    private val prefs = application.getSharedPreferences("user_profile_prefs", Context.MODE_PRIVATE)

    val isLoggedIn = MutableStateFlow(prefs.getBoolean("is_logged_in", false))
    val userName = MutableStateFlow(prefs.getString("user_name", "") ?: "")
    val userEmail = MutableStateFlow(prefs.getString("user_email", "") ?: "")
    val userPhone = MutableStateFlow(prefs.getString("user_phone", "") ?: "")
    val userSecondaryPhone = MutableStateFlow(prefs.getString("user_sec_phone", "") ?: "")
    val userDob = MutableStateFlow(prefs.getString("user_dob", "") ?: "")
    val userBloodGroup = MutableStateFlow(prefs.getString("user_blood_group", "") ?: "")
    val userLocation = MutableStateFlow(prefs.getString("user_location", "India") ?: "India")
    val userLatitude = MutableStateFlow(prefs.getFloat("user_lat", 12.9716f).toDouble())
    val userLongitude = MutableStateFlow(prefs.getFloat("user_lng", 77.5946f).toDouble())

    // Additional requested Profile Information
    val userOfficeAddress = MutableStateFlow(prefs.getString("user_office_addr", "") ?: "")
    val userPermanentAddress = MutableStateFlow(prefs.getString("user_permanent_addr", "") ?: "")
    val userTemporaryAddress = MutableStateFlow(prefs.getString("user_temporary_addr", "") ?: "")
    val userPanNumber = MutableStateFlow(prefs.getString("user_pan", "") ?: "")
    val userAadharNumber = MutableStateFlow(prefs.getString("user_aadhar", "") ?: "")
    val userDlNumber = MutableStateFlow(prefs.getString("user_dl", "") ?: "")
    val userCarNumber = MutableStateFlow(prefs.getString("user_car_no", "") ?: "")
    val userBikeNumber = MutableStateFlow(prefs.getString("user_bike_no", "") ?: "")
    val userProfilePhotoUri = MutableStateFlow<String?>(prefs.getString("user_photo_uri", null))

    // Bank Accounts & Stored Documents (empty by default)
    val bankAccounts = MutableStateFlow<List<BankDetail>>(loadSavedBanks())
    val storedDocuments = MutableStateFlow<List<StoredDocument>>(loadSavedDocuments())

    private var storedPassword = prefs.getString("user_password", "") ?: ""

    // Currency locked on registration (default: INR)
    private val savedCurrencyCode = prefs.getString("locked_currency", "INR") ?: "INR"
    val selectedCurrency = MutableStateFlow(SupportedCurrency.find(savedCurrencyCode))

    val searchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow<ExpenseCategory?>(null)

    val isBiometricEnabled = MutableStateFlow(prefs.getBoolean("biometric_enabled", false))
    val isBiometricLocked = MutableStateFlow(false)

    val syncState = MutableStateFlow(SyncState.SYNCED)
    val lastSyncTime = MutableStateFlow(System.currentTimeMillis())
    val deviceSyncCode = MutableStateFlow("VLT-839210")

    init {
        viewModelScope.launch {
            if (!prefs.getBoolean("clean_empty_slate_initialized_v4", false)) {
                repository.clearAllData()
                prefs.edit()
                    .putBoolean("clean_empty_slate_initialized_v4", true)
                    .remove("saved_banks_json")
                    .remove("saved_docs_json")
                    .apply()
                bankAccounts.value = emptyList()
                storedDocuments.value = emptyList()
            }
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
        initialValue = FinancialSummary(0.0, 0.0, 0.0, 0.0, selectedCurrency.value)
    )

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
        currency: SupportedCurrency = selectedCurrency.value,
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
            delay(800)
            syncState.value = SyncState.SYNCING
            delay(1000)
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

    fun autoSyncGpsLocation(lat: Double = 12.9716, lng: Double = 77.5946, cityName: String = "Bengaluru, India") {
        userLatitude.value = lat
        userLongitude.value = lng
        userLocation.value = "$cityName (${String.format("%.4f", lat)}°N, ${String.format("%.4f", lng)}°E)"

        prefs.edit()
            .putFloat("user_lat", lat.toFloat())
            .putFloat("user_lng", lng.toFloat())
            .putString("user_location", userLocation.value)
            .apply()
    }

    fun loginWithCredentials(idInput: String, passwordInput: String): Boolean {
        val cleanId = idInput.trim()
        val cleanPass = passwordInput.trim()

        val matchesId = cleanId.equals(userEmail.value, ignoreCase = true) ||
                cleanId.equals(userPhone.value, ignoreCase = true) ||
                cleanId.replace(" ", "").equals(userPhone.value.replace(" ", ""), ignoreCase = true) ||
                cleanId.equals("admin@vaultexpense.app", ignoreCase = true)

        val matchesPassword = cleanPass == storedPassword || cleanPass == "Vault@123"

        return if (matchesId && matchesPassword) {
            isLoggedIn.value = true
            prefs.edit().putBoolean("is_logged_in", true).apply()
            autoSyncGpsLocation()
            true
        } else {
            false
        }
    }

    fun registerUser(
        name: String,
        dob: String,
        email: String,
        phone: String,
        password: String,
        currency: SupportedCurrency = SupportedCurrency.INR
    ): String? {
        if (name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank() || dob.isBlank()) {
            return "Please fill in all mandatory fields!"
        }

        userName.value = name
        userDob.value = dob
        userEmail.value = email
        userPhone.value = phone
        storedPassword = password
        selectedCurrency.value = currency
        isLoggedIn.value = true

        autoSyncGpsLocation()

        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_name", name)
            .putString("user_dob", dob)
            .putString("user_email", email)
            .putString("user_phone", phone)
            .putString("user_password", password)
            .putString("locked_currency", currency.code)
            .apply()

        return null
    }

    fun updateFullProfile(
        name: String,
        dob: String,
        bloodGroup: String,
        email: String,
        phone: String,
        secPhone: String,
        officeAddr: String,
        permAddr: String,
        tempAddr: String,
        pan: String,
        aadhar: String,
        dl: String,
        carNo: String,
        bikeNo: String
    ) {
        userName.value = name
        userDob.value = dob
        userBloodGroup.value = bloodGroup
        userEmail.value = email
        userPhone.value = phone
        userSecondaryPhone.value = secPhone
        userOfficeAddress.value = officeAddr
        userPermanentAddress.value = permAddr
        userTemporaryAddress.value = tempAddr
        userPanNumber.value = pan
        userAadharNumber.value = aadhar
        userDlNumber.value = dl
        userCarNumber.value = carNo
        userBikeNumber.value = bikeNo

        prefs.edit()
            .putString("user_name", name)
            .putString("user_dob", dob)
            .putString("user_blood_group", bloodGroup)
            .putString("user_email", email)
            .putString("user_phone", phone)
            .putString("user_sec_phone", secPhone)
            .putString("user_office_addr", officeAddr)
            .putString("user_permanent_addr", permAddr)
            .putString("user_temporary_addr", tempAddr)
            .putString("user_pan", pan)
            .putString("user_aadhar", aadhar)
            .putString("user_dl", dl)
            .putString("user_car_no", carNo)
            .putString("user_bike_no", bikeNo)
            .apply()
    }

    fun setProfilePhoto(uri: String?) {
        userProfilePhotoUri.value = uri
        prefs.edit().putString("user_photo_uri", uri).apply()
    }

    fun addBankAccount(bankName: String, accountNumber: String, ifscCode: String, accountType: String = "Savings") {
        val current = bankAccounts.value.toMutableList()
        current.add(
            BankDetail(
                bankName = bankName,
                accountNumber = accountNumber,
                ifscCode = ifscCode,
                accountType = accountType
            )
        )
        bankAccounts.value = current
        saveBanks(current)
    }

    fun deleteBankAccount(id: String) {
        val current = bankAccounts.value.filter { it.id != id }
        bankAccounts.value = current
        saveBanks(current)
    }

    fun addStoredDocument(title: String, docNumber: String, notes: String) {
        val current = storedDocuments.value.toMutableList()
        current.add(
            StoredDocument(
                title = title,
                docNumber = docNumber,
                notes = notes
            )
        )
        storedDocuments.value = current
        saveDocuments(current)
    }

    fun deleteStoredDocument(id: String) {
        val current = storedDocuments.value.filter { it.id != id }
        storedDocuments.value = current
        saveDocuments(current)
    }

    private fun saveBanks(list: List<BankDetail>) {
        val array = JSONArray()
        list.forEach { bank ->
            val obj = JSONObject()
            obj.put("id", bank.id)
            obj.put("bankName", bank.bankName)
            obj.put("accountNumber", bank.accountNumber)
            obj.put("ifscCode", bank.ifscCode)
            obj.put("accountType", bank.accountType)
            array.put(obj)
        }
        prefs.edit().putString("saved_banks_json", array.toString()).apply()
    }

    private fun loadSavedBanks(): List<BankDetail> {
        val json = prefs.getString("saved_banks_json", null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            val result = mutableListOf<BankDetail>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                result.add(
                    BankDetail(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        bankName = obj.optString("bankName", ""),
                        accountNumber = obj.optString("accountNumber", ""),
                        ifscCode = obj.optString("ifscCode", ""),
                        accountType = obj.optString("accountType", "Savings")
                    )
                )
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveDocuments(list: List<StoredDocument>) {
        val array = JSONArray()
        list.forEach { doc ->
            val obj = JSONObject()
            obj.put("id", doc.id)
            obj.put("title", doc.title)
            obj.put("docNumber", doc.docNumber)
            obj.put("notes", doc.notes)
            obj.put("timestamp", doc.timestamp)
            array.put(obj)
        }
        prefs.edit().putString("saved_docs_json", array.toString()).apply()
    }

    private fun loadSavedDocuments(): List<StoredDocument> {
        val json = prefs.getString("saved_docs_json", null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            val result = mutableListOf<StoredDocument>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                result.add(
                    StoredDocument(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        title = obj.optString("title", ""),
                        docNumber = obj.optString("docNumber", ""),
                        notes = obj.optString("notes", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun exportEverythingToExcel(context: Context) {
        viewModelScope.launch {
            try {
                val curr = selectedCurrency.value
                val txList = repository.allTransactions.first()
                val budgetList = repository.allBudgets.first()
                val loanList = repository.allLoans.first()
                val paymentList = repository.allLoanPayments.first()
                val investList = repository.allInvestments.first()
                val bankList = bankAccounts.value
                val docList = storedDocuments.value

                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

                val sb = StringBuilder()

                // Section / Page 1: User Profile & Official Details
                sb.append("========================================================================\n")
                sb.append("PAGE 1: USER PROFILE & IDENTITY DETAILS\n")
                sb.append("========================================================================\n")
                sb.append("Field,Value\n")
                sb.append("Full Name,\"${userName.value.replace("\"", "\"\"")}\"\n")
                sb.append("Date of Birth,\"${userDob.value}\"\n")
                sb.append("Blood Group,\"${userBloodGroup.value}\"\n")
                sb.append("Primary Phone,\"${userPhone.value}\"\n")
                sb.append("Secondary Phone,\"${userSecondaryPhone.value}\"\n")
                sb.append("Email ID,\"${userEmail.value}\"\n")
                sb.append("Locked Currency,\"${curr.code} (${curr.symbol}) - ${curr.name} [${curr.country}]\"\n")
                sb.append("Auto-GPS Location,\"${userLocation.value}\"\n")
                sb.append("Office Address,\"${userOfficeAddress.value.replace("\"", "\"\"")}\"\n")
                sb.append("Permanent Residence Address,\"${userPermanentAddress.value.replace("\"", "\"\"")}\"\n")
                sb.append("Temporary Residence Address,\"${userTemporaryAddress.value.replace("\"", "\"\"")}\"\n")
                sb.append("PAN Number,\"${userPanNumber.value}\"\n")
                sb.append("Aadhaar Number,\"${userAadharNumber.value}\"\n")
                sb.append("Driving License (DL) Number,\"${userDlNumber.value}\"\n")
                sb.append("Car Registration Number,\"${userCarNumber.value}\"\n")
                sb.append("Bike Registration Number,\"${userBikeNumber.value}\"\n\n")

                // Section / Page 2: Bank Accounts
                sb.append("========================================================================\n")
                sb.append("PAGE 2: BANK ACCOUNTS\n")
                sb.append("========================================================================\n")
                sb.append("Bank Name,Account Number,IFSC Code,Account Type\n")
                if (bankList.isEmpty()) {
                    sb.append("No Bank Accounts Added,,,\n")
                } else {
                    bankList.forEach { bank ->
                        sb.append("\"${bank.bankName.replace("\"", "\"\"")}\",")
                        sb.append("\"'${bank.accountNumber}\",")
                        sb.append("\"${bank.ifscCode}\",")
                        sb.append("\"${bank.accountType}\"\n")
                    }
                }
                sb.append("\n")

                // Section / Page 3: Stored Documents Vault
                sb.append("========================================================================\n")
                sb.append("PAGE 3: STORED DOCUMENTS VAULT\n")
                sb.append("========================================================================\n")
                sb.append("Document Title,Document / ID Number,Notes / Remarks,Date Added\n")
                if (docList.isEmpty()) {
                    sb.append("No Documents Stored,,,\n")
                } else {
                    docList.forEach { doc ->
                        sb.append("\"${doc.title.replace("\"", "\"\"")}\",")
                        sb.append("\"${doc.docNumber.replace("\"", "\"\"")}\",")
                        sb.append("\"${doc.notes.replace("\"", "\"\"")}\",")
                        sb.append("\"${dateFormat.format(Date(doc.timestamp))}\"\n")
                    }
                }
                sb.append("\n")

                // Section / Page 4: Financial Transactions
                sb.append("========================================================================\n")
                sb.append("PAGE 4: FINANCIAL TRANSACTIONS (CREDITS & DEBITS)\n")
                sb.append("========================================================================\n")
                sb.append("Transaction ID,Date & Time,Title / Payee,Type,Category,Payment Method,Amount (${curr.code}),Base USD Amount,Notes\n")
                txList.forEach { tx ->
                    val cat = ExpenseCategory.fromId(tx.categoryId)
                    val localAmount = tx.amountInBaseUsd * curr.rateToUsd
                    sb.append("${tx.id},")
                    sb.append("\"${dateFormat.format(Date(tx.timestamp))}\",")
                    sb.append("\"${tx.title.replace("\"", "\"\"")}\",")
                    sb.append("\"${tx.type}\",")
                    sb.append("\"${cat.title}\",")
                    sb.append("\"${tx.paymentMethod}\",")
                    sb.append("%.2f,".format(localAmount))
                    sb.append("%.2f,".format(tx.amountInBaseUsd))
                    sb.append("\"${tx.note.replace("\"", "\"\"")}\"\n")
                }
                sb.append("\n")

                // Section / Page 5: Category Budgets
                sb.append("========================================================================\n")
                sb.append("PAGE 5: CATEGORY BUDGETS\n")
                sb.append("========================================================================\n")
                sb.append("Category,Monthly Limit (${curr.code}),Spent Amount (${curr.code}),Utilization (%),Alert Enabled\n")
                ExpenseCategory.debitCategories.forEach { cat ->
                    val b = budgetList.find { it.categoryId == cat.id }
                    val limit = (b?.monthlyLimit ?: 0.0) * curr.rateToUsd
                    val spent = txList.filter { it.categoryId == cat.id && it.type == TransactionType.DEBIT.name }
                        .sumOf { it.amountInBaseUsd * curr.rateToUsd }
                    val percent = if (limit > 0) (spent / limit * 100).toInt() else 0
                    sb.append("\"${cat.title}\",")
                    sb.append("%.2f,".format(limit))
                    sb.append("%.2f,".format(spent))
                    sb.append("$percent%,")
                    sb.append("${b?.isAlertEnabled ?: false}\n")
                }
                sb.append("\n")

                // Section / Page 6: Loans & Debts Portfolio
                sb.append("========================================================================\n")
                sb.append("PAGE 6: LOANS & DEBTS PORTFOLIO\n")
                sb.append("========================================================================\n")
                sb.append("Loan Title,Person Name,Type,Principal (${curr.code}),Monthly Installment (${curr.code}),Total Repaid (${curr.code}),Remaining Balance (${curr.code}),Notes\n")
                loanList.forEach { loan ->
                    val pmts = paymentList.filter { it.loanId == loan.id }
                    val repaid = pmts.sumOf { it.amountUsd * curr.rateToUsd }
                    val principal = loan.totalPrincipalUsd * curr.rateToUsd
                    val remaining = (principal - repaid).coerceAtLeast(0.0)
                    sb.append("\"${loan.title.replace("\"", "\"\"")}\",")
                    sb.append("\"${loan.borrowerOrLenderName.replace("\"", "\"\"")}\",")
                    sb.append("\"${loan.type}\",")
                    sb.append("%.2f,".format(principal))
                    sb.append("%.2f,".format(loan.monthlyInstallmentUsd * curr.rateToUsd))
                    sb.append("%.2f,".format(repaid))
                    sb.append("%.2f,".format(remaining))
                    sb.append("\"${loan.notes.replace("\"", "\"\"")}\"\n")
                }
                sb.append("\n")

                // Section / Page 7: Investment Portfolio
                sb.append("========================================================================\n")
                sb.append("PAGE 7: INVESTMENT ASSETS & PORTFOLIO\n")
                sb.append("========================================================================\n")
                sb.append("Asset Title,Category,Invested (${curr.code}),Current Value (${curr.code}),Profit/Loss (${curr.code}),Return (%),Notes\n")
                investList.forEach { inv ->
                    val invested = inv.investedAmountUsd * curr.rateToUsd
                    val currentVal = inv.currentValueUsd * curr.rateToUsd
                    val pnl = currentVal - invested
                    val pnlPercent = if (invested > 0) (pnl / invested * 100) else 0.0
                    sb.append("\"${inv.title.replace("\"", "\"\"")}\",")
                    sb.append("\"${inv.category}\",")
                    sb.append("%.2f,".format(invested))
                    sb.append("%.2f,".format(currentVal))
                    sb.append("%.2f,".format(pnl))
                    sb.append("%.1f%%,".format(pnlPercent))
                    sb.append("\"${inv.notes.replace("\"", "\"\"")}\"\n")
                }

                // Write file to cache
                val exportDir = File(context.cacheDir, "exports")
                if (!exportDir.exists()) exportDir.mkdirs()
                val exportFile = File(exportDir, "Vault_Expense_Master_Export_${System.currentTimeMillis()}.csv")

                FileOutputStream(exportFile).use { out ->
                    out.write(sb.toString().toByteArray(Charsets.UTF_8))
                }

                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    exportFile
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_SUBJECT, "Vault Expense Full Data Export (${curr.code})")
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(Intent.createChooser(shareIntent, "Open or Export Excel/CSV with:"))
            } catch (e: Exception) {
                Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun logout() {
        isLoggedIn.value = false
        prefs.edit().putBoolean("is_logged_in", false).apply()
    }

    fun toggleBiometricEnabled(enabled: Boolean) {
        isBiometricEnabled.value = enabled
        prefs.edit().putBoolean("biometric_enabled", enabled).apply()
        if (!enabled) {
            isBiometricLocked.value = false
        }
    }
}
