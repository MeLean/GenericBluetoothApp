package com.milen.bluetoothapp.base.adapters

import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import com.milen.bluetoothapp.base.OnItemClickListener

abstract class BaseAdapterDelegate<T, VH : BaseViewHolder, M> {

    abstract fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder

    @CallSuper
    open fun bindViewHolder(holder: VH, model: M, position: Int, listener: OnItemClickListener<M?>, isChecked : Boolean = false) {
        //unused
    }
}
