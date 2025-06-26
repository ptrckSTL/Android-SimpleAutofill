package com.ptrckstl.demofill

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit
import com.ptrckstl.demofill.InlinePreference.Companion.PREFER_INLINE

interface InlinePreference {
    val context: Context

    companion object {
        const val PREFER_INLINE = "InlinePreference.prefer_inline"
    }
}

private fun getPrefs(context: Context) = context.getSharedPreferences("prefs", MODE_PRIVATE)

var InlinePreference.preferInline: Boolean
    get() = getPrefs(context).getBoolean(PREFER_INLINE, true)
    set(value) = getPrefs(context).edit(commit = true) { putBoolean(PREFER_INLINE, value) }