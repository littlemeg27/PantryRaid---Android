package com.onhand.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class InventoryRepository(context: Context) {
    private val prefs = context.getSharedPreferences("onhand_kitchen", Context.MODE_PRIVATE)

    fun loadInventory(): List<InventoryItem> {
        val raw = prefs.getString(ITEMS_KEY, null) ?: return SeedData.inventory
        return runCatching { decodeItems(raw) }.getOrDefault(SeedData.inventory)
    }

    fun saveInventory(items: List<InventoryItem>) {
        prefs.edit().putString(ITEMS_KEY, encodeItems(items)).apply()
    }

    fun loadShopping(): List<ShoppingItem> {
        val raw = prefs.getString(SHOP_KEY, null) ?: return emptyList()
        return runCatching { decodeShopping(raw) }.getOrDefault(emptyList())
    }

    fun saveShopping(items: List<ShoppingItem>) {
        prefs.edit().putString(SHOP_KEY, encodeShopping(items)).apply()
    }

    fun loadAiSettings(): AiSettings {
        return AiSettings(
            apiKey = prefs.getString(AI_KEY, "").orEmpty(),
            baseUrl = prefs.getString(AI_URL, "https://api.x.ai/v1") ?: "https://api.x.ai/v1",
            model = prefs.getString(AI_MODEL, "grok-3") ?: "grok-3",
        )
    }

    fun saveAiSettings(settings: AiSettings) {
        prefs.edit()
            .putString(AI_KEY, settings.apiKey)
            .putString(AI_URL, settings.baseUrl)
            .putString(AI_MODEL, settings.model)
            .apply()
    }

    private fun encodeItems(items: List<InventoryItem>): String {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("name", item.name)
                    put("quantity", item.quantity)
                    put("unit", item.unit.name)
                    put("location", item.location.name)
                    put("addedAtEpochMs", item.addedAtEpochMs)
                    if (item.expiresAtEpochMs != null) put("expiresAtEpochMs", item.expiresAtEpochMs)
                },
            )
        }
        return array.toString()
    }

    private fun decodeItems(raw: String): List<InventoryItem> {
        val array = JSONArray(raw)
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    InventoryItem(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        quantity = obj.getDouble("quantity"),
                        unit = UnitKind.valueOf(obj.getString("unit")),
                        location = StorageLocation.valueOf(obj.getString("location")),
                        addedAtEpochMs = obj.optLong("addedAtEpochMs", System.currentTimeMillis()),
                        expiresAtEpochMs = if (obj.has("expiresAtEpochMs")) obj.getLong("expiresAtEpochMs") else null,
                    ),
                )
            }
        }
    }

    private fun encodeShopping(items: List<ShoppingItem>): String {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("name", item.name)
                    put("quantity", item.quantity)
                    put("unit", item.unit.name)
                    put("destination", item.destination.name)
                    put("purchased", item.purchased)
                    if (item.expiresAtEpochMs != null) put("expiresAtEpochMs", item.expiresAtEpochMs)
                },
            )
        }
        return array.toString()
    }

    private fun decodeShopping(raw: String): List<ShoppingItem> {
        val array = JSONArray(raw)
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    ShoppingItem(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        quantity = obj.getDouble("quantity"),
                        unit = UnitKind.valueOf(obj.getString("unit")),
                        destination = StorageLocation.valueOf(obj.getString("destination")),
                        purchased = obj.optBoolean("purchased"),
                        expiresAtEpochMs = if (obj.has("expiresAtEpochMs")) obj.getLong("expiresAtEpochMs") else null,
                    ),
                )
            }
        }
    }

    companion object {
        private const val ITEMS_KEY = "items_v2"
        private const val SHOP_KEY = "shopping_v1"
        private const val AI_KEY = "ai_key"
        private const val AI_URL = "ai_url"
        private const val AI_MODEL = "ai_model"
    }
}
