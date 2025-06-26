package com.ptrckstl.demofill

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ptrckstl.demofill.ui.theme.AutoFillDemoTheme
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AutoFillDemoTheme {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                   Text("Hello World!")
                }
            }
        }
    }
}