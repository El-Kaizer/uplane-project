package com.uilover.project2262.Activities.TicketDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.uilover.project2262.Activities.SeatSelect.TicketDetailHeader
import com.uilover.project2262.Activities.Splash.GradientButton
import com.uilover.project2262.Domain.AirlineModel
import com.uilover.project2262.Domain.BookingModel
import com.uilover.project2262.Domain.FlightModel
import com.uilover.project2262.Domain.LocationModel
import com.uilover.project2262.R

@Composable
fun TicketDetailScreen(
    flight: FlightModel,
    booking: BookingModel,
    airline: AirlineModel?,
    departureAirport: LocationModel?,
    arrivalAirport: LocationModel?,
    onBackClick: () -> Unit,
    onDownloadTicketClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.darkPurple2))
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .background(colorResource(R.color.darkPurple2))
        ) {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorResource(R.color.darkPurple2))
            ) {
                val (topSection, ticketDetail, bottomSection) = createRefs()

                TicketDetailHeader(
                    onBackClick = onBackClick,
                    Modifier.constrainAs(topSection) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    })

                TicketDetailContent(
                    flight = flight,
                    booking = booking,
                    airline = airline,
                    departureAirport = departureAirport,
                    arrivalAirport = arrivalAirport,
                    modifier = Modifier.constrainAs(ticketDetail) {
                        top.linkTo(parent.top, margin = 110.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                )
            }

            GradientButton(
                onClick = onDownloadTicketClick,  // Jangan lupa pakai param callbacknya
                text = "Download Ticket"
            )
        }
    }
}