package com.uilover.project2262.Activities.SearchResult

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator // androidx.compose.material bukan material3
import androidx.compose.material3.Text // Ini material3
import androidx.compose.material.icons.Icons // Untuk ikon AppBar
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Untuk ikon AppBar
import androidx.compose.material3.ExperimentalMaterial3Api // Untuk TopAppBar
import androidx.compose.material3.Icon // Untuk ikon AppBar
import androidx.compose.material3.IconButton // Untuk ikon AppBar
import androidx.compose.material3.Scaffold // Untuk struktur layout dasar
import androidx.compose.material3.TopAppBar // Untuk AppBar
import androidx.compose.material3.TopAppBarDefaults // Untuk warna AppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect // Dipertahankan untuk isLoading yang lebih halus
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout // Tetap pakai ConstraintLayout jika itu layout yang Anda inginkan
import com.uilover.project2262.R
import com.uilover.project2262.Domain.FlightSearchModel // Pastikan ini diimpor
// ViewModel tidak perlu diimpor di sini lagi karena tidak memuat data secara langsung

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemListScreen(
    from: String,
    to: String,
    selectedClass: String? = null,
    // ViewModelMain tidak perlu lagi karena data sudah diterima via parameter 'flights'
    // viewModel: ViewModelMain, // Hapus ini
    flights: List<FlightSearchModel>, // <-- Ini adalah parameter kunci yang menerima data
    onBackClick: () -> Unit
) {
    // isLoading state tetap bisa dipertahankan untuk indikator visual
    var isLoading by remember(flights) { mutableStateOf(true) } // Reset isLoading saat flights berubah

    // Set isLoading ke false saat flights pertama kali diterima atau berubah
    LaunchedEffect(flights) {
        if (flights.isNotEmpty() || (flights.isEmpty() && !isLoading)) {
            // Jika flights tidak kosong, atau jika kosong tapi sudah tidak loading sebelumnya,
            // artinya data sudah selesai dimuat (bisa jadi kosong)
            isLoading = false
        }
    }

    // Menggunakan Scaffold untuk struktur dasar yang direkomendasikan Material Design
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Flights from $from to $to",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.darkPurple)
                )
            )
        },
        containerColor = colorResource(R.color.darkPurple2) // Background untuk seluruh scaffold
    ) { paddingValues -> // Padding dari Scaffold
        // Konten utama, yang sekarang bisa menggunakan ConstraintLayout Anda
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Terapkan padding dari Scaffold
                .background(color = colorResource(R.color.darkPurple2))
        ) {
            val (headerContent, listContent) = createRefs() // Rename agar lebih jelas

            // Header Section - Sekarang lebih sederhana karena TopAppBar sudah ada
            // Anda bisa memindahkan elemen "Search Result" dan "world" ke sini
            // atau menyesuaikan TopAppBar untuk menampungnya.
            // Untuk contoh ini, saya akan menyertakan elemen gambar dunia dan teks "Search Result"
            // di dalam kolom terpisah di bagian atas, atau Anda bisa mengintegrasikannya ke TopAppBar jika diinginkan.
            // Saya akan mengasumsikan Anda ingin mempertahankan tata letak visual "header" Anda.

            Column( // Gunakan Column untuk menampung elemen header kustom Anda
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .constrainAs(headerContent) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            ) {
                // Gambar dunia (jika Anda ingin ini di bawah TopAppBar)
                Image(
                    painter = painterResource(R.drawable.world),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.CenterHorizontally) // Posisikan di tengah
                )
                Text(
                    text = "Search Result",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp) // Tambahkan padding atas jika perlu
                )
            }


            // Show list content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp) // Padding dari headerContent
                    .constrainAs(listContent) {
                        top.linkTo(headerContent.bottom) // Link ke bawah headerContent
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    }
            ) {
                when {
                    // isLoading awal adalah true, baru false setelah flights di-emit
                    isLoading -> {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    flights.isEmpty() && !isLoading -> { // Hanya tampilkan ini jika sudah tidak loading DAN daftar kosong
                        Text(
                            text = "No flights found\nfrom $from to $to" +
                                    if (selectedClass != null) "\nfor $selectedClass class" else "",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(flights) { index, searchResult ->
                                FlightItem(searchResult = searchResult, index = index)
                            }
                        }
                    }
                }
            }
        }
    }
}