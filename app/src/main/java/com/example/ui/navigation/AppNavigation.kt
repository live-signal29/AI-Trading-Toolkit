package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.MarketItem
import com.example.ui.screens.AnalyzerScreen
import com.example.ui.screens.CalculatorsScreen
import com.example.ui.screens.ChartDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.JournalScreen
import com.example.ui.screens.ScannerSignalsScreen
import com.example.ui.screens.SettingsHealthScreen
import com.example.viewmodel.CalculatorViewModel
import com.example.viewmodel.JournalViewModel
import com.example.viewmodel.MarketViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Markets : Screen("markets", "Markets", Icons.Default.ShowChart)
    object Analyzer : Screen("analyzer", "AI Analyzer", Icons.Default.AutoAwesome)
    object Scanner : Screen("scanner", "Scanner", Icons.Default.Radar)
    object Calculators : Screen("calculators", "Calculators", Icons.Default.Calculate)
    object Journal : Screen("journal", "Journal", Icons.Default.Book)
    object ChartDetail : Screen("chart_detail", "Chart", Icons.Default.ShowChart)
    object SettingsHealth : Screen("settings_health", "Health", Icons.Default.ShowChart)
}

@Composable
fun AppNavigation(
    marketViewModel: MarketViewModel = viewModel(),
    calculatorViewModel: CalculatorViewModel = viewModel(),
    journalViewModel: JournalViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = remember(context) { com.example.util.AdMobConfig.getActivity(context) }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        Screen.Markets,
        Screen.Analyzer,
        Screen.Scanner,
        Screen.Calculators,
        Screen.Journal
    )

    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Markets.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(imageVector = screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Markets.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Markets.route) {
                HomeScreen(
                    viewModel = marketViewModel,
                    onMarketClick = { item ->
                        marketViewModel.selectMarketItem(item)
                        com.example.util.AdMobConfig.showInterstitialIfReady(activity) {
                            navController.navigate(Screen.ChartDetail.route)
                        }
                    },
                    onNavigateToHealth = {
                        navController.navigate(Screen.SettingsHealth.route)
                    }
                )
            }

            composable(Screen.ChartDetail.route) {
                val selectedItem = marketViewModel.selectedMarketItem.value
                if (selectedItem != null) {
                    ChartDetailScreen(
                        marketItem = selectedItem,
                        viewModel = marketViewModel,
                        onBack = { navController.popBackStack() },
                        onOpenCalculator = { price ->
                            calculatorViewModel.riskEntry.value = price.toString()
                            calculatorViewModel.computeRisk()
                            navController.navigate(Screen.Calculators.route)
                        }
                    )
                } else {
                    navController.popBackStack()
                }
            }

            composable(Screen.Analyzer.route) {
                AnalyzerScreen(
                    viewModel = marketViewModel,
                    onNavigateToRiskCalculator = { entry ->
                        calculatorViewModel.riskEntry.value = entry.toString()
                        calculatorViewModel.computeRisk()
                        navController.navigate(Screen.Calculators.route)
                    }
                )
            }

            composable(Screen.Scanner.route) {
                ScannerSignalsScreen(
                    viewModel = marketViewModel,
                    onSelectSymbol = { symbol ->
                        val item = marketViewModel.marketItems.value.find { it.symbol == symbol }
                        if (item != null) {
                            marketViewModel.selectMarketItem(item)
                            com.example.util.AdMobConfig.showInterstitialIfReady(activity) {
                                navController.navigate(Screen.ChartDetail.route)
                            }
                        }
                    }
                )
            }

            composable(Screen.Calculators.route) {
                CalculatorsScreen(viewModel = calculatorViewModel)
            }

            composable(Screen.Journal.route) {
                JournalScreen(viewModel = journalViewModel)
            }

            composable(Screen.SettingsHealth.route) {
                SettingsHealthScreen(
                    viewModel = marketViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
