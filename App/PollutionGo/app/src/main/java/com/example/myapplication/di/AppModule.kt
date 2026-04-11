package com.example.myapplication.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import com.example.myapplication.data.AirQualityReceiveManager
import com.example.myapplication.data.ble.AirQualityBLEReceiveManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/*
* In this Module we tell dagger hilt how to provide the dependencies
*
* */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideBluetoothAdapter(@ApplicationContext context: Context): BluetoothAdapter{
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return manager.adapter
    }

    @Provides
    @Singleton
    fun provideAirQualityReceiveManager(
        @ApplicationContext context: Context,
        bluetoothAdapter: BluetoothAdapter): AirQualityReceiveManager{
        return AirQualityBLEReceiveManager(bluetoothAdapter,context)
    }

}