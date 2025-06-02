@Composable
fun FlightItem(searchResult: FlightSearchModel, index: Int) {
    val context = LocalContext.current
    val flight = searchResult.flight
    val classInfo = searchResult.classInfo // classInfo ini mungkin null jika tidak ada di seatConfiguration

    ConstraintLayout(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clickable {
                val intent = Intent(context, SeatSelectActivity::class.java).apply {
                    putExtra("flight", flight)
                    putExtra("selectedClass", searchResult.selectedClass)
                    // Anda mungkin juga ingin mengirim classInfo atau detail lain yang relevan ke SeatSelectActivity
                    // putExtra("classInfo", classInfo) // Pastikan ClassInfoModel Parcelable/Serializable
                }
                startActivity(context, intent, null)
            }
            .background(
                color = getClassBackgroundColor(searchResult.selectedClass),
                shape = RoundedCornerShape(15.dp)
            )
    ) {
        val (logo, timeTxt, airplaneIcon, dashLine, priceTxt, seatIcon, classTxt,
            availableSeatsTxt, fromTxt, fromShortTxt, toTxt, toShortTxt) = createRefs()

        // Class indicator badge
        Text(
            text = searchResult.selectedClass.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .background(
                    color = getClassAccentColor(searchResult.selectedClass),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
                .constrainAs(classTxt) {
                    top.linkTo(parent.top, margin = 8.dp)
                    end.linkTo(parent.end, margin = 8.dp)
                }
        )

        AsyncImage(
            model = flight.airlineLogo,
            contentDescription = null,
            modifier = Modifier
                .size(200.dp, 50.dp) // Ukuran ini mungkin terlalu besar, sesuaikan
                .constrainAs(logo) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                }
        )

        Text(
            text = "${flight.departureTime} - ${flight.arrivalTime}",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = colorResource(R.color.darkPurple2),
            modifier = Modifier
                .padding(top = 8.dp)
                .constrainAs(timeTxt) {
                    start.linkTo(parent.start)
                    top.linkTo(logo.bottom)
                    end.linkTo(parent.end)
                }
        )

        Image(
            painter = painterResource(R.drawable.line_airple_blue),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 8.dp)
                .constrainAs(airplaneIcon) {
                    start.linkTo(parent.start)
                    top.linkTo(timeTxt.bottom)
                    end.linkTo(parent.end)
                }
        )

        Image(
            painter = painterResource(R.drawable.dash_line),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 8.dp)
                .constrainAs(dashLine) {
                    start.linkTo(parent.start)
                    top.linkTo(airplaneIcon.bottom)
                    end.linkTo(parent.end)
                }
        )

        // Gunakan nilai dari classInfo jika ada, fallback ke searchResult.price jika tidak
        Text(
            text = "Rp ${String.format("%,.0f", classInfo?.price ?: searchResult.price)}", // Gunakan classInfo.price
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            color = colorResource(R.color.orange),
            modifier = Modifier
                .padding(8.dp)
                .constrainAs(priceTxt) {
                    top.linkTo(dashLine.bottom)
                    end.linkTo(parent.end)
                }
        )

        // Gunakan nilai dari classInfo jika ada, fallback ke 0 jika tidak
        Text(
            text = "${classInfo?.availableSeats ?: 0} seats left", // Gunakan classInfo.availableSeats
            fontSize = 12.sp,
            color = if ((classInfo?.availableSeats ?: 0) < 10) Color.Red else Color.Gray, // Gunakan classInfo.availableSeats
            modifier = Modifier
                .padding(8.dp)
                .constrainAs(availableSeatsTxt) {
                    top.linkTo(priceTxt.bottom)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
        )

        Image(
            painter = painterResource(getSeatIcon(searchResult.selectedClass)),
            contentDescription = null,
            modifier = Modifier
                .padding(8.dp)
                .constrainAs(seatIcon) {
                    start.linkTo(parent.start)
                    top.linkTo(dashLine.bottom)
                }
        )

        // Airport info remains the same...
        // Gunakan airportFromName dan airportToName yang sudah diperkaya
        Text(text = flight.airportFromName ?: flight.fromAirport, // Gunakan airportFromName
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier
                .padding(start = 16.dp)
                .constrainAs(fromTxt) {
                    top.linkTo(timeTxt.bottom)
                    start.linkTo(parent.start)
                }
        )

        Text(text = flight.fromAirport, // Ini biasanya kode bandara (CGK)
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier
                .padding(start = 16.dp)
                .constrainAs(fromShortTxt) {
                    top.linkTo(fromTxt.bottom)
                    start.linkTo(fromTxt.start)
                    end.linkTo(fromTxt.end)
                }
        )

        Text(text = flight.airportToName ?: flight.toAirport, // Gunakan airportToName
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier
                .padding(end = 16.dp)
                .constrainAs(toTxt) {
                    top.linkTo(timeTxt.bottom)
                    end.linkTo(parent.end)
                }
        )

        Text(text = flight.toAirport, // Ini biasanya kode bandara (DPS)
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier
                .constrainAs(toShortTxt) {
                    top.linkTo(toTxt.bottom)
                    start.linkTo(toTxt.start)
                    end.linkTo(toTxt.end)
                }
        )
    }
}

@Composable
fun getClassBackgroundColor(seatClass: String): Color { // Perhatikan import Color
    return when (seatClass.lowercase()) { // Gunakan lowercase untuk case-insensitivity
        "economy class", "economy" -> colorResource(R.color.lightPurple)
        "business class", "business" -> colorResource(R.color.blue)
        "first class", "first" -> colorResource(R.color.lightGold)
        else -> colorResource(R.color.lightPurple)
    }
}

@Composable
fun getClassAccentColor(seatClass: String): Color { // Perhatikan import Color
    return when (seatClass.lowercase()) { // Gunakan lowercase untuk case-insensitivity
        "economy class", "economy" -> colorResource(R.color.darkPurple2)
        "business class", "business" -> colorResource(R.color.darkBlue)
        "first class", "first" -> colorResource(R.color.darkGold)
        else -> colorResource(R.color.darkPurple2)
    }
}

fun getSeatIcon(seatClass: String): Int {
    return when (seatClass.lowercase()) { // Gunakan lowercase untuk case-insensitivity
        "economy class", "economy" -> R.drawable.seat_human
        "business class", "business" -> R.drawable.seat_human
        "first class", "first" -> R.drawable.seat_human
        else -> R.drawable.seat_black_ic
    }
}