package com.milen.bluetoothapp.utils

import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup

fun ViewGroup.shouldShow(shouldShow : Boolean){
    when(shouldShow){
        true -> this.visibility = VISIBLE
        else -> this.visibility = GONE
    }

}