package com.us.ronsportspeedo

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.us.ronsportspeedo.ui.theme.RonsportSpeedoTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: SpeedoMeterViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.S)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {

        } else {
            Toast.makeText(this, "Allow permission for it to work", Toast.LENGTH_SHORT).show()
            checkAndRequestPermissions()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        sensorManager.registerListener(
            viewModel.getAccelerometerListener(),
            accelerometerSensor,
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM
        )


        setContent {
            RonsportSpeedoTheme {
                SpeedoMeterScreen(viewModel = viewModel)
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkAndRequestPermissions()
        }
    }


    private fun checkAndRequestPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BODY_SENSORS
            ) != PackageManager.PERMISSION_GRANTED -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    requestPermissionLauncher.launch(android.Manifest.permission.BODY_SENSORS)
                }
            }

            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.HIGH_SAMPLING_RATE_SENSORS
            ) != PackageManager.PERMISSION_GRANTED -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    requestPermissionLauncher.launch(android.Manifest.permission.HIGH_SAMPLING_RATE_SENSORS)
                }
            }

            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                }
            }

        }
    }




}

@Composable
fun SpeedoMeterScreen(viewModel: SpeedoMeterViewModel) {

    val speedData = viewModel.speedData
    val context = LocalContext.current
    var recordedSpeeds by remember { mutableStateOf<List<Float>>(emptyList()) }
    var isStartButtonPressed by remember { mutableStateOf(false) }
    var startTimeMilli by remember { mutableLongStateOf(0L) }
    var stopTimeMIlli by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isStartButtonPressed) {
        while (isStartButtonPressed) {
            speedData.value.currentSpeed.let {
               // soundPlayer.playSoundForSpeed(it)
             //   Log.d("SpeedoMeterUpdate", "Updated Speed: $it")
            }

            if (System.currentTimeMillis() - startTimeMilli >= 5000) {
                recordedSpeeds = recordedSpeeds + speedData.value.currentSpeed
              //  Log.d("SpeedoMeterUpdate", "Recorded Speeds: $recordedSpeeds")
              //  startTimeMilli = System.currentTimeMillis()
            }

            delay(100)
        }
    }


 
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,

        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Speedometer UI

            SpeedoCanvas(recordedSpeeds.average())


            Spacer(modifier = Modifier.height(10.dp))

            ElevatedCard {
                //Timer
                SpeedoMeter(speedData.value, isStartButtonPressed, startTimeMilli, stopTimeMIlli)
                // Recorded Speeds UI
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Column(
                        Modifier .padding(4.dp)
                    ) {
                        //avarage Speed View
                        AvarageSpeed(recordedSpeeds = recordedSpeeds)

                        Spacer(modifier = Modifier.height(10.dp))
                        //Time used View
                        TimerView(startTimeMilli = startTimeMilli, stopTimeMIlli = stopTimeMIlli)

                        Spacer(modifier = Modifier.height(10.dp))
                        //Speedlog View
                        SpeedLog(recordedSpeeds = recordedSpeeds)


                        Spacer(modifier = Modifier.width(10.dp))
                    }
                        //Speed Meesage Text
                        Text(
                            text = texts(recordedSpeeds.average()),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            textDecoration = TextDecoration.Underline,
                            color = Color.Red,
                            modifier = Modifier.padding(8.dp),
                        )

                }


                // Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if(!isStartButtonPressed) {
                        OutlinedButton(
                            onClick = {
                                viewModel.updateStartButtonPressed(true)
                                viewModel.speedData.value.timeMillis
                                viewModel.updateTime(System.currentTimeMillis())
                                viewModel.speedData.value.currentSpeed// Reset speed to 0 when starting
                                isStartButtonPressed = true
                                recordedSpeeds = emptyList()
                                startTimeMilli = System.currentTimeMillis()

                            },
                            modifier = Modifier
                                .height(100.dp)
                                .width(100.dp)
                                .padding(2.dp)
                                .background(Color.Green, shape = RoundedCornerShape(50.dp)),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Text(
                                text = "Start",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                        if(isStartButtonPressed) {
                            OutlinedButton(
                                onClick = {
                                    isStartButtonPressed = false
                                    viewModel.updateStartButtonPressed(false)
                                    viewModel.updateTime(System.currentTimeMillis())
                                    //viewModel.stopTime(System.currentTimeMillis())
                                    stopTimeMIlli = System.currentTimeMillis()
                                    //recordedSpeeds = recordedSpeeds + speedData.value.currentSpeed
                                },
                                modifier = Modifier
                                    .height(100.dp)
                                    .width(100.dp)
                                    .padding(2.dp)
                                    .background(Color.Red, shape = RoundedCornerShape(50.dp)),
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Text(
                                    text = "Stop",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }
                        }
                }
            }
        }


}

