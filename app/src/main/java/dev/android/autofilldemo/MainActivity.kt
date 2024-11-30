package dev.android.autofilldemo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.android.autofilldemo.ui.theme.AutoFillDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AutoFillDemoTheme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Welcome to My Password Manager", fontSize = 18.sp)
                    ClickableText(
                        modifier = Modifier.padding(8.dp),
                        text = AnnotatedString("Open settings"),
                        onClick = {
                            startActivity(Intent(this@MainActivity, AutoFillSaveActivity::class.java))
                        })
                }
            }
        }
    }

    override fun onBackPressed() {
        Log.d("Autofill", "Main Activity onBackPressed")
        super.onBackPressed()

        getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(Constants.PREF_KEY_VAULT_LOCKED, true)
            .apply()
    }
}

