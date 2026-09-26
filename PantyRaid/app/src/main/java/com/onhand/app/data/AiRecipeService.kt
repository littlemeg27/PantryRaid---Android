package com.onhand.app.data

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class AiRecipeService {
    fun search(items: List<InventoryItem>, query: String, settings: AiSettings): List<RecipeMatch> {
        val key = settings.apiKey.trim()
        if (key.isEmpty()) {
            return RecipeMatcher.suggest(items, query).map { it.copy(source = RecipeSource.Local) }
        }

        val root = settings.baseUrl.trim().trimEnd('/')
        val messages = JSONArray()
            .put(JSONObject().put("role", "system").put("content", SYSTEM_PROMPT))
            .put(JSONObject().put("role", "user").put("content", RecipeMatcher.buildOnHandPrompt(items, query)))
        val body = JSONObject()
            .put("model", settings.model.ifBlank { "grok-3" })
            .put("messages", messages)
            .put("temperature", 0.3)
            .toString()

        val connection = (URL("$root/chat/completions").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 25_000
            readTimeout = 60_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $key")
        }

        OutputStreamWriter(connection.outputStream).use { it.write(body) }
        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val raw = stream.bufferedReader().use(BufferedReader::readText)
        if (code !in 200..299) {
            throw IllegalStateException("AI request failed ($code): ${raw.take(240)}")
        }

        val content = JSONObject(raw)
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")

        return parseRecipes(content, items)
    }

    private fun parseRecipes(content: String, items: List<InventoryItem>): List<RecipeMatch> {
        val jsonStart = content.indexOf('{')
        val jsonEnd = content.lastIndexOf('}')
        if (jsonStart < 0 || jsonEnd <= jsonStart) {
            throw IllegalStateException("AI did not return JSON recipes.")
        }
        val parsed = JSONObject(content.substring(jsonStart, jsonEnd + 1))
        val array = parsed.optJSONArray("recipes") ?: JSONArray()
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val ingredientsJson = obj.optJSONArray("ingredients") ?: JSONArray()
                val ingredients = buildList {
                    for (j in 0 until ingredientsJson.length()) {
                        val entry = ingredientsJson.optJSONObject(j)
                        if (entry != null) {
                            add(RecipeIngredient(entry.optString("name"), entry.optBoolean("optional")))
                        } else {
                            add(RecipeIngredient(ingredientsJson.optString(j)))
                        }
                    }
                }
                val stepsJson = obj.optJSONArray("instructions") ?: JSONArray()
                val instructions = buildList {
                    for (j in 0 until stepsJson.length()) add(stepsJson.optString(j))
                }
                val tagsJson = obj.optJSONArray("tags") ?: JSONArray()
                val tags = buildList {
                    for (j in 0 until tagsJson.length()) add(tagsJson.optString(j))
                }
                val recipe = Recipe(
                    id = obj.optString("id").ifBlank { "ai-${UUID.randomUUID()}" },
                    title = obj.optString("title").ifBlank { "Untitled recipe" },
                    description = obj.optString("description"),
                    minutes = obj.optInt("minutes", 20),
                    servings = obj.optInt("servings", 2),
                    tags = tags,
                    ingredients = ingredients,
                    instructions = instructions,
                )
                add(RecipeMatcher.match(recipe, items).copy(source = RecipeSource.Ai))
            }
        }
    }

    companion object {
        private const val SYSTEM_PROMPT =
            "You are the OnHand kitchen assistant. Reply with JSON only, no markdown. " +
                "Shape: {\"recipes\":[{\"id\":\"slug\",\"title\":\"\",\"description\":\"\",\"minutes\":20," +
                "\"servings\":2,\"tags\":[\"quick\"],\"ingredients\":[{\"name\":\"Eggs\",\"optional\":false}]," +
                "\"instructions\":[\"step\"]}]}. Prefer recipes the user can cook with on-hand items. " +
                "Missing ingredients should stay rare and cheap."
    }
}
