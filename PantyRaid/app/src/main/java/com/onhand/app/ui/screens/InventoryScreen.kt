package com.onhand.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
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
import com.onhand.app.data.ExpiryState
import com.onhand.app.data.StorageLocation
import com.onhand.app.ui.theme.Cream
import com.onhand.app.ui.theme.Muted
import com.onhand.app.ui.theme.Sage
import com.onhand.app.ui.theme.Terracotta

@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    onAddItem: (StorageLocation) -> Unit,
) {
    val items by viewModel.items.collectAsState()
    var filter by rememberSaveable { mutableStateOf("ALL") }
    val visible = if (filter == "ALL") items else items.filter { it.location.name == filter }
    val locationFilter = runCatching { StorageLocation.valueOf(filter) }.getOrDefault(StorageLocation.Pantry)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(top = 16.dp),
    ) {
        Text(
            "Refrigerator, freezer, pantry, spices — everything currently in the kitchen.",
            color = Muted,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FilterChip(selected = filter == "ALL", onClick = { filter = "ALL" }, label = { Text("All") })
            }
            items(StorageLocation.entries) { location ->
                FilterChip(
                    selected = filter == location.name,
                    onClick = { filter = location.name },
                    label = { Text(location.label) },
                )
            }
        }
        TextButton(onClick = { onAddItem(if (filter == "ALL") StorageLocation.Pantry else locationFilter) }) {
            Text("+ Add item", color = Terracotta, fontWeight = FontWeight.Bold)
        }
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(visible, key = { it.id }) { item ->
                val expiry = Expiry.label(item.expiresAtEpochMs)
                val expiryState = Expiry.state(item.expiresAtEpochMs)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, fontWeight = FontWeight.Bold)
                        Text(
                            "${item.quantity} ${item.unit.label} · ${item.location.label}",
                            color = Muted,
                        )
                        if (expiry != null) {
                            Text(
                                expiry,
                                color = if (expiryState == ExpiryState.Fresh) Sage else Terracotta,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    TextButton(onClick = { viewModel.changeQuantity(item.id, item.quantity - 1) }) { Text("−") }
                    TextButton(onClick = { viewModel.changeQuantity(item.id, item.quantity + 1) }) { Text("+") }
                    TextButton(onClick = { viewModel.remove(item.id) }) {
                        Text("Remove", color = Terracotta)
                    }
                }
            }
        }
        if (visible.isEmpty()) {
            Text(
                "Add what is in the fridge, freezer, pantry, or spice rack so recipes can use what you already have.",
                color = Sage,
                modifier = Modifier.padding(20.dp),
            )
        }
    }
}
