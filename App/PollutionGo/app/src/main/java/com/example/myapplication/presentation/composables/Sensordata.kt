package com.example.myapplication.presentation.composables

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import com.example.myapplication.presentation.composables.helperClasses.StyleValue



@Composable
fun Sensordata(isVisible: Boolean, UI_Choice: StyleValue, sensorDataMap: MutableMap<SensorType, Float>) {
    if (isVisible) {

        if (UI_Choice == StyleValue.SideSheet) {
            Column(
                modifier = Modifier
                    .fillMaxHeight().fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                Spacer(Modifier.height(16.dp))
                Text(
                    "Temperature \uD83C\uDF21\uFE0F",
                    fontSize = 15.sp,
                    style = TextStyle(
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    ), modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(30.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentSize(Alignment.Center),
                        text = "60 µg/m³",
                        color = Color.Black,

                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(thickness = 2.dp)
                Spacer(Modifier.height(10.dp))

            }

        }else if (UI_Choice == StyleValue.BottomSheet) {
            Log.d("SensorData", "SensorMap in SensorData: {$sensorDataMap}")

            Column() {
                Row(modifier = Modifier.fillMaxWidth(),
                    Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically) {





                    SensorElement(SensorType.Temperature, sensorDataMap[SensorType.Temperature]!!)

                    SensorElement(SensorType.Humidity, sensorDataMap[SensorType.Humidity]!!)


                }



                Spacer(Modifier.height(5.dp))
                HorizontalDivider(thickness = 2.dp)
                Spacer(Modifier.height(5.dp))

                Row(modifier = Modifier.fillMaxWidth(),
                    Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically) {

                    Text("PM - Particulat matter",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(5.dp))
                HorizontalDivider(thickness = 2.dp)
                Spacer(Modifier.height(5.dp))

                Row(modifier = Modifier.fillMaxWidth(),
                    Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically) {





                    SensorElement(SensorType.PM_1, sensorDataMap[SensorType.PM_1]!!)

                    SensorElement(SensorType.PM2_5, sensorDataMap[SensorType.PM2_5]!!)

                    SensorElement(SensorType.PM_10, sensorDataMap[SensorType.PM_10]!!)
                }



                Spacer(Modifier.height(5.dp))
                HorizontalDivider(thickness = 2.dp)
                Spacer(Modifier.height(5.dp))

                Row(modifier = Modifier.fillMaxWidth(),
                    Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically) {





                    SensorElement(SensorType.Pressure, sensorDataMap[SensorType.Pressure]!!)
                    Log.d("Pressure value", "${sensorDataMap[SensorType.Pressure]}")

                    SensorElement(SensorType.CO2, sensorDataMap[SensorType.CO2]!!)


                }

                Spacer(Modifier.height(5.dp))
                HorizontalDivider(thickness = 2.dp)


            }
        }
            }



}
