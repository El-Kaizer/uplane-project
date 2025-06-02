package com.uilover.project2262.Domain

import android.R
import java.io.Serializable

data class FlightModel(
    var key: String? = null,
    val id: String = "",
    val flightNumber: String = "",
    val airline: String = "",
    val fromAirport: String = "",
    val toAirport: String = "",
    val departureDate: String = "",
    val departureTime: String = "",
    val arrivalTime: String = "",
    val duration: Int = 0,
    val aircraft: String = "",
    val seatConfiguration: Map<String, SeatClassModel> = emptyMap(),
    val availableClasses: List<String> = emptyList(),
    val status: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",

    // Additional fields for UI
    var airlineName: String = "",
    var airlineLogo: String = "",
    var airportFromName: String = "",
    var airportToName: String = ""
):Serializable
