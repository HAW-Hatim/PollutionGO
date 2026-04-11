package com.example.myapplication.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R


@Composable
fun SimpleToolbar(title: String, onBackAction: (() -> Unit)? = null
){
    Column {
        Row {
            if (onBackAction != null){
                Box(modifier = Modifier
                    .clickable(
                        onClick = onBackAction, indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        enabled = true,
                        onClickLabel = "Back arrow",
                        role = null
                    )
                    .padding(4.dp)
                    .background(shape = RoundedCornerShape(12.dp), color = Color.Transparent)){
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = "back arrow",
                        tint = Color.Black
                    )
                }
                Spacer(Modifier.width(12.dp))
            }
            Text(
                text = title,
                fontSize = 30.sp,
                style = TextStyle(
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            )

        }
        Box(modifier = Modifier
            .background(color = Color.Black)
            .fillMaxWidth()
            .height(1.dp))
    }
}

