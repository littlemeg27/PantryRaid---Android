package com.onhand.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.onhand.app.InventoryViewModel
import com.onhand.app.data.StorageLocation
import com.onhand.app.ui.screens.AddItemScreen
import com.onhand.app.ui.screens.InventoryScreen
import com.onhand.app.ui.screens.KitchenScreen
import com.onhand.app.ui.screens.RecipeDetailScreen
import com.onhand.app.ui.screens.RecipesScreen
import com.onhand.app.ui.screens.SettingsScreen
import com.onhand.app.ui.screens.ShopScreen

private data class Tab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val tabs = listOf(
    Tab("kitchen", "Kitchen", Icons.Outlined.Kitchen),
    Tab("inventory", "On hand", Icons.Outlined.ListAlt),
    Tab("recipes", "Recipes", Icons.Outlined.Restaurant),
    Tab("shop", "Shop", Icons.Outlined.ShoppingCart),
)

@Composable
fun OnHandApp(viewModel: InventoryViewModel) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination?.route.orEmpty()
    val showBar = tabs.any { current == it.route || current.startsWith(it.route + "?") }

    Scaffold(
        bottomBar = {
            if (showBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = current == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "kitchen",
            modifier = Modifier.padding(padding),
        ) {
            composable("kitchen") {
                KitchenScreen(
                    viewModel = viewModel,
                    onOpenRecipes = { navController.navigate("recipes") },
                    onAddItem = { navController.navigate("add/Pantry") },
                )
            }
            composable("inventory") {
                InventoryScreen(
                    viewModel = viewModel,
                    onAddItem = { location -> navController.navigate("add/${location.name}") },
                )
            }
            composable("recipes") {
                RecipesScreen(
                    viewModel = viewModel,
                    onOpenRecipe = { navController.navigate("recipe/$it") },
                    onOpenSettings = { navController.navigate("settings") },
                )
            }
            composable("shop") {
                ShopScreen(viewModel = viewModel)
            }
            composable(
                route = "add/{location}",
                arguments = listOf(navArgument("location") { type = NavType.StringType }),
            ) { entry ->
                val location = runCatching {
                    StorageLocation.valueOf(entry.arguments?.getString("location") ?: "Pantry")
                }.getOrDefault(StorageLocation.Pantry)
                AddItemScreen(
                    initialLocation = location,
                    onSave = { name, qty, unit, dest, expiresAt ->
                        viewModel.addItem(name, qty, unit, dest, expiresAt)
                        navController.popBackStack()
                    },
                    onClose = { navController.popBackStack() },
                )
            }
            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = "recipe/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                RecipeDetailScreen(
                    recipeId = entry.arguments?.getString("id").orEmpty(),
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
