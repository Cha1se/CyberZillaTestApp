package com.cha1se.cyberzillatestapp.presentation.helpers

sealed class Routes(val route: String) {

    object Home : Routes("home")
    object Detail : Routes("detail")

}
