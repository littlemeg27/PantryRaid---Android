package com.onhand.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onhand.app.data.AiRecipeService
import com.onhand.app.data.AiSettings
import com.onhand.app.data.Expiry
import com.onhand.app.data.ExpiryState
import com.onhand.app.data.InventoryItem
import com.onhand.app.data.InventoryRepository
import com.onhand.app.data.Recipe
import com.onhand.app.data.RecipeMatch
import com.onhand.app.data.RecipeMatcher
import com.onhand.app.data.ShoppingItem
import com.onhand.app.data.StorageLocation
import com.onhand.app.data.UnitKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class InventoryViewModel(
    private val repository: InventoryRepository,
    private val aiService: AiRecipeService = AiRecipeService(),
) : ViewModel() {
    private val _items = MutableStateFlow(repository.loadInventory())
    val items: StateFlow<List<InventoryItem>> = _items.asStateFlow()

    private val _shopping = MutableStateFlow(repository.loadShopping())
    val shopping: StateFlow<List<ShoppingItem>> = _shopping.asStateFlow()

    private val _aiSettings = MutableStateFlow(repository.loadAiSettings())
    val aiSettings: StateFlow<AiSettings> = _aiSettings.asStateFlow()

    private val _aiMatches = MutableStateFlow<List<RecipeMatch>>(emptyList())
    val aiMatches: StateFlow<List<RecipeMatch>> = _aiMatches.asStateFlow()

    private val _aiBusy = MutableStateFlow(false)
    val aiBusy: StateFlow<Boolean> = _aiBusy.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    private val extraRecipes = mutableMapOf<String, Recipe>()

    fun addItem(
        name: String,
        quantity: Double,
        unit: UnitKind,
        location: StorageLocation,
        expiresAtEpochMs: Long? = null,
    ) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || quantity <= 0) return

        _items.update { current ->
            val existing = current.find {
                it.name.equals(trimmed, ignoreCase = true) &&
                    it.location == location &&
                    it.unit == unit
            }
            val next = if (existing == null) {
                listOf(
                    InventoryItem(
                        id = "item-${UUID.randomUUID()}",
                        name = trimmed,
                        quantity = quantity,
                        unit = unit,
                        location = location,
                        expiresAtEpochMs = expiresAtEpochMs,
                    ),
                ) + current
            } else {
                current.map {
                    if (it.id != existing.id) it
                    else it.copy(
                        quantity = it.quantity + quantity,
                        expiresAtEpochMs = earlier(it.expiresAtEpochMs, expiresAtEpochMs),
                    )
                }
            }
            repository.saveInventory(next)
            next
        }
    }

    fun changeQuantity(id: String, quantity: Double) {
        _items.update { current ->
            val next = current
                .map { if (it.id == id) it.copy(quantity = quantity) else it }
                .filter { it.quantity > 0 }
            repository.saveInventory(next)
            next
        }
    }

    fun remove(id: String) {
        _items.update { current ->
            val next = current.filterNot { it.id == id }
            repository.saveInventory(next)
            next
        }
    }

    fun addToShoppingList(
        name: String,
        quantity: Double,
        unit: UnitKind,
        destination: StorageLocation,
        expiresAtEpochMs: Long? = null,
    ) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || quantity <= 0) return
        _shopping.update { current ->
            val next = listOf(
                ShoppingItem(
                    id = "shop-${UUID.randomUUID()}",
                    name = trimmed,
                    quantity = quantity,
                    unit = unit,
                    destination = destination,
                    expiresAtEpochMs = expiresAtEpochMs,
                ),
            ) + current
            repository.saveShopping(next)
            next
        }
    }

    fun togglePurchased(id: String) {
        _shopping.update { current ->
            val next = current.map { if (it.id == id) it.copy(purchased = !it.purchased) else it }
            repository.saveShopping(next)
            next
        }
    }

    fun removeShopping(id: String) {
        _shopping.update { current ->
            val next = current.filterNot { it.id == id }
            repository.saveShopping(next)
            next
        }
    }

    fun checkoutPurchased() {
        val bought = _shopping.value.filter { it.purchased }
        if (bought.isEmpty()) return
        bought.forEach { item ->
            addItem(item.name, item.quantity, item.unit, item.destination, item.expiresAtEpochMs)
        }
        _shopping.update { current ->
            val next = current.filterNot { it.purchased }
            repository.saveShopping(next)
            next
        }
    }

    fun saveAiSettings(settings: AiSettings) {
        _aiSettings.value = settings
        repository.saveAiSettings(settings)
    }

    fun localMatches(query: String = "") = RecipeMatcher.suggest(_items.value, query)

    fun recipe(id: String): Recipe? = extraRecipes[id] ?: RecipeMatcher.find(id)

    fun matchFor(id: String) = recipe(id)?.let { RecipeMatcher.match(it, _items.value) }

    fun searchWithAi(query: String) {
        viewModelScope.launch {
            _aiBusy.value = true
            _aiError.value = null
            try {
                val matches = withContext(Dispatchers.IO) {
                    aiService.search(_items.value, query, _aiSettings.value)
                }
                matches.forEach { extraRecipes[it.recipe.id] = it.recipe }
                _aiMatches.value = matches
            } catch (error: Exception) {
                _aiError.value = error.message ?: "AI search failed"
            } finally {
                _aiBusy.value = false
            }
        }
    }

    fun expiringCount(): Int = _items.value.count {
        val state = Expiry.state(it.expiresAtEpochMs)
        state == ExpiryState.Soon || state == ExpiryState.Expired
    }

    private fun earlier(a: Long?, b: Long?): Long? = when {
        a == null -> b
        b == null -> a
        else -> minOf(a, b)
    }
}
