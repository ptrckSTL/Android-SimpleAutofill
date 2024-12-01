package dev.amirraza.autofill.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import dev.amirraza.autofill.Constants.REQUEST_CODE_AUTOFILL_SERVICE

object Util {

    fun parseIdentifierForNameField(identifier: String?): String {
        return identifier?.let { id ->
            var name = ""
            id.split(".").forEach {
                if (skipList.contains(it).not())
                    name += " ${it.capitalize(Locale("US"))}"
            }
            name.trim()
        } ?: run {
            ""
        }
    }

    private val skipList = arrayListOf(
        "com", "dev", "www", "in", "org", "co", "m", "auth"
    )

    fun promptUserToEnableAutofillService(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE).apply {
                setData(Uri.parse("package:dev.android.autofilldem"))
            }
            (context as Activity).startActivityForResult(intent, REQUEST_CODE_AUTOFILL_SERVICE)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open Autofill settings.", Toast.LENGTH_SHORT).show()
        }
    }
}