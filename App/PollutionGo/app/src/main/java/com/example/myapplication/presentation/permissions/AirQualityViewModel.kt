package com.example.myapplication.presentation.permissions

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AirQualityReceiveManager
import com.example.myapplication.data.ConnectionState
import com.example.myapplication.presentation.composables.helperClasses.CollectedData
import com.example.myapplication.presentation.composables.helperClasses.CollectedDataRepository
import com.example.myapplication.presentation.composables.helperClasses.CurrentLocationRepository
import com.example.myapplication.services.LocationService
import com.example.myapplication.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// Formatter for time/date
private val formatter_time = DateTimeFormatter.ofPattern("HH:mm:ss")
private val formatter_date = DateTimeFormatter.ofPattern("yyyy-MM-dd")

@HiltViewModel
class AirQualityViewModel @Inject constructor(
    private val airQualityReceiveManager: AirQualityReceiveManager,
    private val measurementRepo: CollectedDataRepository,
    private val currentLocationRepo: CurrentLocationRepository): ViewModel(){


    private var isSubscribed = false   // <-- add this flag
    var initializationMessage by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var temperature by mutableFloatStateOf(0f)
        private set

    var humidity by mutableFloatStateOf(0f)
        private set

    var pm2_5 by mutableFloatStateOf(0f)
        private set

    var pm1 by mutableFloatStateOf(0f)
        private set

    var pm10 by mutableFloatStateOf(0f)
        private set


    var co2 by mutableFloatStateOf(0f)
        private set

    var pressure by mutableFloatStateOf(0f)
        private set
    private var _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Uninitialized)
    val connectionState : StateFlow<ConnectionState> = _connectionState.asStateFlow()

    fun updateConnectionState(state: ConnectionState) {
        _connectionState.value = state

    }
    private fun subscribeToChanges(){
        if (isSubscribed) return
        isSubscribed = true
        viewModelScope.launch {
            airQualityReceiveManager.data.collect {
                result ->
                when(result){
                    is Resource.Success -> {
                        updateConnectionState(result.data.connectionState)
                        temperature = result.data.temperature
                        pressure = result.data.pressure
                        humidity = result.data.humidity
                        pm2_5 =  result.data.pm2_5
                        pm1 =  result.data.pm1
                        pm10 =  result.data.pm10
                        co2 = result.data.co2
                        Log.d("SensorData", "temp=$temperature, time=${System.currentTimeMillis()}")
                        val location = currentLocationRepo.location.value
                        val measurement = CollectedData(
                            temperature = result.data.temperature,
                            humidity = result.data.humidity,
                            pressure = result.data.pressure,
                            pm1_0 = result.data.pm1,
                            pm2_5 = result.data.pm2_5,
                            pm10 = result.data.pm10,
                            co2 = result.data.co2,
                            lat = location?.latitude,
                            lng = location?.longitude,
                            date = LocalDate.now().format(formatter_date),
                            time = LocalTime.now().format(formatter_time)
                        )

                        val allZero = measurement.temperature == 0f &&
                                measurement.humidity == 0f &&
                                measurement.pm1_0 == 0f &&
                                measurement.pm2_5 == 0f &&
                                measurement.pm10 == 0f &&
                                measurement.co2 == 0f &&
                                measurement.pressure == 0f

                        // 2. Skip if location is missing
                        val locationMissing = currentLocationRepo.location.value == null


                        if(isServiceRunning()) {
                            // Validity check
                            if(!locationMissing && !allZero){
                                Log.d("MeasurmentRepo Udate", "Added Data: $measurement")
                                measurementRepo.addCollectedData(measurement)
                            }
                        }
                    }
                    is Resource.Loading -> {
                        initializationMessage = result.message
                        updateConnectionState(ConnectionState.CurrentlyInitializing)

                    }
                    is Resource.Error -> {
                        errorMessage = result.errorMessage
                        updateConnectionState(ConnectionState.Uninitialized)
                    }
                }
            }
        }
    }

    fun disconnect(){
        airQualityReceiveManager.disconnect()
    }

    fun reconnect(){
        airQualityReceiveManager.reconnect()
    }

    fun initializeConnection(){
        errorMessage = null

        subscribeToChanges()
        airQualityReceiveManager.startReceiving()

    }

    override fun onCleared(){
        super.onCleared()
        airQualityReceiveManager.closeConnection()
    }

    // Add service state tracking
    private var _serviceState = MutableStateFlow(LocationService.STATE_STOPPED)
    val serviceState: StateFlow<String> = _serviceState.asStateFlow()

    // In AirQualityViewModel.kt
    fun updateServiceState(state: String) {
        _serviceState.value = state
        Log.d("ViewModel", "Service state updated to: $state")
    }

    // Make sure your service control methods broadcast the state
    fun startForegroundService(context: Context) {
        Log.d("ViewModel", "Starting foreground service")
        val intent = LocationService.createStartIntent(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }


        // Note: The actual state update will come from the broadcast
    }

    fun pauseForegroundService(context: Context) {
        val intent = Intent(context, LocationService::class.java).apply {
            action = LocationService.ACTION_PAUSE
        }
        context.startService(intent)
    }

    fun resumeForegroundService(context: Context) {
        val intent = Intent(context, LocationService::class.java).apply {
            action = LocationService.ACTION_RESUME
        }
        context.startService(intent)
    }

    fun stopForegroundService(context: Context) {
        val intent = Intent(context, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
        }
        context.startService(intent)
    }
    fun isServiceRunning(): Boolean {
        return _serviceState.value == LocationService.STATE_RUNNING
    }

    fun isServicePaused(): Boolean {
        return _serviceState.value == LocationService.STATE_PAUSED
    }
    fun isServiceStopped(): Boolean {
        return _serviceState.value == LocationService.STATE_STOPPED
    }

    
    
}