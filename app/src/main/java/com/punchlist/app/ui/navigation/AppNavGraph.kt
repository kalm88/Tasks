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
                onProjectSelected = { projectId ->
                    navController.navigate(NavRoutes.punchItemFeed(projectId))
                },
                onCreateProject = { navController.navigate(NavRoutes.CREATE_PROJECT) }
            )
        }

        composable(NavRoutes.CREATE_PROJECT) {
            CreateProjectScreen(
                onProjectCreated = { projectId ->
                    navController.navigate(NavRoutes.punchItemFeed(projectId)) {
                        popUpTo(NavRoutes.PROJECT_LIST)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.PUNCH_ITEM_FEED,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStack ->
            val projectId = backStack.arguments?.getString("projectId") ?: return@composable
            PunchItemFeedScreen(
                projectId = projectId,
                onItemClick = { itemId ->
                    navController.navigate(NavRoutes.punchItemDetail(projectId, itemId))
                },
                onNewItem = {
                    // Camera-first: go to camera, which then hands off to create form
                    navController.navigate(NavRoutes.punchItemFeed(projectId).let {
                        NavRoutes.createPunchItem(projectId)
                    })
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
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.CAMERA) {
            CameraScreen(
                onPhotoTaken = { uri ->
                    // Pass URI back to the create screen via SavedStateHandle
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("captured_photo_uri", uri.toString())
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.PUNCH_ITEM_DETAIL,
            arguments = listOf(
                navArgument("projectId") { type = NavType.StringType },
                navArgument("itemId") { type = NavType.StringType }
            )
        ) { backStack ->
            val projectId = backStack.arguments?.getString("projectId") ?: return@composable
            val itemId = backStack.arguments?.getString("itemId") ?: return@composable
            PunchItemDetailScreen(
                projectId = projectId,
                itemId = itemId,
                onBack = { navController.popBackStack() },
                onOpenCamera = { navController.navigate(NavRoutes.CAMERA) }
            )
        }
    }
}
