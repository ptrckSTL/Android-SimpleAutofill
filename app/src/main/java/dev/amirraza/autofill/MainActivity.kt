package dev.amirraza.autofill

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.autofill.AutofillManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.amirraza.autofill.ui.theme.AutoFillDemoTheme
import dev.amirraza.autofill.util.Util

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AutoFillDemoTheme {
                val openDialog = remember {
                    mutableStateOf(true)
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome to My Password Manager",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    ElevatedButton(onClick = {
                        startActivity(
                            Intent(
                                this@MainActivity,
                                AutoFillEntriesActivity::class.java
                            )
                        )
                        finish()
                    }) {
                        Text(text = "Go to your vault")
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Press back button to lock vault.",
                        modifier = Modifier.padding(8.dp)
                    )
                    Text(
                        text = "Press home button keep vault unlocked.",
                        modifier = Modifier.padding(16.dp)
                    )

                    if ((getSystemService(AutofillManager::class.java) as AutofillManager).hasEnabledAutofillServices()
                            .not()
                    ) {

                        if (openDialog.value) {
                            AlertDialog(containerColor = Color.DarkGray,
                                title = {
                                    Text(text = "Enable Autofill Service")
                                }, text = {
                                    Text(text = "Please enable autofill service")
                                }, dismissButton = {
                                    TextButton(onClick = {
                                        openDialog.value = openDialog.value.not()
                                    }) {
                                        Text(text = "Cancel")
                                    }
                                }, onDismissRequest = {
                                    openDialog.value = openDialog.value.not()
                                }, confirmButton = {
                                    Button(onClick = {
                                        openDialog.value = openDialog.value.not()
                                        Util.promptUserToEnableAutofillService(this@MainActivity)
                                    }
                                    ) {
                                        Text(text = "Enable")
                                    }
                                })
                        }
                    }
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