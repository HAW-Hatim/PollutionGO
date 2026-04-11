@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.example.myapplication.presentation.composables.pages


import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.data.ConnectionState
import com.example.myapplication.presentation.composables.Sensordata
import com.example.myapplication.presentation.composables.ToggleButton
import com.example.myapplication.presentation.composables.helperClasses.AirQualityCategory
import com.example.myapplication.presentation.composables.helperClasses.CollectedData
import com.example.myapplication.presentation.composables.helperClasses.PollutantScale
import com.example.myapplication.presentation.composables.helperClasses.SensorType
import com.example.myapplication.presentation.composables.helperClasses.SheetValue
import com.example.myapplication.presentation.composables.helperClasses.StyleValue
import com.example.myapplication.presentation.permissions.AirQualityViewModel
import com.example.myapplication.presentation.permissions.permissions.PermissionUtils
import com.example.myapplication.presentation.permissions.permissions.ServiceStateReceiver
import com.example.myapplication.presentation.permissions.permissions.SystemBroadcastReceiver
import com.example.myapplication.services.LocationService
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.gson.Gson
import io.morfly.compose.bottomsheet.material3.BottomSheetScaffold
import io.morfly.compose.bottomsheet.material3.rememberBottomSheetScaffoldState
import io.morfly.compose.bottomsheet.material3.rememberBottomSheetState
import io.morfly.compose.bottomsheet.material3.requireSheetVisibleHeightDp
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraMoveReason
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.ast.Expression
import org.maplibre.compose.expressions.dsl.Feature
import org.maplibre.compose.expressions.dsl.Feature.get
import org.maplibre.compose.expressions.dsl.asNumber
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.div
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.interpolate
import org.maplibre.compose.expressions.dsl.linear
import org.maplibre.compose.expressions.dsl.not
import org.maplibre.compose.expressions.dsl.plus
import org.maplibre.compose.expressions.value.NumberValue
import org.maplibre.compose.gms.rememberFusedLocationProvider
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.rememberUserLocationState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.material3.LocationPuckDefaults
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.GeoJsonOptions
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalLayoutApi::class, ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    navController: NavController,
    onBluetoothStateChanged: () -> Unit,
    viewModel: AirQualityViewModel = hiltViewModel(),
    context: Context = LocalContext.current
) {
    var isInitialState by rememberSaveable { mutableStateOf(true) }
    var isTrackingLocation by rememberSaveable { mutableStateOf(false) }

    val maskColor: Color = Color.Black.copy(alpha = 0.5f)
    val coroutineScope = rememberCoroutineScope()

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    var measurementStarted by rememberSaveable { mutableStateOf(false) }
    var measurementRunning by rememberSaveable { mutableStateOf(false) }


    // Location information
    // Empty string as start value
    var info by rememberSaveable { mutableStateOf("")}


    var displayClickedFeature by rememberSaveable { mutableStateOf(false) }


// Define scales for each pollutant
    val co2Scale = PollutantScale("CO2", "ppm", listOf(600, 1000, 1500, 2500))
    val pm1Scale = PollutantScale("PM1", "µg/m³", listOf(15, 35, 62, 95))
    val pm25Scale = PollutantScale("PM2.5", "µg/m³", listOf(21, 51, 91, 140))
    val pm10Scale = PollutantScale("PM10", "µg/m³", listOf(31, 76, 126, 200))


    // Rating
    val pollutantRatingMap = mapOf(
        "CO2" to co2Scale,
        "PM1" to pm1Scale,
        "PM2.5" to pm25Scale,
        "PM10" to pm10Scale
    )


    // Radiobutton
    val radioOptions = listOf("CO2", "PM1", "PM2.5", "PM10")
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }

    // Toggle for radio buttons
    var radioVisible by rememberSaveable { mutableStateOf(false) }


    // Location request setup
    val MIN_DISTANCE = 15f
    val minUpdateIntervalMillis = 5000L
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
        .setMinUpdateIntervalMillis(minUpdateIntervalMillis)
        .setMinUpdateDistanceMeters(MIN_DISTANCE)
        .build()

    fun calculateZoomFor50x50Meters(): Double = 17.5

    val sensorDataMap = remember { mutableStateMapOf<SensorType, Float>() }

    // Initial Values for location data
    val locationDataMap = remember { mutableStateMapOf("Latitude" to 0.0, "Longitude" to 0.0) }

    // Export GeoJSON array for the database
    val geoJsonExportList = mutableListOf<CollectedData>()

    //Sensor values are floats, date/time will be a single string
    val allCollectedData = remember {mutableStateMapOf<String, MutableList<Float>>(
        SensorType.Pressure.toString() to mutableListOf(),
        SensorType.Temperature.toString() to mutableListOf(),
        SensorType.Humidity.toString() to mutableListOf(),
        SensorType.PM_1.toString() to mutableListOf(),
        SensorType.PM2_5.toString() to mutableListOf(),
        SensorType.PM_10.toString() to mutableListOf(),
        SensorType.CO2.toString() to mutableListOf()

    )}

    // List of the time we received data -> x-Axis for the plots
    val timeList = mutableListOf<String>()


    // Formatter for time/date
    val formatter_time = DateTimeFormatter.ofPattern("HH:mm:ss")
    val formatter_date = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // Listen for service state changes
    var directServiceState by remember { mutableStateOf(LocationService.STATE_STOPPED) }

    ServiceStateReceiver { newState ->
        directServiceState = newState
        viewModel.updateServiceState(newState)

        // SYNCHRONIZE local UI state with service state
        when (newState) {
            LocationService.STATE_RUNNING -> {
                measurementStarted = true
                measurementRunning = true
            }
            LocationService.STATE_PAUSED -> {
                measurementStarted = true
                measurementRunning = false
            }
            LocationService.STATE_STOPPED -> {
                measurementStarted = false
                measurementRunning = false
            }
        }
    }

    // Combine ViewModel state and direct broadcast state
    val viewModelServiceState by viewModel.serviceState.collectAsState()
    val serviceState = if (directServiceState != LocationService.STATE_STOPPED) {
        directServiceState
    } else {
        viewModelServiceState
    }

    val isServiceRunning = serviceState == LocationService.STATE_RUNNING
    val isServicePaused = serviceState == LocationService.STATE_PAUSED
    val isServiceStopped = serviceState == LocationService.STATE_STOPPED

    // Initialize sensor map once
    LaunchedEffect(Unit) {
        sensorDataMap.putAll(
            SensorType.entries.associateWith { 99999f }
        )

    }

    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Collapsed,
        defineValues = {
            SheetValue.Collapsed at height(87.dp)
            SheetValue.PartiallyExpanded at height(150.dp)
            SheetValue.Expanded at height(302.dp)
        },
        confirmValueChange = {
            if (isInitialState) {
                isInitialState = false
            }
            true
        }
    )
    val scaffoldState = rememberBottomSheetScaffoldState(sheetState)

    val cameraState = rememberCameraState()

    // Add this to detect user-initiated camera movements
    var lastProgrammaticMove by remember { mutableStateOf(false) }
    var lastMoveReason by remember { mutableStateOf<CameraMoveReason?>(null) }

    // Observe camera movement reasons to detect user interaction
    LaunchedEffect(cameraState.moveReason) {
        val currentReason = cameraState.moveReason

        if (currentReason != lastMoveReason) {
            lastMoveReason = currentReason

            // Stop tracking if the user moved the camera (not programmatic animation)
            when (currentReason) {
                CameraMoveReason.GESTURE -> {
                    // User dragged the map
                    if (isTrackingLocation) {
                        isTrackingLocation = false
                        Log.d("TRACKING", "Stopped tracking due to user gesture")
                    }
                }
                CameraMoveReason.PROGRAMMATIC -> {
                    // Our own programmatic animation
                    lastProgrammaticMove = true
                }
                else -> {
                    // For other reasons, we might want to stop tracking too
                    if (!lastProgrammaticMove && isTrackingLocation) {
                        isTrackingLocation = false
                        Log.d("TRACKING", "Stopped tracking due to camera move")
                    }
                    lastProgrammaticMove = false
                }
            }
        }
    }

    SystemBroadcastReceiver(
        systemAction = BluetoothAdapter.ACTION_STATE_CHANGED,
        onSystemEvent = { bluetoothState ->
            val action = bluetoothState?.action ?: return@SystemBroadcastReceiver
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                onBluetoothStateChanged()
            }
        }
    )

    val permissionState = rememberMultiplePermissionsState(permissions = PermissionUtils.permissions)
    val lifecycleOwner = LocalLifecycleOwner.current
    val bleConnectionState by viewModel.connectionState.collectAsState()

    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            Log.d("LifeCycle", "{$event}")

            when (event) {

                Lifecycle.Event.ON_RESUME -> {
                    permissionState.launchMultiplePermissionRequest()
                    if (permissionState.allPermissionsGranted) {
                        when (bleConnectionState) {
                            ConnectionState.Uninitialized -> viewModel.initializeConnection()
                            else -> {}
                        }
                    }
                }


                else -> {}

            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)


        }
    }

    Log.d("HATIM ConnectState", "{$bleConnectionState}")

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            Column(
                Modifier
                    .fillMaxHeight()
                    .clickable { },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {



                if (bleConnectionState == ConnectionState.CurrentlyInitializing) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        if (viewModel.initializationMessage != null) {
                            Text(
                                text = viewModel.initializationMessage!!
                            )
                        }
                    }
                } else if (!permissionState.allPermissionsGranted) {
                    Text(
                        text = "Go to the app setting and allow the missing permissions.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(10.dp),
                        textAlign = TextAlign.Center
                    )
                } else if (viewModel.errorMessage != null) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = viewModel.errorMessage!!
                        )
                        Button(
                            onClick = {
                                if (permissionState.allPermissionsGranted) {
                                    viewModel.initializeConnection()
                                }
                            }
                        ) {
                            Text("Try again")
                        }
                    }


                }
                else if (bleConnectionState == ConnectionState.Disconnected && measurementRunning) {
                    Button(onClick = {
                        viewModel.initializeConnection()
                    }) {
                        Text("Initialize again")
                    }
                }
                else if (bleConnectionState == ConnectionState.Connected || isServiceRunning || isServicePaused) {
                    Log.d("Hatim", "Bluetooth enabled successfully or service is running")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                ToggleButton(
                                    measurementStarted = measurementStarted,
                                    toggleMeasurementStart = { newState ->
                                        measurementStarted = newState
                                    },
                                    measurementRunning = measurementRunning,
                                    toggleMeasurementRunning = { newState ->
                                        measurementRunning = newState
                                    },
                                    disconnect = { viewModel.disconnect() },
                                    reconnect = { viewModel.reconnect() },
                                    navController = navController,
                                    context = context,
                                    viewModel = viewModel,
                                    onActivityStart = {
                                        if (locationPermissionState.status.isGranted) {
                                            isTrackingLocation = true
                                            Log.d("TRACKING", "Auto-started tracking on activity start")
                                        }
                                    }
                                )
                            }
                            Text(
                                text = when {
                                    isServiceRunning -> "Activity Running"
                                    isServicePaused -> "Activity Paused"
                                    else -> "Start Activity"
                                },
                                color = Color.Black,
                                fontSize = 15.sp,
                                style = TextStyle(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                    }
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(thickness = 2.dp)

                    // YOUR ORIGINAL LOGIC RESTORED - but with service state
                    if (measurementStarted && measurementRunning) {
                        Log.d("Vals Updated", "Values have been updated")

                        val currentTemperature = viewModel.temperature
                        val currentPressure = viewModel.pressure
                        val currentHumidity = viewModel.humidity
                        val currentCo2 = viewModel.co2
                        val currentPM1 = viewModel.pm1
                        val currentPM2_5 = viewModel.pm2_5
                        val currentPM10 = viewModel.pm10

                        val currentTime = LocalTime.now().format(formatter_time)


                        LaunchedEffect(measurementStarted, measurementRunning) {
                            sensorDataMap.apply {
                                this[SensorType.Temperature] = viewModel.temperature
                                this[SensorType.Pressure] = viewModel.pressure
                                this[SensorType.Humidity] = viewModel.humidity
                                this[SensorType.CO2] = viewModel.co2
                                this[SensorType.PM_1] = viewModel.pm1
                                this[SensorType.PM2_5] = viewModel.pm2_5
                                this[SensorType.PM_10] = viewModel.pm10
                            }
                        }

                        // Also update directly (same as before)
                        sensorDataMap[SensorType.Temperature] = viewModel.temperature
                        sensorDataMap[SensorType.Pressure] = viewModel.pressure
                        sensorDataMap[SensorType.Humidity] = viewModel.humidity
                        sensorDataMap[SensorType.CO2] = viewModel.co2
                        sensorDataMap[SensorType.PM_1] = viewModel.pm1
                        sensorDataMap[SensorType.PM2_5] = viewModel.pm2_5
                        sensorDataMap[SensorType.PM_10] = viewModel.pm10

                        val currentSensorData = CollectedData(
                            viewModel.temperature,
                            viewModel.humidity,
                            viewModel.pressure,
                            viewModel.pm2_5,
                            viewModel.pm1,
                            viewModel.pm10,
                            viewModel.co2,
                            locationDataMap["Latitude"],
                            locationDataMap["Longitude"],
                            LocalDate.now().format(formatter_date),
                            currentTime
                        )

                        // Adding our freshly collected values to our list to later export to database
                        geoJsonExportList.add(currentSensorData)
                        val gson = Gson()
                        val jsonOutput = gson.toJson(geoJsonExportList)
                        Log.d("Geo JSON", "{$jsonOutput}")

                        // Fill map with our values to use it for the plots on the finish screen
                        allCollectedData[SensorType.Temperature.toString()]?.add(currentTemperature)
                        allCollectedData[SensorType.Pressure.toString()]?.add(currentPressure)
                        allCollectedData[SensorType.Humidity.toString()]?.add(currentHumidity)
                        allCollectedData[SensorType.CO2.toString()]?.add(currentCo2)
                        allCollectedData[SensorType.PM_1.toString()]?.add(currentPM1)
                        allCollectedData[SensorType.PM2_5.toString()]?.add(currentPM2_5)
                        allCollectedData[SensorType.PM_10.toString()]?.add(currentPM10)

                        //Add the current time to our list
                        timeList.add(currentTime)

                    }

                    Box {
                        Sensordata(measurementStarted, StyleValue.BottomSheet, sensorDataMap)

                        if (measurementStarted && !measurementRunning) {
                            Box(
                                modifier = Modifier
                                    .background(maskColor)
                                    .matchParentSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.pause_24px),
                                    tint = Color.Black,
                                    contentDescription = "Pause Image"
                                )
                            }
                        }
                    }
                }  else {
                    //viewModel.initializeConnection()
                    //viewModel.reconnect()
                }

                Column(
                    modifier = Modifier.padding(
                        bottom = 2 * WindowInsets
                            .navigationBarsIgnoringVisibility
                            .asPaddingValues()
                            .calculateBottomPadding()
                    )
                ) {}
            }
        },
        content = {
            val bottomPadding by remember {
                derivedStateOf { sheetState.requireSheetVisibleHeightDp() }
            }
            val isBottomSheetMoving by remember {
                derivedStateOf { sheetState.currentValue != sheetState.targetValue }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                MaplibreMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraState = cameraState,
                    baseStyle = BaseStyle.Uri("https://basemaps.cartocdn.com/gl/voyager-gl-style/style.json"),
                    onMapClick = { position, offset ->
                        // Stop tracking when user clicks the map (not bottom sheet)
                        if (isTrackingLocation) {
                            isTrackingLocation = false
                            Log.d("TRACKING", "Stopped tracking due to map click at $position")
                        }

                        // Reset our clicked feature and set empty value
                        // Basically double safety
                         info = ""
                         displayClickedFeature = false


                        ClickResult.Pass // Allow other click handlers to process
                    }
                ) {
                    //Layer


                    MapLayers(
                        pollutantRatingMap, selectedOption,
                        getFeature = {clickedFeature -> info = clickedFeature},
                        toggleFeatureInfoVisibility = {newState ->
                            displayClickedFeature = newState
                        }
                    )

                    if (locationPermissionState.status.isGranted) {
                        @SuppressLint("MissingPermission")
                        val locationProvider = rememberFusedLocationProvider(locationRequest)
                        val locationState = rememberUserLocationState(locationProvider)

                        LaunchedEffect(locationState) {
                            Log.d("DEBUG", "Location State: $locationState")
                        }

                        if (locationState.location != null) {
                            LocationPuck(
                                idPrefix = "gms-location",
                                locationState = locationState,
                                cameraState = cameraState,
                                accuracyThreshold = Float.POSITIVE_INFINITY,
                                colors = LocationPuckDefaults.colors(),
                            )
                        }

                        // Effect for initial tracking start
                        LaunchedEffect(isTrackingLocation, locationState.location) {
                            if (isTrackingLocation && locationState.location != null) {
                                val targetZoom = calculateZoomFor50x50Meters()
                                val currentPosition = cameraState.position
                                val targetPosition = CameraPosition(
                                    target = locationState.location!!.position,
                                    zoom = targetZoom,
                                    bearing = currentPosition.bearing,
                                    tilt = currentPosition.tilt
                                )

                                lastProgrammaticMove = true
                                coroutineScope.launch {
                                    cameraState.animateTo(targetPosition, duration = 500.milliseconds)
                                    Log.d("TRACKING", "Camera animated to initial position")
                                }
                            }
                        }

                        // Effect for continuous tracking updates
                        LaunchedEffect(locationState.location) {
                            if (isTrackingLocation && locationState.location != null) {
                                val currentPosition = cameraState.position
                                val newPosition = currentPosition.copy(
                                    target = locationState.location!!.position
                                )

                                lastProgrammaticMove = true
                                coroutineScope.launch {
                                    cameraState.animateTo(newPosition, duration = 300.milliseconds)
                                    Log.d(
                                        "TRACKING",
                                        "Camera updated to new position: ${locationState.location!!.position}"
                                    )
                                }
                            }
                        }

                        LaunchedEffect(locationState.location) {
                            locationState.location?.let { loc ->
                                locationDataMap["Latitude"] = loc.position.latitude
                                locationDataMap["Longitude"] = loc.position.longitude
                                Log.d(
                                    "MAP_LOCATION",
                                    "Update: Lat=${locationDataMap["Latitude"]} , Lng=${locationDataMap["Longitude"]}"
                                )
                            }
                        }
                    }
                }

                // Location tracking button
                if (locationPermissionState.status.isGranted) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 70.dp, end = 16.dp)
                    ) {
                        IconButton(
                            onClick = {
                                isTrackingLocation = !isTrackingLocation
                                if (isTrackingLocation) {
                                    Log.d("TRACKING", "Location tracking STARTED by button")
                                } else {
                                    Log.d("TRACKING", "Location tracking STOPPED by button")
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .shadow(4.dp, CircleShape)
                                .clip(CircleShape)
                                .background(
                                    if (isTrackingLocation) Color.Blue.copy(alpha = 0.8f)
                                    else Color.White.copy(alpha = 0.9f)
                                )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_my_location_24),
                                contentDescription = if (isTrackingLocation) "Stop location tracking" else "Start location tracking",
                                tint = if (isTrackingLocation) Color.White else Color.Blue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Column(Modifier.padding(vertical = 25.dp, horizontal = 4.dp)
                    .background(Color.White, shape = RoundedCornerShape(20.dp))
                    .width(150.dp)
                    .wrapContentHeight()) {

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Text(
                            "LEGEND"
                        )
                    }


                    listOf(
                        Triple("Good", Color(0, 218, 0), 0),
                        Triple("Moderate", Color(255, 255, 0), 1),
                        Triple("Polluted", Color(255, 126, 0), 2),
                        Triple("Very Polluted", Color(255, 0, 0), 3),
                        Triple("Severly Polluted", Color(126, 0, 60), 4)
                    ).forEach { (rating, color, index) ->
                        Row(
                            Modifier.padding(vertical = 4.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(color)
                                    .width(15.dp)
                                    .height(15.dp)

                            )
                            Text(
                                modifier = Modifier.padding(start = 5.dp),
                                text = rating + "\n" + pollutantRatingMap[selectedOption]?.getRangeString(
                                    index
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )

                        }
                    }


                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.background(shape = RoundedCornerShape(12.dp), color = Color.Black).clickable(
                            onClick = {
                                    radioVisible = !radioVisible

                            }, indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            enabled = true,
                            onClickLabel = "Upload Button",
                            role = null
                        )){

                            Row(
                                modifier = Modifier
                                    .wrapContentWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            )
                            {
                                if(radioVisible){Icon(
                                    painter = painterResource(id = R.drawable.outline_arrow_drop_down_24),
                                    contentDescription = "Upload Button",
                                    tint = Color.White
                                )}
                                else {
                                    Icon(
                                        painter = painterResource(id = R.drawable.outline_arrow_drop_up_24),
                                        contentDescription = "Upload Button",
                                        tint = Color.White
                                    )
                                }




                            }

                        }

                    }
                    if (radioVisible) {
                        Column(Modifier.selectableGroup()) {
                            radioOptions.forEach { text ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .selectable(
                                            selected = (text == selectedOption),
                                            onClick = { onOptionSelected(text) },
                                            role = Role.RadioButton
                                        ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(

                                        selected = (text == selectedOption),
                                        onClick = null // null recommended for accessibility with screen readers
                                    )
                                    Text(
                                        text = text,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 5.dp)
                                    )
                                }
                            }
                        }


                    }


                }



            }

            // TODO: Still need to see if this makes any sense to implement, focus on shippable probduct
            //  -> Feature: get information from a clicked location
            if(false)


                Box {
                    val textMeasurer = rememberTextMeasurer()

                    Canvas(
                        modifier = Modifier
                            .size(250.dp)
                            .padding(40.dp)
                    ) {
                        val trianglePath = Path().let {
                            it.moveTo(this.size.width * .40f, 0f)
                            it.lineTo(this.size.width * .50f, -30f)
                            it.lineTo(this.size.width * .60f, 0f)
                            it.close()
                            it
                        }
                        drawRoundRect(
                            Color.LightGray,
                            size = Size(this.size.width, this.size.height * 0.95f),
                            cornerRadius = CornerRadius(60f)
                        )
                        drawPath(
                            path = trianglePath,
                            Color.LightGray,
                        )
                        drawText(text = info,
                            topLeft = Offset(40f, 40f),
                            textMeasurer = textMeasurer)
                    }

                }



        },
        modifier = Modifier.fillMaxSize(),
    )


}

