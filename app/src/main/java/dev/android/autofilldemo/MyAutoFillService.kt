package dev.android.autofilldemo

import android.app.PendingIntent
import android.app.assist.AssistStructure
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.Dataset
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import android.util.Log
import android.view.View
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import dev.android.autofilldemo.Constants.EXTRA_IDENTIFIER
import dev.android.autofilldemo.db.AutofillDatabase
import dev.android.autofilldemo.model.ParsedStructure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MyAutoFillService : AutofillService(), CoroutineScope {
    private val tag = MyAutoFillService::class.java.simpleName

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        try {
            val serviceContext = this@MyAutoFillService
            launch {
                // Get the structure of the activity requesting autofill
                val structure = request.fillContexts.last().structure
                val componentPkg =
                    structure.activityComponent.packageName // App requesting autofill
                Log.d(tag, "component pkg: $componentPkg")

                val parsedStructure: ParsedStructure = traverseStructure(structure)
                Log.d(tag, "parsed data: $parsedStructure")
                if (parsedStructure.identifier.isNullOrEmpty()) {
                    parsedStructure.identifier = componentPkg
                }

                val identifier: String = parsedStructure.identifier ?: componentPkg

                if (checkIfPasswordManagerAppIsLocked()) {
                    if (parsedStructure.usernameId == null)
                        return@launch

                    val listOfIds = arrayListOf<AutofillId?>()
                    if (parsedStructure.nameId != null) {
                        listOfIds.add(parsedStructure.nameId)
                    }
                    if (parsedStructure.usernameId != null) {
                        listOfIds.add(parsedStructure.usernameId)
                    }
                    if (parsedStructure.passwordId != null) {
                        listOfIds.add(parsedStructure.passwordId)
                    }

                    val intent = Intent(serviceContext, AutofillLockedActivity::class.java).apply {
                        putExtra(EXTRA_IDENTIFIER, identifier)
                    }

                    val pendingIntent = PendingIntent.getActivity(
                        serviceContext,
                        0,
                        intent,
                        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val unlockView = RemoteViews(
                        serviceContext.packageName,
                        R.layout.autofill_locked_remote_view
                    )

                    unlockView.setOnClickPendingIntent(R.id.lockedRemoteViewLL, pendingIntent)

                    // Respond with an authentication request
                    val fillResponse = FillResponse.Builder()
                        .setAuthentication(
                            listOfIds.toTypedArray(),
                            pendingIntent.intentSender,
                            unlockView
                        )
                        .build()

                    callback.onSuccess(fillResponse)

                    return@launch
                }

                val databaseDao =
                    AutofillDatabase.getDatabase(this@MyAutoFillService).autofillDataDao()
                val allRecords = databaseDao.getAllRecordsByIdentifier(identifier)
                Log.d(tag, "allRecords for $identifier: ${allRecords.size}")

                if (allRecords.isNotEmpty()) {
                    val datasetList = arrayListOf<Dataset>()

                    allRecords.forEach { item ->
                        Log.d(tag, "$item")
                        if (item.username.isNullOrEmpty().not()) {
                            val display =
                                if (item.name.isNullOrEmpty().not()) item.name else item.username

                            val usernameRemoteView = RemoteViews(
                                serviceContext.packageName,
                                R.layout.autofill_item_remote_view
                            )
                            usernameRemoteView.setTextViewText(R.id.display, display)

                            val datasetBuilder = Dataset.Builder()
                            if (parsedStructure.usernameId != null) {
                                datasetBuilder.setValue(
                                    parsedStructure.usernameId!!,
                                    AutofillValue.forText(item.username),
                                    usernameRemoteView
                                )
                            }
                            if (parsedStructure.passwordId != null) {
                                datasetBuilder.setValue(
                                    parsedStructure.passwordId!!,
                                    AutofillValue.forText(item.password),
                                    usernameRemoteView
                                )
                            }

                            datasetList.add(datasetBuilder.build())
                        }
                    }

                    parsedStructure.usernameId?.let { usernameAutofillId ->
                        val addNewSet = Dataset.Builder().setValue(
                            usernameAutofillId,
                            AutofillValue.forText(""),
                            addNewRemoteView(serviceContext, identifier)
                        )

                        datasetList.add(addNewSet.build())
                    }

                    val fillResponseBuilder: FillResponse.Builder = FillResponse.Builder()

                    if (datasetList.isNotEmpty()) {

                        datasetList.forEach { set ->
                            fillResponseBuilder.addDataset(set)
                        }
                    }

                    callback.onSuccess(fillResponseBuilder.build())

                } else {
                    parsedStructure.usernameId?.let { usernameAutofillId ->
                        val fillResponse: FillResponse = FillResponse.Builder()
                            .addDataset(
                                Dataset.Builder().setValue(
                                    usernameAutofillId,
                                    AutofillValue.forText(""),
                                    getDefaultRemoteView(serviceContext, identifier)
                                ).build()
                            )
                            .build()
                        // If there are no errors, call onSuccess() and pass the response
                        callback.onSuccess(fillResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MyAutoFill", "Error processing autofill request", e)
            callback.onFailure(e.message)
        }
    }


    private fun traverseStructure(structure: AssistStructure): ParsedStructure {
        Log.d("MyAutoFill", "traverseStructure")
        val parsedStructure = ParsedStructure()

        val windowNodes: List<AssistStructure.WindowNode> =
            structure.run {
                (0 until windowNodeCount).map {
                    getWindowNodeAt(it)
                }
            }

        Log.d("MyAutoFill", "windowNodes size ${windowNodes.size}")

        windowNodes.forEach { windowNode: AssistStructure.WindowNode ->
            traverseNode(windowNode.rootViewNode, parsedStructure)
        }

        return parsedStructure
    }

    private fun traverseNode(
        viewNode: AssistStructure.ViewNode?,
        parsedStructure: ParsedStructure
    ) {
        if (viewNode?.autofillHints?.isNotEmpty() == true) {
            // If the client app provides autofill hints, you can obtain them using
            // viewNode.getAutofillHints();
            Log.d("MyAutoFill", "viewNode.autofillHints = ${viewNode.autofillHints}")
            viewNode.autofillHints?.forEach { item ->
                Log.d(
                    "MyAutoFill",
                    "autofill hints item = $item >> viewNode.autofillId=${viewNode.autofillId} >> text=${viewNode.text} >> class=${viewNode.className}"
                )
            }
        } else {
            // Or use your own heuristics to describe the contents of a view
            // using methods such as getText() or getHint()
            Log.d(
                "MyAutoFill",
                "hint=${viewNode?.hint} >> autofillId ${viewNode?.autofillId} >> text=${viewNode?.text} >> class=${viewNode?.className}"
            )
        }

        prepareParsedStructure(viewNode, parsedStructure)

        val children: List<AssistStructure.ViewNode>? =
            viewNode?.run {
                (0 until childCount).map { getChildAt(it) }
            }

        children?.forEach { childNode: AssistStructure.ViewNode ->
            traverseNode(childNode, parsedStructure)
        }
    }

    private fun prepareParsedStructure(
        viewNode: AssistStructure.ViewNode?,
        parsedStructure: ParsedStructure
    ) {
        val clazz = viewNode?.className
        if (clazz != null) {
            if (clazz.contains("EditText") || clazz.contains("AutoCompleteTextView")) {
                if (viewNode.webDomain.isNullOrEmpty()
                        .not() && parsedStructure.identifier.isNullOrEmpty()
                ) {
                    parsedStructure.identifier = viewNode.webDomain
                }

                if (viewNode.autofillHints.isNullOrEmpty().not()) {
                    viewNode.autofillHints?.forEach { item ->
                        Log.d(
                            "MyAutoFill",
                            "autofillHints=${viewNode.autofillHints?.size} > item = $item > viewNode.autofillId = ${viewNode.autofillId}"
                        )
                        if (item != null) {
                            buildParsedStructure(item, viewNode.autofillId, parsedStructure)
                        }
                    }
                } else if (viewNode.hint != null) {
                    buildParsedStructure(viewNode.hint!!, viewNode.autofillId, parsedStructure)
                } else {
                    if (clazz.contains("EditText") && parsedStructure.usernameId == null) {
                        buildParsedStructure("username", viewNode.autofillId, parsedStructure)
                    }
                }
                Log.d("MyAutoFill", "parsed EditText data : $parsedStructure")
            } else if (clazz.contains("TextView") &&
                viewNode.idEntry == "url_bar" &&
                viewNode.webDomain.isNullOrEmpty().not() &&
                parsedStructure.identifier.isNullOrEmpty()
            ) {
                Log.d("MyAutoFill", "InApp browser domain address : ${viewNode.webDomain}")
                parsedStructure.identifier = viewNode.webDomain
            }
        } else {
            if (viewNode?.autofillHints.isNullOrEmpty().not()) {
                viewNode?.autofillHints?.forEach { item ->
                    Log.d(
                        "MyAutoFill",
                        "autofillHints=${viewNode.autofillHints?.size} > item = $item > viewNode.autofillId = ${viewNode.autofillId}"
                    )
                    if (item != null) {
                        buildParsedStructure(item, viewNode.autofillId, parsedStructure)
                    }
                }
            } else if (viewNode?.hint != null) {
                buildParsedStructure(viewNode.hint!!, viewNode.autofillId, parsedStructure)
            }
        }
    }

    private fun buildParsedStructure(
        hint: String,
        autofillId: AutofillId?,
        parsedStructure: ParsedStructure
    ) {
        if (hint == View.AUTOFILL_HINT_NAME) {
            parsedStructure.nameId = autofillId
        } else if (View.AUTOFILL_HINT_USERNAME.equals(hint, ignoreCase = true) ||
            View.AUTOFILL_HINT_PHONE.equals(hint, ignoreCase = true) ||
            View.AUTOFILL_HINT_EMAIL_ADDRESS.equals(hint, ignoreCase = true) ||
            "id".equals(hint, ignoreCase = true) ||
            "login".equals(hint, ignoreCase = true) ||
            "email".equals(hint, ignoreCase = true) ||
            hint.contains("email", ignoreCase = true) ||
            hint.contains("mobile", ignoreCase = true) ||
            hint.contains("number", ignoreCase = true)
        ) {
            parsedStructure.usernameId = autofillId
        } else if (View.AUTOFILL_HINT_PASSWORD.equals(hint, ignoreCase = true) ||
            hint.contains(View.AUTOFILL_HINT_PASSWORD, ignoreCase = true)
        ) {
            parsedStructure.passwordId = autofillId
        }
    }

    private fun getDefaultRemoteView(serviceContext: Context, identifier: String) =
        RemoteViews(serviceContext.packageName, R.layout.autofill_default_remote_view).apply {
            setOnClickPendingIntent(
                R.id.defaultRemoteViewLL,
                PendingIntent.getActivity(
                    serviceContext, 0,
                    Intent(serviceContext, AutoFillEntriesActivity::class.java).apply {
                        putExtra(EXTRA_IDENTIFIER, identifier)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        }

    private fun addNewRemoteView(serviceContext: Context, identifier: String) =
        RemoteViews(serviceContext.packageName, R.layout.autofill_add_new_remote_view).apply {
            setOnClickPendingIntent(
                R.id.addNewVaultViewLL,
                PendingIntent.getActivity(
                    serviceContext, 0,
                    Intent(serviceContext, AutoFillEntriesActivity::class.java).apply {
                        putExtra(EXTRA_IDENTIFIER, identifier)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        }

    private fun checkIfPasswordManagerAppIsLocked(): Boolean {
        return getSharedPreferences(
            Constants.PREF_NAME,
            Context.MODE_PRIVATE
        ).getBoolean(Constants.PREF_KEY_VAULT_LOCKED, false)
    }

    override fun onSaveRequest(
        request: SaveRequest,
        callback: SaveCallback
    ) {
        // Handle saving credentials logic here
        Log.d("MyAutoFill", "onSaveRequest")
        callback.onSuccess()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate() {
        Log.d("MyAutoFill", "onCreate")
        super.onCreate()
    }

    override fun onConnected() {
        Log.d("MyAutoFill", "onConnected")
        super.onConnected()
    }

    override fun onDestroy() {
        Log.d("MyAutoFill", "onDestroy")
        super.onDestroy()
    }

    override fun onDisconnected() {
        Log.d("MyAutoFill", "onDisconnected")
        super.onDisconnected()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("MyAutoFill", "onStartCommand flag: $flags")
        return super.onStartCommand(intent, flags, startId)
    }
}
