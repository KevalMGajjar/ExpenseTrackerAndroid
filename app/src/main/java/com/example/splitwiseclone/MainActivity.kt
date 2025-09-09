package com.example.splitwiseclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.splitwiseclone.ui.theme.SplitWiseCloneTheme
import com.example.splitwiseclone.ui.ui_components.*
import com.example.splitwiseclone.ui.ui_components.homeui_com.ActivityUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.AddExpenseUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.DashBoardUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.FriendsUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.GroupsUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.add_new.AddNewFriendUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.add_new.AddNewGroupMemberUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.add_new.AddNewGroupUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.expense_ui.*
import com.example.splitwiseclone.ui.ui_components.homeui_com.friend_profile_ui.FriendOuterProfileUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.friend_profile_ui.FriendSettingsScreen
import com.example.splitwiseclone.ui.ui_components.homeui_com.group_profile_ui.GroupOuterProfileUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.group_profile_ui.GroupSettingsScreen
import com.example.splitwiseclone.ui.ui_components.homeui_com.friend_profile_ui.SettleUpScreen
import com.example.splitwiseclone.ui.ui_components.homeui_com.group_profile_ui.EditGroupMembersScreen
import com.example.splitwiseclone.ui_viewmodels.SplashScreenViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                splashViewModel.isLoggedIn.value == null
            }
        }

        setContent {
            SplitWiseCloneTheme {
                val isLoggedIn by splashViewModel.isLoggedIn.collectAsState()
                AppNavigation(isLoggedIn = isLoggedIn)
            }
        }
    }
}

@Composable
fun AppNavigation(isLoggedIn: Boolean?) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") { SplashScreen(navController = navController, isLoggedIn = isLoggedIn) }
        composable("welcome") { WelcomeUi(navController) }
        composable("signup") { SignUpUi(navController) }
        composable("login") { LoginUi(navController) }
        composable("addPhoneNumberUi") { AddPhoneNumberScreen(navController) }
        composable("dashboard") { MainScreenWithBottomNav(mainNavController = navController) }

        // --- FIX: Create a nested navigation graph for the expense flows ---
        navigation(startDestination = "addExpense?groupId={groupId}", route = "expense_flow") {
            composable(
                route = "addExpense?groupId={groupId}",
                arguments = listOf(navArgument("groupId") { type = NavType.StringType; nullable = true })
            ) { AddExpenseUi(navController) }

            composable(
                route = "expenseEdit/{expenseId}",
                arguments = listOf(navArgument("expenseId") { type = NavType.StringType })
            ) { ExpenseEditScreen(navController) }

            composable("twoPersonExpenseUi/{friendId}", arguments = listOf(navArgument("friendId") { type = NavType.StringType })) { backStackEntry ->
                val friendId = backStackEntry.arguments?.getString("friendId"); requireNotNull(friendId)
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry("expense_flow") }
                TwoPersonExpenseUi(navController, friendId, parentEntry)
            }
            composable("customPBUUi") {
                val parentEntry = remember(it) { navController.getBackStackEntry("expense_flow") }
                CustomPBUUi(navController, parentEntry)
            }
            composable("customPaidByMultipleUi") {
                val parentEntry = remember(it) { navController.getBackStackEntry("expense_flow") }
                CustomPaidByMultipleUi(navController, parentEntry)
            }
            composable("customSplitUi") {
                val parentEntry = remember(it) { navController.getBackStackEntry("expense_flow") }
                CustomSplitScreen(navController, parentEntry)
            }
        }

        // --- Other Detail Screens ---
        composable("addNewGroupUi") { AddNewGroupUi(navController) }
        composable("addNewFriendUi") { AddNewFriendUi(navController) }
        composable("addNewGroupMemberUi/{groupId}", arguments = listOf(navArgument("groupId") { type = NavType.StringType })) { AddNewGroupMemberUi(navController) }
        composable("friendsOuterProfileUi/{friendId}", arguments = listOf(navArgument("friendId") { type = NavType.StringType })) { FriendOuterProfileUi(navController) }
        composable("friendSettingsUi/{friendId}", arguments = listOf(navArgument("friendId") { type = NavType.StringType })) { FriendSettingsScreen(navController) }
        composable("groupsOuterProfileUi/{groupId}", arguments = listOf(navArgument("groupId") { type = NavType.StringType })) { GroupOuterProfileUi(navController) }
        composable("groupSettingsUi/{groupId}", arguments = listOf(navArgument("groupId") { type = NavType.StringType })) { GroupSettingsScreen(navController) }
        composable("settleUp/{friendId}", arguments = listOf(navArgument("friendId") { type = NavType.StringType })) { SettleUpScreen(navController) }
        composable("expenseDetail/{expenseId}", arguments = listOf(navArgument("expenseId") { type = NavType.StringType })) { ExpenseDetailScreen(navController) }
        composable("profileUi") { ProfileUi(navController) }
        composable("editProfile") { EditProfileUi(navController) }
        composable("editGroupMembers/{groupId}", arguments = listOf(navArgument("groupId") { type = NavType.StringType })) { EditGroupMembersScreen(navController) }
    }
}

@Composable
fun MainScreenWithBottomNav(mainNavController: NavHostController) {
    val nestedNavController = rememberNavController()
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                nestedNavController = nestedNavController,
                mainNavController = mainNavController
            )
        }
    ) { padding ->
        NavHost(navController = nestedNavController, startDestination = BottomBarScreen.DashBoard.route, modifier = Modifier.padding(padding)) {
            composable(BottomBarScreen.DashBoard.route) { DashBoardUi(mainNavController) }
            composable(BottomBarScreen.Friends.route) { FriendsUi(mainNavController) }
            composable(BottomBarScreen.Groups.route) { GroupsUi(mainNavController) }
            composable(BottomBarScreen.Activity.route) { ActivityUi(mainNavController) }
        }
    }
}

sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: @Composable () -> Unit
) {
    object DashBoard : BottomBarScreen(
        route = "dashboard_main",
        title = "Home",
        icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") }
    )
    object Friends : BottomBarScreen(
        route = "friendsUi",
        title = "Friends",
        icon = { Icon(Icons.Default.Person, contentDescription = "Friends") }
    )
    object Groups : BottomBarScreen(
        route = "groupsUi",
        title = "Groups",
        icon = { Icon(painter = painterResource(id = R.drawable.group_24dp_e3e3e3_fill0_wght400_grad0_opsz24), contentDescription = "Groups") }
    )
    object Activity : BottomBarScreen(
        route = "activity",
        title = "Activity",
        icon = { Icon(Icons.Default.Notifications, contentDescription = "Activity") }
    )
    object Expense : BottomBarScreen(
        route = "addExpense",
        title = "Expense",
        icon = { Icon(Icons.Default.AddCircle, contentDescription = "Add Expense") }
    )
}

@Composable
fun BottomNavigationBar(nestedNavController: NavHostController, mainNavController: NavHostController) {
    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val items = listOf(
        BottomBarScreen.DashBoard,
        BottomBarScreen.Friends,
        BottomBarScreen.Expense,
        BottomBarScreen.Groups,
        BottomBarScreen.Activity
    )

    NavigationBar {
        items.forEach { screen ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            NavigationBarItem(
                icon = { screen.icon() },
                label = { Text(text = screen.title) },
                selected = isSelected,
                onClick = {
                    if (screen.route == "addExpense") {
                        mainNavController.navigate("addExpense?groupId=null")
                    } else {
                        nestedNavController.navigate(screen.route) {
                            popUpTo(nestedNavController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}