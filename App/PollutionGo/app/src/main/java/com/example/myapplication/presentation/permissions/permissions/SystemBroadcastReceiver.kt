package com.example.myapplication.presentation.permissions.permissions

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun SystemBroadcastReceiver(
    systemAction: String,
    onSystemEvent: (intent: Intent?) -> Unit
) {
    val context = LocalContext.current
    val currentOnSystemEvent by rememberUpdatedState(onSystemEvent)

    DisposableEffect(context, systemAction) {
        val intentFilter = IntentFilter(systemAction)
        val broadcast = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, intent: Intent?) {
                currentOnSystemEvent(intent)
            }
        }

        // For Android 14+, we need to specify export behavior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ requires specifying export behavior
            context.registerReceiver(
                broadcast,
                intentFilter,
                Context.RECEIVER_EXPORTED
            )
        } else {
            context.registerReceiver(broadcast, intentFilter)
        }

        onDispose {
            try {
                context.unregisterReceiver(broadcast)
            } catch (e: IllegalArgumentException) {
                // Receiver was not registered, ignore
            }
        }
    }
}