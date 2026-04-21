package com.example.appmibancosem2.data.model

import java.text.NumberFormat
import java.util.Locale

// ─── Entidades ────────────────────────────────────────────────
data class Cuenta(
    val numero: String,
    val tipo: String,
    val saldo: Double,
    val titular: String
)

data class Transaccion(
    val descripcion: String,
    val fecha: String,
    val monto: Double,
    val categoria: String = ""
) {
    fun esDebito() = monto < 0
    fun montoFormateado(): String {
        val fmt = NumberFormat.getNumberInstance(Locale("es", "PE"))
        fmt.minimumFractionDigits = 2
        fmt.maximumFractionDigits = 2
        return "S/ ${fmt.format(kotlin.math.abs(monto))}"
    }
}

data class Servicio(val nombre: String, val icono: String = "")

data class CuentaAhorro(
    val nombre: String,
    val saldo: Double,
    val meta: Double,
    val plazo: String
) {
    fun porcentaje() = (saldo / meta).coerceIn(0.0, 1.0).toFloat()
}

// ─── Simulador de Préstamos (Amortización Francesa) ───────────
data class SimuladorPrestamo(
    val monto: Double,
    val tasaAnual: Double,
    val cuotas: Int
) {
    fun calcularCuota(): Double {
        val r = tasaAnual / 12.0 / 100.0
        if (r == 0.0) return monto / cuotas
        val factor = Math.pow(1 + r, cuotas.toDouble())
        return monto * (r * factor) / (factor - 1)
    }
}

// ─── Datos Demo ───────────────────────────────────────────────
object DemoData {
    val cuenta = Cuenta(
        numero  = "002-123456789",
        tipo    = "Cuenta Corriente",
        saldo   = 8_450.75,
        titular = "Carlos Mendoza Ríos"
    )

    val transacciones = listOf(
        Transaccion("Pago luz ENEL",           "28/06/2025", -145.60, "Servicios"),
        Transaccion("Depósito salario",        "25/06/2025", +3500.00,"Ingresos"),
        Transaccion("Supermercado Wong",       "23/06/2025", -289.40, "Compras"),
        Transaccion("Transferencia recibida",  "20/06/2025", +800.00, "Ingresos"),
        Transaccion("Netflix",                 "18/06/2025", -39.90,  "Entretenimiento"),
        Transaccion("Pago agua SEDAPAL",       "15/06/2025", -78.30,  "Servicios"),
        Transaccion("Farmacia Inkafarma",      "12/06/2025", -56.80,  "Salud"),
        Transaccion("Retiro cajero",           "10/06/2025", -200.00, "Efectivo")
    )

    val servicios = listOf(
        Servicio("Luz (ENEL)"),
        Servicio("Agua (SEDAPAL)"),
        Servicio("Gas (CÁLIDDA)"),
        Servicio("Internet (Claro)"),
        Servicio("Teléfono (Movistar)"),
        Servicio("Cable (DirecTV)")
    )

    val cuentaAhorro = CuentaAhorro(
        nombre = "Meta Viaje Europa",
        saldo  = 12_875.00,
        meta   = 20_000.00,
        plazo  = "Dic 2025"
    )
}