package com.cha1se.cyberzillatestapp.presentation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.Navigation
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.cha1se.cyberzillatestapp.data.models.Event
import com.cha1se.cyberzillatestapp.presentation.helpers.LocationHelper
import com.cha1se.cyberzillatestapp.presentation.helpers.Routes
import com.cha1se.cyberzillatestapp.presentation.screens.DetailScreen
import com.cha1se.cyberzillatestapp.presentation.screens.MainScreen
import com.cha1se.cyberzillatestapp.presentation.viewmodels.MainActivityViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : ComponentActivity() {

    val vm: MainActivityViewModel by viewModel()

    val fusedLocationProviderClient by lazy {  LocationServices.getFusedLocationProviderClient(this) }

    val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            vm.location.value = mLastLocation
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            getLastLocation(this)

            Navigation()
        }
    }

    @Composable
    fun Navigation() {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = Routes.Home.route) {
            composable(Routes.Home.route) {
                MainScreen(navController = navController, viewModel = vm,)
            }
            composable<Event> { navBackStackEntry ->
                val event: Event = navBackStackEntry.toRoute()
                DetailScreen(navController = navController, vm = vm, event = event)

            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation(context: Context) {
        if (hasNetwork(context)) {
            if (LocationHelper().checkPermissions(context)) {
                if (LocationHelper().isLocationEnabled(context)) {
                    fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                        val location: Location? = task.result
                        if (location == null) {
                            requestNewLocationData()
                        } else {
                            vm.location.value = location
                            vm.getNameByLocation(location, context)
                            vm.updateEventsNearby()
                        }
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Please turn on" + " your location...",
                        Toast.LENGTH_LONG
                    )
                        .show()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
            } else {
                LocationHelper().requestPermissions(context as MainActivity)
            }
        } else {
            Toast.makeText(
                context,
                "Please turn on Internet",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {

        val mLocationRequest = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5
            fastestInterval = 0
            numUpdates = 1
        }
        fusedLocationProviderClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        var helper = LocationHelper()
        if (requestCode == helper.PERM_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (LocationHelper().checkPermissions(this)) {
            getLastLocation(this)
        }
    }

    fun hasNetwork(context: Context): Boolean = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo?.isConnected == true
}