package com.milen.bluetoothapp.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.milen.bluetoothapp.R
import com.milen.bluetoothapp.utils.SetDataUtils
import com.milen.bluetoothapp.utils.beGone
import kotlinx.android.synthetic.main.custom_dialog_fragment.view.*


internal const val FRAG_TAG = "dialogs.CustomDialogFragment"

class CustomDialogFragment @JvmOverloads constructor(private val dialogTitle: String? = null,
                                                     private val dialogText: String? = null,
                                                     private val dialogBackgroundAnimationUrl: String? = null,
                                                     private val dialogLogoAnimationUrl: String? = null,

                                                     private val leftButtonText: String? = null,
                                                     private val rightButtonText: String? = null,
                                                     private val centerButtonText: String? = null,

                                                     private val leftButtonCallback: () -> Unit? = { },
                                                     private val rightButtonCallback: () -> Unit? = { },
                                                     private val centerButtonCallback: () -> Unit = { }
) : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.setBackgroundDrawableResource(R.drawable.rounded_corners)
        val view = inflater.inflate(R.layout.custom_dialog_fragment, container, false)
        setData(view)
        return view
    }

    private fun setData(view: View?) {
        view?.let {
            SetDataUtils.setAnimateResOrHide(it.dialog_logo_background, dialogBackgroundAnimationUrl)
            SetDataUtils.setAnimateResOrHide(it.dialog_logo, dialogLogoAnimationUrl)
            SetDataUtils.setTextOrHide(it.dialog_title, dialogTitle)
            SetDataUtils.setTextOrHide(it.dialog_text, dialogText)

            setButtonVisibility(it.dialog_center_btn, centerButtonText)
            setButtonVisibility(it.dialog_left_btn, leftButtonText)
            setButtonVisibility(it.dialog_right_btn, rightButtonText)

            it.dialog_center_btn.setOnClickListener {
                centerButtonCallback()
                dismiss()
            }
            it.dialog_left_btn.setOnClickListener {
                leftButtonCallback()
                dismiss()
            }
            it.dialog_right_btn.setOnClickListener {
                rightButtonCallback()
                dismiss()
            }
        }
    }

    private fun setButtonVisibility(button: Button?, buttonText: String?) {
        if (buttonText != null) {
            button?.let {
                it.text = buttonText
            }
        } else {
            button?.beGone()
        }
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.95).toInt()
        //val height = (resources.displayMetrics.heightPixels * 0.35).toInt()
        dialog?.window?.attributes?.width = width
        dialog?.window?.attributes?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog?.setCancelable(false)
    }
}
