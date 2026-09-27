package com.onhand.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import com.onhand.app.data.RecipeMatch
import com.onhand.app.data.RecipeSource
import com.onhand.app.ui.theme.Cream
import com.onhand.app.ui.theme.Muted
import com.onhand.app.ui.theme.Sage
import com.onhand.app.ui.theme.Terracotta

@Composable
fun RecipesScreen(
    viewModel: InventoryViewModel,
    onOpenRecipe: (String) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val items by viewModel.items.collectAsState()
    val aiMatches by viewModel.aiMatches.collectAsState()
    val aiBusy by viewModel.aiBusy.collectAsState()
    val aiError by viewModel.aiError.collectAsState()
    val settings by viewModel.aiSettings.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    val localMatches = viewModel.localMatches(query)
    val matches = if (aiMatches.isNotEmpty()) aiMatches else localMatches

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(top = 16.dp),
    ) {
        Text(
            "Search recipes from refrigerator, freezer, pantry, and spice items already on hand.",
            color = Muted,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Pasta, eggs, something fast...") },
                singleLine = true,
            )
        }
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = { viewModel.searchWithAi(query) }, enabled = !aiBusy) {
                Text(if (settings.apiKey.isBlank()) "Find on hand" else "Ask AI")
            }
            OutlinedButton(onClick = onOpenSettings) { Text("AI setup") }
        }
        if (aiBusy) {
            CircularProgressIndicator(modifier = Modifier.padding(20.dp), color = Sage)
        }
        if (aiError != null) {
            Text(aiError.orEmpty(), color = Terracotta, modifier = Modifier.padding(20.dp))
        }
        Text(
            if (aiMatches.isNotEmpty()) {
                "AI suggestions · ${items.size} items on hand"
            } else {
                "Local matcher · ${items.size} items on hand"
            },
            color = Terracotta,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(20.dp),
        )
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(matches, key = { it.recipe.id }) { match ->
                RecipeResultCard(match = match, onOpen = { onOpenRecipe(match.recipe.id) })
            }
        }
    }
}

@Composable
private fun RecipeResultCard(match: RecipeMatch, onOpen: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(match.recipe.title, fontWeight = FontWeight.Bold)
            Text(match.recipe.description, color = Muted)
            Text(
                buildString {
                    append(if (match.source == RecipeSource.Ai) "AI · " else "On hand · ")
                    append(if (match.missing.isEmpty()) "ready" else "${match.missing.size} missing")
                    append(" · ${match.recipe.minutes} min")
                },
                color = Sage,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
