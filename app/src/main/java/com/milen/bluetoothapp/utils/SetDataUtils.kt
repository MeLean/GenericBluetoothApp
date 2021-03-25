package com.milen.bluetoothapp.utils

import android.widget.ImageView
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView


class SetDataUtils {
    companion object{
         private const val DEFAULT_ANIMATION:String = "logoloader.json"

         fun setImageOrHide(imageView: ImageView?, imageUrl: String?) {
            if(isNullOrEmpty(imageUrl?.trim())){
                imageView?.beGone()
            }else{
                imageView?.loadUrl(imageUrl!!)
            }
        }

         fun setAnimateUrlOrHide(animationView: LottieAnimationView?, animationUrl: String?) {
            if(isNullOrEmpty(animationUrl?.trim())){
                animationView?.beGone()
            }else{
                animationView?.animateFromUrlOrDefault(animationUrl,
                   DEFAULT_ANIMATION
                )
            }
        }

        fun setAnimateResOrHide(animationView: LottieAnimationView?, filename: String?) {
            if(isNullOrEmpty(filename?.trim())){
                animationView?.beGone()
            }else{
                animationView?.setAnimation(filename)
                animationView?.playAnimation()
            }
        }

         fun setTextOrHide(textView: TextView?, text: String?) {
            if(isNullOrEmpty(text?.trim())){
                textView?.beGone()
            }else{
                textView?.text = text
            }
        }

         fun isNullOrEmpty(text: String?): Boolean {
            return text == null || text.isEmpty()
        }
    }
}


private fun ImageView?.loadUrl(imageUrl: String) {

}
