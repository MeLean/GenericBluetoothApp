package com.milen.bluetoothapp.base

import android.view.View

interface OnItemClickListener<T> {
    fun onItemClick(view: View, selectedItem: T?)
}
