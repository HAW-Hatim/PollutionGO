package com.example.myapplication.presentation.composables.helperClasses

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class CollectedData(
    // Sensor data

    @SerializedName("temperatur") val temperature:Float,
    @SerializedName("luftfeuchtigkeit") val humidity:Float,
    @SerializedName("luftdruck") val pressure:Float,
    val pm2_5:Float,
    val pm1_0:Float,
    val pm10:Float,
    val co2:Float,

    // Location Data

    @SerializedName("breitengrad") val lat: Double?,
    @SerializedName("laengengrad") val lng: Double?,

    // Time
    @SerializedName("datum") val date: String,
    @SerializedName("zeit") val time: String
    )


// Repository for measurements (singleton)
@Singleton
class CollectedDataRepository @Inject constructor() {

    //
    private val collectedDataAmount = 7
    private val _measurements = MutableStateFlow<List<CollectedData>>(emptyList())
    val measurements: StateFlow<List<CollectedData>> = _measurements.asStateFlow()

    fun addCollectedData(measurement: CollectedData) {
        //Validity check of data inside Airqualityviewmodel
        _measurements.value += measurement
    }

    fun clear() {
        _measurements.value = emptyList()
    }
}

// Repository for current location (singleton)
@Singleton
class CurrentLocationRepository @Inject constructor() {
    private val _location = MutableStateFlow<android.location.Location?>(null)
    val location: StateFlow<android.location.Location?> = _location.asStateFlow()

    fun updateLocation(location: android.location.Location) {
        _location.value = location
    }
}
