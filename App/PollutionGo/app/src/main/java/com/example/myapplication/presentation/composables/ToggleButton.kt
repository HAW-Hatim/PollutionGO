package com.example.myapplication.presentation.composables

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.presentation.composables.helperClasses.Screen
import com.example.myapplication.presentation.permissions.AirQualityViewModel
import com.example.myapplication.services.LocationService

@Composable
fun ToggleButton(
    measurementStarted: Boolean,
    toggleMeasurementStart: (Boolean) -> Unit,
    measurementRunning: Boolean,
    toggleMeasurementRunning: (Boolean) -> Unit,
    disconnect: () -> Unit,
    reconnect: () -> Unit,
    navController: NavController,
    context: Context,
    viewModel: AirQualityViewModel,
    onActivityStart: (() -> Unit)? = null
) {
    // Get service state
    val serviceState by viewModel.serviceState.collectAsState()
    val isServiceRunning = serviceState == LocationService.STATE_RUNNING
    val isServicePaused = serviceState == LocationService.STATE_PAUSED
    val isServiceStopped = serviceState == LocationService.STATE_STOPPED

    Row(
        modifier = Modifier.wrapContentWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Main button (play/pause)
        IconButton(
            onClick = {
                if (isServiceStopped) {
                    // Service not started yet - start it
                    viewModel.startForegroundService(context)
                    reconnect()
                    toggleMeasurementStart(true)
                    toggleMeasurementRunning(true)
                    onActivityStart?.invoke()
                } else {
                    // Service already started - toggle pause/resume
                    if (isServiceRunning) {
                        // Currently running, pause it
                        viewModel.pauseForegroundService(context)
                        toggleMeasurementRunning(false)
                    } else if (isServicePaused) {
                        // Currently paused, resume it
                        viewModel.resumeForegroundService(context)
                        toggleMeasurementRunning(true)
                    }
                }
            },
            modifier = Modifier.indication(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
        ) {
            Icon(
                painter = if (isServiceStopped) {
                    // Not started - show play button
                    painterResource(R.drawable.play_arrow_24px)
                } else if (isServiceRunning) {
                    // Running - show pause button
                    painterResource(R.drawable.pause_24px)
                } else {
                    // Paused - show play button (to resume)
                    painterResource(R.drawable.play_arrow_24px)
                },
                contentDescription = if (isServiceStopped) {
                    "Start measurement"
                } else if (isServiceRunning) {
                    "Pause measurement"
                } else {
                    "Resume measurement"
                }
            )
        }

        // Stop button - only show when service is started (running or paused)
        if (!isServiceStopped) {
            IconButton(
                onClick = {
                    // Stop the service
                    viewModel.stopForegroundService(context)

                    // Navigate to results screen
                    navController.navigate(Screen.ResultScreen.route) {
                        popUpTo(Screen.MapScreen.route) {
                            inclusive = true
                        }
                    }

                    // Disconnect BLE
                    disconnect()

                    // Update local states
                    toggleMeasurementStart(false)
                    toggleMeasurementRunning(false)
                },
                modifier = Modifier.indication(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.stop_24px),
                    contentDescription = "Stop measurement",
                    modifier = Modifier.background(Color.White)
                )
            }
        }
    }
}