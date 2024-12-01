package dev.amirraza.autofill.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import dev.amirraza.autofill.R
import dev.amirraza.autofill.db.AutofillDataSet

class VaultListAdapter(private val context: Context, private val data: List<AutofillDataSet>) :
    BaseAdapter() {

    override fun getCount(): Int = data.size

    override fun getItem(position: Int): Any = data[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View =
            convertView ?: LayoutInflater.from(context).inflate(R.layout.vault_items, parent, false)
        val titleTextView: TextView = view.findViewById(R.id.titleTextView)
        val subtitleTextView: TextView = view.findViewById(R.id.subtitleTextView)

        val item = data[position]
        titleTextView.text = if (item.name.isNullOrEmpty().not()) item.name else item.username
        subtitleTextView.text = item.identifier

        return view
    }
}
