package com.onhand.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.onhand.app.InventoryViewModel
import com.onhand.app.data.AiSettings
import com.onhand.app.ui.theme.Cream
import com.onhand.app.ui.theme.Muted

@Composable
fun SettingsScreen(
    viewModel: InventoryViewModel,
    onBack: () -> Unit,
) {
    val saved by viewModel.aiSettings.collectAsState()
    var apiKey by rememberSaveable { mutableStateOf(saved.apiKey) }
    var baseUrl by rememberSaveable { mutableStateOf(saved.baseUrl) }
    var model by rememberSaveable { mutableStateOf(saved.model) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Live AI recipe search", fontWeight = FontWeight.Bold)
        Text(
            "Leave the API key empty to stay on the built-in on-hand matcher. Any OpenAI-compatible chat API works: xAI, OpenAI, or a local proxy.",
            color = Muted,
        )
        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text("API key") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = baseUrl,
            onValueChange = { baseUrl = it },
            label = { Text("Base URL") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = model,
            onValueChange = { model = it },
            label = { Text("Model") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Button(
            onClick = {
                viewModel.saveAiSettings(AiSettings(apiKey = apiKey, baseUrl = baseUrl, model = model))
                onBack()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save AI setup")
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}
