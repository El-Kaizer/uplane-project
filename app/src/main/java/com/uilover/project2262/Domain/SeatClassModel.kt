package com.uilover.project2262.Domain

import java.io.Serializable

data class SeatClassModel(
    val price: Double = 0.0,
    val totalSeats: Int = 0,
    val seatsPerRow: Int = 0,
    val rows: Int = 0,
    val rowRange: String = "",
    val reservedSeats: List<String> = emptyList()
) : Serializable
