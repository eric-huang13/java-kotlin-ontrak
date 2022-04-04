package com.delphiaconsulting.timestar.view.extension

import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner

/**
 * Created by dxsier on 7/14/17.
 */

inline fun View.snack(@StringRes messageRes: Int, length: Int = Snackbar.LENGTH_LONG, f: Snackbar.() -> Unit) {
    snack(resources.getString(messageRes), length, f)
}

inline fun View.snack(message: String, length: Int = Snackbar.LENGTH_LONG, f: Snackbar.() -> Unit) {
    val snack = Snackbar.make(this, message, length)
    snack.f()
    snack.show()
}

fun Snackbar.action(@StringRes actionRes: Int, color: Int? = null, listener: (View) -> Unit) {
    action(view.resources.getString(actionRes), color, listener)
}

fun Snackbar.action(action: String, color: Int? = null, listener: (View) -> Unit) {
    setAction(action, listener)
    color?.let { setActionTextColor(color) }
}

fun Int.partOf(intArray: IntArray) = intArray.any { it == this }

fun EditText.addOnTextChanged(onTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = onTextChanged(p0.toString())

        override fun afterTextChanged(editable: Editable?) {}
    })
}

fun Spinner.onItemSelected(onItemSelected: (AdapterView<*>, Int) -> Unit) {
    this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(adapterView: AdapterView<*>, view: View, position: Int, l: Long) = onItemSelected(adapterView, position)

        override fun onNothingSelected(adapterView: AdapterView<*>) {}
    }
}

var View.visible: Boolean
    get() = visibility == View.VISIBLE
    set(show) {
        visibility = if (show) View.VISIBLE else View.GONE
    }