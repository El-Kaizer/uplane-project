package com.uilover.project2262.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap // Import fungsi ekstensi switchMap
// Tidak perlu lagi import androidx.lifecycle.Transformations secara eksplisit jika hanya menggunakan switchMap atau map sebagai ekstensi
import com.uilover.project2262.Domain.FlightModel
import com.uilover.project2262.Domain.FlightSearchModel
import com.uilover.project2262.Domain.LocationModel
import com.uilover.project2262.Repository.MainRepository // Assuming MainRepository is the correct name

class ViewModelMain : ViewModel() {

    private val repository = MainRepository()

    fun loadLocations(): LiveData<List<LocationModel>> {
        return repository.loadLocations()
    }

    private val _searchParams = MutableLiveData<Triple<String, String, String?>>()

    // LiveData yang akan secara reaktif memuat daftar penerbangan yang difilter.
    // Menggunakan fungsi ekstensi 'switchMap' langsung pada _searchParams.
    // Ketika _searchParams berubah, switchMap akan membatalkan observasi sebelumnya
    // dan memulai observasi baru dari LiveData yang dikembalikan oleh repository.
    val filteredFlights: LiveData<List<FlightSearchModel>> = _searchParams.switchMap { params ->
        val (from, to, seatClass) = params
        // Gunakan instance 'repository' yang sudah dideklarasikan
        repository.loadFilteredFlights(from, to, seatClass)
    }

    // Fungsi untuk mengatur parameter pencarian dan memicu pemuatan penerbangan
    fun setSearchParams(from: String, to: String, selectedClass: String?) {
        // Hanya update jika parameter berbeda untuk menghindari pemuatan ulang yang tidak perlu
        if (_searchParams.value?.first != from ||
            _searchParams.value?.second != to ||
            _searchParams.value?.third != selectedClass) {
            _searchParams.value = Triple(from, to, selectedClass)
        }
    }
}