package com.example.myapplication.data
import com.google.gson.Gson

data class AirQualityResults(
    val temperature:Float,
    val humidity:Float,
    val pressure:Float,
    val pm2_5:Float,
    val pm1:Float,
    val pm10:Float,
    val co2:Float,
    val connectionState: ConnectionState

)
