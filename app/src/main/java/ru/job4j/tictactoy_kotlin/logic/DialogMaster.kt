package ru.job4j.tictactoy_kotlin.logic

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import ru.job4j.tictactoy_kotlin.R
import ru.job4j.tictactoy_kotlin.model.Player

/**
 * @author Daniil Stebnitsky
 */

class DialogMaster(private val context: Context) {
    @SuppressLint("InflateParams")
    fun openDialog(textView: TextView, player: Player) {
        with(AlertDialog.Builder(context)) {
            val view: View = LayoutInflater.from(context)
                    .inflate(R.layout.dialog, null)
            val textField = view.findViewById<EditText>(R.id.editTextPersonName)
            textField.setText(textView.text)
            textField.setSelection(textField.text.toString().length)
            setView(view)
            setPositiveButton(context.resources.getString(R.string.ok)) {
                _: DialogInterface, _: Int ->
                val text = textField.text
                if (text.isNotEmpty()) {
                    textView.text = textField.text
                    player.name = textField.text.toString()
                }
            }
            show()
        }
    }
}