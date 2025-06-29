package com.example.weather_app

import android.content.Context
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout

object DialogManager {

    fun searchByNameDialog(context: Context, listener: Listener) {
        // Создаем кастомный layout для диалога
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_city_search, null)
        val inputLayout = dialogView.findViewById<TextInputLayout>(R.id.inputLayout)
        val editText = dialogView.findViewById<EditText>(R.id.etCityName)

        // Создаем Material Design диалог
        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle("Search city")
            .setView(dialogView)
            .setPositiveButton("Search", null) // Обработчик будет переопределен позже
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .create()

        // Кастомная обработка кнопки Search
        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                validateAndSubmit(editText, inputLayout, listener, dialog)
            }
        }

        // Обработка нажатия Enter на клавиатуре
        editText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.action == KeyEvent.ACTION_DOWN &&
                        event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                validateAndSubmit(editText, inputLayout, listener, dialog)
                true
            } else {
                false
            }
        }

        // Автофокус и показ клавиатуры
        editText.requestFocus()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        dialog.show()
    }

    private fun validateAndSubmit(
        editText: EditText,
        inputLayout: TextInputLayout,
        listener: Listener,
        dialog: AlertDialog
    ) {
        val cityName = editText.text.toString().trim()

        when {
            cityName.isEmpty() -> {
                inputLayout.error = "Please enter city name"
            }
            cityName.length < 2 -> {
                inputLayout.error = "Name too short"
            }
            else -> {
                listener.onClick(cityName)
                hideKeyboard(editText)
                dialog.dismiss()
            }
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    interface Listener {
        fun onClick(cityName: String)
    }
}