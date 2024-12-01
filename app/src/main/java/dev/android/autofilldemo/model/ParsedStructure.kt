package dev.android.autofilldemo.model

import android.view.autofill.AutofillId

data class ParsedStructure(
    var nameId: AutofillId? = null,
    var usernameId: AutofillId? = null,
    var passwordId: AutofillId? = null,
    var identifier: String? = null
)