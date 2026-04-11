package com.example.myapplication.presentation.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.presentation.composables.helperClasses.SensorType


@Composable
fun SensorElement(sensorType: SensorType, value: Float)
{

    val sensorUnitsMap = mapOf(
        SensorType.Temperature to "C°",
        SensorType.Humidity to "%", //relative Humidity
        SensorType.PM_1 to "μg/m³",
        SensorType.PM2_5 to "μg/m³",
        SensorType.PM_10 to "μg/m³",
        SensorType.CO2 to "ppm", // ppm = parts per million
        SensorType.Pressure to "mbar" // millibar
    )


    Column(

        horizontalAlignment = Alignment.CenterHorizontally

    ) {


        Spacer(Modifier.height(5.dp))
        Text(
            sensorType.toString(),
            fontSize = 13.sp,
            style = TextStyle(
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(Modifier.height(5.dp))

        Surface(
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                modifier = Modifier
                    .wrapContentSize(Alignment.Center),
                text = value.toString() + " " + sensorUnitsMap.getValue(sensorType),
                color = Color.Black,

                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

