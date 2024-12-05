package com.cha1se.cyberzillatestapp.presentation.screens

import android.content.Context
import android.graphics.Path.Op
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cha1se.cyberzillatestapp.R
import com.cha1se.cyberzillatestapp.data.EventsRepositoryImpl
import com.cha1se.cyberzillatestapp.data.NetworkApi
import com.cha1se.cyberzillatestapp.data.models.Event
import com.cha1se.cyberzillatestapp.presentation.MainActivity
import com.cha1se.cyberzillatestapp.presentation.helpers.Option
import com.cha1se.cyberzillatestapp.presentation.helpers.Resource
import com.cha1se.cyberzillatestapp.presentation.ui.theme.AccentColor
import com.cha1se.cyberzillatestapp.presentation.ui.theme.Background
import com.cha1se.cyberzillatestapp.presentation.ui.theme.CyberZillaTestAppTheme
import com.cha1se.cyberzillatestapp.presentation.ui.theme.LightPrimaryColor
import com.cha1se.cyberzillatestapp.presentation.ui.theme.PrimaryColor
import com.cha1se.cyberzillatestapp.presentation.ui.theme.Roboto
import com.cha1se.cyberzillatestapp.presentation.ui.theme.RobotoMedium
import com.cha1se.cyberzillatestapp.presentation.ui.theme.TextColor
import com.cha1se.cyberzillatestapp.presentation.viewmodels.MainActivityViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.text.SimpleDateFormat
import kotlin.math.roundToInt

lateinit var vm: MainActivityViewModel

@Composable
fun MainScreen(navController: NavHostController, viewModel: MainActivityViewModel) {
    vm = viewModel
    CyberZillaTestAppTheme {
        val systemUiController = rememberSystemUiController()
        systemUiController.setSystemBarsColor(
            color = AccentColor
        )
        Scaffold(modifier = Modifier.fillMaxSize(), containerColor = Background) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                Header(vm = vm, context = navController.context)
                ListEvents(navController = navController)
            }
        }
    }
}

