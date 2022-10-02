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


class TimePickerDialogFragment : SimpleDialogFragment() {

    companion object {
        val TAG: String = TimePickerDialogFragment::class.java.simpleName
        private const val TOTAL_MINUTES_EXTRA = "TOTAL_MINUTES_EXTRA"
        private const val INCREMENT_MINUTES_EXTRA = "INCREMENT_MINUTES_EXTRA"
        private const val PRESELECTED_MINUTES_EXTRA = "PRESELECTED_MINUTES_EXTRA"
        private const val DEFAULT_TOTAL_MINUTES = 1440
        private const val DEFAULT_PRESELECTED_MINUTES = 480
        private const val DEFAULT_INCREMENT_MINUTES = 0

        fun newInstance(totalMinutes: Int = DEFAULT_TOTAL_MINUTES, incrementMinutes: Int = DEFAULT_INCREMENT_MINUTES, preselectedMinutes: Int = DEFAULT_PRESELECTED_MINUTES): TimePickerDialogFragment {
            val bundle = Bundle()
            bundle.putInt(TOTAL_MINUTES_EXTRA, totalMinutes)
            bundle.putInt(INCREMENT_MINUTES_EXTRA, incrementMinutes)
            bundle.putInt(PRESELECTED_MINUTES_EXTRA, preselectedMinutes)
            val fragment = TimePickerDialogFragment()
            //fragment.setStyle(STYLE_NORMAL, R.style.DialogTheme)
            fragment.arguments = bundle
            return fragment
        }
    }

    private var listener: ((Int) -> Unit)? = null
    private val firstMap = mutableMapOf<Int, Pair<Int, String>>()
    private val secondMap = mutableMapOf<Int, Pair<Int, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

    }

    override fun build(builder: Builder): Builder = builder
            .setView(LayoutInflater.from(activity).inflate(R.layout.fragment_time_picker_dialog, null))
            .setTitle(R.string.select_hours_per_day_text)
            .setPositiveButton(R.string.ok_btn_text) { onValueSelected() }
            .setNegativeButton(R.string.cancel_btn_text) { dismiss() }

    override fun onShow(dialog: DialogInterface?) {
        super.onShow(dialog)
        val btnOk = view?.findViewById<Button>(com.avast.android.dialogs.R.id.sdl_button_positive)
        btnOk?.setTextColor(Color.parseColor("#1B2A3D"))
        val btnCancel = view?.findViewById<Button>(com.avast.android.dialogs.R.id.sdl_button_negative)
        btnCancel?.setTextColor(Color.parseColor("#1B2A3D"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val totalMinutes = arguments?.getInt(TOTAL_MINUTES_EXTRA) ?: DEFAULT_TOTAL_MINUTES
        val incrementMinutes = arguments?.getInt(INCREMENT_MINUTES_EXTRA) ?: DEFAULT_INCREMENT_MINUTES
        val preselectedMinutes = arguments?.getInt(PRESELECTED_MINUTES_EXTRA) ?: DEFAULT_PRESELECTED_MINUTES
        setupPickers(totalMinutes, incrementMinutes, preselectedMinutes)

    }

    private fun setupPickers(totalMinutes: Int, incrementMinutes: Int, preselectedMinutes: Int) {
        val possibleValues = IntRange(0, totalMinutes).filter { incrementMinutes == 0 || it % incrementMinutes == 0 }.toIntArray()
        if (incrementMinutes >= 60) {
            val generalValues = possibleValues.filter { it != 0 }.toIntArray()
            val defaultValueIndex = generalValues.indexOf(generalValues.minBy { Math.abs(preselectedMinutes - it) } ?: 0)
            initPicker(firstPicker, generalValues, defaultValueIndex, firstMap) { "${it / 60}h" + if (it % 60 != 0) " ${it % 60}m" else "" }
            return
        }
        val hourValues = possibleValues.filter { it % 60 == 0 }.toIntArray()
        val minuteValues = possibleValues.filter { it < 60 }.distinct().toIntArray()
        val defaultHourIndex = hourValues.indexOf(hourValues.minBy { Math.abs(((preselectedMinutes / 60) * 60) - it) } ?: 0)
        val defaultMinuteIndex = minuteValues.indexOf(minuteValues.minBy { Math.abs((preselectedMinutes % 60) - it) } ?: 0)
        initPicker(firstPicker, hourValues, defaultHourIndex, firstMap) { "${it / 60}h" }
        initPicker(secondPicker, minuteValues, defaultMinuteIndex, secondMap) { "${it % 60}m" }

        setupPickerListeners()
    }

    private fun initPicker(picker: NumberPicker, values: IntArray, defaultValueIndex: Int, map: MutableMap<Int, Pair<Int, String>>, transformFn: (Int) -> String) {
        values.associateByTo(map, { values.indexOf(it) }, { Pair(it, transformFn(it)) })
        picker.minValue = 0
        picker.maxValue = map.size - 1
        picker.displayedValues = map.values.map { it.second }.toTypedArray()
        picker.visibility = View.VISIBLE
        picker.wrapSelectorWheel = false
        picker.value = defaultValueIndex
    }

    private fun setupPickerListeners() {
        firstPicker.setOnValueChangedListener { _, _, newValue ->
            if (newValue == firstMap.size - 1) {
                secondPicker.value = 0
            }
            if (newValue == 0 && secondPicker.value == 0) {
                secondPicker.value++
            }
        }
        secondPicker.setOnValueChangedListener { _, _, newValue ->
            if (newValue != 0 && firstPicker.value == firstMap.size - 1) {
                firstPicker.value--
            }
            if (newValue == 0 && firstPicker.value == 0) {
                firstPicker.value++
            }
        }
    }

    private fun onValueSelected() {
        var value = firstMap[firstPicker.value]?.first ?: 0
        if (secondMap.isNotEmpty()) {
            value += secondMap[secondPicker.value]?.first ?: 0
        }
        dismiss()
        listener?.let { it(value) }
    }

    fun setOnTimeValueSetListener(listener: (Int) -> Unit): TimePickerDialogFragment {
        this.listener = listener
        return this
    }
}

