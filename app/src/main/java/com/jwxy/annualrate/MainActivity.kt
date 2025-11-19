package com.jwxy.annualrate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jwxy.annualrate.ui.AprScreen
import com.jwxy.annualrate.ui.theme.AnnualrateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnnualrateTheme {
                AprScreen()
            }
        }
    }
}
