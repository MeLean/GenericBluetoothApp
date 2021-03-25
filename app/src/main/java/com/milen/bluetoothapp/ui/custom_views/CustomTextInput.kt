package com.milen.bluetoothapp.ui.custom_views

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.milen.bluetoothapp.R
import com.milen.bluetoothapp.utils.beGone
import com.milen.bluetoothapp.utils.beVisible
import kotlinx.android.synthetic.main.ahs_input_layout.view.*

const val DEFAULT_REST_VALUE = 0
class CustomTextInput  @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attr, defStyleAttr) {

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.ahs_input_layout, this, true)

        val attributeArray: TypedArray? = context.theme.obtainStyledAttributes(
            attr,
            R.styleable.CustomTextInput, 0, 0
        )
        setResOrHide(
            left_img,
            attributeArray?.getResourceId(
                R.styleable.CustomTextInput_cti_left_icon,
                DEFAULT_REST_VALUE
            )
        )

        setResOrHide(
            right_img,
            attributeArray?.getResourceId(
                R.styleable.CustomTextInput_cti_right_icon,
                DEFAULT_REST_VALUE
            )
        )

        val strValue = attributeArray?.getString(R.styleable.CustomTextInput_cti_hint) ?: ""
        attributeArray?.getResourceId(R.styleable.CustomTextInput_cti_hint, DEFAULT_REST_VALUE)
            ?.let {
                when {
                    it != DEFAULT_REST_VALUE -> input_layout.setHint(it)
                    else -> input_layout.hint = strValue
                }
            }

        applyMode(
            attributeArray?.getInt(
                R.styleable.CustomTextInput_cti_mode,
                MODE.CTI_DEFAULT.ordinal
            ) ?: MODE.CTI_DEFAULT.ordinal
        )

        applyInputType(
            attributeArray?.getInt(
                R.styleable.CustomTextInput_cti_input_type,
                INPUT_TYPE.CTI_DEFAULT.ordinal
            ) ?: INPUT_TYPE.CTI_DEFAULT.ordinal
        )

        attributeArray?.recycle()
    }

    private fun applyInputType(InputOriginal: Int) {
        when(InputOriginal){
            INPUT_TYPE.CTI_PASSWORD.ordinal -> input_edit_text.inputType = EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else -> input_edit_text.inputType = EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE
        }
    }

    private fun setResOrHide(img: ImageView?, resId: Int?) {
        img?.let {
            if (resId != null && resId != DEFAULT_REST_VALUE) {
                it.setImageResource(resId)
                it.beVisible()
            } else {
                it.beGone()
            }
        }
    }

    private fun applyMode(modeOriginal: Int) {
        when (modeOriginal) {
            MODE.CTI_VALID.ordinal ->
                applyResForMode(
                    R.color.cti_green_hint,
                    R.drawable.cti_rounded_green
                )
            MODE.CTI_ERROR.ordinal ->
                applyResForMode(
                    R.color.cti_red_hint,
                    R.drawable.cti_rounded_red
                )
            else ->
                applyResForMode(
                    R.color.cti_gray_hint,
                    R.drawable.cti_rounded_gray
                )
        }
    }

    private fun applyResForMode(
        @ColorRes colorRes: Int,
        @DrawableRes drawableRes: Int
    ) {
        left_img.setTint(colorRes)
        right_img.setTint(colorRes)
        input_edit_text.setHintResTextColor(colorRes)
        input_layout.setHintResTextColor(colorRes)
        result_msg.setResTextColor(colorRes)

        input_wrapper.setBackgroundResource(drawableRes)
    }

//    fun setLeftImgRes(@DrawableRes drawableRes: Int = DEFAULT_REST_VALUE) {
//        setResOrHide(left_img, drawableRes)
//    }
//
//    fun setRightImgRes(@DrawableRes drawableRes: Int = DEFAULT_REST_VALUE) {
//        setResOrHide(right_img, drawableRes)
//    }

    fun setHintRes(@StringRes strRes: Int) {
        input_edit_text.setHint(strRes)
        input_layout.setHint(strRes)
    }

    fun setText(text: String = "") {
        input_edit_text.setText(text)
    }

    fun showResultMessage(mode: MODE, msg: String = "", @DrawableRes rightImrRes: Int? = null) {
        rightImrRes?.let {
            setResOrHide(right_img, it)
        }
        applyMode(mode.ordinal)

        result_msg.text = msg
    }

    enum class MODE {
        CTI_DEFAULT,
        CTI_VALID,
        CTI_ERROR
    }

    enum class INPUT_TYPE {
        CTI_DEFAULT,
        CTI_PASSWORD,
    }
}

private fun TextView.setResTextColor(colorRes: Int) {
    this.setTextColor(ContextCompat.getColor(context, colorRes))
}

fun TextInputLayout.setHintResTextColor(@ColorRes colorRes: Int) {
    this.defaultHintTextColor = ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))
}

fun TextInputEditText.setHintResTextColor(@ColorRes color: Int) {
    this.setHintTextColor(ContextCompat.getColor(context, color))
}

fun ImageView.setTint(@ColorRes colorRes: Int) {
    ImageViewCompat.setImageTintList(
        this,
        ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))
    )
}