package com.cha1se.cyberzillatestapp.presentation.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cha1se.cyberzillatestapp.data.models.Event
import com.cha1se.cyberzillatestapp.presentation.helpers.Routes
import com.cha1se.cyberzillatestapp.presentation.ui.theme.AccentColor
import com.cha1se.cyberzillatestapp.presentation.ui.theme.Background
import com.cha1se.cyberzillatestapp.presentation.ui.theme.CyberZillaTestAppTheme
import com.cha1se.cyberzillatestapp.presentation.ui.theme.DividerColor
import com.cha1se.cyberzillatestapp.presentation.ui.theme.LightPrimaryColor
import com.cha1se.cyberzillatestapp.presentation.ui.theme.PrimaryTextColor
import com.cha1se.cyberzillatestapp.presentation.ui.theme.Roboto
import com.cha1se.cyberzillatestapp.presentation.ui.theme.RobotoMedium
import com.cha1se.cyberzillatestapp.presentation.ui.theme.TextColor
import com.cha1se.cyberzillatestapp.presentation.viewmodels.MainActivityViewModel
import java.text.SimpleDateFormat

val timePattern = "dd.MM.yyyy'T'HH:mm"

@Composable
fun DetailScreen(
    navController: NavHostController,
    event: Event,
    vm: MainActivityViewModel,
) {
    CyberZillaTestAppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(), containerColor = Background
        ) { innerPadding ->
            AboutCard(Modifier.padding(innerPadding), event, vm, navController)
        }
    }
}

@Composable
fun HeaderDetails(event: Event, navController: NavHostController) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
        .fillMaxWidth()
        .background(AccentColor)) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            tint = TextColor,
            contentDescription = "back",
            modifier = Modifier
                .clickable {
                    navController.navigate(Routes.Home.route)
                }
                .padding(8.dp)
                )

        Spacer(Modifier.size(4.dp))
        Text(
            text = event.name,
            fontSize = 20.sp,
            fontFamily = RobotoMedium,
            color = TextColor,
            modifier = Modifier.padding(vertical = 12.dp)
        )
    }
}

@Composable
fun AboutCard(modifier: Modifier = Modifier, event: Event, vm: MainActivityViewModel, navController: NavHostController) {
    val mSimpleDateFormat = SimpleDateFormat(timePattern)
    val date = mSimpleDateFormat.parse(event.date)

    val currentLocation = vm.location.collectAsState()
    val eventLocation = vm.latLonToLocation(lat = event.latitude, lon = event.longitude)

    Column (modifier.fillMaxSize().verticalScroll(rememberScrollState(0))) {
        HeaderDetails(event, navController)
        Category(Modifier.padding(8.dp), event.type)
        Text(modifier = Modifier.padding(horizontal = 8.dp), text = event.description, color = PrimaryTextColor)
        HorizontalDivider(color = DividerColor)
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = "${date.day.makeNumbersWithZero()}.${date.month.makeNumbersWithZero()} ${date.hours.makeNumbersWithZero()}:${date.minutes.makeNumbersWithZero()}",
                color = PrimaryTextColor,
                fontSize = 14.sp,
                fontFamily = Roboto,
                textAlign = TextAlign.Start
            )
            Text(
                modifier = Modifier.weight(1f),
                text = currentLocation.value?.distanceTo(eventLocation)
                    ?.let { it.getDistanceInKmOrM() } ?: "Loading...",
                fontSize = 14.sp,
                color = PrimaryTextColor,
                fontFamily = Roboto,
                textAlign = TextAlign.End
            )
        }
        AddToCalendarButton(event, navController.context)
        Spacer(Modifier.size(20.dp))
    }
}

fun Int.makeNumbersWithZero(): String = if(this > 10) this.toString() else "0$this"

@Composable
fun AddToCalendarButton(event: Event, context: Context) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(LightPrimaryColor)
            .clickable {
                addToCalendar(context, event)
            }
            .padding(8.dp)

    ) {
        Icon(
            Icons.Default.DateRange,
            modifier = Modifier.size(24.dp),
            contentDescription = "",
            tint = TextColor
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = "Add to calendar",
            color = TextColor,
            fontSize = 18.sp,
            fontFamily = Roboto
        )
    }
}

@Composable
fun Category(modifier: Modifier = Modifier, name: String) {
    Text(
        modifier = modifier
            .clip(CircleShape)
            .background(LightPrimaryColor)
            .padding(4.dp, 0.dp, 4.dp, 0.dp),
        text = name,
        fontSize = 14.sp,
        fontFamily = Roboto,
    )
}

fun addToCalendar(context: Context, event: Event) {
    val mSimpleDateFormat = SimpleDateFormat(timePattern)
    val date = mSimpleDateFormat.parse(event.date)

    val mIntent = Intent(Intent.ACTION_EDIT)
    mIntent.apply {
        type = "vnd.android.cursor.item/event"
        putExtra("beginTime", date.time)
        putExtra("time", true)
        putExtra("rule", "FREQ=YEARLY")
        putExtra("title", event.name)
    }
    context.startActivity(mIntent)
}




