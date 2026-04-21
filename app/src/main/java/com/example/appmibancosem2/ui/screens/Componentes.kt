
package com.example.appmibancosem2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appmibancosem2.data.model.Cuenta
import com.example.appmibancosem2.data.model.Transaccion
import com.example.appmibancosem2.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiBancoTopBar(
    titulo: String,
    mostrarBack: Boolean = false,
    onBack: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text       = titulo,
                color      = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            if (mostrarBack) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector        = Icons.Default.ArrowBack,
                        contentDescription = "Regresar",
                        tint               = Color.White
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = NavyPrimary
        )
    )
}

@Composable
fun TarjetaCuenta(
    cuenta: Cuenta,
    modifier: Modifier = Modifier,
    gradiente: List<Color> = listOf(NavyPrimary, NavyLight)
) {
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(Brush.horizontalGradient(gradiente))
                .padding(24.dp)
        ) {
            Column {
                Text(cuenta.tipo, color = GoldLight, fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    text          = cuenta.numero,
                    color         = Color.White.copy(alpha = 0.7f),
                    fontSize      = 13.sp,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(16.dp))
                Text("Saldo disponible", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Text(
                    text       = "S/ %,.2f".format(cuenta.saldo),
                    color      = Color.White,
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(cuenta.titular, color = GoldLight, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun BotonAccesoRapido(
    icono: ImageVector,
    etiqueta: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledIconButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = color.copy(alpha = 0.15f)
            )
        ) {
            Icon(
                imageVector        = icono,
                contentDescription = etiqueta,
                tint               = color,
                modifier           = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text       = etiqueta,
            fontSize   = 12.sp,
            color      = NavyDark,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun FilaTransaccion(transaccion: Transaccion) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp, 40.dp)
                .background(
                    color = if (transaccion.esDebito()) RedNegative else GreenPositive,
                    shape = RoundedCornerShape(4.dp)
                )
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = transaccion.descripcion,
                fontWeight = FontWeight.Medium,
                fontSize   = 14.sp,
                color      = NavyDark
            )
            Text(
                text     = transaccion.fecha,
                fontSize = 12.sp,
                color    = GrayMedium
            )
        }
        Text(
            text       = (if (transaccion.esDebito()) "−" else "+") + transaccion.montoFormateado(),
            color      = if (transaccion.esDebito()) RedNegative else GreenPositive,
            fontWeight = FontWeight.Bold,
            fontSize   = 14.sp
        )
    }
    HorizontalDivider(color = Color(0xFFEEEEEE))
}