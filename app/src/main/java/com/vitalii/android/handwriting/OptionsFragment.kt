package com.vitalii.android.handwriting

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.util.Log
import android.widget.EditText

class OptionsFragment : DialogFragment() {

    interface OnInputListener {
        fun onOptionsInput(input: String)
    }

    private val TAG = this::class.java.simpleName

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inputListener = activity as OnInputListener
        val editText = EditText(activity)
        return AlertDialog.Builder(activity)
                .setTitle("Host address")
                .setView(editText)
                .setNegativeButton("Cancel", { dialog, _ ->
                    Log.d(TAG, "onCreateDialog: Cancel option dialog")
                    dialog.dismiss()
                })
                .setPositiveButton("Ok", { dialog, _ ->
                    Log.d(TAG, "onCreateDialog: Pressed Ok")
                    inputListener.onOptionsInput(editText.text.toString())
                    dialog.dismiss()
                })
                .create()
    }
}