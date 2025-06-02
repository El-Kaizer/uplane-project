package com.uilover.project2262.Activities.TicketDetail

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.*
import com.google.firebase.database.FirebaseDatabase
import com.uilover.project2262.Activities.Splash.StatusTopBarColor
import com.uilover.project2262.Domain.AirlineModel
import com.uilover.project2262.Domain.BookingModel
import com.uilover.project2262.Domain.FlightModel
import com.uilover.project2262.Domain.LocationModel

class TicketDetailActivity : AppCompatActivity() {
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val flight = intent.getSerializableExtra("flight") as FlightModel
        val booking = intent.getSerializableExtra("booking") as BookingModel

        setContent {
            StatusTopBarColor()

            // State untuk data dari Firebase
            val airlineMap = remember { mutableStateOf<AirlineModel?>(null) }
            val locationMap = remember { mutableStateOf<Pair<LocationModel?, LocationModel?>?>(null) }
            val isLoading = remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                fetchAirlineData(flight.airline) { airline ->
                    airlineMap.value = airline
                }

                fetchAllLocationData { locations ->
                    // Cari lokasi keberangkatan dan kedatangan dari map locations
                    val departureLocation = locations[flight.fromAirport]
                    val arrivalLocation = locations[flight.toAirport]

                    locationMap.value = Pair(departureLocation, arrivalLocation)
                    isLoading.value = false
                }
            }

            if (isLoading.value) {
                // Bisa tampilkan loading indicator kalau perlu
            } else {
                TicketDetailScreen(
                    flight = flight,
                    booking = booking,
                    airline = airlineMap.value,
                    departureAirport = locationMap.value?.first,
                    arrivalAirport = locationMap.value?.second,
                    onBackClick = { finish() },
                    onDownloadTicketClick = { finish() }
                )
            }
        }
    }

    private fun fetchAirlineData(airlineId: String, onResult: (AirlineModel?) -> Unit) {
        firebaseDatabase.getReference("airlines")
            .child(airlineId)
            .get()
            .addOnSuccessListener { snapshot ->
                val airline = snapshot.getValue(AirlineModel::class.java)
                onResult(airline)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    private fun fetchAllLocationData(onResult: (Map<String, LocationModel>) -> Unit) {
        firebaseDatabase.getReference("dataBandara")
            .get()
            .addOnSuccessListener { snapshot ->
                val locations = mutableMapOf<String, LocationModel>()
                snapshot.children.forEach { child ->
                    val location = child.getValue(LocationModel::class.java)
                    location?.let {
                        locations[child.key ?: ""] = it
                    }
                }
                onResult(locations)
            }
            .addOnFailureListener {
                onResult(emptyMap())
            }
    }
}
