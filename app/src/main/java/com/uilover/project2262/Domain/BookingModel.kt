package com.uilover.project2262.Domain

import java.io.Serializable

data class BookingModel (
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val userName: String = "",
    val flightId: String = "",
    val seatClass: String = "",
    val selectedSeats: List<String> = emptyList(),
    val pricePerSeat: Double = 0.0,
    val totalPrice: Double = 0.0,
    val bookingDate: String = "",
    val status: String = ""
) : Serializable