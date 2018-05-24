package com.vitalii.android.handwriting

import android.app.Dialog
import android.os.Bundle
import android.app.DialogFragment
import android.app.AlertDialog
import android.util.Log

class TeachFragment : DialogFragment() {

    interface OnInputListener {
        fun onTeachInput(name: String)
    }

    private val TAG = this::class.java.simpleName
    private val listItems = ('A'..'Z').map { it.toString() }.toTypedArray()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val onInputListener = activity as OnInputListener
        var chosenItem: Int = -1
        return AlertDialog.Builder(activity)
                .setTitle("What is the letter?")
                .setSingleChoiceItems(listItems,-1, { _, which ->
                    Log.d(TAG, "onClick: $which selected")
                    chosenItem = which
                })
                .setNegativeButton("Cancel", { dialog, _ ->
                    Log.d(TAG, "onClick: closing dialog")
                    chosenItem = -1
                    dialog.cancel()
                })
                .setPositiveButton("Ok", { dialog, _ ->
                    Log.d(TAG, "onClick: capturing input, $chosenItem selected")
                    if (chosenItem != -1) onInputListener.onTeachInput(listItems[chosenItem])
                    dialog.dismiss()
                })
                .create()
    }
}