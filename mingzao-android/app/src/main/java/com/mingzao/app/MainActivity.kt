package com.mingzao.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.mingzao.app.ui.MingzaoApp
import com.mingzao.app.ui.theme.MingzaoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MingzaoTheme {
                MingzaoApp()
            }
        }
    }
}
