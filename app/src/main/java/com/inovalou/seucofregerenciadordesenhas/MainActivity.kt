package com.inovalou.seucofregerenciadordesenhas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.inovalou.seucofregerenciadordesenhas.navigation.SeuCofreNavHost
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SeuCofreNavHost()
                }
            }
        }
    }
}
