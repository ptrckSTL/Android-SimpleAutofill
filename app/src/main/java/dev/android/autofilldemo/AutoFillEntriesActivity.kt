package dev.android.autofilldemo

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import dev.android.autofilldemo.Constants.EXTRA_IDENTIFIER
import dev.android.autofilldemo.db.AutofillDataDao
import dev.android.autofilldemo.db.AutofillDataSet
import dev.android.autofilldemo.db.AutofillDatabase
import dev.android.autofilldemo.util.Util
import dev.android.autofilldemo.util.VaultListAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class AutoFillEntriesActivity : ComponentActivity(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    private lateinit var databaseDao: AutofillDataDao
    private lateinit var adapter: VaultListAdapter
    private val allRecords = arrayListOf<AutofillDataSet>()
    private var identifier: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.auto_fill_entries_activity)

        lifecycleScope.launch {
            databaseDao =
                AutofillDatabase.getDatabase(this@AutoFillEntriesActivity).autofillDataDao()
            allRecords.clear()
            allRecords.addAll(databaseDao.getAllRecords())
            val listView: ListView = findViewById(R.id.vaultListView)
            adapter = VaultListAdapter(this@AutoFillEntriesActivity, allRecords)
            listView.adapter = adapter

            val nameET = findViewById<EditText>(R.id.name)
            val usernameET = findViewById<EditText>(R.id.username)
            val pwdET = findViewById<EditText>(R.id.pwd)
            val identifierET = findViewById<EditText>(R.id.identifier)

            intent?.let {
                it.getStringExtra(EXTRA_IDENTIFIER)?.apply {
                    nameET.setText(Util.parseIdentifierForNameField(this))
                    identifierET.setText(this)
                }.also { txt ->
                    identifier = txt
                }
            }

            findViewById<Button>(R.id.saveBtn).setOnClickListener {
                if (usernameET.text.isNullOrEmpty()) {
                    Toast.makeText(
                        this@AutoFillEntriesActivity,
                        "please enter username field",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                } else if (pwdET.text.isNullOrEmpty()) {
                    Toast.makeText(
                        this@AutoFillEntriesActivity,
                        "please enter password field",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                } else if (identifierET.text.isNullOrEmpty()) {
                    Toast.makeText(
                        this@AutoFillEntriesActivity,
                        "please enter identifier field",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                insertRecord(
                    AutofillDataSet(
                        name = nameET.text.toString().trim(),
                        username = usernameET.text.toString().trim(),
                        password = pwdET.text.toString().trim(),
                        identifier = identifierET.text.toString().trim()
                    )
                )

//                if (identifier.isNullOrEmpty().not())
//                    finish()
            }
        }
    }

    private fun insertRecord(dataSet: AutofillDataSet) = launch {
        databaseDao.insertRecord(dataSet)
        allRecords.add(0, dataSet)
        Log.d("Autofill", "${databaseDao.getAllRecords()}")
        withContext(Dispatchers.Main) {
            adapter.notifyDataSetChanged()
            Toast.makeText(
                this@AutoFillEntriesActivity,
                "Vault saved!",
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }
}