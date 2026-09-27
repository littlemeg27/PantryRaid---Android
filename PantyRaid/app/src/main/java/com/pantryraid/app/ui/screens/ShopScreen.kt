package com.onhand.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.onhand.app.InventoryViewModel
import com.onhand.app.data.Expiry
import com.onhand.app.data.StorageLocation
import com.onhand.app.data.UnitKind
import com.onhand.app.ui.theme.Cream
import com.onhand.app.ui.theme.Muted
import com.onhand.app.ui.theme.Sage
import com.onhand.app.ui.theme.Terracotta

@Composable
fun ShopScreen(viewModel: InventoryViewModel) {
    val shopping by viewModel.shopping.collectAsState()
    var name by rememberSaveable { mutableStateOf("") }
    var quantity by rememberSaveable { mutableStateOf("1") }
    var destination by rememberSaveable { mutableStateOf(StorageLocation.Pantry.name) }
    var expires by rememberSaveable { mutableStateOf("") }
    val purchasedCount = shopping.count { it.purchased }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            "Grocery list only. Nothing enters the kitchen until you check it out after you buy it.",
            color = Muted,
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Item") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text("Quantity") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = expires,
            onValueChange = { expires = it },
            label = { Text("Expires after purchase (optional YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Text("Goes into", fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StorageLocation.entries.take(4).forEach { location ->
                FilterChip(
                    selected = destination == location.name,
                    onClick = { destination = location.name },
                    label = { Text(location.label) },
                )
            }
        }
        Button(
            onClick = {
                val qty = quantity.toDoubleOrNull() ?: return@Button
                viewModel.addToShoppingList(
                    name = name,
                    quantity = qty,
                    unit = UnitKind.Each,
                    destination = StorageLocation.valueOf(destination),
                    expiresAtEpochMs = Expiry.parseDay(expires),
                )
                name = ""
                quantity = "1"
                expires = ""
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Add to shopping list")
        }

        if (shopping.isNotEmpty()) {
            Text("In the cart", fontWeight = FontWeight.Bold)
            shopping.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = item.purchased, onCheckedChange = { viewModel.togglePurchased(item.id) })
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${item.quantity} ${item.unit.label} → ${item.destination.label}" +
                                (Expiry.label(item.expiresAtEpochMs)?.let { " · $it" } ?: ""),
                            color = Muted,
                        )
                    }
                    TextButton(onClick = { viewModel.removeShopping(item.id) }) {
                        Text("Remove", color = Terracotta)
                    }
                }
            }
            Button(
                onClick = { viewModel.checkoutPurchased() },
                enabled = purchasedCount > 0,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Check out $purchasedCount bought item${if (purchasedCount == 1) "" else "s"}")
            }
            Text(
                "Checked-out items move into the chosen location (pantry by default).",
                color = Sage,
            )
        }
    }
}