@Composable
fun AvarageSpeed(
    recordedSpeeds: List<Float>
){
    ElevatedCard(
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth(1f)
            .background(Color.Transparent, shape = RoundedCornerShape(15.dp)),

        ) {
        Row {
            Text(
                text = "Av. Speed",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(3.dp),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier .width(50.dp))
            Text(
                text = String.format("%.2f", recordedSpeeds.average().coerceAtLeast(0.0)),
                modifier = Modifier.padding(3.dp),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "m/s",
                modifier = Modifier.padding(3.dp),
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun SpeedLog(
    recordedSpeeds: List<Float>
){
    ElevatedCard (
        Modifier .fillMaxWidth(1f)
    ){
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(1f),

            ) {


            //Avarage
            Text(
                text = "Speed logs",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(3.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier .width(50.dp))
            Text(
                text = recordedSpeeds.joinToString(", ")
                    .takeLast(20)
                    .split(", ")
                    .map { it.toFloatOrNull()?.coerceAtLeast(0.1f) ?: 1.0f }
                    .joinToString(", "),
                modifier = Modifier.padding(8.dp),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )


        }
    }
}



@Composable
fun SpeedoMeter(
    speedData: SpeedData,
    isStartButtonPressed: Boolean,
    startTimeMilli: Long,
    stopTimeMIlli: Long
) {


    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val speed by rememberUpdatedState(speedData.currentSpeed)


        Column(
            modifier = Modifier
                .fillMaxWidth(1f)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
Row (
    modifier = Modifier .fillMaxWidth(1f) .padding(4.dp)
){
    ElevatedCard(
        Modifier.fillMaxWidth(.5f) .padding(5.dp),
        shape = RoundedCornerShape(5.dp)

    ) {
        //start
        Text(
            text = "Speed: $speed ms",
            fontSize = 22.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(8.dp),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
    Spacer(modifier = Modifier.width(10.dp))
    //Timer on Start

    if (isStartButtonPressed) {
        val elapsedTimeMillis by rememberUpdatedState(System.currentTimeMillis() - startTimeMilli)
        val elapsedDifference by rememberUpdatedState(formatTimeDifference(elapsedTimeMillis))
        ElevatedCard {
            Row(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.timer),
                    contentDescription = "Timer Icon",
                    modifier = Modifier.size(30.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = elapsedDifference,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.primary
                )


            }
        }
    }else{
        ElevatedCard (
            modifier = Modifier
                .fillMaxWidth(1f) .padding(5.dp)

        ){
            Text(text = "00:00:00",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp),
                color = MaterialTheme.colorScheme.primary
                )
        }
    }

}


                //times
                    Row(
                        Modifier
                            .fillMaxWidth(1f)
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        ElevatedCard(
                            Modifier.fillMaxWidth(.5f) .padding(5.dp),
                            shape = RoundedCornerShape(5.dp)

                        ) {
                            Text(
                                text = "Start: ${formatter.format(startTimeMilli)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(8.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        ElevatedCard(
                            Modifier.fillMaxWidth(1f) .padding(5.dp),
                            shape = RoundedCornerShape(5.dp)

                        ) {
                            Text(
                                text = "Stop: ${formatter.format(stopTimeMIlli)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(8.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }



        }

}

@Composable
fun TimerView(
    startTimeMilli: Long,
    stopTimeMIlli: Long
){
    val elapsedTimeMilli by rememberUpdatedState(stopTimeMIlli - startTimeMilli)
    val elapsedDifferences by rememberUpdatedState(formatTimeDifference(elapsedTimeMilli))

    ElevatedCard {
        Row(
            Modifier
                .fillMaxWidth(1f)
                .padding(4.dp),

        ) {
            Text(
                text = "Time used",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(3.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier .width(50.dp))
            Text(
                text = elapsedDifferences.coerceAtLeast(0L.toString()),
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(8.dp),

                )
        }
    }
}


// Function to format the time difference
private fun formatTimeDifference(timeMillis: Long): String {
    val seconds = timeMillis / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "$minutes m : $remainingSeconds s"
}

internal fun calculateDistanceCovered(accelerometerListener: AccelerometerListener?): Float {
    //  logic to calculate distance covered


    return (accelerometerListener?.currentSpeed ?: 0f) / 1000 * 60
}

 fun texts(speed: Double): String {
     return when {
         speed > 70 -> "Excellent!"
         speed > 50 -> "Keep up the good work!"
         speed > 10 -> "Speed Up"
         speed >0.1 -> "Too slow"
         else -> "Let's Go"
     }
 }




@Preview
@Composable
fun Previews(){
    SpeedoMeterScreen(viewModel = viewModel())
}

