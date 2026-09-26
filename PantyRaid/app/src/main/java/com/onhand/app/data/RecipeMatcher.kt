package com.onhand.app.data

object RecipeMatcher {
    private val aliases = mapOf(
        "chicken thigh" to "chicken thighs",
        "chicken" to "chicken thighs",
        "pea" to "frozen peas",
        "peas" to "frozen peas",
        "frozen pea" to "frozen peas",
        "tomato" to "canned tomatoes",
        "tomatoes" to "canned tomatoes",
        "crushed tomatoes" to "canned tomatoes",
        "diced tomatoes" to "canned tomatoes",
        "bean" to "black beans",
        "beans" to "black beans",
        "black bean" to "black beans",
        "cheddar cheese" to "cheddar",
        "cheese" to "cheddar",
        "egg" to "eggs",
        "spaghetti" to "pasta",
        "noodles" to "pasta",
        "white rice" to "rice",
        "kosher salt" to "salt",
        "sea salt" to "salt",
        "pepper" to "black pepper",
        "extra virgin olive oil" to "olive oil",
        "yellow onion" to "onion",
        "onions" to "onion",
        "garlic cloves" to "garlic",
        "garlic clove" to "garlic",
    )

    fun normalize(raw: String): String {
        val cleaned = raw.lowercase().replace(Regex("[^a-z0-9\\s]"), " ").replace(Regex("\\s+"), " ").trim()
        aliases[cleaned]?.let { return it }
        val stripped = cleaned
            .replace(Regex("\\b(fresh|frozen|canned|dried|ground|whole|chopped|minced)\\b"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
        return aliases[stripped] ?: stripped
    }

    fun namesMatch(a: String, b: String): Boolean {
        val left = normalize(a)
        val right = normalize(b)
        return left == right || left.contains(right) || right.contains(left)
    }

    fun match(recipe: Recipe, items: List<InventoryItem>): RecipeMatch {
        val have = mutableListOf<String>()
        val missing = mutableListOf<String>()
        val optionalMissing = mutableListOf<String>()

        recipe.ingredients.forEach { ingredient ->
            val owned = items.any { it.quantity > 0 && namesMatch(it.name, ingredient.name) }
            when {
                owned -> have += ingredient.name
                ingredient.optional -> optionalMissing += ingredient.name
                else -> missing += ingredient.name
            }
        }

        val required = recipe.ingredients.filterNot { it.optional }
        val requiredHave = required.count { ingredient ->
            items.any { it.quantity > 0 && namesMatch(it.name, ingredient.name) }
        }
        val coverage = if (required.isEmpty()) 0.0 else requiredHave.toDouble() / required.size
        val score = (coverage - missing.size * 0.18).coerceIn(0.0, 1.0)

        return RecipeMatch(recipe, have, missing, optionalMissing, score)
    }

    fun suggest(items: List<InventoryItem>, query: String = ""): List<RecipeMatch> {
        val q = query.trim().lowercase()
        return SeedData.recipes
            .map { match(it, items) }
            .filter { it.score > 0.15 }
            .filter { match ->
                if (q.isBlank()) true
                else {
                    val haystack = "${match.recipe.title} ${match.recipe.tags.joinToString(" ")} ${match.recipe.description}".lowercase()
                    q.split(Regex("\\s+")).any { haystack.contains(it) }
                }
            }
            .sortedWith(compareBy<RecipeMatch> { it.missing.size }.thenByDescending { it.score })
    }

    fun find(id: String): Recipe? = SeedData.recipes.find { it.id == id }

    fun buildOnHandPrompt(items: List<InventoryItem>, query: String = ""): String {
        val grouped = items.groupBy { it.location }
            .map { (location, list) ->
                "${location.label}: " + list.joinToString(", ") { item ->
                    val expiry = item.expiresAtEpochMs?.let { " expires ${Expiry.formatDay(it)}" }.orEmpty()
                    "${item.name} (${item.quantity} ${item.unit.label}$expiry)"
                }
            }
            .joinToString("\n")
        val request = query.trim().ifBlank { "whatever is easiest with current on-hand items" }
        return """
            Suggest recipes that use what the user already has.
            Prefer zero extra grocery trips. If something is missing, keep it to 1–2 inexpensive staples.
            Favor ingredients that expire sooner.

            Ingredients on hand:
            ${grouped.ifBlank { "(inventory is empty)" }}

            User request: $request
        """.trimIndent()
    }
}
