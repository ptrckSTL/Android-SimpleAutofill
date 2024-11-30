package dev.android.autofilldemo

import android.app.Activity
import android.app.PendingIntent
import android.app.assist.AssistStructure
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.service.autofill.Dataset
import android.service.autofill.FillResponse
import android.util.Log
import android.view.autofill.AutofillId
import android.view.autofill.AutofillManager
import android.view.autofill.AutofillManager.EXTRA_ASSIST_STRUCTURE
import android.view.autofill.AutofillManager.EXTRA_AUTHENTICATION_RESULT
import android.view.autofill.AutofillValue
import android.widget.Button
import android.widget.EditText
import android.widget.RemoteViews
import android.widget.Toast
import androidx.activity.ComponentActivity
import dev.android.autofilldemo.db.AutofillDataSet
import dev.android.autofilldemo.db.AutofillDatabase
import dev.android.autofilldemo.model.ParsedStructure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class AutoFillSaveActivity : ComponentActivity(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.auto_fill_save_activity)

        val nameET = findViewById<EditText>(R.id.name)
        val usernameET = findViewById<EditText>(R.id.username)
        val pwdET = findViewById<EditText>(R.id.pwd)
        val identifierET = findViewById<EditText>(R.id.identifier)

        intent?.let {
            val component = it.getStringExtra("componentPkg")
            component?.let { value ->
                identifierET.setText(value)
            }
        }

        findViewById<Button>(R.id.saveBtn).setOnClickListener {
            if (usernameET.text.isNullOrEmpty()) {
                Toast.makeText(
                    this@AutoFillSaveActivity,
                    "please enter username field",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            } else if (pwdET.text.isNullOrEmpty()) {
                Toast.makeText(
                    this@AutoFillSaveActivity,
                    "please enter password field",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            } else if (identifierET.text.isNullOrEmpty()) {
                Toast.makeText(
                    this@AutoFillSaveActivity,
                    "please enter identifier field",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }


            launch {
                AutofillDatabase.getDatabase(this@AutoFillSaveActivity).autofillDataDao()
                    .insertRecord(AutofillDataSet(
                        name = nameET.text.toString().trim(),
                        username = usernameET.text.toString().trim(),
                        password = pwdET.text.toString().trim(),
                        identifier = identifierET.text.toString().trim())
                    )
            }

            Toast.makeText(this@AutoFillSaveActivity, "Vault saved!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}