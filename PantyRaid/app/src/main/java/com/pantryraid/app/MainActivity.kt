package com.onhand.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onhand.app.ui.OnHandApp
import com.onhand.app.ui.theme.OnHandTheme

class MainActivity : ComponentActivity() {
    private val viewModel by lazy {
        ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val app = application as OnHandApplication
                    return InventoryViewModel(app.repository) as T
                }
            },
        )[InventoryViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OnHandTheme {
                OnHandApp(viewModel)
            }
        }
    }
}
