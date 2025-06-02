package com.uilover.project2262.Domain

data class FlightSearchModel(
    val flight: FlightModel,
    val selectedClass: String,
    val classInfo: SeatClassModel
) {
    val price: Double get() = classInfo.price
    val availableSeats: Int get() = classInfo.totalSeats - classInfo.reservedSeats.size
}
