package com.ptrckstl.demofill

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ptrckstl.demofill.ui.theme.AutoFillDemoTheme

class MainActivity : ComponentActivity(), InlinePreference {
    override val context = this

    fun updateInlinePref(value: Boolean) {
        preferInline = value
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AutoFillDemoTheme {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    PreferInlineToggle(
                        defaultValue = preferInline,
                        onChangePref = ::updateInlinePref
                    )
                }
            }
        }
    }

    @Composable
    private fun PreferInlineToggle(defaultValue: Boolean, onChangePref: (Boolean) -> Unit) {
        var inlineChecked by remember { mutableStateOf(defaultValue) }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Prefer Inline Autofill")
            Switch(
                checked = inlineChecked,
                onCheckedChange = { isChecked ->
                    inlineChecked = isChecked
                    onChangePref(isChecked)
                }
            )
        }
    }
}