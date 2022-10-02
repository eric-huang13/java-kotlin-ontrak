package com.delphiaconsulting.timestar.view.fragment


import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import androidx.core.content.ContextCompat
import com.avast.android.dialogs.fragment.SimpleDialogFragment
import com.delphiaconsulting.timestar.R
import kotlinx.android.synthetic.main.fragment_time_picker_dialog.*


class CustomDialogFragment : SimpleDialogFragment() {

    companion object {
        val TAG: String = CustomDialogFragment::class.java.simpleName

        fun newInstance(): CustomDialogFragment {
            val bundle = Bundle()
            val fragment = CustomDialogFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun build(builder: Builder): Builder = builder
        .setMessage(R.string.onboard_instruction_text)
        .setTitle(R.string.need_help_dialog_title)
        .setPositiveButton(R.string.close_btn_text){dismiss()}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

    }

    override fun onShow(dialog: DialogInterface?) {
        super.onShow(dialog)
        val btnOk = view?.findViewById<Button>(com.avast.android.dialogs.R.id.sdl_button_positive)
        btnOk?.setTextColor(Color.parseColor("#1B2A3D"))
        val btnCancel = view?.findViewById<Button>(com.avast.android.dialogs.R.id.sdl_button_negative)
        btnCancel?.setTextColor(Color.parseColor("#1B2A3D"))
    }

}

