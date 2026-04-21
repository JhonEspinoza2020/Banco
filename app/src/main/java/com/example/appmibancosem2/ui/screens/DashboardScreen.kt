package com.example.appmibancosem2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appmibancosem2.data.model.DemoData
import com.example.appmibancosem2.navigation.Screen
import com.example.appmibancosem2.ui.theme.*
import androidx.constraintlayout.compose.ConstraintLayout

/**
 * M2 - DashboardScreen: Pantalla principal que utiliza ConstraintLayout para organizar
 * múltiples tarjetas de cuenta y accesos rápidos a otros módulos.
 */
@Composable
fun DashboardScreen(
    userName: String,
    onNavigateTo: (Screen) -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = { MiBancoTopBar(titulo = "Mi Banco") }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Cabecera con saludo personalizado (M2 - HU-01)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint               = NavyPrimary,
                    modifier           = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text       = "Hola, $userName",
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = NavyDark
                )
            }

            // Uso de ConstraintLayout para diseño flexible (M2 - HU-02)
            ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
                val (card1, card2, grid) = createRefs()

                // Tarjeta Principal (LinearLayout implícito vía TarjetaCuenta)
                Box(modifier = Modifier.constrainAs(card1) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }) {
                    TarjetaCuenta(cuenta = DemoData.cuenta)
                }

                // Segunda Tarjeta (Requerimiento de diseño corporativo)
                Box(modifier = Modifier.constrainAs(card2) {
                    top.linkTo(card1.bottom, margin = 12.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }) {
                    TarjetaCuenta(
                        cuenta = DemoData.cuenta.copy(
                            numero = "002-456789012",
                            tipo   = "Cuenta Sueldo",
                            saldo  = 8450.00
                        ),
                        gradiente = listOf(NavyMid, NavyDark)
                    )
                }

                // Cuadrícula de Navegación (LinearLayout horizontal)
                Card(
                    shape  = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.constrainAs(grid) {
                        top.linkTo(card2.bottom, margin = 20.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text       = "Servicios",
                            fontWeight = FontWeight.SemiBold,
                            color      = NavyDark,
                            fontSize   = 15.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        // Fila de accesos rápidos con navegación completa (M1 - NavController)
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            BotonAccesoRapido(
                                icono    = Icons.Default.History,
                                etiqueta = "Historial",
                                color    = NavyPrimary,
                                onClick  = { onNavigateTo(Screen.Transacciones) }
                            )
                            BotonAccesoRapido(
                                icono    = Icons.Default.Payment,
                                etiqueta = "Pagos",
                                color    = GreenPositive,
                                onClick  = { onNavigateTo(Screen.Pagos) }
                            )
                            BotonAccesoRapido(
                                icono    = Icons.Default.AccountBalance,
                                etiqueta = "Préstamos",
                                color    = GoldAccent,
                                onClick  = { onNavigateTo(Screen.Prestamos) }
                            )
                            BotonAccesoRapido(
                                icono    = Icons.AutoMirrored.Filled.TrendingUp,
                                etiqueta = "Mis Ahorros",
                                color    = NavyLight,
                                onClick  = { onNavigateTo(Screen.Ahorro) }
                            )
                        }
                    }
                }
            }

            // Vista previa de movimientos (M3)
            Card(
                shape  = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("Últimas operaciones", fontWeight = FontWeight.SemiBold, color = NavyDark)
                        TextButton(onClick = { onNavigateTo(Screen.Transacciones) }) {
                            Text("Ver historial", color = GoldAccent, fontSize = 12.sp)
                        }
                    }
                    DemoData.transacciones.take(3).forEach { tx ->
                        FilaTransaccion(transaccion = tx)
                    }
                }
            }

            // Botón de salida con limpieza de navegación
            OutlinedButton(
                onClick  = onLogout,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Salir del aplicativo")
            }
        }
    }
}
