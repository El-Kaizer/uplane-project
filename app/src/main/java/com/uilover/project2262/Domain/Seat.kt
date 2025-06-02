package com.uilover.project2262.Domain

import java.io.Serializable

data class Seat(
    var status: SeatStatus,
    var name: String,
    val seatClass: String   , // economy, business, first
    val isAisle: Boolean = false,
    val isWindow: Boolean = false
) : Serializable