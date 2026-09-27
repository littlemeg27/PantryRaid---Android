package com.onhand.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.onhand.app.data.Expiry
import com.onhand.app.data.StorageLocation
import com.onhand.app.data.UnitKind
import com.onhand.app.ui.theme.Cream
import com.onhand.app.ui.theme.Muted

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddItemScreen(
    initialLocation: StorageLocation,
    onSave: (String, Double, UnitKind, StorageLocation, Long?) -> Unit,
    onClose: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var quantity by rememberSaveable { mutableStateOf("1") }
    var unit by rememberSaveable { mutableStateOf(UnitKind.Each.name) }
    var location by rememberSaveable { mutableStateOf(initialLocation.name) }
    var expires by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            "Log refrigerator, freezer, pantry, or spice items so the recipe tool knows what you can cook tonight.",
            color = Muted,
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Item name") },
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
            label = { Text("Expires (optional YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Text("Unit", fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UnitKind.entries.take(8).forEach { option ->
                FilterChip(
                    selected = unit == option.name,
                    onClick = { unit = option.name },
                    label = { Text(option.label) },
                )
            }
        }
        Text("Location", fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StorageLocation.entries.forEach { option ->
                FilterChip(
                    selected = location == option.name,
                    onClick = { location = option.name },
                    label = { Text(option.label) },
                )
            }
        }
        Button(
            onClick = {
                val qty = quantity.toDoubleOrNull() ?: return@Button
                onSave(
                    name,
                    qty,
                    UnitKind.valueOf(unit),
                    StorageLocation.valueOf(location),
                    Expiry.parseDay(expires),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save to kitchen")
        }
        OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel")
        }
    }
}
