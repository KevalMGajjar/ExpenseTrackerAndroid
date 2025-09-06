package com.example.splitwiseclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.splitwiseclone.central.SyncViewModel
import com.example.splitwiseclone.rest_api.api_viewmodels.ExpenseApiViewModel
import com.example.splitwiseclone.rest_api.api_viewmodels.FriendApiViewModel
import com.example.splitwiseclone.rest_api.api_viewmodels.GroupApiViewModel
import com.example.splitwiseclone.rest_api.api_viewmodels.UserApiViewModel
import com.example.splitwiseclone.roomdb.expense.ExpenseRoomViewModel
import com.example.splitwiseclone.roomdb.friends.FriendsRoomViewModel
import com.example.splitwiseclone.roomdb.groups.GroupRoomViewModel
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel
import com.example.splitwiseclone.ui.theme.SplitWiseCloneTheme
import com.example.splitwiseclone.ui.ui_components.AddExpenseUi
import com.example.splitwiseclone.ui.ui_components.AddPhoneNumberScreen
import com.example.splitwiseclone.ui.ui_components.homeui_com.DashBoardUi
import com.example.splitwiseclone.ui.ui_components.EditProfileUi
import com.example.splitwiseclone.ui.ui_components.LoginUi
import com.example.splitwiseclone.ui.ui_components.ProfileUi
import com.example.splitwiseclone.ui.ui_components.SignUpUi
import com.example.splitwiseclone.ui.ui_components.SplashScreen
import com.example.splitwiseclone.ui.ui_components.WelcomeUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.ActivityUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.FriendsUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.GroupsUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.add_new.AddNewFriendUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.add_new.AddNewGroupMemberUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.add_new.AddNewGroupUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.expense_ui.CustomPBUUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.expense_ui.CustomPaidByMultipleUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.expense_ui.CustomSplitUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.expense_ui.ExpenseDetailUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.expense_ui.ExpenseEditUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.expense_ui.TwoPersonExpenseUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.friend_profile_ui.FriendOuterProfileUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.friend_profile_ui.FriendSettingsUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.group_profile_ui.GroupOuterProfileUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.group_profile_ui.GroupSettingsUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.friend_profile_ui.SettleUpScreen
import com.example.splitwiseclone.ui_viewmodels.AddExpenseViewModel
import com.example.splitwiseclone.ui_viewmodels.AddFriendViewModel
import com.example.splitwiseclone.ui_viewmodels.AddGroupMemberViewModel
import com.example.splitwiseclone.ui_viewmodels.FriendsUiViewModel
import com.example.splitwiseclone.ui_viewmodels.GroupViewModel
import com.example.splitwiseclone.ui_viewmodels.LoginViewModel
import com.example.splitwiseclone.ui_viewmodels.PaidByViewModel
import com.example.splitwiseclone.ui_viewmodels.SignUpViewModel
import com.example.splitwiseclone.ui_viewmodels.SplashScreenViewModel
import com.example.splitwiseclone.ui_viewmodels.SplitOptionsViewModel
import com.example.splitwiseclone.ui_viewmodels.TwoPersonExpenseViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val navController = rememberNavController()
            SplitWiseCloneTheme {
                MainUi(navController)
            }
        }
    }
}
@Composable
fun MainUi(navHostController: NavHostController) {
    val signUpViewModel: SignUpViewModel = viewModel()
    val userApiViewModel: UserApiViewModel = viewModel()
    val loginViewModel: LoginViewModel = viewModel()
    val addExpenseViewModel: AddExpenseViewModel = viewModel()
    val friendRoomViewModel: FriendsRoomViewModel = viewModel()
    val currentUserViewModel: CurrentUserViewModel = viewModel()
    val friendsUiViewModel: FriendsUiViewModel = viewModel()
    val friendApiViewModel: FriendApiViewModel = viewModel()
    val groupRoomViewModel: GroupRoomViewModel = viewModel()
    val groupApiViewModel: GroupApiViewModel = viewModel()
    val splashScreenViewModel: SplashScreenViewModel = viewModel()
    val addGroupMemberViewModel: AddGroupMemberViewModel = viewModel()
    val addFriendViewModel: AddFriendViewModel = viewModel()
    val groupViewModel: GroupViewModel = viewModel()
    val expenseRoomViewModel: ExpenseRoomViewModel = viewModel()
    val expenseApiViewModel: ExpenseApiViewModel = viewModel()
    val twoPersonExpenseViewModel: TwoPersonExpenseViewModel = viewModel()
    val syncViewModel: SyncViewModel = viewModel()
    val paidByViewModel: PaidByViewModel = viewModel()
    val splitOptionsViewModel: SplitOptionsViewModel = viewModel()


    Scaffold(
        bottomBar = {
            BottomNavigationBar(navHostController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navHostController,
            startDestination = "splash",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("welcome") {
                WelcomeUi(navHostController, userApiViewModel, currentUserViewModel)
            }
            composable("signup") {
                SignUpUi(navHostController, signUpViewModel, userApiViewModel)
            }
            composable("login") {
                LoginUi(navHostController, loginViewModel, userApiViewModel, syncViewModel)
            }
            composable("dashboard") {
                DashBoardUi(navHostController)
            }
            composable(
                // Define the route with an optional groupId argument
                route = "addExpense?groupId={groupId}",
                arguments = listOf(
                    navArgument("groupId") {
                        type = NavType.StringType
                        nullable = true // Mark it as optional
                    }
                )
            ) {
                AddExpenseUi(navHostController)
            }

            composable("friendsUi") {
                FriendsUi(
                    navHostController,
                    friendRoomViewModel,
                    currentUserViewModel,
                    friendApiViewModel,
                    friendsUiViewModel,
                    expenseApiViewModel
                )
            }
            composable("activity") {
                ActivityUi(navHostController)
            }
            composable("addNewFriendUi") {
                AddNewFriendUi(
                    navHostController,
                    currentUserViewModel,
                    friendApiViewModel,
                    addFriendViewModel
                )
            }
            composable("friendsOuterProfileUi") {
                FriendOuterProfileUi(navHostController, friendsUiViewModel, expenseRoomViewModel, currentUserViewModel)
            }
            composable("groupsUi") {
                GroupsUi(
                    navHostController,
                    groupRoomViewModel,
                    groupApiViewModel,
                    currentUserViewModel,
                    addGroupMemberViewModel,
                    groupViewModel
                )
            }
            composable("addNewGroupUi") {
                AddNewGroupUi(
                    navHostController,
                    groupApiViewModel,
                    groupRoomViewModel,
                    currentUserViewModel
                )
            }
            composable(
                route = "groupsOuterProfileUi/{groupId}",
                arguments = listOf(navArgument("groupId") { type = NavType.StringType })
            ) {
                GroupOuterProfileUi(navHostController)
            }
            composable("profileUi") {
                ProfileUi(navHostController, currentUserViewModel)
            }
            composable("splash") {
                SplashScreen(navHostController)
            }
            composable(
                // Define the route with a *required* groupId argument
                route = "addNewGroupMemberUi/{groupId}",
                arguments = listOf(navArgument("groupId") { type = NavType.StringType })
            ) {
                AddNewGroupMemberUi(
                    navHostController = navHostController
                )
            }
            composable("friendSettingsUi/{friendId}") {
                FriendSettingsUi(navHostController)
            }
            composable(
                route = "groupSettingsUi/{groupId}",
                arguments = listOf(navArgument("groupId") { type = NavType.StringType })
            ) {
                GroupSettingsUi(navController = navHostController)
            }
            composable(
                route = "twoPersonExpenseUi/{friendId}",
                arguments = listOf(navArgument("friendId") { type = NavType.StringType })
            ) { backStackEntry ->
                // Extract the friendId from the current route's arguments
                val friendId = backStackEntry.arguments?.getString("friendId")
                requireNotNull(friendId) { "Friend ID cannot be null for this screen." }

                // FIX: Get the back stack entry for the parent screen ("addExpense").
                // This is the key that allows us to share the ViewModel instance.
                val parentEntry = remember(backStackEntry) {
                    navHostController.getBackStackEntry("addExpense?groupId={groupId}")
                }

                // Pass both the navController, the friendId, and the parent's entry to the UI.
                TwoPersonExpenseUi(
                    navController = navHostController,
                    friendId = friendId,
                    parentEntry = parentEntry
                )
            }
            composable("customPBUUi") {
                // Get the back stack entry for the parent screen
                val parentEntry = remember(it) {
                    navHostController.getBackStackEntry("addExpense?groupId={groupId}")
                }
                // Pass the parent entry to the composable
                CustomPBUUi(navController = navHostController, parentEntry = parentEntry)
            }
            composable("customPaidByMultipleUi") {
                val parentEntry = remember(it) {
                    navHostController.getBackStackEntry("addExpense?groupId={groupId}")
                }
                CustomPaidByMultipleUi(navController = navHostController, parentEntry = parentEntry)
            }
            composable("customSplitUi") {
                val parentEntry = remember(it) {
                    navHostController.getBackStackEntry("addExpense?groupId={groupId}")
                }
                CustomSplitUi(navController = navHostController, parentEntry = parentEntry)
            }

            composable(
                // Define the route with a *required* expenseId argument
                route = "expenseDetail/{expenseId}",
                arguments = listOf(navArgument("expenseId") { type = NavType.StringType })
            ) {
                // The ViewModel will automatically receive the expenseId from the route
                // thanks to Hilt and SavedStateHandle.
                ExpenseDetailUi(navController = navHostController)
            }

            composable(
                route = "expenseEdit/{expenseId}",
                arguments = listOf(navArgument("expenseId") { type = NavType.StringType })
            ) {
                ExpenseEditUi(navController = navHostController, friendsRoomViewModel = friendRoomViewModel, paidByViewModel = paidByViewModel, splitOptionsViewModel = splitOptionsViewModel, twoPersonExpenseViewModel = twoPersonExpenseViewModel, currentUserViewModel = currentUserViewModel)
            }
            composable(
                "settleUp/{friendId}",
                arguments = listOf(navArgument("friendId") { type = NavType.StringType })
            ) {
                SettleUpScreen(navController = navHostController)
            }
            composable("editProfile") {
                EditProfileUi(navController = navHostController)
            }
            composable("addPhoneNumberUi") {
                AddPhoneNumberScreen(navController = navHostController)
            }
        }
    }
}

sealed class BottomBarScreen(val route: String, val title: String, val icon: ImageVector) {

    object DashBoard : BottomBarScreen("dashboard", "Home", Icons.Default.Home)
    object Friends : BottomBarScreen("friendsUi", "Friends", Icons.Default.Person)
    object Expense : BottomBarScreen("addExpense", "Add Expense", Icons.Filled.AddCircle)
    object Groups: BottomBarScreen("groupsUi", "Groups", Icons.Default.Face)
    object Activity: BottomBarScreen("activity", "Activities", Icons.Filled.Notifications)

    companion object {
        val items = listOf(DashBoard, Friends, Expense, Groups, Activity)
    }
}

@Composable
fun BottomNavigationBar(navHostController: NavHostController) {

    val currentRoute = navHostController.currentBackStackEntryAsState().value?.destination?.route

    val noBottomBarRoutes = listOf("login", "welcome", "splash", "signup", "addPhoneNumberUi", "expenseDetailUi", "expenseEditUi", "customSplitUi", "customPaidByMultipleUi", "customPBUUi", "woPersonExpenseUi", "woPersonExpenseUi", "friendSettingsUi", "addPhoneNumberUi", "addNewGroupMemberUi", "addNewFriendUi", "friendsOuterProfileUi", "profileUi", "groupsOuterProfileUi", "addNewGroupUi", "settleUp/{friendId}")

    if(currentRoute !in noBottomBarRoutes) {

        NavigationBar {
            BottomBarScreen.items.forEach { screen ->
                NavigationBarItem(
                    icon = { Icon(imageVector = screen.icon, contentDescription = "screen icon") },
                    label = { Text(text = screen.title) },
                    selected = currentRoute == screen.route,
                    onClick = {
                        if (currentRoute != screen.route) {
                            navHostController.navigate(screen.route) {
                                popUpTo("dashboard") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }
    }
}

