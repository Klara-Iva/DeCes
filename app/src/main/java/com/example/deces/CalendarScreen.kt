package com.example.deces

import android.view.ContextThemeWrapper
import android.widget.CalendarView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CalendarScreen() {
    val today = java.util.Calendar.getInstance()
    val selectedDate = remember { mutableStateOf(today.timeInMillis) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF291b11))
            .padding(16.dp)
            .wrapContentSize(Alignment.Center)

    ) {
        // Naslov
        Text(
            text = "Event Calendar",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
        )

        // Kalendar- not using the correct colors TODO
        AndroidView(
            factory = { context ->
                CalendarView(ContextThemeWrapper(context, R.style.CustomCalendarView)).apply {
                    date = selectedDate.value
                    setOnDateChangeListener { _, year, month, dayOfMonth ->
                        val calendar = java.util.Calendar.getInstance()
                        calendar.set(year, month, dayOfMonth)
                        selectedDate.value = calendar.timeInMillis
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(21.dp))
                .background(Color(0xFF8a6d57))
        )

        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .background(Color(0xFFE57373), shape = RoundedCornerShape(50)) // Boja i zaobljenost
                .padding(8.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "Danas: ${today.get(java.util.Calendar.DAY_OF_MONTH)}.${today.get(java.util.Calendar.MONTH) + 1}.${
                    today.get(
                        java.util.Calendar.YEAR
                    )
                }", color = Color.White, fontWeight = FontWeight.Bold
            )
        }
    }
}
