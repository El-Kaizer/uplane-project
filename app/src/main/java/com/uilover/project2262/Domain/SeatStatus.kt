package com.uilover.project2262.Domain

enum class SeatStatus {
    AVAILABLE,
    SELECTED,
    UNAVAILABLE,
    EMPTY,
    PREMIUM_AVAILABLE,  // For business/first class seats
    PREMIUM_SELECTED,
    PREMIUM_UNAVAILABLE
}