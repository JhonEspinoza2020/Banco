package com.example.appmibancosem2.ui.screens

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appmibancosem2.ui.theme.*

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    // 1. Obtenemos el contexto y la instancia de SharedPreferences
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("mibanco_prefs", Context.MODE_PRIVATE)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    // 2. Estado para el Checkbox
    var recordarSesion by remember { mutableStateOf(false) }

    // 3. Cargar datos guardados al iniciar la pantalla
    LaunchedEffect(Unit) {
        recordarSesion = prefs.getBoolean("recordar_sesion", false)
        if (recordarSesion) {
            email = prefs.getString("ultimo_usuario", "") ?: ""
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyDark, NavyPrimary))),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Mi Banco",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NavyPrimary
                )
                Text(
                    text = "Portal Financiero",
                    fontSize = 14.sp,
                    color = GoldAccent
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; error = "" },
                    label = { Text("Correo electrónico") },
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = NavyPrimary) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; error = "" },
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = NavyPrimary) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 4. Interfaz del Checkbox "Recordar sesión"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = recordarSesion,
                        onCheckedChange = { recordarSesion = it },
                        colors = CheckboxDefaults.colors(checkedColor = NavyPrimary)
                    )
                    Text("Recordar sesión", fontSize = 14.sp, color = NavyDark)
                }

                if (error.isNotEmpty()) {
                    Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            error = "Completa todos los campos"
                        } else {
                            // 5. Lógica para guardar o limpiar SharedPreferences al hacer login
                            val editor = prefs.edit()
                            if (recordarSesion) {
                                editor.putBoolean("recordar_sesion", true)
                                editor.putString("ultimo_usuario", email)
                            } else {
                                editor.remove("recordar_sesion")
                                editor.remove("ultimo_usuario")
                            }
                            editor.apply() // Asíncrono, recomendado en Android

                            onLoginSuccess(email)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(10.dp)
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