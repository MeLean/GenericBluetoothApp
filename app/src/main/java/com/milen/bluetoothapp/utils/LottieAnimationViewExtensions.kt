package com.milen.bluetoothapp.utils

import com.airbnb.lottie.LottieAnimationView

fun LottieAnimationView.animateFromUrlOrDefault(url : String?, defaultJson: String){
    this.setAnimationFromUrl(url)
    this.setFailureListener {
        this.setAnimation(defaultJson)
    }
}