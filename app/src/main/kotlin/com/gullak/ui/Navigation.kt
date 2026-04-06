package com.animesh.gullak.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.animesh.gullak.ui.goals.GoalsScreen
import com.animesh.gullak.ui.home.HomeScreen
import com.animesh.gullak.ui.insights.InsightsScreen
import com.animesh.gullak.ui.profile.ProfileScreen
import com.animesh.gullak.ui.transactions.AddEditTransactionScreen
import com.animesh.gullak.ui.transactions.TransactionsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Transactions : Screen("transactions")
    object AddEditTransaction : Screen("add_edit_transaction?transactionId={transactionId}") {
        fun createRoute(transactionId: Long? = null) =
            if (transactionId != null) "add_edit_transaction?transactionId=$transactionId"
            else "add_edit_transaction"
    }
    object Goals : Screen("goals")
    object Insights : Screen("insights")
    object Profile : Screen("profile")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Default.Home),
    BottomNavItem(Screen.Transactions, "Wallet", Icons.Default.AccountBalanceWallet),
    BottomNavItem(Screen.Goals, "Goals", Icons.Default.Flag),
    BottomNavItem(Screen.Insights, "Insights", Icons.Default.BarChart),
    BottomNavItem(Screen.Profile, "Profile", Icons.Default.Person),
)

@Composable
fun FinanceNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomNavItems.any {
        currentDestination?.hierarchy?.any { dest ->
            dest.route == it.screen.route
        } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy
                            ?.any { it.route == item.screen.route } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {                    // ← only navigate if not already selected
                                    navController.navigate(item.screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                            inclusive = false       // ← add this
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(item.icon, contentDescription = item.label)
                            },
                            label = {
                                Text(
                                    item.label,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToAddTransaction = {
                        navController.navigate(Screen.AddEditTransaction.createRoute())
                    },
                    onNavigateToTransactions = {
                        navController.navigate(Screen.Transactions.route)
                    }
                )
            }
            composable(Screen.Transactions.route) {
                TransactionsScreen(
                    onNavigateToAdd = {
                        navController.navigate(Screen.AddEditTransaction.createRoute())
                    },
                    onNavigateToEdit = { id ->
                        navController.navigate(Screen.AddEditTransaction.createRoute(id))
                    }
                )
            }
            composable(
                route = Screen.AddEditTransaction.route,
                arguments = listOf(
                    navArgument("transactionId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("transactionId") ?: -1L
                AddEditTransactionScreen(
                    transactionId = if (id == -1L) null else id,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Goals.route) {
                GoalsScreen()
            }
            composable(Screen.Insights.route) {
                InsightsScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen()
            }
        }
    }
}