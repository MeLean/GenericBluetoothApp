package com.milen.bluetoothapp.ui.custom_views

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.milen.bluetoothapp.R
import com.milen.bluetoothapp.data.entities.ConditionNames
import kotlinx.android.synthetic.main.fw_layout.view.*

const val SELECTED_KEY = "selectedItemId"
const val SUPER_STATE_KEY = "superState"

class FlowView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attr, defStyleAttr) {
    private var views: MutableList<TextView> = mutableListOf()
    private var listener: OnFlowViewItemClickListener? = null
    private var selectedItemId: String? = null

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.fw_layout, this, true)
        isSaveEnabled = true
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putString(SELECTED_KEY, selectedItemId)
        bundle.putParcelable(SUPER_STATE_KEY, super.onSaveInstanceState())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        var viewState = state
        if (viewState is Bundle) {
            selectedItemId = viewState.getString(SELECTED_KEY, null)
            viewState = viewState.getParcelable(SUPER_STATE_KEY) ?: Bundle()
            selectedItemId?.let{
                views.forEach {view ->
                    view.isSelected = it == view.tag
                }
            }
        }
        super.onRestoreInstanceState(viewState)
    }

    fun setItems(items: List<ConditionNames>) {
        setViewItems(items)
        invalidate()
    }

    private fun setViewItems(items: List<ConditionNames>) {

        removeAllAddedView()
        createViewsFrom(items)
    }

    private fun removeAllAddedView() {
        views.forEach { textView ->
            this.removeView(textView)
        }

        views.clear()
    }

    private fun createViewsFrom(items: List<ConditionNames>) {
        val ids = mutableListOf<Int>()
        for (item in items) {
            val textView = TextView(ContextThemeWrapper(this.context, R.style.ConditionItem)).also {
                it.text = item.name
                it.tag = item.id
                it.isSelected = item.id == selectedItemId
                val viewId = View.generateViewId()
                ids.add(viewId)
                it.id = viewId
                it.setOnClickListener { _ ->
                    listener?.onItemClicked(item)
                    deselectAll(views)
                    selectedItemId = item.id
                    it.isSelected = true
                }

                views.add(it)
            }

            this.addView(textView)
        }

        flow_view.referencedIds = ids.toIntArray()
    }

    private fun deselectAll(views: MutableList<TextView>) {
        views.forEach {
            it.isSelected = false
        }
    }

    fun setFlowViewOnClickListener(listener: OnFlowViewItemClickListener) {
        this.listener = listener
    }

    interface OnFlowViewItemClickListener {
        fun onItemClicked(item: ConditionNames)
    }
}