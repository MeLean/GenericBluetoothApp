package com.milen.bluetoothapp.utils

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import com.squareup.picasso.Picasso

fun View.beGone(){
    this.visibility = GONE
}

fun View.beVisible(){
    this.visibility = VISIBLE
}

fun ImageView.loadUrl(imageUrl: String) {
    if (imageUrl.isNotEmpty()) {
            Picasso.get()
                .load(imageUrl)
                .into(this)
    }
}