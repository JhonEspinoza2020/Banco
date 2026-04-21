package com.example.appmibancosem2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.appmibancosem2.navigation.MiBancoNavGraph
import com.example.appmibancosem2.ui.theme.Appmibancosem2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Appmibancosem2Theme {
                val navController = rememberNavController()
                // M1 - Configuración de la Navegación Principal
                // Se inicia en Screen.Login según el NavGraph
                MiBancoNavGraph(navController = navController)
            }
        }
    }
}
