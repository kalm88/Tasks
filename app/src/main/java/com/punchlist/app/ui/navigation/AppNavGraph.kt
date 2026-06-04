package com.punchlist.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.punchlist.app.ui.auth.LoginScreen
import com.punchlist.app.ui.auth.RegisterScreen
import com.punchlist.app.ui.camera.CameraScreen
import com.punchlist.app.ui.create.CreatePunchItemScreen
import com.punchlist.app.ui.detail.PunchItemDetailScreen
import com.punchlist.app.ui.feed.PunchItemFeedScreen
import com.punchlist.app.ui.project.CreateProjectScreen
import com.punchlist.app.ui.project.ProjectListScreen
import com.punchlist.app.ui.scanner.BarcodeScannerScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavRoutes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(NavRoutes.REGISTER) },
                onLoginSuccess = {
                    navController.navigate(NavRoutes.PROJECT_LIST) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(NavRoutes.PROJECT_LIST) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.PROJECT_LIST) {
            ProjectListScreen(
                onProjectSelected = { projectId, projectName ->
                    navController.navigate(NavRoutes.punchItemFeed(projectId, projectName))
                },
                onCreateProject = { navController.navigate(NavRoutes.CREATE_PROJECT) }
            )
        }

        composable(NavRoutes.CREATE_PROJECT) {
            CreateProjectScreen(
                onProjectCreated = { projectId, projectName ->
                    navController.navigate(NavRoutes.punchItemFeed(projectId, projectName)) {
                        popUpTo(NavRoutes.PROJECT_LIST)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.PUNCH_ITEM_FEED,
            arguments = listOf(
                navArgument("projectId") { type = NavType.StringType },
                navArgument("projectName") { type = NavType.StringType; defaultValue = "Project" }
            )
        ) { backStack ->
            val projectId = backStack.arguments?.getString("projectId") ?: return@composable
            val projectName = backStack.arguments?.getString("projectName") ?: "Project"
            PunchItemFeedScreen(
                projectId = projectId,
                onItemClick = { itemId ->
                    navController.navigate(NavRoutes.punchItemDetail(projectId, itemId, projectName))
                },
                onNewItem = {
                    navController.navigate(NavRoutes.createPunchItem(projectId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.CREATE_PUNCH_ITEM,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStack ->
            val projectId = backStack.arguments?.getString("projectId") ?: return@composable
            CreatePunchItemScreen(
                projectId = projectId,
                onItemCreated = {
                    navController.popBackStack()
                },
                onOpenCamera = { navController.navigate(NavRoutes.CAMERA) },
                onOpenScanner = { navController.navigate(NavRoutes.BARCODE_SCANNER) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.CAMERA) {
            CameraScreen(
                onPhotoTaken = { uri ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("captured_photo_uri", uri.toString())
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.BARCODE_SCANNER) {
            BarcodeScannerScreen(
                onScanned = { sku ->
                    // Pass scanned SKU back to the calling screen via SavedStateHandle
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("scanned_sku", sku)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.PUNCH_ITEM_DETAIL,
            arguments = listOf(
                navArgument("projectId") { type = NavType.StringType },
                navArgument("itemId") { type = NavType.StringType },
                navArgument("projectName") { type = NavType.StringType; defaultValue = "Project" }
            )
        ) { backStack ->
            val projectId = backStack.arguments?.getString("projectId") ?: return@composable
            val itemId = backStack.arguments?.getString("itemId") ?: return@composable
            val projectName = backStack.arguments?.getString("projectName") ?: "Project"
            PunchItemDetailScreen(
                projectId = projectId,
                itemId = itemId,
                projectName = projectName,
                onBack = { navController.popBackStack() },
                onOpenCamera = { navController.navigate(NavRoutes.CAMERA) }
            )
        }
    }
}
