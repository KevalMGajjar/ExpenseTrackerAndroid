package com.example.splitwiseclone.ui.ui_components

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.splitwiseclone.rest_api.api_viewmodels.UserApiViewModel
import com.example.splitwiseclone.roomdb.user.CurrentUserViewModel
import com.example.splitwiseclone.utils.Constants
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException

@Composable
fun WelcomeUi(navHostController: NavHostController, userApiViewModel: UserApiViewModel, currentUserViewModel: CurrentUserViewModel) {

    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    val loginSuccess by userApiViewModel.loginSuccess.collectAsStateWithLifecycle()
    val currentUser by currentUserViewModel.currentUser.collectAsState()

    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            if(currentUser?.phoneNumber != null) {
                navHostController.navigate("dashboard")
                userApiViewModel.resetLoginStatus()
            }else {
                navHostController.navigate("addPhoneNumberUi")
                userApiViewModel.resetLoginStatus()
            }
        }
    }

    val signInRequest = remember {
        BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(Constants.GOOGLE_WEB_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }

    val activityResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        isLoading = false
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val oneTapClient = Identity.getSignInClient(context)
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    Log.d("LoginUi", "Got Google ID Token: $idToken")
                    userApiViewModel.loginWithGoogle(idToken)
                } else {
                    Log.e("LoginUi", "Google ID Token was null.")
                }
            } catch (e: ApiException) {
                Log.e("LoginUi", "Google Sign-In failed with ApiException", e)
            }
        } else {
            Log.w("LoginUi", "Google Sign-In was cancelled or failed.")
        }
    }

    Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxHeight()) {
        Row(modifier = Modifier.fillMaxWidth().align(alignment = Alignment.CenterHorizontally)) {
            Button(onClick = {navHostController.navigate("signup")},
                modifier = Modifier.fillMaxWidth()) {
                Text(text = "SignUp")
            }
        }
        Row(modifier = Modifier.fillMaxWidth().align(alignment = Alignment.CenterHorizontally)) {
            Button(onClick = {navHostController.navigate("login")},
                modifier = Modifier.fillMaxWidth()) {
                Text(text = "Login")
            }
        }
        Row(modifier = Modifier.fillMaxWidth().align(alignment = Alignment.CenterHorizontally)) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(onClick = {
                    isLoading = true
                    val oneTapClient = Identity.getSignInClient(context)
                    oneTapClient.beginSignIn(signInRequest)
                        .addOnSuccessListener { result ->
                            try {
                                val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                                activityResultLauncher.launch(intentSenderRequest)
                            } catch (e: Exception) {
                                isLoading = false
                                Log.e("LoginUi", "Couldn't start One Tap UI: ${e.localizedMessage}")
                            }
                        }
                        .addOnFailureListener { e ->
                            isLoading = false
                            Log.e("LoginUi", "Google Sign-In begin failed", e)
                        }
                }) {
                    Text("Sign in with Google")
                }
            }
        }
    }

}