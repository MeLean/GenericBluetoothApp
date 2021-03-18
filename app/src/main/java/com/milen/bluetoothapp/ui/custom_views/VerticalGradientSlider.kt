package com.milen.bluetoothapp.ui.custom_views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.milen.bluetoothapp.R
import kotlinx.android.synthetic.main.vertival_gradient_slider.view.*
import kotlin.math.roundToInt


class VerticalGradientSlider @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attr, defStyleAttr) {

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.vertival_gradient_slider, this, true)

        setOnTouchEventListener(vgs_ring)
        calculateRingPosition()
    }

    private fun calculateRingPosition(deltaY: Int = 0) {
        var curPosition = 0.0f

        vgs_ring?.constraintLayoutParams()?.let{
            curPosition = calculateCurPosition(it.verticalBias, deltaY)
            it.verticalBias = curPosition
            vgs_ring.invalidate()
        }

        val ringPosition = 100 - (curPosition * 100).toInt()
        vgs_score_num.text = ringPosition.toString()
        vgs_score_text.text = calculateTextFor(ringPosition)
    }

    private fun calculateCurPosition(
        bias: Float,
        deltaY: Int
    ) : Float{
        val deltaFloat = deltaY / 1000f
        val value = bias + deltaFloat
        return when {
            value < 0.0f -> 0.0f
            value > 1f -> 1f
            else -> value
        }
    }


    private fun calculateTextFor(value: Int): String {
        val moods = context.resources.getStringArray(R.array.moods)
        val part = 100f / (moods.size -1)
        val roundInt = (value / part).roundToInt()
        return moods[roundInt]
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setOnTouchEventListener(view: View?) {
        view?.setOnTouchListener(object :
            OnTouchListener {
            private var prevY = 0
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val par = v.constraintLayoutParams()
                when (event.action) {
                    MotionEvent.ACTION_MOVE -> {
                        val deltaY = event.rawY.toInt() - prevY
                        prevY = event.rawY.toInt()
                        //Log.d("TEST_IT", "ACTION_MOVE deltaY: $deltaY prevY: $prevY" )
                        calculateRingPosition(deltaY)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        val deltaY = event.rawY.toInt() - prevY
                        //Log.d("TEST_IT", "ACTION_UP deltaY: $deltaY")
                        calculateRingPosition(deltaY)
                        return true
                    }
                    MotionEvent.ACTION_DOWN -> {
                        prevY = event.rawY.toInt()
                        //Log.d("TEST_IT", "ACTION_DOWN prevY: $prevY")
                        return true
                    }
                }
                return false
            }
        })
    }
}


fun View.constraintLayoutParams(): ConstraintLayout.LayoutParams? {
    return when (this.parent) {
        is ConstraintLayout ->
            this.layoutParams as ConstraintLayout.LayoutParams
        else -> null
    }
}