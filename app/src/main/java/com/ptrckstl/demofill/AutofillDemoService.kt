package com.ptrckstl.demofill

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.Dataset
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.InlinePresentation
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import android.util.Log
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.autofill.inline.v1.InlineSuggestionUi

class AutofillDemoService : AutofillService(), InlinePreference {
    override val context: Context = this
    private val tag = AutofillDemoService::class.java.simpleName

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        try {
            Log.d(tag, "onFillRequest")

            // Get the structure of the activity requesting autofill
            val structure = request.fillContexts.last().structure
            val componentPkg = structure.activityComponent.packageName
            Log.d(tag, "component pkg: $componentPkg")

            // Debug inline suggestions request
            val inlineSuggestionsRequest = request.inlineSuggestionsRequest
            Log.d(tag, "Inline suggestions request: $inlineSuggestionsRequest")

            val autofillIds = mutableListOf<AutofillId>()

            // Traverse structure to find autofill fields
            for (i in 0 until structure.windowNodeCount) {
                val windowNode = structure.getWindowNodeAt(i)
                findAutofillFields(windowNode.rootViewNode, autofillIds)
            }

            Log.d(tag, "Found ${autofillIds.size} autofill fields")

            if (autofillIds.isNotEmpty()) {
                // Create a fallback remote view for regular autofill
                val remoteView = RemoteViews(packageName, android.R.layout.simple_list_item_1)
                remoteView.setTextViewText(android.R.id.text1, "Test")

                val datasetBuilder = Dataset.Builder()

                // Fill all found fields with "Test"
                autofillIds.forEach { autofillId ->
                    datasetBuilder.setValue(
                        autofillId,
                        AutofillValue.forText("Test"),
                        remoteView
                    )
                }

                val inlinePresentation =
                    if (preferInline) createInlinePresentation(request) else null
                if (inlinePresentation != null) {
                    Log.d(tag, "using inline presentation")
                    @Suppress("DEPRECATION") // new api requires minSDK >= 33
                    datasetBuilder.setInlinePresentation(inlinePresentation)
                } else {
                    Log.d(tag, "using RemoteView presentation")
                }
                val fillResponse = FillResponse.Builder()
                    .addDataset(datasetBuilder.build())
                    .build()

                Log.d(tag, "Fulfilling autofill callback!")
                callback.onSuccess(fillResponse)
            } else {
                Log.d(tag, "No autofill fields found")
                callback.onSuccess(null)
            }

        } catch (e: Exception) {
            Log.e(tag, "Error processing autofill request", e)
            callback.onFailure(e.message)
        }
    }

    @SuppressLint("RestrictedApi")
    private fun createInlineSuggestionSlice(
        title: CharSequence = "Simple",
        subtitle: CharSequence = "Autofill",
        pendingIntent: PendingIntent = buildPendingIntent()
    ) = InlineSuggestionUi.newContentBuilder(pendingIntent)
        .setTitle(title)
        .setSubtitle(subtitle)
        .build()
        .getSlice() // property access syntax causing wacky android studio lag for some reason

    private fun buildPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(Intent.ACTION_VIEW),
        PendingIntent.FLAG_IMMUTABLE
    )

    @SuppressLint("RestrictedApi")
    private fun createInlinePresentation(request: FillRequest): InlinePresentation? {
        return try {
            val inlineSuggestionsRequest = request.inlineSuggestionsRequest
            if (inlineSuggestionsRequest?.inlinePresentationSpecs != null) {
                val inlineSlice = createInlineSuggestionSlice()
                InlinePresentation(
                    inlineSlice,
                    inlineSuggestionsRequest.inlinePresentationSpecs.first(),
                    false
                )
            } else {
                Log.d(tag, "No inline presentation specs available")
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Error creating inline presentation", e)
            null
        }
    }

    private fun findAutofillFields(
        viewNode: android.app.assist.AssistStructure.ViewNode?,
        autofillIds: MutableList<AutofillId>
    ) {
        viewNode?.let { node ->
            // If this node has an autofill ID and is important for autofill, add it
            if (node.autofillId != null && node.autofillType != android.view.View.AUTOFILL_TYPE_NONE) {
                autofillIds.add(node.autofillId!!)
                Log.d(tag, "Added autofill field: ${node.autofillId} - ${node.className}")
            }

            // Recursively check children
            for (i in 0 until node.childCount) {
                findAutofillFields(node.getChildAt(i), autofillIds)
            }
        }
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        Log.d(tag, "onSaveRequest")
        callback.onSuccess()
    }
}
