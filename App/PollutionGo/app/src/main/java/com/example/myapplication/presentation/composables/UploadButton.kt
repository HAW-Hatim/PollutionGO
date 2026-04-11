package com.example.myapplication.presentation.composables





import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException

//TODO Change URL if necessary
private const val postUrl = "https://pollutiongo.org/uploadL"


@Composable
fun UploadButton(jsonData: String) {

    var isUploaded by rememberSaveable { mutableStateOf(false) }
    var uploadSuccess by rememberSaveable { mutableStateOf(false) }
    val okHttpClient = OkHttpClient()
    val requestBody = jsonData.toRequestBody("application/json".toMediaType())
    fun onUpload()  {
        val request = Request.Builder()
            .post(requestBody)
            .url(postUrl)
            .build()
        okHttpClient.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.e("MainActivity", "Fehler bei der Netzwerkanfrage", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body.string()
                if(responseBody.contains("\"status\":\"ok\""))
                    uploadSuccess = true
                Log.d("MainActivity", "Antwort vom Server: $responseBody")
            }

        })

    }

    Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.End) {

            Box(Modifier.background(shape = RoundedCornerShape(12.dp), color = Color(35, 164, 219)).clickable(
                onClick = {
                    if (!isUploaded) {
                        onUpload()
                        isUploaded = !isUploaded
                    }
                }, indication = null,
                interactionSource = remember { MutableInteractionSource() },
                enabled = true,
                onClickLabel = "Upload Button",
                role = null
            )){

                Row(
                    modifier = Modifier
                        .wrapContentWidth(),
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    Text(
                        text = if (!isUploaded) {
                            "SAVE & UPLOAD"
                        } else if (uploadSuccess) {
                            "Upload complete!"
                        } else {
                            "Upload failed!"
                        },
                        fontSize = 20.sp,
                        style = TextStyle(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ),
                        )
                    Icon(
                        painter = painterResource(id = R.drawable.cloud_upload_24px),
                        contentDescription = "Upload Button",
                        tint = Color.White
                    )

                    Log.d("jsonString Upload", "{ $jsonData }")

                }

            }
    }
}




