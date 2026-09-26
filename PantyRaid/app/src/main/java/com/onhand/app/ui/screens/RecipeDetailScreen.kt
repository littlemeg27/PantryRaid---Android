package com.onhand.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onhand.app.InventoryViewModel
import com.onhand.app.ui.theme.Cream
import com.onhand.app.ui.theme.Muted
import com.onhand.app.ui.theme.Sage
import com.onhand.app.ui.theme.Terracotta

@Composable
fun RecipeDetailScreen(
    recipeId: String,
    viewModel: InventoryViewModel,
    onBack: () -> Unit,
) {
    val match = viewModel.matchFor(recipeId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextButton(onClick = onBack) { Text("Back") }
        if (match == null) {
            Text("Recipe not found")
            return
        }
        Text(match.recipe.title, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Text("${match.recipe.minutes} min · ${match.recipe.servings} servings", color = Sage, fontWeight = FontWeight.Bold)
        Text(match.recipe.description, color = Muted, fontSize = 16.sp)
        Text("On hand", fontWeight = FontWeight.ExtraBold)
        if (match.have.isEmpty()) {
            Text("None of the required items are logged yet.", color = Muted)
        } else {
            match.have.forEach { Text("✓ $it", color = Sage) }
        }
        if (match.missing.isNotEmpty()) {
            Text("Still needed", fontWeight = FontWeight.ExtraBold)
            match.missing.forEach { Text("• $it", color = Terracotta) }
        } else {
            Text("You can make this with what is already in the kitchen.", color = Sage, fontWeight = FontWeight.Bold)
        }
        Text("Steps", fontWeight = FontWeight.ExtraBold)
        match.recipe.instructions.forEachIndexed { index, step ->
            Text("${index + 1}. $step", fontSize = 16.sp)
        }
    }
}
