package com.uilover.project2262.Activities.Dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.uilover.project2262.Activities.SearchResult.SearchResultActivity
import com.uilover.project2262.Activities.Splash.GradientButton
import com.uilover.project2262.Activities.Splash.StatusTopBarColor
import com.uilover.project2262.Domain.LocationModel
import com.uilover.project2262.R
import com.uilover.project2262.ViewModel.ViewModelMain

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainScreen()
        }
    }
}

@Composable
@Preview
fun MainScreen() {
    val locations = remember { mutableStateListOf<LocationModel>() }
    val viewModel = ViewModelMain()
    var showLocationLoading by remember { mutableStateOf(true) }
    val lifecycleOwner = LocalLifecycleOwner.current
    var from: String = ""
    var to: String = ""
    var classes: String = ""
    val context= LocalContext.current

    StatusTopBarColor()

    DisposableEffect(viewModel) {
        val liveData = viewModel.loadLocations()
        val observer = Observer<List<LocationModel>> { result ->
            locations.clear()
            locations.addAll(result)
            showLocationLoading = false
        }

        // ✅ Lifecycle-aware, otomatis cleanup
        liveData.observe(lifecycleOwner, observer)

        onDispose {
            // Cleanup otomatis by lifecycle, tapi bisa manual juga
            liveData.removeObserver(observer)
        }
    }

    Scaffold(
        bottomBar = { MyBottomBar() },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(color = colorResource(R.color.darkPurple2))
                .padding(paddingValues = paddingValues)
        ) {
            item { TopBar() }
            item {
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .background(
                            colorResource(R.color.darkPurple), shape = RoundedCornerShape(20.dp)
                        )
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 24.dp)
                ) {

                    // From Section
                    YellowTitle("From")

                    val locationNames: List<String> = remember(locations.size) {
                        locations.mapNotNull { location ->
                            if (!location.city.isNullOrBlank()) {
                                location.city // atau "${location.city} (${location.code})"
                            } else null
                        }
                    }

                    DropDownList(
                        items = locationNames,
                        loadingIcon = painterResource(R.drawable.from_ic),
                        hint = if (locationNames.isEmpty()) "No locations available" else "Select origin",
                        showLocationLoading = showLocationLoading,
                        selectedValue = from
                    ) { selectedItem ->
                        from = selectedItem
                        Log.d("DropdownSelection", "Selected from: $selectedItem")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // To Section
                    YellowTitle("To")

                    DropDownList(
                        items = locationNames,
                        loadingIcon = painterResource(R.drawable.to_ic), // Pastikan icon ini ada
                        hint = if (locationNames.isEmpty()) "No locations available" else "Select destination",
                        showLocationLoading = showLocationLoading,
                        selectedValue = to
                    ) { selectedItem ->
                        to = selectedItem
                        Log.d("DropdownSelection", "Selected to: $selectedItem")
                    }

                    //calender Picker
                    Spacer(modifier = Modifier.height(16.dp))
                    Row{
                        YellowTitle("Departure date",Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(16.dp))
                        YellowTitle("Return date",Modifier.weight(1f))
                    }
                    DatePickerScreen(Modifier.weight(1f))

                    Spacer(modifier = Modifier.height(16.dp))

                    //classes Section
                    YellowTitle("classes")
                    val classItems= listOf("Business class","First class","Economy Class")
                    DropDownList(
                        items = classItems,
                        loadingIcon = painterResource(R.drawable.seat_black_ic),
                        hint = "Select class",
                        showLocationLoading = showLocationLoading
                    ) { selectedItem ->
                        classes  = selectedItem
                    }

                    //Search Button
                    Spacer(modifier = Modifier.height(16.dp))
                    GradientButton(
                        onClick = {
                            val intent=Intent(context,SearchResultActivity::class.java).apply {
                                putExtra("from",from)
                                putExtra("to",to)
                                putExtra("selectedClass",classes)
                            }
                            startActivity(context,intent,null)
                        },
                        text="Search",
                    )
                }
            }
        }
    }
}


@Composable
fun YellowTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontWeight = FontWeight.SemiBold,
        color = colorResource(R.color.orange),
        modifier = modifier
    )
}