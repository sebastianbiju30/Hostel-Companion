package com.example.hostel_companion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.hostel_companion.ui.nav.HostelNavGraph
import com.example.hostel_companion.ui.theme.Hostel_CompanionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Hostel_CompanionTheme {
                HostelNavGraph()
            }
        }
    }
}