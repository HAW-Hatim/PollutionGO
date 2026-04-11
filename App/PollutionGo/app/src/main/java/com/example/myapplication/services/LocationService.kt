package com.example.myapplication.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.PendingIntentCompat.send
import androidx.core.net.toUri
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.presentation.composables.helperClasses.CurrentLocationRepository
import com.example.myapplication.presentation.composables.helperClasses.myURI
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.maplibre.compose.gms.rememberFusedLocationProvider
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {
    @Inject
    lateinit var currentLocationRepo: CurrentLocationRepository
    companion object {
        const val CHANNEL_ID = "LocationServiceChannel"
        const val NOTIFICATION_ID = 1

        // Actions for controlling the service
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"

        // Keys for intent extras
        const val KEY_SERVICE_STATE = "service_state"

        // Service states
        const val STATE_RUNNING = "running"
        const val STATE_PAUSED = "paused"
        const val STATE_STOPPED = "stopped"

        // Broadcast constants
        const val ACTION_SERVICE_STATE_CHANGED = "com.example.myapplication.ACTION_SERVICE_STATE_CHANGED"
        const val EXTRA_STATE = "extra_state"

        // Helper function to create start intent
        fun createStartIntent(context: Context): Intent {
            return Intent(context, LocationService::class.java).apply {
                action = ACTION_START
            }
        }
    }

    private lateinit var locationClient: FusedLocationProviderClient

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                currentLocationRepo.updateLocation(location)
                Log.d("LocationService", "Lat: ${location.latitude}, Lng: ${location.longitude}")
            }
        }
    }

    private var isRunning = false
    private var isPaused = false
    private lateinit var notificationManager: NotificationManager

//TODO Adjust request so it matches the maplibre request

    val locationRequest =LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
        .setMinUpdateIntervalMillis(1000)   // fastest interval (optional, defaults to interval)
        .setMaxUpdateDelayMillis(0)         // no batching (default 0)
        .build()

    override fun onCreate() {
        super.onCreate()
        locationClient = LocationServices.
        getFusedLocationProviderClient(this)



        Log.d("LocationService", "Service onCreate()")
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("LocationService", "onStartCommand with action: ${intent?.action}")

        when (intent?.action) {
            ACTION_START -> {
                startForegroundService()
                return START_STICKY
            }
            ACTION_PAUSE -> {
                pauseService()
                return START_STICKY
            }
            ACTION_RESUME -> {
                resumeService()
                return START_STICKY
            }
            ACTION_STOP -> {
                val launchFinish = intent.getBooleanExtra("LAUNCH_FINISH", false)
                stopForegroundService()

                if (launchFinish) {
                    // Open MainActivity with a deep link to the finish screen
                    val deepLinkIntent = Intent(Intent.ACTION_VIEW, myURI.toUri(), this, MainActivity::class.java).apply {
                        setFlags( Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }



                    startActivity(deepLinkIntent)
                }
                return START_NOT_STICKY
            }
            else -> {
                // If service was started without action, start it
                startForegroundService()
                return START_STICKY
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun startForegroundService() {
        isRunning = true
        isPaused = false

        // Broadcast state change BEFORE starting foreground
        broadcastServiceState(STATE_RUNNING)

        locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        startForeground(NOTIFICATION_ID, createNotification(STATE_RUNNING), FOREGROUND_SERVICE_TYPE_LOCATION)
        Log.d("LocationService", "Service started")
    }

    private fun pauseService() {
        isPaused = true

        // Broadcast state change
        broadcastServiceState(STATE_PAUSED)

        updateNotification(STATE_PAUSED)
        Log.d("LocationService", "Service paused")
    }

    private fun resumeService() {
        isPaused = false

        // Broadcast state change
        broadcastServiceState(STATE_RUNNING)

        updateNotification(STATE_RUNNING)
        Log.d("LocationService", "Service resumed")
    }

    private fun stopForegroundService() {
        isRunning = false
        isPaused = false

        // Broadcast state change BEFORE stopping
        broadcastServiceState(STATE_STOPPED)

        stopForeground(STOP_FOREGROUND_REMOVE)

        stopSelf()
        Log.d("LocationService", "Service stopped")
    }

    private fun broadcastServiceState(state: String) {
        val intent = Intent(ACTION_SERVICE_STATE_CHANGED).apply {
            putExtra(EXTRA_STATE, state)
        }

        // For Android 14+, specify package name
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.setPackage(packageName)
        }

        // Use sendBroadcast with flags for Android 8.0+
        sendBroadcast(intent, null)
        Log.d("LocationService", "Broadcast sent: $state")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Air Quality Measurement",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Shows when measurement service is running"
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(null, null)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
            Log.d("LocationService", "Notification channel created")
        }
    }

    private fun createNotification(state: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(KEY_SERVICE_STATE, state)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (title, contentText) = when (state) {
            STATE_RUNNING -> Pair(
                "Air Quality - Running",
                "Collecting sensor data..."
            )
            STATE_PAUSED -> Pair(
                "Air Quality - Paused",
                "Tap to resume collection"
            )
            else -> Pair(
                "Air Quality Service",
                "Service running"
            )
        }

        // Create action buttons based on state
        val actions = mutableListOf<NotificationCompat.Action>()

        if (state == STATE_RUNNING) {
            // Pause button
            val pauseIntent = Intent(this, LocationService::class.java).apply {
                action = ACTION_PAUSE
            }
            val pausePendingIntent = PendingIntent.getService(
                this,
                2,
                pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            actions.add(
                NotificationCompat.Action.Builder(
                    R.drawable.pause_24px,
                    "Pause",
                    pausePendingIntent
                ).build()
            )
        } else if (state == STATE_PAUSED) {
            // Resume button
            val resumeIntent = Intent(this, LocationService::class.java).apply {
                action = ACTION_RESUME
            }
            val resumePendingIntent = PendingIntent.getService(
                this,
                3,
                resumeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            actions.add(
                NotificationCompat.Action.Builder(
                    R.drawable.play_arrow_24px,
                    "Resume",
                    resumePendingIntent
                ).build()
            )
        }


        // Always add stop button
        // New (starts an activity)
        val stopIntent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = myURI.toUri()               // your deep link URI
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("STOP_SERVICE", true)      // extra to tell the activity to stop the service
        }
        val stopPendingIntent = PendingIntent.getActivity(this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        actions.add(
            NotificationCompat.Action.Builder(
                R.drawable.stop_24px,
                "Stop",
                stopPendingIntent
            ).build()
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setAutoCancel(false)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .apply {
                // Add action buttons
                actions.forEach { addAction(it) }
            }
            .build()
    }

    private fun updateNotification(state: String) {
        notificationManager.notify(NOTIFICATION_ID, createNotification(state))
        Log.d("LocationService", "Notification updated to: $state")
    }

    override fun onDestroy() {
        // Broadcast stopped state on destroy
        broadcastServiceState(STATE_STOPPED)
        super.onDestroy()
        Log.d("LocationService", "Service destroyed")
    }
}