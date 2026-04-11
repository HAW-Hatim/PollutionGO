package com.example.myapplication.data.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.example.myapplication.data.AirQualityReceiveManager
import com.example.myapplication.data.AirQualityResults
import com.example.myapplication.data.ConnectionState
import com.example.myapplication.presentation.composables.helperClasses.SensorType
import com.example.myapplication.util.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

class AirQualityBLEReceiveManager @Inject constructor
    (private val bluetoothAdapter: BluetoothAdapter,
     @ApplicationContext private val context: Context): AirQualityReceiveManager {

    private val DEVICE_NAME = "ESP32_BLE_UART"
    private val AIR_QUALITY_SERVICE_UIID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e"
    private val AIR_QUALITY_CHARACTERISTICS_UUID = "6e400003-b5a3-f393-e0a9-e50e24dcca9e"
    override val data: MutableSharedFlow<Resource<AirQualityResults>> = MutableSharedFlow()

    private val bleScanner by lazy{
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private var gatt: BluetoothGatt? = null

    private var isScanning = false

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

private val scanCallback = object : ScanCallback(){
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
    override fun onScanResult(callbackType: Int, result: ScanResult) {

        if(result.device.name == DEVICE_NAME){
            coroutineScope.launch {
                data.emit(Resource.Loading(message = "Connectiong to device..."))
            }
                if(isScanning){
                    result.device.connectGatt(context,false,gattCallback, BluetoothDevice.TRANSPORT_LE)
                    isScanning = false
                    bleScanner.stopScan(this)
                }

        }
    }
}

    private var currentConnectionAttempt = 1
    private var MAXIMUM_CONNECTION_ATTEMPTS = 3



    private val gattCallback = object: BluetoothGattCallback(){
        @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.d("Gatt callback", "Connection attempts: $currentConnectionAttempt, state bluetooth profile: $newState")

            if(status == BluetoothGatt.GATT_SUCCESS){

                if(newState == BluetoothProfile.STATE_CONNECTED){
                    coroutineScope.launch {
                        data.emit(Resource.Loading(message = "Discovering Services..."))

                    }
                    gatt.discoverServices()
                    this@AirQualityBLEReceiveManager.gatt = gatt

                }else if(newState == BluetoothProfile.STATE_DISCONNECTED){

                    gatt.close()

                }
            }else{

                currentConnectionAttempt+=1
                coroutineScope.launch {
                    data.emit(Resource.Loading(message = "Attempting to connect $currentConnectionAttempt/$MAXIMUM_CONNECTION_ATTEMPTS"))
                }
                if (currentConnectionAttempt <= MAXIMUM_CONNECTION_ATTEMPTS){
                    startReceiving()
                }else{
                    coroutineScope.launch {
                        data.emit(Resource.Error(errorMessage = "Could not connect to $DEVICE_NAME"))
                    }
                }

                gatt.close()
            }
        }

        @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt){
                printGattTable()
                coroutineScope.launch {
                    data.emit(Resource.Loading(message = "Adjusting MTU space..."))
                }
                // The MTU is max data amount our BLE device can send
                // Default is 20 Bytes; 517 is max amount
                // you can request but will be set to max the device can offer
                gatt.requestMtu(517)
            }
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            val charachteristic = findCharachteristics(AIR_QUALITY_SERVICE_UIID, AIR_QUALITY_CHARACTERISTICS_UUID)
            if(charachteristic == null){
                coroutineScope.launch {
                    data.emit(Resource.Error(errorMessage = "Could not find air quality publisher"))
                }
                return
            }
            enableNotification(charachteristic)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {



            with(characteristic){
                when(uuid){
                    UUID.fromString(AIR_QUALITY_CHARACTERISTICS_UUID) -> {

                        //Convert byte array send via BLE to string which has following syntax: (value1,value2,...,valueN)
                        val buffer = value.toString(Charsets.UTF_8)
                        val stringValues = buffer.split(",").map { it.toFloat() }

                        Log.d("BLE RECEIVE", "Received string from ESP32: {$stringValues}")
                        val temperature = stringValues[SensorType.Temperature.sensorIndexRx]
                        val humidity = stringValues[SensorType.Humidity.sensorIndexRx]
                        val pressure = stringValues[SensorType.Pressure.sensorIndexRx]
                        val co2 = stringValues[SensorType.CO2.sensorIndexRx]
                        val pm1 = stringValues[SensorType.PM_1.sensorIndexRx]
                        val pm2_5 = stringValues[SensorType.PM2_5.sensorIndexRx]
                        val pm10 = stringValues[SensorType.PM_10.sensorIndexRx]

                        val airQualityResult = AirQualityResults(temperature,humidity,
                            pressure,pm2_5,pm1, pm10, co2, ConnectionState.Connected)
                        coroutineScope.launch {
                            data.emit(
                                Resource.Success(airQualityResult)
                            )
                        }
                    }
                    else -> Unit


                }
                Log.d("BLE_DATA", "Received: ${value.joinToString()}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    private fun enableNotification(characteristic: BluetoothGattCharacteristic) {
        val cccdUuid = UUID.fromString(CCCD_DESCRIPTOR_UUID)
        val payload = when {
            characteristic.isIndicatable() -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            characteristic.isNotifiable() -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else -> {
                Log.e("BLE", "Characteristic doesn't support notification/indication")
                return
            }
        }

        characteristic.getDescriptor(cccdUuid)?.let { descriptor ->
            // 1. Enable local notifications FIRST
            if (gatt?.setCharacteristicNotification(characteristic, true) == false) {
                Log.e("BLE", "Failed to set local characteristic notification")
                return
            }
            Log.d("BLE", "Local notification enabled for ${characteristic.uuid}")

            // 2. Write to the descriptor to subscribe on the server (ESP32) side
            writeDescriptor(descriptor, payload)
        } ?: run {
            Log.e("BLE", "CCCD descriptor not found!")
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    private fun writeDescriptor(descriptor: BluetoothGattDescriptor, payload: ByteArray) {
        gatt?.let { gatt ->
            // Option 1: Use the new method that includes the value[citation:4][citation:7]
            gatt.writeDescriptor(descriptor, payload)
            Log.d("BLE", "Attempting to write to CCCD descriptor: ${payload.joinToString()}")
        } ?: run {
            Log.e("BLE", "writeDescriptor: GATT is null. Not connected.")
        }
    }

    private fun findCharachteristics(serviceUUID: String, charachteristicsUUID:String): BluetoothGattCharacteristic? {
        return gatt?.services?.find {services ->
            services.uuid.toString() == serviceUUID
        }?.characteristics?.find { characteristics ->
            characteristics.uuid.toString() == charachteristicsUUID
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun reconnect() {
        gatt?.connect()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun disconnect() {
        gatt?.disconnect()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun startReceiving() {
        coroutineScope.launch {
            data.emit(Resource.Loading(message = "Scanning BLE devices..."))
        }
        isScanning = true
        bleScanner.startScan(null, scanSettings,scanCallback)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    override fun closeConnection() {
        bleScanner.stopScan(scanCallback)
        val characteristic = findCharachteristics(AIR_QUALITY_SERVICE_UIID, AIR_QUALITY_CHARACTERISTICS_UUID)

        if(characteristic != null){
            disconnectCharachteristic(characteristic)
        }
        gatt?.close()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])

    private fun disconnectCharachteristic(charachteristic: BluetoothGattCharacteristic){
        val cccdUuid = UUID.fromString(CCCD_DESCRIPTOR_UUID)
        charachteristic.getDescriptor(cccdUuid)?.let { cccdDescriptor ->
            if(gatt?.setCharacteristicNotification(charachteristic, false) == false){
                Log.d("AirQualityReceiveManager", "set charachteristics notification failed")
                return
            }
            writeDescriptor(cccdDescriptor, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
        }
    }
}