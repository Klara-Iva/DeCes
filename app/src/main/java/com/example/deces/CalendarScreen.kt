package com.example.deces

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.ContextThemeWrapper
import android.widget.CalendarView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(navController: NavController) {
    val today = Calendar.getInstance()
    val selectedDate = remember { mutableStateOf(today.timeInMillis) }
    val calendar = Calendar.getInstance()
    var startDateTimestamp by remember { mutableStateOf<Long?>(null) }
    var endDateTimestamp by remember { mutableStateOf<Long?>(null) }


    fun showDateTimePicker(onDateTimeSelected: (Long) -> Unit) {
        DatePickerDialog(
            navController.context,
            { _, year, month, dayOfMonth ->
                TimePickerDialog(
                    navController.context, { _, hourOfDay, minute ->
                        calendar.set(year, month, dayOfMonth, hourOfDay, minute)
                        onDateTimeSelected(calendar.timeInMillis)
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF291b11))
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Pregledaj po datumu:",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        item {

            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            Column(
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp) // Razmak između elemenata u Column
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp) // Razmak između gumba
                ) {
                    Button(
                        onClick = {
                            showDateTimePicker { timestamp -> startDateTimestamp = timestamp }
                        }, modifier = Modifier.weight(1f)
                    ) {
                        Text(startDateTimestamp?.let { "Start Date: ${dateFormat.format(Date(it))}" }
                            ?: "Odaberi početak:", modifier = Modifier.padding(8.dp))
                    }

                    Button(
                        onClick = {
                            showDateTimePicker { timestamp -> endDateTimestamp = timestamp }
                        }, modifier = Modifier.weight(1f)
                    ) {
                        Text(endDateTimestamp?.let { "End Date: ${dateFormat.format(Date(it))}" }
                            ?: "Odaberi kraj:", modifier = Modifier.padding(8.dp))
                    }
                }

                Button(
                    onClick = { //dodaj novi screen u kojem ce se filtrirani rezz pokazat
                        Toast.makeText(
                            navController.context,
                            "Od: ${dateFormat.format(Date(startDateTimestamp!!))} Sve do: ${
                                dateFormat.format(
                                    Date(endDateTimestamp!!)
                                )
                            }",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally) // Centriranje gumba unutar Column
                ) {
                    Text("Pretraži")
                }
            }
        }






        item {

            AndroidView(
                factory = { context ->
                    CalendarView(ContextThemeWrapper(context, R.style.CustomCalendarView)).apply {
                        date = selectedDate.value
                        setOnDateChangeListener { _, year, month, dayOfMonth ->
                            val calendar = java.util.Calendar.getInstance()
                            calendar.set(year, month, dayOfMonth)
                            selectedDate.value = calendar.timeInMillis
                            val selectedDateMillis = calendar.timeInMillis

                            navController.navigate("calendarEvent/$selectedDateMillis")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(21.dp))
                    .background(Color(0xFF8a6d57))
            )

        }
    }
}
