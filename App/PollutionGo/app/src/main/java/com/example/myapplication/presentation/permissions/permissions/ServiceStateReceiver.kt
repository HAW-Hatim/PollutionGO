package com.example.myapplication.presentation.permissions.permissions

import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.myapplication.services.LocationService

@Composable
fun ServiceStateReceiver(
    onServiceStateChanged: (String) -> Unit
) {
    SystemBroadcastReceiver(
        systemAction = LocationService.ACTION_SERVICE_STATE_CHANGED,
        onSystemEvent = { intent ->
            intent?.let {
                val state = intent.getStringExtra(LocationService.EXTRA_STATE)
                state?.let { newState ->
                    onServiceStateChanged(newState)
                    Log.d("ServiceStateReceiver", "Service state changed to: $newState")
                }
            }
        }
    )
}