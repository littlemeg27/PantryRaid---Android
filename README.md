# PantryRaid for Android

Kotlin + Jetpack Compose app. Track fridge, freezer, pantry, and spices, find recipes from what’s on hand, and keep a shopping list that only stocks the kitchen after checkout.

## Run

1. Open this `android/` folder in Android Studio (Koala / Ladybug or newer).
2. Let Gradle sync (JDK 17, compile SDK 35).
3. Run the `app` configuration on an emulator or device.

- Application ID: `com.onhand.app`
- Min SDK: 26
- Display name: PantryRaid

## What’s in the app

| Screen | What it does |
| --- | --- |
| Kitchen | Counts by location, expiry warnings, shopping-list reminder |
| On hand | Full inventory, filter by location, add / adjust / remove |
| Recipes | Local on-hand matcher, optional live AI search |
| Shop | Grocery list → mark purchased → check out into fridge / freezer / pantry / spices |
| Add item | Name, quantity, unit, location, optional `YYYY-MM-DD` expiry |
| AI setup | API key, base URL, model |

First launch loads a seed kitchen so Recipes already has matches.

## Code map

```
app/src/main/java/com/onhand/app/
  MainActivity.kt
  OnHandApplication.kt
  InventoryViewModel.kt
  data/
    Models.kt
    SeedData.kt
    RecipeMatcher.kt
    AiRecipeService.kt
    InventoryRepository.kt
    Expiry.kt
  ui/
    OnHandApp.kt
    theme/Theme.kt
    screens/
      KitchenScreen.kt
      InventoryScreen.kt
      RecipesScreen.kt
      RecipeDetailScreen.kt
      ShopScreen.kt
      AddItemScreen.kt
      SettingsScreen.kt
```

Inventory and the shopping list persist in SharedPreferences (`onhand_kitchen`). Internet permission is only for the AI call.

## AI

Leave the API key empty to stay on the built-in matcher.

Defaults if you turn AI on:

- Base URL: `https://api.x.ai/v1`
- Model: `grok-3`

Any OpenAI-compatible chat API works if you change the URL and model. Do not commit keys.

## Notes

This is starter code. No Room database, no notifications, no launcher icon set yet. The Gradle wrapper jar is not in the repo — Android Studio can generate it on first open if needed.
