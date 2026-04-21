package com.example.appmibancosem2.ui.screens

import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appmibancosem2.data.model.DemoData
import com.example.appmibancosem2.data.model.SimuladorPrestamo
import com.example.appmibancosem2.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.appmibancosem2.data.model.Transaccion

// ═══════════════════════════════════════════════════════════════
// M3 - Transacciones: Historial con filtros dinámicos (HU-03)
// ═══════════════════════════════════════════════════════════════
/**
 * Pantalla que muestra el historial de movimientos utilizando LazyColumn.
 * Implementa filtros reactivos por tipo de transacción (Débito/Crédito).
 */
// ═══════════════════════════════════════════════════════════════
// M3 - Transacciones: Historial Real conectado a Archivos (HU-03)
// ═══════════════════════════════════════════════════════════════
@Composable
fun TransaccionesScreen(onBack: () -> Unit) {
    var filtro by remember { mutableStateOf("Todos") }

    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val prefs = context.getSharedPreferences("mibanco_prefs", android.content.Context.MODE_PRIVATE)

    // --- 1. Estado reactivo para nuestra "Base de Datos" de texto ---
    var listaTransacciones by remember { mutableStateOf(listOf<Transaccion>()) }
    val nombreArchivo = "historial_movimientos.txt"

    // --- 2. Función para leer el archivo de texto y convertirlo a Objetos ---
    fun cargarMovimientos() {
        val archivo = java.io.File(context.filesDir, nombreArchivo)
        val bdInicializada = prefs.getBoolean("bd_inicializada", false)

        // MIGRACIÓN: Solo la primera vez, copiamos el DemoData al archivo real
        if (!archivo.exists() && !bdInicializada) {
            // Los guardamos invertidos para que al leerlos mantengan el orden correcto
            DemoData.transacciones.reversed().forEach { tx ->
                archivo.appendText("${tx.descripcion}|${tx.fecha}|${tx.monto}|${tx.categoria}\n")
            }
            prefs.edit().putBoolean("bd_inicializada", true).apply()
        }

        // LECTURA REAL: A partir de aquí, la pantalla SOLO confía en el archivo
        if (archivo.exists()) {
            val lineas = archivo.readLines()
            listaTransacciones = lineas.mapNotNull { linea ->
                val partes = linea.split("|")
                if (partes.size >= 3) {
                    Transaccion(
                        descripcion = partes[0],
                        fecha = partes[1],
                        monto = partes[2].toDoubleOrNull() ?: 0.0,
                        categoria = if (partes.size > 3) partes[3] else ""
                    )
                } else null
            }.reversed() // Invertimos la lista final para que los pagos más nuevos salgan arriba
        } else {
            listaTransacciones = emptyList() // Si no hay archivo, la lista está vacía
        }
    }

    // --- 3. Cargar datos apenas se abre la pantalla ---
    LaunchedEffect(Unit) {
        cargarMovimientos()
    }

    // El filtro ahora opera sobre la lista real (listaTransacciones), no sobre DemoData
    val transaccionesFiltradas = when (filtro) {
        "Débito"  -> listaTransacciones.filter { it.esDebito() }
        "Crédito" -> listaTransacciones.filter { !it.esDebito() }
        else      -> listaTransacciones
    }

    Scaffold(
        topBar = {
            MiBancoTopBar(titulo = "Mis Movimientos", mostrarBack = true, onBack = onBack)
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
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

            // --- 4. Mostramos los datos filtrados que provienen del archivo ---
            items(transaccionesFiltradas) { tx ->
                FilaTransaccion(transaccion = tx)
            }

            // --- BOTÓN: Limpiar Movimientos ---
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        val archivo = java.io.File(context.filesDir, nombreArchivo)

                        if (archivo.exists()) {
                            val fecha = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                            android.util.Log.d("AppMiBanco", "[$fecha] EVENTO: Se limpió el historial general.")

                            // 1. Borramos el archivo
                            val borrado = archivo.delete()

                            // 2. Refrescamos la UI instantáneamente y mostramos mensaje
                            if (borrado) {
                                cargarMovimientos() // Esto vaciará la lista visual
                                scope.launch {
                                    snackbarHostState.showSnackbar("Historial eliminado con éxito")
                                }
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("No hay movimientos para borrar")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Limpiar todos los movimientos", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
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
// ═══════════════════════════════════════════════════════════════
// M4 - Pagos: Formulario con validación y guardado real (HU-04)
// ═══════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagosScreen(onBack: () -> Unit) {
    // --- 1. Variable de contexto añadida para acceder a los archivos ---
    val context = androidx.compose.ui.platform.LocalContext.current

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
                        Text("¡Pago guardado y realizado con éxito!", color = GreenPositive, fontWeight = FontWeight.Bold)
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

            // Resumen de la operación
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
                    // --- 2. LÓGICA DE PERSISTENCIA REAL: Escribir en el archivo de texto ---
                    val archivo = java.io.File(context.filesDir, "historial_movimientos.txt")
                    val fecha = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())

                    // Creamos una línea con estructura: Titulo|Fecha|Monto|Tipo
                    val registro = "Pago de $servicio|$fecha|-$monto|Debito\n"

                    // Agregamos la línea al final del archivo sin borrar lo anterior
                    archivo.appendText(registro)
                    // ------------------------------------------------------------------------

                    mostrarConfirmacion = false
                    pagoExitoso = true
                    contrato = ""
                    monto = ""
                    servicio = ""
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
// ═══════════════════════════════════════════════════════════════
// M5 - Préstamos: Simulador con cronograma e Historial (HU-05)
// ═══════════════════════════════════════════════════════════════
@Composable
fun PrestamosScreen(onBack: () -> Unit) {
    // --- 1. Variables de Persistencia ---
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("mibanco_prefs", android.content.Context.MODE_PRIVATE)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // --- 2. Cargar Borrador (si existe) o valores por defecto ---
    var monto by remember { mutableFloatStateOf(prefs.getFloat("borrador_monto", 5000f)) }
    var plazo by remember { mutableIntStateOf(prefs.getInt("borrador_plazo", 12)) }
    var tasa by remember { mutableDoubleStateOf(24.0) }

    // Variables para el AlertDialog del historial
    var mostrarHistorial by remember { mutableStateOf(false) }
    var contenidoHistorial by remember { mutableStateOf("") }

    // --- 3. Guardar Borrador automáticamente al cambiar valores ---
    LaunchedEffect(monto, plazo) {
        prefs.edit()
            .putFloat("borrador_monto", monto)
            .putInt("borrador_plazo", plazo)
            .apply()
    }

    // Cálculo instantáneo
    val simulador = SimuladorPrestamo(monto.toDouble(), tasa, plazo)
    val cuota = simulador.calcularCuota()

    Scaffold(
        topBar = {
            MiBancoTopBar(titulo = "Simulador Financiero", mostrarBack = true, onBack = onBack)
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Resultado principal
            Card(colors = CardDefaults.cardColors(containerColor = NavyPrimary)) {
                Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Cuota Mensual Estimada", color = GoldLight)
                    Text("S/ %,.2f".format(cuota), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Slider para Monto
            Column {
                Text("Monto solicitado: S/ ${monto.toInt()}", fontWeight = FontWeight.Bold)
                Slider(
                    value = monto,
                    onValueChange = { monto = it },
                    valueRange = 1000f..50000f,
                    colors = SliderDefaults.colors(thumbColor = NavyPrimary, activeTrackColor = NavyPrimary)
                )
            }

            // Chips para Plazo
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

            // --- 4. BOTÓN: Solicitar Préstamo (Escribe en el archivo) ---
            Button(
                onClick = {
                    val archivo = File(context.filesDir, "historial_solicitudes.txt")
                    val timestamp = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

                    // A. Escribir registro (appendText no borra lo anterior)
                    val detalle = "Préstamo: S/ ${monto.toInt()} - $plazo meses - ENVIADA"
                    archivo.appendText("[$timestamp] $detalle\n")

                    // B. Limpiar el borrador de SharedPreferences tras el éxito
                    prefs.edit().remove("borrador_monto").remove("borrador_plazo").apply()

                    // C. Notificar al usuario y resetear valores
                    scope.launch {
                        snackbarHostState.showSnackbar("Solicitud enviada correctamente")
                    }
                    monto = 5000f
                    plazo = 12
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPositive),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Solicitar Préstamo", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            }

            // --- 5. BOTÓN: Ver Historial ---
            TextButton(
                onClick = {
                    val archivo = File(context.filesDir, "historial_solicitudes.txt")
                    contenidoHistorial = if (archivo.exists()) archivo.readText() else "Sin solicitudes registradas."
                    mostrarHistorial = true
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Ver historial de solicitudes", color = NavyDark, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    // --- 6. MODAL (AlertDialog) para mostrar el historial ---
    if (mostrarHistorial) {
        AlertDialog(
            onDismissRequest = { mostrarHistorial = false },
            title = { Text("Historial Local", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = contenidoHistorial,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { mostrarHistorial = false }) { Text("Cerrar") }
            }
        )
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