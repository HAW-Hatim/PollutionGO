package com.example.myapplication.presentation.composables.helperClasses


import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.example.myapplication.presentation.composables.pages.MainScreen
import com.example.myapplication.presentation.composables.pages.FinishScreen
import com.example.myapplication.presentation.permissions.FinishViewModel

const val myURI = "https://pollutionGo-results.com"
@Composable
fun Navigation(
    onBluetoothStateChanged:()->Unit,
    viewModel: FinishViewModel = hiltViewModel()

) {

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.MapScreen.route){

        composable(Screen.MapScreen.route){

            MainScreen(
                navController,
                onBluetoothStateChanged = onBluetoothStateChanged
            )
        }



        composable(Screen.ResultScreen.route,
            deepLinks = listOf(navDeepLink { uriPattern = myURI })
        )
        {
            FinishScreen(navController, viewModel.sharedData)
        }
    }

}

sealed class Screen(val route:String){
    object MapScreen:Screen("start_screen")
    object ResultScreen:Screen("result_screen")
}

