package com.example.data.repository

import com.example.data.local.BudgetDao
import com.example.data.local.BudgetEntity
import com.example.data.local.CurrencyDao
import com.example.data.local.CurrencyRateEntity
import com.example.data.local.InvestmentDao
import com.example.data.local.InvestmentEntity
import com.example.data.local.InvestmentCategory
import com.example.data.local.LoanDao
import com.example.data.local.LoanEntity
import com.example.data.local.LoanPaymentEntity
import com.example.data.local.LoanType
import com.example.data.local.TransactionDao
import com.example.data.local.TransactionEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.FinancialSummary
import com.example.data.model.SupportedCurrency
import com.example.data.model.TransactionType
import com.example.notification.NotificationHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val currencyDao: CurrencyDao,
    private val loanDao: LoanDao,
    private val investmentDao: InvestmentDao,
    private val notificationHelper: NotificationHelper
) {

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()
    val allCurrencyRates: Flow<List<CurrencyRateEntity>> = currencyDao.getAllRates()
    val allLoans: Flow<List<LoanEntity>> = loanDao.getAllLoans()
    val allLoanPayments: Flow<List<LoanPaymentEntity>> = loanDao.getAllPayments()
    val allInvestments: Flow<List<InvestmentEntity>> = investmentDao.getAllInvestments()

    suspend fun seedInitialDataIfEmpty() {
        val currentRates = currencyDao.getAllRates().first()
        if (currentRates.isEmpty()) {
            val initialRates = SupportedCurrency.ALL.map {
                CurrencyRateEntity(
                    code = it.code,
                    symbol = it.symbol,
                    name = it.name,
                    rateToUsd = it.rateToUsd
                )
            }
            currencyDao.insertAllRates(initialRates)
        }
    }

    suspend fun clearAllData() {
        transactionDao.deleteAllTransactions()
        budgetDao.deleteAllBudgets()
        loanDao.deleteAllLoans()
        loanDao.deleteAllLoanPayments()
        investmentDao.deleteAllInvestments()
    }


    suspend fun addInvestment(
        title: String,
        category: InvestmentCategory,
        investedAmountUsd: Double,
        currentValueUsd: Double,
        notes: String
    ) {
        val investment = InvestmentEntity(
            title = title,
            category = category.name,
            investedAmountUsd = investedAmountUsd,
            currentValueUsd = currentValueUsd,
            notes = notes
        )
        investmentDao.insertInvestment(investment)
    }

    suspend fun updateInvestmentValue(
        id: String,
        newCurrentValueUsd: Double
    ) {
        val currentList = investmentDao.getAllInvestments().first()
        val existing = currentList.find { it.id == id }
        if (existing != null) {
            investmentDao.insertInvestment(existing.copy(currentValueUsd = newCurrentValueUsd))
        }
    }

    suspend fun deleteInvestment(id: String) {
        investmentDao.deleteInvestment(id)
    }

    suspend fun addLoan(
        title: String,
        personName: String,
        type: LoanType,
        totalPrincipal: Double,
        monthlyInstallment: Double,
        notes: String
    ) {
        val loan = LoanEntity(
            title = title,
            borrowerOrLenderName = personName,
            type = type.name,
            totalPrincipalUsd = totalPrincipal,
            monthlyInstallmentUsd = monthlyInstallment,
            notes = notes
        )
        loanDao.insertLoan(loan)
    }

    suspend fun recordLoanPayment(
        loanId: String,
        amountUsd: Double,
        note: String,
        payerOrPayeeName: String
    ) {
        val payment = LoanPaymentEntity(
            loanId = loanId,
            amountUsd = amountUsd,
            paymentDate = System.currentTimeMillis(),
            note = note,
            payerOrPayeeName = payerOrPayeeName
        )
        loanDao.insertPayment(payment)
    }

    suspend fun deleteLoan(loanId: String) {
        loanDao.deletePaymentsForLoan(loanId)
        loanDao.deleteLoan(loanId)
    }

    suspend fun deleteLoanPayment(paymentId: String) {
        loanDao.deletePayment(paymentId)
    }

    suspend fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: ExpenseCategory,
        currency: SupportedCurrency,
        note: String,
        paymentMethod: String
    ): Long {
        val amountInBaseUsd = if (currency.rateToUsd > 0) amount / currency.rateToUsd else amount
        val entity = TransactionEntity(
            title = title,
            amount = amount,
            type = type.name,
            categoryId = category.id,
            currencyCode = currency.code,
            amountInBaseUsd = amountInBaseUsd,
            timestamp = System.currentTimeMillis(),
            note = note,
            paymentMethod = paymentMethod,
            syncState = "SYNCED"
        )
        val insertedId = transactionDao.insertTransaction(entity)

        // Check budget limits for notifications
        if (type == TransactionType.DEBIT) {
            checkAndTriggerBudgetAlert(category.id, currency)
        }

        return insertedId
    }

    suspend fun checkAndTriggerBudgetAlert(categoryId: String, currency: SupportedCurrency) {
        val budget = budgetDao.getBudgetByCategory(categoryId) ?: return
        if (!budget.isAlertEnabled) return

        val allTx = transactionDao.getAllTransactions().first()
        val categoryDebitsUsd = allTx
            .filter { it.categoryId == categoryId && it.type == TransactionType.DEBIT.name }
            .sumOf { it.amountInBaseUsd }

        val limitUsd = budget.monthlyLimit
        val percent = ((categoryDebitsUsd / limitUsd) * 100).toInt()

        if (percent >= budget.alertThresholdPercent) {
            val category = ExpenseCategory.fromId(categoryId)
            notificationHelper.sendBudgetAlert(
                categoryName = category.title,
                spentAmount = categoryDebitsUsd * currency.rateToUsd,
                limitAmount = limitUsd * currency.rateToUsd,
                currencySymbol = currency.symbol,
                percent = percent
            )
        }
    }

    suspend fun setBudget(categoryId: String, limit: Double, isAlertEnabled: Boolean = true) {
        val budget = BudgetEntity(
            categoryId = categoryId,
            monthlyLimit = limit,
            isAlertEnabled = isAlertEnabled
        )
        budgetDao.insertBudget(budget)
    }

    suspend fun deleteTransaction(id: Long) {
        transactionDao.deleteTransactionById(id)
    }

    suspend fun updateCurrencyRate(code: String, rateToUsd: Double) {
        val rates = currencyDao.getAllRates().first()
        val existing = rates.find { it.code == code }
        if (existing != null) {
            currencyDao.insertRate(existing.copy(rateToUsd = rateToUsd, lastUpdated = System.currentTimeMillis()))
        }
    }

    // Export JSON string for cloud backup
    suspend fun exportDataToJson(): String {
        val txs = transactionDao.getAllTransactions().first()
        val budgets = budgetDao.getAllBudgets().first()

        val root = JSONObject()
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())

        val txArray = JSONArray()
        txs.forEach { tx ->
            val obj = JSONObject()
            obj.put("title", tx.title)
            obj.put("amount", tx.amount)
            obj.put("type", tx.type)
            obj.put("categoryId", tx.categoryId)
            obj.put("currencyCode", tx.currencyCode)
            obj.put("amountInBaseUsd", tx.amountInBaseUsd)
            obj.put("timestamp", tx.timestamp)
            obj.put("note", tx.note)
            obj.put("paymentMethod", tx.paymentMethod)
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        val budgetArray = JSONArray()
        budgets.forEach { b ->
            val obj = JSONObject()
            obj.put("categoryId", b.categoryId)
            obj.put("monthlyLimit", b.monthlyLimit)
            obj.put("alertThresholdPercent", b.alertThresholdPercent)
            obj.put("isAlertEnabled", b.isAlertEnabled)
            budgetArray.put(obj)
        }
        root.put("budgets", budgetArray)

        return root.toString(2)
    }

    // Import JSON string from cloud backup
    suspend fun importDataFromJson(jsonString: String): Int {
        val root = JSONObject(jsonString)
        val txArray = root.optJSONArray("transactions") ?: JSONArray()
        val budgetArray = root.optJSONArray("budgets") ?: JSONArray()

        val importedTxs = mutableListOf<TransactionEntity>()
        for (i in 0 until txArray.length()) {
            val obj = txArray.getJSONObject(i)
            importedTxs.add(
                TransactionEntity(
                    title = obj.getString("title"),
                    amount = obj.getDouble("amount"),
                    type = obj.getString("type"),
                    categoryId = obj.getString("categoryId"),
                    currencyCode = obj.optString("currencyCode", "USD"),
                    amountInBaseUsd = obj.optDouble("amountInBaseUsd", obj.getDouble("amount")),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                    note = obj.optString("note", ""),
                    paymentMethod = obj.optString("paymentMethod", "Cloud Sync")
                )
            )
        }

        if (importedTxs.isNotEmpty()) {
            transactionDao.deleteAllTransactions()
            transactionDao.insertAll(importedTxs)
        }

        val importedBudgets = mutableListOf<BudgetEntity>()
        for (i in 0 until budgetArray.length()) {
            val obj = budgetArray.getJSONObject(i)
            importedBudgets.add(
                BudgetEntity(
                    categoryId = obj.getString("categoryId"),
                    monthlyLimit = obj.getDouble("monthlyLimit"),
                    alertThresholdPercent = obj.optInt("alertThresholdPercent", 80),
                    isAlertEnabled = obj.optBoolean("isAlertEnabled", true)
                )
            )
        }

        if (importedBudgets.isNotEmpty()) {
            budgetDao.insertAll(importedBudgets)
        }

        notificationHelper.sendSyncNotification(deviceCount = 2, itemsSynced = importedTxs.size)
        return importedTxs.size
    }
}
