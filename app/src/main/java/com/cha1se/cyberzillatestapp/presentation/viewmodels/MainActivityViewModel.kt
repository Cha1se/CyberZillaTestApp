package com.cha1se.cyberzillatestapp.presentation.viewmodels

import android.content.Context
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cha1se.cyberzillatestapp.data.EventsRepositoryImpl
import com.cha1se.cyberzillatestapp.data.models.Event
import com.cha1se.cyberzillatestapp.data.models.Events
import com.cha1se.cyberzillatestapp.presentation.helpers.Option
import com.cha1se.cyberzillatestapp.presentation.helpers.Resource
import com.cha1se.cyberzillatestapp.presentation.screens.timePattern
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivityViewModel(val eventsRepositoryImpl: EventsRepositoryImpl) : ViewModel() {

    val location: MutableStateFlow<Location?> = MutableStateFlow(null)
    val locationName: MutableStateFlow<String?> = MutableStateFlow(null)
    val filterOptionState: MutableStateFlow<Option> = MutableStateFlow(Option.Nothing)

    private val _eventsList: MutableStateFlow<Resource<Events>> =
        MutableStateFlow(Resource.Loading())
    val eventsList: StateFlow<Resource<Events>> = _eventsList.asStateFlow()

    fun getNameByLocation(location: Location, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val cacheName = Geocoder(context, Locale.getDefault()).getFromLocation(
                location.latitude,
                location.longitude,
                1
            )?.first()?.locality
            locationName.value = cacheName
        }
    }

    fun updateEventsNearby() {
        viewModelScope.launch {
            val mSimpleDateFormat = SimpleDateFormat(timePattern)
            val cacheEventsList = Events(emptyList())

            eventsRepositoryImpl.getEventsList().collect() { mEventsList ->

                if (mEventsList.data != null && location.value != null) {
                    val eventsList = mEventsList.data!!.list

                    when (filterOptionState.value) {
                        Option.Category -> {
                            if (filterOptionState.value.isAscending) {
                                eventsList.sortedBy { it.type }
                            } else {
                                eventsList.sortedByDescending { it.type }
                            }
                        }
                        Option.Alphabet -> {
                            if (filterOptionState.value.isAscending) {
                                eventsList.sortedBy { it.name }
                            } else {
                                eventsList.sortedByDescending { it.name }
                            }
                        }
                        Option.Distance -> {
                            if (filterOptionState.value.isAscending) {
                                eventsList.sortedBy {
                                    location.value!!.distanceTo(
                                        latLonToLocation(
                                            lat = it.latitude,
                                            lon = it.longitude
                                        )
                                    )
                                }
                            } else {
                                eventsList.sortedByDescending {
                                    location.value!!.distanceTo(
                                        latLonToLocation(
                                            lat = it.latitude,
                                            lon = it.longitude
                                        )
                                    )
                                }
                            }
                        }
                        Option.Date -> {
                            if (filterOptionState.value.isAscending) {
                                eventsList.sortedBy {
                                    mSimpleDateFormat.parse(it.date)
                                }
                            } else {
                                eventsList.sortedByDescending {
                                    mSimpleDateFormat.parse(it.date)
                                }
                            }
                        }
                        Option.Nothing -> {
                            eventsList
                        }
                    }.forEach { event ->
                        val eventLocation =
                            latLonToLocation(lat = event.latitude, lon = event.longitude)

                        if (location.value!!.distanceTo(eventLocation) < 20000) {
                            cacheEventsList.list += event
                        }
                    }

                }
                if (mEventsList.message != null) {
                    _eventsList.value = Resource.Error(message = mEventsList.message!!)
                } else {
                    _eventsList. value = Resource.Success(data = cacheEventsList,)
                }
            }
        }
    }

    fun latLonToLocation(lat: Double, lon: Double) = Location("event").apply {
        latitude = lat
        longitude = lon
    }

}