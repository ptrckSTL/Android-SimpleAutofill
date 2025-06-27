package com.ptrckstl.demofill

import android.content.Context
import com.ptrckstl.demofill.InlinePreference.Companion.PREFER_INLINE_FILE
import java.io.File

// We don't use shared preferences because main activity and autofill service will desync
interface InlinePreference {
    val context: Context

    companion object {
        const val PREFER_INLINE_FILE = "prefer_inline.txt"
    }
}

private fun getPreferenceFile(context: Context) = File(context.filesDir, PREFER_INLINE_FILE)

var InlinePreference.preferInline: Boolean
    get() {
        val file = getPreferenceFile(context)
        return if (!file.exists()) {
            getPreferenceFile(context).writeText("true")
            true
        } else file.readText().toBoolean()
    }
    set(value) {
        getPreferenceFile(context).writeText(value.toString())
    }