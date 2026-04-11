package com.example.myapplication.data

import com.example.myapplication.util.Resource
import kotlinx.coroutines.flow.MutableSharedFlow

interface AirQualityReceiveManager {

    val data: MutableSharedFlow<Resource<AirQualityResults>>

    fun reconnect()

    fun disconnect()

    fun startReceiving()

    fun closeConnection()
}