package com.uilover.project2262.Activities.SearchResult

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
// Perhatikan: collectAsState digunakan untuk Flow, LiveData menggunakan observeAsState
// Jika Anda mengamati LiveData, Anda perlu androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState // Pastikan ini diimpor
import com.uilover.project2262.Activities.Splash.StatusTopBarColor
import com.uilover.project2262.ViewModel.ViewModelMain
import com.uilover.project2262.Domain.FlightSearchModel // Pastikan ini diimpor jika FlightSearchModel belum ada

class SearchResultActivity : AppCompatActivity() {
    private val viewModel by lazy { ViewModelMain() } // Gunakan lazy untuk inisialisasi ViewModel

    private var from: String = ""
    private var to: String = ""
    private var selectedClass: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        from = intent.getStringExtra("from") ?: ""
        to = intent.getStringExtra("to") ?: ""
        selectedClass = intent.getStringExtra("selectedClass")

        Log.d("SearchResultActivity", "Searching from: $from, to: $to, class: $selectedClass")

        setContent {
            StatusTopBarColor()

            // Gunakan observeAsState untuk mengamati LiveData
            // Pastikan initial = emptyList() sesuai dengan tipe data yang diharapkan
            val flights: List<FlightSearchModel> by viewModel.filteredFlights.observeAsState(initial = emptyList())

            // LaunchedEffect akan berjalan ketika kunci (from, to, selectedClass) berubah
            LaunchedEffect(from, to, selectedClass) {
                // Panggil setSearchParams di ViewModel untuk memicu pemuatan penerbangan
                viewModel.setSearchParams(from, to, selectedClass)
                Log.d("SearchResultActivity", "Triggered setSearchParams with parameters: from=$from, to=$to, class=$selectedClass")
            }

            // ItemListScreen akan menerima daftar flights yang diperbarui secara reaktif
            ItemListScreen(
                from = from,
                to = to,
                selectedClass = selectedClass,
                viewModel = viewModel,
                flights = flights, // Kirimkan data penerbangan yang sudah dimuat
                onBackClick = { finish() }
            )
        }
    }
}