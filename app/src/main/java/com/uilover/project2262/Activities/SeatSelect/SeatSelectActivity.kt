package com.uilover.project2262.Activities.SeatSelect

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.uilover.project2262.Activities.Splash.StatusTopBarColor
import com.uilover.project2262.Activities.TicketDetail.TicketDetailActivity
import com.uilover.project2262.Domain.BookingModel
import com.uilover.project2262.Domain.FlightModel
import com.uilover.project2262.Repository.MainRepository
import kotlinx.coroutines.launch

class SeatSelectActivity : AppCompatActivity() {
    private lateinit var flight: FlightModel
    private var seatClass: String? = null
    private val bookingRepository = MainRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Hanya ambil flight dari intent
        flight = intent.getSerializableExtra("flight") as FlightModel
        seatClass = intent.getStringExtra("selectedClass") ?: "economy"

        setContent {
            StatusTopBarColor()

            SeatListScreen(
                flight = flight,
                selectedClass = seatClass?: "economy",
                onBackClick = {
                    finish()
                },
                onConfirm = { updatedFlight, newBooking ->
                    // Process booking di sini
                    processBooking(updatedFlight, newBooking)
                }
            )
        }
    }

    private fun processBooking(updatedFlight: FlightModel, booking: BookingModel) {
        lifecycleScope.launch {
            try {
                // Show loading (optional)
                // showLoadingDialog()

                val result = bookingRepository.createBooking(updatedFlight, booking)

                result.fold(
                    onSuccess = { bookingId ->
                        // Booking berhasil
                        Toast.makeText(
                            this@SeatSelectActivity,
                            "Booking successful!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Navigate ke TicketDetailActivity dengan booking data
                        val intent = Intent(this@SeatSelectActivity, TicketDetailActivity::class.java).apply {
                            putExtra("flight", updatedFlight)
                            putExtra("booking", booking.copy(id = bookingId)) // Gunakan booking dengan ID yang sudah di-generate
                        }
                        startActivity(intent)
                        finish() // Close current activity
                    },
                    onFailure = { exception ->
                        // Booking gagal
                        Toast.makeText(
                            this@SeatSelectActivity,
                            "Booking failed: ${exception.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(
                    this@SeatSelectActivity,
                    "An error occurred: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}