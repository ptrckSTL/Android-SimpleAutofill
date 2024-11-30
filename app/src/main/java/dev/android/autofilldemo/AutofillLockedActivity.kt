package dev.android.autofilldemo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity

class AutofillLockedActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.auto_fill_locked_activity)

        findViewById<Button>(R.id.unlockBtn).setOnClickListener {

            val unlocked = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(Constants.PREF_KEY_VAULT_LOCKED, false)
                .commit()

            if (unlocked) {
                Toast.makeText(this, "Vault Unlocked!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, AutoFillSaveActivity::class.java).apply {
                    putExtra("componentPkg", intent?.getStringExtra("componentPkg"))
                })
                finish()
            } else {
                Toast.makeText(this, "Error unlocking vault.", Toast.LENGTH_SHORT).show()
            }
        }

    }
}