package com.spazoodle.guardian.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.spazoodle.guardian.ui.history.HistoryScreen
import com.spazoodle.guardian.ui.home.AlarmEditorScreen
import com.spazoodle.guardian.ui.home.HomeScreen
import com.spazoodle.guardian.ui.reliability.ReliabilityScreen

@Composable
fun GuardianRoot() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onCreateAlarm = { navController.navigate("editor") },
                onEditAlarm = { alarmId -> navController.navigate("editor?alarmId=$alarmId") },
                onOpenReliability = { navController.navigate("reliability") },
                onOpenHistory = { navController.navigate("history") }
            )
        }
        composable(
            route = "editor?alarmId={alarmId}",
            arguments = listOf(
                navArgument("alarmId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getLong("alarmId") ?: -1L
            AlarmEditorScreen(
                alarmId = alarmId.takeIf { it > 0L },
                onBack = { navController.popBackStack() }
            )
        }
        composable("reliability") {
            ReliabilityScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("history") {
            HistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
