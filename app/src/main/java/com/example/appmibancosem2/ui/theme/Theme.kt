package com.example.appmibancosem2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MiBancoColorScheme = lightColorScheme(
    primary         = NavyPrimary,
    onPrimary       = Color.White,
    secondary       = GoldAccent,
    onSecondary     = Color.White,
    background      = GrayLight,
    onBackground    = NavyDark,
    surface         = Color.White,
    onSurface       = NavyDark,
    error           = RedNegative,
    onError         = Color.White
)

@Composable
fun Appmibancosem2Theme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MiBancoColorScheme,
        typography  = Typography,
        content     = content
    )
}