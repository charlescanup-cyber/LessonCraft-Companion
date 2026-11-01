package com.lessoncraft.companion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lessoncraft.companion.ui.CompanionNav
import com.lessoncraft.companion.ui.theme.CompanionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as CompanionApp).container
        setContent { CompanionTheme { CompanionNav(container) } }
    }
}
