package com.onhand.app.data

data class InventoryItem(
    val id: String,
    val name: String,
    val quantity: Double,
    val unit: UnitKind,
    val location: StorageLocation,
    val addedAtEpochMs: Long = System.currentTimeMillis(),
    val expiresAtEpochMs: Long? = null,
)

data class ShoppingItem(
    val id: String,
    val name: String,
    val quantity: Double,
    val unit: UnitKind,
    val destination: StorageLocation,
    val purchased: Boolean = false,
    val expiresAtEpochMs: Long? = null,
)

enum class StorageLocation(val label: String) {
    Refrigerator("Fridge"),
    Freezer("Freezer"),
    Pantry("Pantry"),
    Spices("Spices"),
    Other("Other"),
}

enum class UnitKind(val label: String) {
    Each("each"),
    Cup("cup"),
    Tbsp("tbsp"),
    Tsp("tsp"),
    Oz("oz"),
    Lb("lb"),
    Gram("g"),
    Kg("kg"),
    Ml("ml"),
    Liter("l"),
    Bunch("bunch"),
    Can("can"),
    Bag("bag"),
    Loaf("loaf"),
    Pinch("pinch"),
}

data class RecipeIngredient(
    val name: String,
    val optional: Boolean = false,
)

data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val minutes: Int,
    val servings: Int,
    val tags: List<String>,
    val ingredients: List<RecipeIngredient>,
    val instructions: List<String>,
)

enum class RecipeSource { Local, Ai }

data class RecipeMatch(
    val recipe: Recipe,
    val have: List<String>,
    val missing: List<String>,
    val optionalMissing: List<String>,
    val score: Double,
    val source: RecipeSource = RecipeSource.Local,
)

data class AiSettings(
    val apiKey: String = "",
    val baseUrl: String = "https://api.x.ai/v1",
    val model: String = "grok-3",
)

enum class ExpiryState { None, Fresh, Soon, Expired }
