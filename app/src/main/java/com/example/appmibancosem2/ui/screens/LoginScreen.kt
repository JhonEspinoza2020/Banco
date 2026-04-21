package com.example.appmibancosem2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appmibancosem2.ui.theme.*

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error    by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyDark, NavyPrimary))),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier  = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            shape     = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier            = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text       = "Mi Banco",
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = NavyPrimary
                )
                Text(
                    text     = "Portal Financiero",
                    fontSize = 14.sp,
                    color    = GoldAccent
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value         = email,
                    onValueChange = { email = it; error = "" },
                    label         = { Text("Correo electrónico") },
                    leadingIcon   = { Icon(Icons.Default.Email, null, tint = NavyPrimary) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction    = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier   = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value                = password,
                    onValueChange        = { password = it; error = "" },
                    label                = { Text("Contraseña") },
                    leadingIcon          = { Icon(Icons.Default.Lock, null, tint = NavyPrimary) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction    = ImeAction.Done
                    ),
                    singleLine = true,
                    modifier   = Modifier.fillMaxWidth()
                )

                if (error.isNotEmpty()) {
                    Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            error = "Completa todos los campos"
                        } else {
                            onLoginSuccess(email)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape  = RoundedCornerShape(10.dp)
                ) {
                    Text("Ingresar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                TextButton(onClick = { }) {
                    Text("¿Olvidaste tu contraseña?", color = GoldAccent, fontSize = 13.sp)
                }
            }
        }
    }
}