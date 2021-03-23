package com.milen.bluetoothapp.ui.custom_views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.milen.bluetoothapp.R

class CustomTextInput  @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attr, defStyleAttr) {

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.ahs_input_layout, this, true)
    }

}