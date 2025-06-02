package com.uilover.project2262.Activities.SeatSelect

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.uilover.project2262.Domain.BookingModel
import com.uilover.project2262.Domain.FlightModel
import com.uilover.project2262.Domain.SeatClassModel
import com.uilover.project2262.R
import com.uilover.project2262.Domain.SeatStatus
import com.uilover.project2262.Domain.Seat

@Composable
fun SeatListScreen(
    flight: FlightModel,
    selectedClass: String,
    onBackClick: () -> Unit,
    onConfirm: (FlightModel, BookingModel) -> Unit
) {
    val context = LocalContext.current

    val seatList = remember { mutableStateListOf<Seat>() }
    val selectedSeatNames = remember { mutableStateListOf<String>() }

    var seatCount by remember { mutableStateOf(0) }
    var totalPrice by remember { mutableStateOf(0.0) }

    val classPrice by remember(selectedClass) {
        mutableStateOf(flight.seatConfiguration[selectedClass]?.price ?: 0.0)
    }

    fun updatePriceAndCount() {
        seatCount = selectedSeatNames.size
        totalPrice = seatCount * classPrice  // Gunakan classPrice di sini
    }

    LaunchedEffect(flight) {
        seatList.clear()
        seatList.addAll(generateSeatList(flight, selectedClass ))
        updatePriceAndCount()
    }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.darkPurple2))
    ) {
        val (topSection, middleSection, bottomSection) = createRefs()

        TopSection(
            modifier = Modifier
                .constrainAs(topSection) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }, onBackClick = onBackClick
        )

        //middle section
        ConstraintLayout(
            modifier = Modifier
                .padding(top = 100.dp)
                .constrainAs(middleSection) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            val (airplane, seatGrid) = createRefs()
            Image(
                painter = painterResource(R.drawable.airple_seat),
                contentDescription = null,
                modifier = Modifier.constrainAs(airplane) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(getGridColumnsForClass(selectedClass)),
                modifier = Modifier
                    .padding(top = 240.dp)
                    .padding(horizontal = 64.dp)
                    .constrainAs(seatGrid) {
                        top.linkTo(parent.top)
                        start.linkTo(airplane.start)
                        end.linkTo(airplane.end)
                    }
            ) {
                items(seatList.size) { index ->
                    val seat = seatList[index]
                    SeatItem(
                        seat = seat,
                        onSeatClick = {
                            when (seat.status) {
                                SeatStatus.AVAILABLE, SeatStatus.PREMIUM_AVAILABLE -> {
                                    seat.status = when (seat.seatClass) {
                                        "economy" -> SeatStatus.SELECTED
                                        else -> SeatStatus.PREMIUM_SELECTED
                                    }
                                    selectedSeatNames.add(seat.name)
                                }

                                SeatStatus.SELECTED, SeatStatus.PREMIUM_SELECTED -> {
                                    seat.status = when (seat.seatClass) {
                                        "economy" -> SeatStatus.AVAILABLE
                                        else -> SeatStatus.PREMIUM_AVAILABLE
                                    }
                                    selectedSeatNames.remove(seat.name)
                                }

                                else -> {
                                }
                            }
                            updatePriceAndCount()
                        }
                    )
                }
            }
        }

        BottomSection(
            seatCount = seatCount,
            selectedSeats = selectedSeatNames.joinToString(","),
            totalPrice = totalPrice,
            onConfirmClick = {
                if (seatCount > 0) {
                    val currentUser = FirebaseAuth.getInstance().currentUser

                    if (currentUser != null) {
                        // Create booking model
                        val booking = BookingModel(
                            id = "", // Will be generated by Firebase
                            userId = currentUser.uid,
                            userEmail = currentUser.email ?: "",
                            userName = currentUser.displayName ?: "User",
                            flightId = flight.id, // Make sure FlightModel has id field
                            seatClass = selectedClass,
                            selectedSeats = selectedSeatNames.toList(),
                            totalPrice = totalPrice,
                            bookingDate = System.currentTimeMillis().toString(),
                            status = "confirmed"
                        )

                        // Update flight reserved seats (this will be handled in repository)
                        val updatedFlight = flight.copy(
                            seatConfiguration = flight.seatConfiguration.mapValues { (classKey, classConfig) ->
                                if (classKey == selectedClass) {
                                    classConfig.copy(
                                        reservedSeats = classConfig.reservedSeats + selectedSeatNames
                                    )
                                } else {
                                    classConfig
                                }
                            }
                        )

                        onConfirm(updatedFlight, booking)
                    } else {
                        Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Please select your seat", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.constrainAs(bottomSection) {
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        )

    }

}

fun generateSeatList(flight: FlightModel, selectedClass: String): List<Seat> {
    val classInfo = flight.seatConfiguration[selectedClass]
        ?: return emptyList()

    return when (selectedClass) {
        "economy" -> generateEconomySeats(classInfo)
        "business" -> generateBusinessSeats(classInfo)
        "first" -> generateFirstClassSeats(classInfo)
        else -> generateEconomySeats(classInfo)
    }
}

private fun generateEconomySeats(classInfo: SeatClassModel): List<Seat> {
    val seatList = mutableListOf<Seat>()
    val seatLetters = listOf("A", "B", "C", "D", "E", "F") // 3-3 configuration
    val startRow = extractStartRow(classInfo.rowRange)

    for (row in startRow until (startRow + classInfo.rows)) {
        for (i in seatLetters.indices) {
            if (i == 3) {
                // Add aisle space
                seatList.add(Seat(
                    status = SeatStatus.EMPTY,
                    name = "${row}_aisle",
                    seatClass = "economy",
                    isAisle = true))
            }

            val seatName = "${row}${seatLetters[i]}"
            val seatStatus = if (classInfo.reservedSeats.contains(seatName)) {
                SeatStatus.UNAVAILABLE
            } else {
                SeatStatus.AVAILABLE
            }

            val isWindow = i == 0 || i == seatLetters.size - 1
            val isAisle = i == 2 || i == 3

            seatList.add(
                Seat(
                    status = seatStatus,
                    name = seatName,
                    seatClass = "economy",
                    isAisle = isAisle,
                    isWindow = isWindow
                )
            )
        }
    }

    return seatList
}

private fun generateBusinessSeats(classInfo: SeatClassModel): List<Seat> {
    val seatList = mutableListOf<Seat>()
    val seatLetters = listOf("A", "C", "D", "F") // 2-2 configuration
    val startRow = extractStartRow(classInfo.rowRange)

    for (row in startRow until (startRow + classInfo.rows)) {
        for (i in seatLetters.indices) {
            if (i == 2) {
                // Add aisle space
                seatList.add(Seat(SeatStatus.EMPTY, "${row}_aisle", "business", true))
            }

            val seatName = "${row}${seatLetters[i]}"
            val seatStatus = if (classInfo.reservedSeats.contains(seatName)) {
                SeatStatus.PREMIUM_UNAVAILABLE
            } else {
                SeatStatus.PREMIUM_AVAILABLE
            }

            val isWindow = i == 0 || i == seatLetters.size - 1
            val isAisle = i == 1 || i == 2

            seatList.add(
                Seat(
                    status = seatStatus,
                    name = seatName,
                    seatClass = "business",
                    isAisle = isAisle,
                    isWindow = isWindow
                )
            )
        }
    }

    return seatList
}

private fun generateFirstClassSeats(classInfo: SeatClassModel): List<Seat> {
    val seatList = mutableListOf<Seat>()
    val seatLetters = listOf("A", "C", "D", "F") // 2-2 configuration, more spacious
    val startRow = extractStartRow(classInfo.rowRange)

    for (row in startRow until (startRow + classInfo.rows)) {
        for (i in seatLetters.indices) {
            if (i == 2) {
                // Add wide aisle space
                seatList.add(Seat(SeatStatus.EMPTY, "${row}_aisle", "first"))
                seatList.add(Seat(SeatStatus.EMPTY, "${row}_aisle2", "first"))
            }

            val seatName = "${row}${seatLetters[i]}"
            val seatStatus = if (classInfo.reservedSeats.contains(seatName)) {
                SeatStatus.PREMIUM_UNAVAILABLE
            } else {
                SeatStatus.PREMIUM_AVAILABLE
            }

            val isWindow = i == 0 || i == seatLetters.size - 1
            val isAisle = i == 1 || i == 2

            seatList.add(
                Seat(
                    status = seatStatus,
                    name = seatName,
                    seatClass = "first",
                    isAisle = isAisle,
                    isWindow = isWindow
                )
            )
        }
    }

    return seatList
}

private fun extractStartRow(rowRange: String): Int {
    return try {
        rowRange.split("-")[0].toInt()
    } catch (e: Exception) {
        1
    }
}

fun getGridColumnsForClass(seatClass: String): Int {
    return when (seatClass) {
        "economy" -> 7 // 3 + 1 (aisle) + 3
        "business" -> 5 // 2 + 1 (aisle) + 2
        "first" -> 6   // 2 + 2 (wide aisle) + 2
        else -> 7
    }
}
