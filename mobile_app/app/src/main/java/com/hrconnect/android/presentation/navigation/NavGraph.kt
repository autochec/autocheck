package com.hrconnect.android.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hrconnect.android.presentation.features.login.LoginScreen
import com.hrconnect.android.presentation.features.results.SubmissionResultsScreen
import com.hrconnect.android.presentation.features.submit.SubmitAssignmentScreen
import com.hrconnect.android.presentation.features.submissions.MySubmissionsScreen

/**
 * Граф навигации приложения.
 *
 * Ответственность:
 * - Определение экранов (composable routes) и переходов между ними.
 * - Передача аргументов навигации (submissionId) в экраны.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 *
 * @param navController контроллер навигации
 * @param startDestination начальный маршрут (по умолчанию Login)
 * @param modifier модификатор для NavHost
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.LOGIN,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Экран авторизации
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToMySubmissions = {
                    navController.navigate(Routes.MY_SUBMISSIONS) {
                        // Убираем Login из стека, чтобы нельзя было вернуться
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // Экран «Мои задания»
        composable(Routes.MY_SUBMISSIONS) {
            MySubmissionsScreen(
                onNavigateToSubmit = {
                    navController.navigate(Routes.SUBMIT_ASSIGNMENT)
                },
                onNavigateToResults = { submissionId ->
                    navController.navigate(Routes.submissionResults(submissionId))
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Экран «Отправить задание»
        composable(Routes.SUBMIT_ASSIGNMENT) {
            SubmitAssignmentScreen(
                onNavigateBack = { navController.popBackStack() },
                onSubmitSuccess = {
                    navController.navigate(Routes.MY_SUBMISSIONS) {
                        popUpTo(Routes.MY_SUBMISSIONS) { inclusive = true }
                    }
                }
            )
        }

        // Экран «Результаты проверки» с аргументом submissionId
        composable(
            route = Routes.SUBMISSION_RESULTS,
            arguments = listOf(
                navArgument("submissionId") { type = NavType.StringType }
            )
        ) {
            SubmissionResultsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
