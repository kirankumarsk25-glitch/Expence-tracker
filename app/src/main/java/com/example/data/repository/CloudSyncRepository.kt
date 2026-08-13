package com.example.data.repository

import android.util.Log
import com.example.data.local.TransactionEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CloudSyncRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val TAG = "CloudSyncRepository"
        private const val COLLECTION_TRANSACTIONS = "transactions"
        private const val COLLECTION_VAULTS = "vaults"
    }

    /**
     * Uploads a transaction to Firebase Firestore under the user's vault collection.
     */
    suspend fun syncTransactionToCloud(userId: String, transaction: TransactionEntity): Boolean {
        return try {
            val docData = hashMapOf(
                "id" to transaction.id,
                "title" to transaction.title,
                "amount" to transaction.amount,
                "type" to transaction.type,
                "categoryId" to transaction.categoryId,
                "currencyCode" to transaction.currencyCode,
                "amountInBaseUsd" to transaction.amountInBaseUsd,
                "timestamp" to transaction.timestamp,
                "note" to transaction.note,
                "paymentMethod" to transaction.paymentMethod,
                "syncState" to "SYNCED"
            )

            firestore.collection(COLLECTION_VAULTS)
                .document(userId)
                .collection(COLLECTION_TRANSACTIONS)
                .document(transaction.id.toString())
                .set(docData)
                .await()

            Log.d(TAG, "Transaction successfully synced to cloud for user: $userId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing transaction to Firestore", e)
            false
        }
    }

    /**
     * Fetches synced transactions from Firebase Firestore for the given user.
     */
    suspend fun fetchTransactionsFromCloud(userId: String): List<Map<String, Any>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_VAULTS)
                .document(userId)
                .collection(COLLECTION_TRANSACTIONS)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc -> doc.data }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching transactions from Firestore", e)
            emptyList()
        }
    }
}
