package com.onhand.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onhand.app.InventoryViewModel
import com.onhand.app.data.StorageLocation
import com.onhand.app.ui.theme.Cream
import com.onhand.app.ui.theme.Muted
import com.onhand.app.ui.theme.Sage
import com.onhand.app.ui.theme.Surface
import com.onhand.app.ui.theme.Terracotta

@Composable
fun KitchenScreen(
    viewModel: InventoryViewModel,
    onOpenRecipes: () -> Unit,
    onAddItem: () -> Unit,
) {
    val items by viewModel.items.collectAsState()
    val shopping by viewModel.shopping.collectAsState()
    val expiring = viewModel.expiringCount()
    val locations = listOf(
        StorageLocation.Refrigerator,
        StorageLocation.Freezer,
        StorageLocation.Pantry,
        StorageLocation.Spices,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("WHAT YOU ALREADY HAVE", color = Terracotta, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text(
            "${items.size} items across the fridge, freezer, pantry, and spices.",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 34.sp,
        )
        Text(
            "OnHand looks at what is in the kitchen right now, then suggests recipes you can make without another store run. Shopping stays on a list until checkout moves food into the kitchen.",
            color = Muted,
            fontSize = 16.sp,
        )
        locations.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { location ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Surface, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                    ) {
                        Text(
                            items.count { it.location == location }.toString(),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Sage,
                        )
                        Text(location.label, color = Muted, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        if (expiring > 0) {
            Text("$expiring item${if (expiring == 1) "" else "s"} expired or due in 3 days.", color = Terracotta, fontWeight = FontWeight.Bold)
        }
        if (shopping.isNotEmpty()) {
            Text("${shopping.size} item${if (shopping.size == 1) "" else "s"} waiting on the shopping list.", color = Sage, fontWeight = FontWeight.SemiBold)
        }
        Button(onClick = onOpenRecipes, modifier = Modifier.fillMaxWidth()) {
            Text("What can I make?")
        }
        OutlinedButton(onClick = onAddItem, modifier = Modifier.fillMaxWidth()) {
            Text("Add something on hand")
        }
    }
}