@Composable
fun ListEvents(modifier: Modifier = Modifier, navController: NavHostController) {
    val listEvents = vm.eventsList.collectAsState()
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .nestedScroll(rememberNestedScrollInteropConnection())
    ) {
        when (listEvents.value) {
            is Resource.Loading -> {
                item(1) {
                    Box(modifier = Modifier.fillMaxWidth(),contentAlignment = Alignment.Center) {
                        Text(
                            text = "Loading...",
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .clip(CircleShape).background(PrimaryColor).padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            is Resource.Success -> {
                items(listEvents.value.data!!.list) { event ->
                    CardEvent(navController, event = event)
                }
            }
            is Resource.Error -> {
                Toast.makeText(navController.context, listEvents.value.message!!, Toast.LENGTH_LONG).show()
            }
        }
    }
}

@Composable
fun Header(
    modifier: Modifier = Modifier, vm: MainActivityViewModel, context: Context
) {
    val locationName = vm.locationName.collectAsState()

    Column {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(AccentColor)
        ) {
            Text(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                text = "Your location's events",
                color = TextColor,
                fontFamily = RobotoMedium,
                fontSize = 20.sp
            )
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clickable {
                        (context as MainActivity).getLastLocation(context)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = TextColor,
                    modifier = Modifier
                        .width(20.dp)
                        .wrapContentHeight()
                )
                Text(
                    modifier = Modifier.padding(end = 4.dp),
                    text = locationName.value ?: "Loading...",
                    color = TextColor,
                    fontSize = 12.sp
                )
            }
        }
        Filter(Modifier.align(Alignment.End), context)
    }

}

@Composable
fun Filter(modifier: Modifier = Modifier, context: Context) {
    var expanded by remember { mutableStateOf(false) }
    val itemColors = MenuItemColors(
        textColor = Color.White,
        leadingIconColor = Color.White,
        trailingIconColor = Color.White,
        disabledTextColor = Color.White,
        disabledLeadingIconColor = PrimaryColor,
        disabledTrailingIconColor = LightPrimaryColor
    )
    var selectedOption = vm.filterOptionState.collectAsState()
    var isAscending = remember { mutableStateOf(selectedOption.value.isAscending) }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(bottomEnd = 8.dp, bottomStart = 8.dp))
            .background(AccentColor),
    ) {
        Icon(
            painterResource(R.drawable.filter),
            tint = Color.White,
            contentDescription = "filter",
            modifier = Modifier
                .clickable {
                    expanded = !expanded
                }
                .height(30.dp)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }

    DropdownMenu(
        modifier = Modifier.background(LightPrimaryColor),
        offset = DpOffset(y = 0.dp, x = 10000.dp),
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        Row(horizontalArrangement = Arrangement.Center) {
            Icon(
                modifier = Modifier
                    .clickable {
                        vm.filterOptionState.value = selectedOption.value.apply { this.isAscending = !this.isAscending }
                        isAscending.value = !isAscending.value
                        vm.updateEventsNearby()
                    }
                    .padding(start = 8.dp)
                    .size(16.dp),
                painter = if (isAscending.value) painterResource(R.drawable.sort_up) else painterResource(R.drawable.sort_down),
                contentDescription = "up/down sort",
                tint = Color.White,
            )
            Text(
                "Filter by",
                fontFamily = RobotoMedium,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 8.dp),
                textAlign = TextAlign.Center
            )
        }
        Divider()
        CustomMenuItem(name = "Category", selectedOption = selectedOption, currentOption = Option.Category, itemColors = itemColors)
        CustomMenuItem(name = "Alphabet", selectedOption = selectedOption, currentOption = Option.Alphabet, itemColors = itemColors)
        CustomMenuItem(name = "Distance", selectedOption = selectedOption, currentOption = Option.Distance, itemColors = itemColors)

        CustomMenuItem(name = "Date", selectedOption = selectedOption, currentOption = Option.Date, itemColors = itemColors)

    }
}

@Composable
fun CustomMenuItem(name: String, selectedOption: State<Option>, currentOption: Option, itemColors: MenuItemColors) {
    DropdownMenuItem(
        onClick = {
            vm.filterOptionState.value = currentOption
            vm.updateEventsNearby()
        },
        enabled = selectedOption.value != currentOption,
        colors = itemColors,
        text = { Text(name, fontFamily = Roboto, fontSize = 13.sp) }
    )
}

@Composable
fun CardEvent(navController: NavHostController, event: Event) {
    val mSimpleDateFormat = SimpleDateFormat(timePattern)
    val date = mSimpleDateFormat.parse(event.date)
    val currentLocation = vm.location.collectAsState()
    val eventLocation = vm.latLonToLocation(lat = event.latitude, lon = event.longitude)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(PrimaryColor)
            .clickable {
                navController.navigate(event)
            }
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(
                text = event.name,
                fontSize = 20.sp,
                fontFamily = Roboto
            )
            Category(modifier = Modifier.padding(top = 4.dp), name = event.type)
        }
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = "${date.day.makeNumbersWithZero()}.${date.month.makeNumbersWithZero()} ${date.hours.makeNumbersWithZero()}:${date.minutes.makeNumbersWithZero()}",
                fontSize = 14.sp,
                fontFamily = Roboto,
                textAlign = TextAlign.Start
            )
            Text(
                modifier = Modifier.weight(1f),
                text = currentLocation.value?.distanceTo(eventLocation)
                    ?.let { it.getDistanceInKmOrM() } ?: "Loading...",
                fontSize = 14.sp,
                fontFamily = Roboto,
                textAlign = TextAlign.End
            )
        }

    }
}

fun Float.getDistanceInKmOrM(): String {
    return if (this < 1000) {
        this.roundToInt().toString() + " m"
    } else {
        (this / 1000).roundToInt().toString() + " km"
    }
}