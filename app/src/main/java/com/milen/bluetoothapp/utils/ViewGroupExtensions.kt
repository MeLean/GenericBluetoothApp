package com.milen.bluetoothapp.utils

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.annotation.LayoutRes

fun ViewGroup.shouldShow(shouldShow : Boolean){
    when {
        shouldShow -> beVisible()
        else -> beGone()
    }
}

fun ViewGroup.inflateViewWithoutAttaching(@LayoutRes resId: Int): View {
    return LayoutInflater.from(context)
        .inflate(resId, this, false)
}