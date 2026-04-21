package com.example.appmibancosem2.navigation


sealed class Screen(val route: String) {
    object Login         : Screen("login")
    object Dashboard     : Screen("dashboard/{userName}") {
        fun createRoute(userName: String) = "dashboard/$userName"
    }
    object Transacciones : Screen("transacciones")
    object Pagos         : Screen("pagos")
    object Prestamos     : Screen("prestamos")
    object Ahorro        : Screen("ahorro")
}