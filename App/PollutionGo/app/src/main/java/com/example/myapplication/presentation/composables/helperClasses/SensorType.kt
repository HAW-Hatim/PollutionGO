package com.example.myapplication.presentation.composables.helperClasses

//sensorIndexRx -> Used for splitting up the received string from our BLE data
enum class SensorType(val sensorIndexRx: Int) {
    Temperature(0),
    Humidity(1),
    PM_1 (4),
    PM2_5(5),
    PM_10(6),
    Pressure(2),
    CO2(3);

    override fun toString(): String {

        if (this == SensorType.Temperature){
            return "Temperature \uD83C\uDF21\uFE0F" // Thermometer emoji
        }
        else if (this == SensorType.PM_1) {
            return "PM1"
        }
        else if (this == SensorType.PM_10) {
            return "PM10"
        }
        else if (this == SensorType.PM2_5) {
            return "PM2.5"
        }

        return super.toString()
    }

}

