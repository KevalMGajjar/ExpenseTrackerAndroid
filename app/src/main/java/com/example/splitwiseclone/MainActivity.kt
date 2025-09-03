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
import com.example.splitwiseclone.ui.ui_components.AddPhoneNumberUi
import com.example.splitwiseclone.ui.ui_components.LoginUi
import com.example.splitwiseclone.ui.ui_components.ProfileUi
import com.example.splitwiseclone.ui.ui_components.SignUpUi
import com.example.splitwiseclone.ui.ui_components.SplashScreen
import com.example.splitwiseclone.ui.ui_components.WelcomeUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.ActivityUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.AddExpenseUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.DashBoardUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.FriendsUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.GroupsUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.add_new.AddNewFriendUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.add_new.AddNewGroupMemberUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.add_new.AddNewGroupUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.expense_ui.CustomPBSUUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.expense_ui.CustomPaidByMultipleUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.expense_ui.CustomSplitUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.expense_ui.ExpenseDetailUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.expense_ui.ExpenseEditUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.expense_ui.TwoPersonExpenseUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.friend_profile_ui.FriendOuterProfileUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.friend_profile_ui.FriendSettingsUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.group_profile_ui.GroupOuterProfileUi
import com.example.splitwiseclone.ui.ui_components.homeui_com.group_profile_ui.GroupSettingsUi
import com.example.splitwiseclone.ui_viewmodels.AddExpenseViewModel
import com.example.splitwiseclone.ui_viewmodels.AddFriendViewModel
import com.example.splitwiseclone.ui_viewmodels.AddGroupMemberViewModel
import com.example.splitwiseclone.ui_viewmodels.AddPhoneNumberViewModel
import com.example.splitwiseclone.ui_viewmodels.ExpenseDetailViewModel
import com.example.splitwiseclone.ui_viewmodels.ExpenseEditViewModel
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
    val addPhoneNumberViewModel: AddPhoneNumberViewModel = viewModel()
    val addFriendViewModel: AddFriendViewModel = viewModel()
    val groupViewModel: GroupViewModel = viewModel()
    val expenseRoomViewModel: ExpenseRoomViewModel = viewModel()
    val expenseApiViewModel: ExpenseApiViewModel = viewModel()
    val twoPersonExpenseViewModel: TwoPersonExpenseViewModel = viewModel()
    val syncViewModel: SyncViewModel = viewModel()
    val paidByViewModel: PaidByViewModel = viewModel()
    val splitOptionsViewModel: SplitOptionsViewModel = viewModel()
    val expenseEditViewModel: ExpenseEditViewModel = viewModel()
    val expenseDetailViewModel: ExpenseDetailViewModel = viewModel()

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
            composable("addExpense") {
                AddExpenseUi(navHostController, addExpenseViewModel, expenseApiViewModel, expenseRoomViewModel, currentUserViewModel, friendRoomViewModel, twoPersonExpenseViewModel, paidByViewModel, splitOptionsViewModel)
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
                ActivityUi()
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
            composable("groupsOuterProfileUi") {
                GroupOuterProfileUi(navHostController, groupViewModel)
            }
            composable("profileUi") {
                ProfileUi(navHostController, currentUserViewModel)
            }
            composable("splash") {
                SplashScreen(navHostController, splashScreenViewModel, currentUserViewModel, syncViewModel)
            }
            composable("addNewGroupMemberUi") {
                AddNewGroupMemberUi(navHostController, friendRoomViewModel, addGroupMemberViewModel, groupApiViewModel, groupViewModel, groupRoomViewModel)
            }
            composable("addPhoneNumberUi") {
                AddPhoneNumberUi(navHostController, addPhoneNumberViewModel, userApiViewModel, currentUserViewModel)
            }
            composable("friendSettingsUi") {
                FriendSettingsUi(navHostController, friendsUiViewModel, friendApiViewModel, currentUserViewModel, friendRoomViewModel)
            }
            composable("groupSettingsUi") {
                GroupSettingsUi(navHostController, groupViewModel, groupApiViewModel, groupRoomViewModel, currentUserViewModel)
            }
            composable("twoPersonExpenseUi") {
                TwoPersonExpenseUi(navHostController, twoPersonExpenseViewModel, addExpenseViewModel, friendRoomViewModel)
            }
            composable("customPBUUi") {
                CustomPBSUUi(navHostController, paidByViewModel, addExpenseViewModel)
            }
            composable("customPaidByMultipleUi") {
                CustomPaidByMultipleUi(navHostController, paidByViewModel, addExpenseViewModel)
            }
            composable("customSplitUi") {
                CustomSplitUi(navHostController, splitOptionsViewModel, addExpenseViewModel)
            }
            composable(
                route = "expenseDetail/{expenseId}",
                arguments = listOf(navArgument("expenseId") { type = NavType.StringType })
            ) {
                ExpenseDetailUi(navController = navHostController)
            }

            composable(
                route = "expenseEdit/{expenseId}",
                arguments = listOf(navArgument("expenseId") { type = NavType.StringType })
            ) {
                ExpenseEditUi(navController = navHostController, friendsRoomViewModel = friendRoomViewModel, paidByViewModel = paidByViewModel, splitOptionsViewModel = splitOptionsViewModel, twoPersonExpenseViewModel = twoPersonExpenseViewModel, currentUserViewModel = currentUserViewModel)
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

    val noBottomBarRoutes = listOf("login", "welcome", "splash", "signup", "addPhoneNumberUi", "expenseDetailUi", "expenseEditUi", "customSplitUi", "customPaidByMultipleUi", "customPBUUi", "woPersonExpenseUi", "woPersonExpenseUi", "friendSettingsUi", "addPhoneNumberUi", "addNewGroupMemberUi", "addNewFriendUi", "friendsOuterProfileUi", "profileUi", "groupsOuterProfileUi", "addNewGroupUi")

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

