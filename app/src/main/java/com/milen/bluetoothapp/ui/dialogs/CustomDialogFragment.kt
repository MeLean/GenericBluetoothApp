package com.milen.bluetoothapp.ui.dialogs

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.milen.bluetoothapp.R
import com.milen.bluetoothapp.utils.SetDataUtils

import kotlinx.android.synthetic.main.custom_dialog_fragment.view.*


internal const val FRAG_TAG = "dialogs.CustomDialogFragment"

class CustomDialogFragment @JvmOverloads constructor(
    private val dialogTitle: String? = null,
    private val dialogText: String? = null,
    private val dialogBackgroundAnimationUrl: String? = null,
    private val dialogLogoAnimationUrl: String? = null,

    private val solidButtonText: String? = null,
    private val textButtonText: String? = null,
    private val strockedButtonText: String? = null,

    private val solidButtonCallback: () -> Unit? = { },
    private val textButtonCallback: () -> Unit? = { },
    private val strockedButtonCallback: () -> Unit = { }
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.apply {
            setBackgroundDrawableResource(R.drawable.bs_very_transparent_black)
        }

        val view = inflater.inflate(R.layout.custom_dialog_fragment, container, false)
        setData(view)
        return view
    }

    private fun setData(view: View?) {
        view?.let {
            SetDataUtils.setAnimateResOrHide(
                it.dialog_logo_background,
                dialogBackgroundAnimationUrl
            )
            SetDataUtils.setAnimateResOrHide(it.dialog_logo, dialogLogoAnimationUrl)
            SetDataUtils.setTextOrHide(it.dialog_title, dialogTitle)
            SetDataUtils.setTextOrHide(it.dialog_text, dialogText)

            SetDataUtils.setTextOrHide(it.dialog_center_btn, strockedButtonText)
            SetDataUtils.setTextOrHide(it.dialog_filled_btn, solidButtonText)
            SetDataUtils.setTextOrHide(it.dialog_just_text_btn, textButtonText)

            it.dialog_center_btn.setOnClickListener {
                strockedButtonCallback()
                dismiss()
            }
            it.dialog_filled_btn.setOnClickListener {
                solidButtonCallback()
                dismiss()
            }
            it.dialog_just_text_btn.setOnClickListener {
                textButtonCallback()
                dismiss()
            }

            it.dialog_close.setOnClickListener {
                dismiss()
            }

            it.rootView?.apply {
                setBackgroundResource(R.drawable.rounded_corners)
                setPaddingInDp(16,16,16,32)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.apply {
            window?.attributes?.let {
                it.width = ViewGroup.LayoutParams.WRAP_CONTENT
                it.height = ViewGroup.LayoutParams.WRAP_CONTENT
            }

            setCancelable(false)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        dialog?.dismiss()
    }

    //new methods
    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialog
    }
}

fun View.dpToPx(dp: Float): Int = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()

fun View.setPaddingInDp(leftDp: Int, topDp: Int, rightDp: Int, bottomDp: Int){
    setPadding(
        dpToPx(leftDp.toFloat()),
        dpToPx(topDp.toFloat()),
        dpToPx(rightDp.toFloat()),
        dpToPx(bottomDp.toFloat())
    )
}