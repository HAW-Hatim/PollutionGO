package com.example.myapplication.presentation.composables.pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.presentation.composables.SimpleToolbar
import com.example.myapplication.presentation.composables.UploadButton
import com.example.myapplication.presentation.composables.helperClasses.CollectedData
import com.example.myapplication.presentation.composables.helperClasses.Screen
import com.example.myapplication.presentation.permissions.FinishViewModel
import com.google.gson.Gson
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.material3.ButtonGroup
import androidx.compose.ui.text.TextStyle
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent


// Could not find the exact colors but close enough
private val chartColors =
    mapOf(
        "PM1" to Color(66, 133, 244), // blue
        "PM2.5" to Color(15, 157, 88), //green
        "PM10" to Color(255, 152, 0), //orange
    )


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)

@Composable
fun FinishScreen(
    navController: NavController,
    sharedData: StateFlow<List<CollectedData>>,
    finishViewModel: FinishViewModel = hiltViewModel()
) {

     val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    // Clear our data, so a new activity can be started
    // Listens to changes in the lifecycle
    DisposableEffect(key1 = lifecycleOwner) {
        onDispose {
        }
    }



    val MarkerValueFormatter = DefaultCartesianMarker.ValueFormatter.default(suffix = " °C")
    // Used sensors for the button names
    val sensorNameList = listOf("Temperature", "Humidity", "Particulat matter", "CO2", "Pressure")
    val pmIndex = 2

    val measurements by sharedData.collectAsState()

    var pm1Visible by rememberSaveable { mutableStateOf(true) }
    var pm25Visible by rememberSaveable { mutableStateOf(true) }
    var pm10Visible by rememberSaveable { mutableStateOf(true) }

    // Selected Sensor data index
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

    val scrollState = rememberScrollState()




    Column(
        modifier = Modifier
            .fillMaxHeight()
            .safeContentPadding()
            .background(Color.White)
            ,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SimpleToolbar("Activity completed!", onBackAction = {
            //Clear Data to be ready to save for new activities
            finishViewModel.clear()
            navController.navigate(Screen.MapScreen.route) {
                popUpTo(Screen.ResultScreen.route) { inclusive = true }
            }
        })

        Spacer(Modifier.width(10.dp))

        Column(Modifier.fillMaxHeight().weight(0.4f)) {
            Row(Modifier.fillMaxWidth().horizontalScroll(scrollState), horizontalArrangement = Arrangement.SpaceAround ) {


                        // Create buttons in a loop for cleaner code
                        sensorNameList.forEachIndexed { index, label ->
                            Button(
                                onClick = { selectedIndex = index },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedIndex == index)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selectedIndex == index)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = if (selectedIndex == index)
                                    ButtonDefaults.outlinedButtonBorder()
                                else
                                    null,
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = if (selectedIndex == index) 6.dp else 2.dp,
                                    pressedElevation = if (selectedIndex == index) 12.dp else 4.dp
                                )
                            ) {
                                Box(Modifier.wrapContentWidth(), contentAlignment = Alignment.Center) {
                                    Text(
                                        label,
                                        fontWeight = if (selectedIndex == index)
                                            FontWeight.Bold
                                        else
                                            FontWeight.Normal,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }


            }

            if (selectedIndex == pmIndex) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp) // space between buttons
                    ) {
                        //Buttons with individual state variables
                        // At least one button needs to be activated at all times
                        listOf(
                            Triple("PM1", pm1Visible) {
                                if( pm25Visible || pm10Visible)
                                    pm1Visible = !pm1Visible
                                                      },
                            Triple("PM2.5", pm25Visible) {
                                if(pm1Visible || pm10Visible)
                                    pm25Visible = !pm25Visible
                                                         },
                            Triple("PM10", pm10Visible) {
                                if(pm1Visible || pm25Visible )
                                    pm10Visible = !pm10Visible
                            }
                        ).forEach { (label, isVisible, onClick) ->
                            Button(
                                onClick = onClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isVisible)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isVisible)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = if (isVisible)
                                    ButtonDefaults.outlinedButtonBorder()
                                else
                                    null,
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = if (isVisible) 6.dp else 2.dp,
                                    pressedElevation = if (isVisible) 12.dp else 4.dp
                                ),
                                modifier = Modifier.wrapContentWidth()
                                    .weight(1f) // each button takes equal width
                            ) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween){
                                    Icon(
                                        painter = if (isVisible) {
                                            painterResource(R.drawable.baseline_visibility_off_24)
                                        } else {
                                            painterResource(R.drawable.baseline_visibility_24)
                                        },
                                        contentDescription = if (isVisible) "Visible" else "Hidden",
                                    )

                                    Spacer(Modifier.width(4.dp))

                                    Text(
                                        label,
                                        fontWeight = if (isVisible) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp,
                                    )
                                }

                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxHeight().weight(1f),
            verticalArrangement = Arrangement.Top
        ) {


            // Title
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(
                    text = sensorNameList[selectedIndex] + " during Activity",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            if (measurements.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No data collected")
                }
            } else {
                val modelProducer = remember { CartesianChartModelProducer() }

                LaunchedEffect(selectedIndex, measurements, pm1Visible, pm25Visible, pm10Visible) {
                    modelProducer.runTransaction {
                        if (selectedIndex != pmIndex) {
                            // Single sensor
                            val values = when (selectedIndex) {
                                0 -> measurements.map { it.temperature }
                                1 -> measurements.map { it.humidity }
                                3 -> measurements.map { it.co2 }
                                4 -> measurements.map { it.pressure }
                                else -> emptyList()
                            }
                            lineSeries {
                                series(values)
                                Log.d("Series", "$values")
                            }
                        } else {
                            // PM sensors – up to three series
                            lineSeries {
                                if (pm1Visible) {
                                    series(measurements.map { it.pm1_0 })
                                }

                                if (pm25Visible) {
                                    series(measurements.map { it.pm2_5 })
                                }
                                if (pm10Visible) {
                                    series(measurements.map { it.pm10 })
                                }
                            }
                        }
                    }
                }

                val lines = remember(selectedIndex, pm1Visible, pm25Visible, pm10Visible) {

                        // PM lines in the order they are added in the transaction
                        buildList {
                            if (pm1Visible) {
                                add(LineCartesianLayer.Line(
                                    fill = LineCartesianLayer.LineFill.single(Fill(chartColors["PM1"]!!))
                                ))
                            }
                            if (pm25Visible) {
                                add(LineCartesianLayer.Line(
                                    fill = LineCartesianLayer.LineFill.single(Fill(chartColors["PM2.5"]!!))
                                ))
                            }
                            if (pm10Visible) {
                                add(LineCartesianLayer.Line(
                                    fill = LineCartesianLayer.LineFill.single(Fill(chartColors["PM10"]!!))
                                ))
                            }
                        }

                }

                key(selectedIndex) {
                    CartesianChartHost(
                    rememberCartesianChart(
                        rememberLineCartesianLayer(lineProvider = LineCartesianLayer.LineProvider.series(lines)),
                        startAxis = VerticalAxis.rememberStart(valueFormatter = CartesianValueFormatter.decimal(suffix = getUnitForIndex(selectedIndex)),

                                label = rememberAxisLabelComponent(
                                    style = TextStyle(Color.Black)
                                )),
                        bottomAxis = HorizontalAxis.rememberBottom(
                            label = rememberAxisLabelComponent(
                            style = TextStyle(Color.Black)
                        ))

                    ),
                    modelProducer,
                    modifier = Modifier
                        .wrapContentSize()
                ) }


                // X-Axis Label
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Top) {
                    Text("Datapoint every 10 seconds")
                }

                if(selectedIndex == pmIndex){
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(thickness = 2.dp)
                    Spacer(Modifier.height(10.dp))
                    // Legend
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically) {

                        if (pm1Visible) {
                            Column {
                                Row(Modifier.wrapContentSize()) {
                                    Box(
                                        Modifier.width(30.dp).height(17.dp),
                                        contentAlignment = Alignment.Center
                                    ) {

                                        chartColors["PM1"]?.let {
                                            HorizontalDivider(
                                                color = it,
                                                thickness = 3.dp
                                            )
                                        }


                                    }
                                    Spacer(Modifier.width(5.dp))
                                    Text("PM1")
                                }
                            }
                        }
                        if (pm25Visible) {
                            Column {
                                Row(Modifier.wrapContentSize()) {
                                    Box(
                                        Modifier.width(30.dp).height(17.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        chartColors["PM2.5"]?.let {
                                            HorizontalDivider(
                                                color = it,
                                                thickness = 3.dp
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(5.dp))
                                    Text("PM2.5")
                                }
                            }
                        }
                        if (pm10Visible) {
                        Column {
                            Row(Modifier.wrapContentSize()) {
                                Box(Modifier.width(30.dp).height(17.dp), contentAlignment = Alignment.Center ) {
                                    chartColors["PM10"]?.let { HorizontalDivider(color = it, thickness = 3.dp) }
                                }
                                Spacer(Modifier.width(5.dp))
                                Text("PM10")
                            }
                        }
                            }
                    }

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(thickness = 2.dp)

                }


            }
        }


        Column(modifier = Modifier.safeContentPadding(), verticalArrangement = Arrangement.Bottom) {

            // Prep data for export
            val gson = Gson()
            val jsonOutput = gson.toJson(measurements)

            UploadButton(jsonOutput)

            // Column to push us above the navigation bar
            Column(
                modifier = Modifier.padding(
                    bottom = 2 * WindowInsets
                        .navigationBarsIgnoringVisibility
                        .asPaddingValues()
                        .calculateBottomPadding()
                )
            ) {}
        }

    }
    Log.d("FinishScreen Data", "$measurements")



}


// Helper to get unit for axis label
fun getUnitForIndex(index: Int): String = when (index) {
    0 -> " °C"       // Temperature
    1 -> " %"        // Humidity
    2 -> " µg/m³"
    3 -> " ppm"      // CO2
    4 -> " mbar"      // Pressure
    else -> ""
}