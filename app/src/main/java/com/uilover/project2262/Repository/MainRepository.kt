package com.uilover.project2262.Repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.google.android.gms.tasks.Tasks
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.uilover.project2262.Domain.AirlineModel
import com.uilover.project2262.Domain.BookingModel
import com.uilover.project2262.Domain.FlightModel
import com.uilover.project2262.Domain.LocationModel
import com.uilover.project2262.Domain.FlightSearchModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// DUAL DATABASE APPROACH
// Android: Primary RTDB, Secondary Firestore (untuk sync ke admin website)
// Admin Website: Firestore

class MainRepository {

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    suspend fun registerUser(
        name: String,
        email: String,
        password: String
    ): Result<Boolean> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            authResult.user?.updateProfile(profileUpdates)?.await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(
        email: String,
        password: String
    ): Result<Boolean> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUser() = firebaseAuth.currentUser

    fun logout() {
        firebaseAuth.signOut()
    }

    fun loadLocations(): LiveData<List<LocationModel>> {
        val database = FirebaseDatabase.getInstance().getReference("dataBandara")
        val locationLiveData = MutableLiveData<List<LocationModel>>()

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val locations = mutableListOf<LocationModel>()
                for (entry in snapshot.children) {
                    for (bandaraSnapshot in entry.children) {
                        val map = bandaraSnapshot.value as? Map<String, Any>
                        if (map != null) {
                            val location = LocationModel().apply {
                                key = bandaraSnapshot.key
                                city = map["city"] as? String ?: ""
                                code = map["code"] as? String ?: ""
                                latitude = (map["latitude"] as? Number)?.toDouble() ?: 0.0
                                longitude = (map["longitude"] as? Number)?.toDouble() ?: 0.0
                                name = map["name"] as? String ?: ""
                                region = map["region"] as? String ?: ""
                            }
                            locations.add(location)
                        }
                    }
                }
                locationLiveData.value = locations
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
            }
        })

        return locationLiveData
    }

    fun loadFilteredFlights(
        from: String,
        to: String,
        seatClass: String? = null
// Perbaikan di sini: Ubah return type menjadi LiveData<List<FlightSearchModel>>
    ): LiveData<List<FlightSearchModel>> = liveData(Dispatchers.IO) { // <-- PENTING: Jalankan di IO dispatcher
        try {
            emit(emptyList()) // Emit emptyList() lebih sesuai untuk List<T> daripada mutableListOf()

            // Step 1: Get flights
            val flightSnapshot = Tasks.await(
                firebaseDatabase.getReference("schedules")
                    .orderByChild("fromAirport")
                    .equalTo(from)
                    .get()
            )

            // Step 2: Filter flights berdasarkan destination dan class
            val filteredFlights = mutableListOf<FlightModel>()
            for (child in flightSnapshot.children) {
                val flight = child.getValue(FlightModel::class.java)
                if (flight != null && flight.toAirport == to) {
                    // Filter berdasarkan class jika ditentukan
                    if (seatClass == null || (flight.availableClasses != null && flight.availableClasses.contains(seatClass))) {
                        filteredFlights.add(flight)
                    }
                }
            }

            // Step 3: Load additional data (airlines, airports)
            val enhancedFlights = loadAdditionalFlightData(filteredFlights)

            // Step 4: Convert to FlightSearchModel dengan class info
            val searchResults = mutableListOf<FlightSearchModel>()
            for (flight in enhancedFlights) {
                if (seatClass != null) {
                    // Hanya kembalikan kelas yang spesifik
                    val classInfo = flight.seatConfiguration?.get(seatClass)
                    if (classInfo != null) {
                        searchResults.add(
                            FlightSearchModel(flight, seatClass, classInfo)
                        )
                    }
                } else {
                    // Kembalikan semua kelas yang tersedia untuk setiap penerbangan
                    flight.availableClasses?.forEach { availableClass ->
                        val classInfo = flight.seatConfiguration?.get(availableClass)
                        if (classInfo != null) {
                            searchResults.add(
                                FlightSearchModel(flight, availableClass, classInfo)
                            )
                        }
                    }
                }
            }

            // Emit hasil akhir sebagai List (bukan MutableList)
            emit(searchResults.toList()) // Konversi ke immutable List sebelum emit

        } catch (e: Exception) {
            Log.e("FlightRepository", "Error loading flights", e)
            emit(emptyList()) // Emit daftar kosong pada error
        }
    }

    // Fungsi suspend untuk memuat data tambahan
    private suspend fun loadAdditionalFlightData(flights: List<FlightModel>): List<FlightModel> {
        return withContext(Dispatchers.IO) {
            val enhancedFlights = mutableListOf<FlightModel>()
            val firebaseDatabase = FirebaseDatabase.getInstance() // Asumsi ini diinisialisasi dengan benar

            // Ambil semua data maskapai dan bandara sekaligus untuk efisiensi
            // Ini akan membantu mengurangi jumlah panggilan Firebase di dalam loop
            val allAirlinesSnapshot = Tasks.await(firebaseDatabase.getReference("airlines").get())
            val allAirportsSnapshot = Tasks.await(firebaseDatabase.getReference("dataBandara").get()) // Asumsi dataBandara juga punya struktur serupa atau sudah dipahami

            // Buat map untuk pencarian cepat
            val airlineMap = mutableMapOf<String, AirlineModel>()
            for (child in allAirlinesSnapshot.children) {
                // Iterasi melalui 0, 1, 2...
                for (grandchild in child.children) { // Ini akan mengakses DELTA, GARUDA, dst.
                    val airline = grandchild.getValue(AirlineModel::class.java)
                    airline?.id?.let { id -> // Asumsi AirlineModel memiliki properti 'id'
                        airlineMap[id] = airline
                    }
                }
            }

            val airportMap = mutableMapOf<String, LocationModel>()
            // Asumsi dataBandara juga memiliki struktur key numerik seperti airlines
            // Jika dataBandara tidak memiliki key numerik, Anda bisa langsung akses .child(key)
            // Saya asumsikan ia memiliki struktur yang sama dengan airlines untuk demonstrasi
            for (child in allAirportsSnapshot.children) {
                for (grandchild in child.children) { // Ini akan mengakses CGK, DPS, dst.
                    val airport = grandchild.getValue(LocationModel::class.java)
                    airport?.code?.let { code -> // Asumsi LocationModel memiliki properti 'code' (misal: "CGK")
                        airportMap[code] = airport
                    }
                }
            }

            for (flight in flights) {
                try {
                    // Temukan maskapai dari map yang sudah dimuat
                    val airline = airlineMap[flight.airline] // flight.airline sekarang adalah ID seperti "DELTA"

                    // Temukan bandara 'from' dari map
                    val fromAirport = airportMap[flight.fromAirport]

                    // Temukan bandara 'to' dari map
                    val toAirport = airportMap[flight.toAirport]

                    // Buat objek flight yang diperkaya
                    val enhancedFlight = flight.copy()
                    airline?.let {
                        enhancedFlight.airlineName = it.name
                        enhancedFlight.airlineLogo = it.logo
                    }
                    fromAirport?.let { enhancedFlight.airportFromName = it.name }
                    toAirport?.let { enhancedFlight.airportToName = it.name }

                    enhancedFlights.add(enhancedFlight)

                } catch (e: Exception) {
                    Log.w("FlightRepository", "Failed to enhance flight ${flight.id}: ${e.message}", e)
                    enhancedFlights.add(flight) // Tambahkan flight asli jika proses pengayaan gagal
                }
            }
            enhancedFlights
        }
    }

    suspend fun createBooking(flight: FlightModel, booking: BookingModel): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Generate booking ID
                val bookingId = "booking_${System.currentTimeMillis()}"

                // Add current timestamp for bookingDate if not provided
                val currentTime = System.currentTimeMillis()
                val bookingWithId = booking.copy(
                    id = bookingId,
                    bookingDate = if (booking.bookingDate.isEmpty()) {
                        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault()).format(java.util.Date(currentTime))
                    } else {
                        booking.bookingDate
                    }
                )

                // 1. PRIORITY: Save to Realtime Database (Primary for Android)
                val realtimeRef = firebaseDatabase.reference.child("bookings").child(bookingId)
                realtimeRef.setValue(bookingWithId).await()

                // 2. Update flight reserved seats in RTDB
                val flightRef = firebaseDatabase.reference
                    .child("schedules")
                    .child(flight.id)
                    .child("seatConfiguration")
                    .child(booking.seatClass)
                    .child("reservedSeats")

                val currentReservedSeats = flightRef.get().await()
                val reservedSeatsList = currentReservedSeats.value as? List<String> ?: emptyList()
                val updatedReservedSeats = reservedSeatsList + booking.selectedSeats
                flightRef.setValue(updatedReservedSeats).await()

                // 3. SECONDARY: Sync to Firestore (for Admin website)
                try {
                    firestore.collection("bookings")
                        .document(bookingId)
                        .set(bookingWithId)
                        .await()
                } catch (firestoreError: Exception) {
                    // Log Firestore error but don't fail the operation
                    Log.w("MainRepository", "Firestore sync failed, but RTDB succeeded", firestoreError)
                }

                Result.success(bookingId)
            } catch (e: Exception) {
                Log.e("MainRepository", "Error creating booking", e)
                Result.failure(e)
            }
        }
    }

    // ANDROID: Get user bookings from RTDB (Primary source)
    suspend fun getUserBookings(userId: String): Result<List<BookingModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = firebaseDatabase.reference
                    .child("bookings")
                    .orderByChild("userId")
                    .equalTo(userId)
                    .get()
                    .await()

                val bookings = mutableListOf<BookingModel>()
                for (childSnapshot in snapshot.children) {
                    val booking = childSnapshot.getValue(BookingModel::class.java)
                    booking?.let { bookings.add(it) }
                }

                Result.success(bookings)
            } catch (e: Exception) {
                Log.e("MainRepository", "Error getting user bookings from RTDB", e)
                Result.failure(e)
            }
        }
    }

    // ANDROID: Get user bookings with LiveData (Real-time updates)
    fun getUserBookingsLiveData(userId: String): LiveData<MutableList<BookingModel>> {
        val result = MutableLiveData<MutableList<BookingModel>>()

        val ref = firebaseDatabase.getReference("bookings")
            .orderByChild("userId")
            .equalTo(userId)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bookings = mutableListOf<BookingModel>()
                for (childSnapshot in snapshot.children) {
                    val booking = childSnapshot.getValue(BookingModel::class.java)
                    booking?.let { bookings.add(it) }
                }
                result.value = bookings
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainRepository", "Failed to load user bookings: ${error.message}")
                result.value = mutableListOf()
            }
        })

        return result
    }

    // ANDROID: Get specific booking from RTDB
    suspend fun getBookingDetails(bookingId: String): Result<BookingModel?> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = firebaseDatabase.reference
                    .child("bookings")
                    .child(bookingId)
                    .get()
                    .await()

                val booking = snapshot.getValue(BookingModel::class.java)
                Result.success(booking)
            } catch (e: Exception) {
                Log.e("MainRepository", "Error getting booking details from RTDB", e)
                Result.failure(e)
            }
        }
    }

    // ANDROID: Cancel booking (Update both databases)
    suspend fun cancelBooking(
        bookingId: String,
        flightId: String,
        seatClass: String,
        seatsToRelease: List<String>
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. PRIORITY: Update RTDB (Primary for Android)
                val realtimeBookingRef = firebaseDatabase.reference.child("bookings").child(bookingId)
                realtimeBookingRef.child("status").setValue("cancelled").await()

                // 2. Release seats in RTDB
                val reservedSeatsRef = firebaseDatabase.reference
                    .child("schedules")
                    .child(flightId)
                    .child("seatConfiguration")
                    .child(seatClass)
                    .child("reservedSeats")

                val currentReservedSeats = reservedSeatsRef.get().await()
                val reservedSeatsList = currentReservedSeats.value as? List<String> ?: emptyList()
                val updatedReservedSeats = reservedSeatsList - seatsToRelease
                reservedSeatsRef.setValue(updatedReservedSeats).await()

                // 3. SECONDARY: Sync to Firestore (for Admin website)
                try {
                    firestore.collection("bookings")
                        .document(bookingId)
                        .update("status", "cancelled")
                        .await()
                } catch (firestoreError: Exception) {
                    Log.w("MainRepository", "Firestore sync failed during cancellation", firestoreError)
                }

                Result.success(true)
            } catch (e: Exception) {
                Log.e("MainRepository", "Error cancelling booking", e)
                Result.failure(e)
            }
        }
    }

    // ANDROID: Update booking status (Update both databases)
    suspend fun updateBookingStatus(bookingId: String, newStatus: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. PRIORITY: Update RTDB
                firebaseDatabase.reference
                    .child("bookings")
                    .child(bookingId)
                    .child("status")
                    .setValue(newStatus)
                    .await()

                // 2. SECONDARY: Sync to Firestore (for Admin website)
                try {
                    firestore.collection("bookings")
                        .document(bookingId)
                        .update("status", newStatus)
                        .await()
                } catch (firestoreError: Exception) {
                    Log.w("MainRepository", "Firestore sync failed during status update", firestoreError)
                }

                Result.success(true)
            } catch (e: Exception) {
                Log.e("MainRepository", "Error updating booking status", e)
                Result.failure(e)
            }
        }
    }

    // UTILITY: Sync specific booking from RTDB to Firestore (manual sync if needed)
    suspend fun syncBookingToFirestore(bookingId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // Get from RTDB
                val snapshot = firebaseDatabase.reference
                    .child("bookings")
                    .child(bookingId)
                    .get()
                    .await()

                val booking = snapshot.getValue(BookingModel::class.java)
                if (booking != null) {
                    // Save to Firestore
                    firestore.collection("bookings")
                        .document(bookingId)
                        .set(booking)
                        .await()
                    Result.success(true)
                } else {
                    Result.failure(Exception("Booking not found in RTDB"))
                }
            } catch (e: Exception) {
                Log.e("MainRepository", "Error syncing booking to Firestore", e)
                Result.failure(e)
            }
        }
    }
}