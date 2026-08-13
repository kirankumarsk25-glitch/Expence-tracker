package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.example.ui.screens.MainAppContainer
import com.example.ui.theme.VaultExpenseTheme
import com.example.ui.viewmodel.FinanceViewModel

class MainActivity : FragmentActivity() {
    private val financeViewModel: FinanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VaultExpenseTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainAppContainer(viewModel = financeViewModel)
                }
            }
        }
    }
}