// TODO Adjust geojsonoptions and heatmaplayer -> currently 24.01.2026 basically just a PoC
@Composable fun MapLayers(pollutantRatingMap: Map<String, PollutantScale>, selectedOption: String,
                          getFeature: (String) -> Unit, toggleFeatureInfoVisibility: (Boolean) -> Unit
) {

    // List to filter non relevant information for user
    val entryFilter = listOf("zeit", "id")


    // selectedOption: "CO2", "PM1", "PM2.5", "PM10"
    val inputString = when(selectedOption){
        "CO2" -> "co2"
        "PM1" -> "pm1_0"
        "PM2.5" -> "pm2_5"
        "PM10" -> "pm10"


        else -> {}
    }

    val inputStringCluster = when(selectedOption){
        "CO2" -> "sum_co2"
        "PM1" -> "sum_pm1_0"
        "PM2.5" -> "sum_pm2_5"
        "PM10" -> "sum_pm10"


        else -> {}
    }


    val airquality_source = rememberGeoJsonSource(GeoJsonData.Uri("https://pollutiongo.org/download"),
        GeoJsonOptions(cluster = true,  clusterRadius = 5,
            clusterProperties = mapOf(
                // Sum of PM2.5 values in the cluster
                "sum_pm2_5" to GeoJsonOptions.ClusterPropertyAggregator(
                    mapper = feature["pm2_5"].asNumber(),
                    reducer = feature["sum_pm2_5"].asNumber() + feature.accumulated().asNumber()
                ),
                // Sum of PM10 values
                "sum_pm10" to GeoJsonOptions.ClusterPropertyAggregator(
                    mapper = feature["pm10"].asNumber(),
                    reducer = feature["sum_pm10"].asNumber() + feature.accumulated().asNumber()
                ),
                // Sum of PM1 values
                "sum_pm1_0" to GeoJsonOptions.ClusterPropertyAggregator(
                    mapper = feature["pm1_0"].asNumber(),
                    reducer = feature["sum_pm1_0"].asNumber() + feature.accumulated().asNumber()
                ),
                // Sum of CO2 values
                "sum_co2" to GeoJsonOptions.ClusterPropertyAggregator(
                    mapper = feature["co2"].asNumber(),
                    reducer = feature["sum_co2"].asNumber() + feature.accumulated().asNumber()
                )

            )
        )
        )


    Log.d("CircleLayer", airquality_source.attributionHtml)
    pollutantRatingMap[selectedOption]?.let {
        CircleLayer("some_id", airquality_source,
            filter = Feature.has("point_count"),
            color = interpolate(
                linear(),

                // 'get' is an expression to fetch a property from the feature
                input = (get(inputStringCluster as String) as Expression<NumberValue<Number>>).div(get("point_count") as Expression<NumberValue<Number>>),
                // Define stops that map PM2.5 values to AQI colors
                it.getThreshold(AirQualityCategory.GOOD) to AirQualityCategory.GOOD.rgbColor,
                it.getThreshold(AirQualityCategory.MODERATE) to AirQualityCategory.MODERATE.rgbColor,
                it.getThreshold(AirQualityCategory.POLLUTED) to AirQualityCategory.POLLUTED.rgbColor,
                it.getThreshold(AirQualityCategory.VERY_POLLUTED) to AirQualityCategory.VERY_POLLUTED.rgbColor,
                it.getThreshold(AirQualityCategory.SEVERELY_POLLUTED) to AirQualityCategory.SEVERELY_POLLUTED.rgbColor
            ),
            radius = const(8.dp), // A fixed radius for all circles
            // You can also make radius dynamic with interpolate and zoom()
            //TODO Possible to create little info box of a selected data point
            strokeColor = const(Color.Transparent), // Optional outline
            strokeWidth = const(1.dp),
            opacity = const(0.8f),
            onClick = {features ->
                Log.d("onClick Circle",                        features[0].properties.toString()
                )
                // Set variable to feature to use it for composable -> Can't invoke in here
                ClickResult.Pass
            },
            blur = const(0.2f)

        )

        CircleLayer("some_id_other", airquality_source,
            filter = Feature.has("point_count").not(),
            color = interpolate(
                linear(),

                // 'get' is an expression to fetch a property from the feature
                input = get(inputString as String) as Expression<NumberValue<Number>>,
                // Define stops that map values to AQI colors
                it.getThreshold(AirQualityCategory.GOOD) to AirQualityCategory.GOOD.rgbColor,
                it.getThreshold(AirQualityCategory.MODERATE) to AirQualityCategory.MODERATE.rgbColor,
                it.getThreshold(AirQualityCategory.POLLUTED) to AirQualityCategory.POLLUTED.rgbColor,
                it.getThreshold(AirQualityCategory.VERY_POLLUTED) to AirQualityCategory.VERY_POLLUTED.rgbColor,
                it.getThreshold(AirQualityCategory.SEVERELY_POLLUTED) to AirQualityCategory.SEVERELY_POLLUTED.rgbColor
            ),
            radius = const(8.dp), // A fixed radius for all circles
            // You can also make radius dynamic with interpolate and zoom()
            //TODO Possible to create little info box of a selected data point
            strokeColor = const(Color.Transparent), // Optional outline
            strokeWidth = const(1.dp),
            opacity = const(0.67f),
            onClick = {features ->
                Log.d("onClick Circle",                        features[0].properties.toString()
                )
                // Need to remove all quotation marks for anything that is saved as a string
                var featureString = ""
                features[0].properties?.forEach { entry ->
                    // Skip entries which are inside filter
                    if(entryFilter.contains(entry.key))
                        return@forEach
                    featureString += entry.key
                    featureString += ": "
                    featureString += entry.value.toString().replace("\"", "")
                    featureString += "\n"
                }
                //Remove the last new Line
                featureString.dropLast(1)
                getFeature(featureString)

                toggleFeatureInfoVisibility(true)
                ClickResult.Pass
            }

        )




        // Heatmap might not be the proper tool
        /*
        HeatmapLayer(id = "test_id", source = airquality_source,
            radius =
                interpolate(
                    linear(),
                    zoom(),
                    0 to const(10.dp), 9 to const(30.dp)),
            weight = interpolate(
                linear(),
                input = get(input_string) as Expression<NumberValue<Number>>,
                it.getThreshold(AirQualityCategory.GOOD) to const(0),
                it.getThreshold(AirQualityCategory.MODERATE) to const(0.25f),
                it.getThreshold(AirQualityCategory.POLLUTED) to const(0.5f),
                it.getThreshold(AirQualityCategory.VERY_POLLUTED) to const(0.75f),
                it.getThreshold(AirQualityCategory.SEVERELY_POLLUTED) to const(1)
            ),
            color = interpolate(
                linear(),
                input = heatmapDensity(),
                0.0 to rgbColor(const(0), const(0), const(0), const(0)),
                0.2 to rgbColor(const(0), const(228), const(0)),
                0.4 to rgbColor(const(255), const(255), const(0)),
                0.6 to rgbColor(const(255), const(126), const(0)),
                0.8 to rgbColor(const(255), const(0), const(0)),
                0.9 to rgbColor(const(143), const(63), const(151)),
                1.0 to rgbColor(const(126), const(0), const(35))

            )
            , opacity = const(0.7f)
        )
        */
    }





}