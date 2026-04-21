package com.example.appmibancosem2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appmibancosem2.data.model.DemoData
import com.example.appmibancosem2.data.model.SimuladorPrestamo
import com.example.appmibancosem2.ui.theme.*
import java.util.Calendar

// ═══════════════════════════════════════════════════════════════
// M3 - Transacciones: Historial con filtros dinámicos (HU-03)
// ═══════════════════════════════════════════════════════════════
/**
 * Pantalla que muestra el historial de movimientos utilizando LazyColumn.
 * Implementa filtros reactivos por tipo de transacción (Débito/Crédito).
 */
@Composable
fun TransaccionesScreen(onBack: () -> Unit) {
    var filtro by remember { mutableStateOf("Todos") }

    // Filtrado reactivo en base al estado del FilterChip
    val transaccionesFiltradas = when (filtro) {
        "Débito"  -> DemoData.transacciones.filter { it.esDebito() }
        "Crédito" -> DemoData.transacciones.filter { !it.esDebito() }
        else      -> DemoData.transacciones
    }

    Scaffold(
        topBar = {
            MiBancoTopBar(titulo = "Mis Movimientos", mostrarBack = true, onBack = onBack)
        }
    ) { padding ->
        // LazyColumn: Eficiencia para listas largas de movimientos
        LazyColumn(
            modifier       = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                TarjetaCuenta(cuenta = DemoData.cuenta)
                Spacer(Modifier.height(16.dp))

                Text("Filtrar por tipo:", fontWeight = FontWeight.Bold, color = NavyDark)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Todos", "Débito", "Crédito").forEach { opcion ->
                        FilterChip(
                            selected = filtro == opcion,
                            onClick  = { filtro = opcion },
                            label    = { Text(opcion) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent,
                                selectedLabelColor     = Color.White
                            )
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Renderizado de ítems individuales
            items(transaccionesFiltradas) { tx ->
                FilaTransaccion(transaccion = tx)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// M4 - Pagos: Formulario con validación y Resumen (HU-04)
// ═══════════════════════════════════════════════════════════════
/**
 * Pantalla de pago de servicios. Incluye:
 * - ExposedDropdownMenuBox para selección de servicios.
 * - Validaciones en tiempo real de campos.
 * - Card de resumen dinámico.
 * - AlertDialog para confirmación de seguridad.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagosScreen(onBack: () -> Unit) {
    var servicio by remember { mutableStateOf("") }
    var contrato by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var expandido by remember { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var pagoExitoso by remember { mutableStateOf(false) }

    // Lógica de validación
    val contratoValido = contrato.length >= 6
    val montoNum = monto.toDoubleOrNull() ?: 0.0
    val montoValido = montoNum > 0
    val formularioValido = servicio.isNotEmpty() && contratoValido && montoValido

    Scaffold(
        topBar = {
            MiBancoTopBar(titulo = "Pago de Servicios", mostrarBack = true, onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (pagoExitoso) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = GreenPositive)
                        Spacer(Modifier.width(8.dp))
                        Text("¡Pago realizado con éxito!", color = GreenPositive, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Selector de servicios corporativos
            ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = it }) {
                OutlinedTextField(
                    value = if (servicio.isEmpty()) "Seleccione un servicio" else servicio,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Servicio") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandido) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                    DemoData.servicios.forEach { srv ->
                        DropdownMenuItem(
                            text = { Text(srv.nombre) },
                            onClick = { servicio = srv.nombre; expandido = false }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = contrato,
                onValueChange = { contrato = it },
                label = { Text("Número de contrato (min. 6)") },
                isError = contrato.isNotEmpty() && !contratoValido,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = monto,
                onValueChange = { monto = it },
                label = { Text("Monto a pagar (S/)") },
                isError = monto.isNotEmpty() && !montoValido,
                modifier = Modifier.fillMaxWidth()
            )

            // Resumen de la operación (HU-04)
            if (formularioValido) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Resumen de Transacción", fontWeight = FontWeight.Bold, color = NavyPrimary)
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        Text("Servicio: $servicio")
                        Text("Contrato: $contrato")
                        Text("Monto: S/ %,.2f".format(montoNum), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Button(
                onClick = { mostrarConfirmacion = true },
                enabled = formularioValido,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Confirmar Pago", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Modal de confirmación (Seguridad bancaria)
    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("¿Confirmar operación?") },
            text = { Text("Se debitará S/ $monto de su cuenta principal para el pago de $servicio.") },
            confirmButton = {
                Button(onClick = {
                    mostrarConfirmacion = false
                    pagoExitoso = true
                    contrato = ""; monto = ""; servicio = ""
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) { Text("Cancelar") }
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// M5 - Préstamos: Simulador con cronograma (HU-05)
// ═══════════════════════════════════════════════════════════════
/**
 * Simulador de préstamos con cálculo de amortización francesa.
 * - Slider para selección de monto dinámico.
 * - Chips para selección de plazos y tasas.
 * - Cronograma detallado de las primeras 6 cuotas.
 */
@Composable
fun PrestamosScreen(onBack: () -> Unit) {
    var monto by remember { mutableStateOf(5000f) }
    var plazo by remember { mutableIntStateOf(12) }
    var tasa by remember { mutableDoubleStateOf(24.0) }

    // Cálculo instantáneo basado en estado
    val simulador = SimuladorPrestamo(monto.toDouble(), tasa, plazo)
    val cuota = simulador.calcularCuota()

    Scaffold(
        topBar = {
            MiBancoTopBar(titulo = "Simulador Financiero", mostrarBack = true, onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Resultado principal en tarjeta corporativa
            Card(colors = CardDefaults.cardColors(containerColor = NavyPrimary)) {
                Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Cuota Mensual Estimada", color = GoldLight)
                    Text("S/ %,.2f".format(cuota), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Widget Slider (HU-05)
            Column {
                Text("Monto solicitado: S/ ${monto.toInt()}", fontWeight = FontWeight.Bold)
                Slider(
                    value = monto,
                    onValueChange = { monto = it },
                    valueRange = 1000f..50000f,
                    colors = SliderDefaults.colors(thumbColor = NavyPrimary, activeTrackColor = NavyPrimary)
                )
            }

            // Widget Chips para Plazo
            Column {
                Text("Plazo de pago (meses):", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(6, 12, 24, 36).forEach { m ->
                        FilterChip(
                            selected = plazo == m,
                            onClick = { plazo = m },
                            label = { Text("$m meses") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NavyPrimary, selectedLabelColor = Color.White)
                        )
                    }
                }
            }

            // Cronograma de Amortización (Primeros 6 meses)
            Text("Proyección del Cronograma (6m)", fontWeight = FontWeight.Bold, color = NavyDark)
            Card(colors = CardDefaults.cardColors(containerColor = GrayLight.copy(alpha = 0.3f))) {
                Column(Modifier.padding(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Mes", Modifier.weight(0.5f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Capital", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Interés", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Saldo", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    
                    var saldoRestante = monto.toDouble()
                    val r = tasa / 100.0 / 12.0
                    repeat(6) { i ->
                        val interesMes = saldoRestante * r
                        val capitalMes = cuota - interesMes
                        saldoRestante -= capitalMes
                        Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Text("${i+1}", Modifier.weight(0.5f), fontSize = 12.sp)
                            Text("%.2f".format(capitalMes), Modifier.weight(1f), fontSize = 12.sp)
                            Text("%.2f".format(interesMes), Modifier.weight(1f), fontSize = 12.sp)
                            Text("%.0f".format(saldoRestante.coerceAtLeast(0.0)), Modifier.weight(1f), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// M6 - Ahorro: Metas y Proyección Compuesta (HU-06)
// ═══════════════════════════════════════════════════════════════
/**
 * Pantalla de metas de ahorro. Incluye:
 * - LinearProgressIndicator para seguimiento visual de la meta.
 * - Tabla de proyección a 6 meses con interés compuesto (0.5% mensual).
 * - Simulación de aportes mensuales fijos.
 */
@Composable
fun AhorroScreen(onBack: () -> Unit) {
    val ahorro = DemoData.cuentaAhorro
    val pct = ahorro.porcentaje()
    val aporteFijo = 500.0 // Simulación de ahorro mensual

    Scaffold(
        topBar = {
            MiBancoTopBar(titulo = "Mis Metas de Ahorro", mostrarBack = true, onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(ahorro.nombre, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)

            // Indicador de progreso (HU-06)
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Avance: ${(pct * 100).toInt()}%", fontWeight = FontWeight.Medium)
                    Text("Meta: S/ %,.2f".format(ahorro.meta), fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { pct },
                    modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                    color = GreenPositive,
                    trackColor = GrayLight
                )
            }

            // Proyección con Interés Compuesto y Aportes (HU-06)
            Text("Proyección de Crecimiento (6 meses)", fontWeight = FontWeight.Bold)
            Card(colors = CardDefaults.cardColors(containerColor = GrayLight.copy(alpha = 0.5f))) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth()) {
                        Text("Mes", Modifier.weight(0.5f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Interés (0.5%)", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Aporte", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Saldo Final", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    
                    var saldoProy = ahorro.saldo
                    repeat(6) { i ->
                        val interes = saldoProy * 0.005 
                        saldoProy += interes + aporteFijo
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text("${i + 1}", Modifier.weight(0.5f), fontSize = 12.sp)
                            Text("%.2f".format(interes), Modifier.weight(1f), fontSize = 12.sp)
                            Text("%.0f".format(aporteFijo), Modifier.weight(1f), fontSize = 12.sp)
                            Text("S/ %,.0f".format(saldoProy), Modifier.weight(1f), fontSize = 12.sp, color = NavyPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
