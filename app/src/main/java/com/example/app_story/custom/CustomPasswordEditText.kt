package com.example.app_story.custom

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.example.app_story.R

class CustomPasswordEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    init {
        setupListener()
    }

    private fun setupListener() {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validatePassword(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun validatePassword(password: CharSequence?) {
        if (password.isNullOrEmpty() || password.length < 8) {
            error = context.getString(R.string.password_error_message)
        } else {
            error = null
        }
    }
}
