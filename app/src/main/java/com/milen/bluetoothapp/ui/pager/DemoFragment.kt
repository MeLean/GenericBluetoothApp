package com.milen.bluetoothapp.ui.pager

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.db.williamchart.ExperimentalFeature
import com.db.williamchart.data.AxisType
import com.db.williamchart.data.shouldDisplayAxisX
import com.db.williamchart.slidertooltip.SliderTooltip
import com.milen.bluetoothapp.R
import kotlinx.android.synthetic.main.demo_fragment.*

@ExperimentalFeature
class DemoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.demo_fragment, container, false)


    override fun onViewCreated(view: View, saveInstanceState: Bundle?) {

        /**
         * Line Chart
         */
        lineChart.gradientFillColors =
            intArrayOf(
                Color.parseColor("#81FFFFFF"),
                Color.TRANSPARENT
            )
        lineChart.animation.duration = animationDuration
        lineChart.tooltip =
            SliderTooltip().also {
                it.color = Color.WHITE
            }
        lineChart.onDataPointTouchListener = { index, _, _ ->
            lineChartValue.text =
                lineSet.toList()[index]
                    .second
                    .toString()
        }
        lineChart.animate(lineSet)
        /**
         * Bar Chart
         */
        barChart.animation.duration = animationDuration
        barChart.barsColorsList = createListColorForBarSet(barSet)

        barChart.animate(barSet)
       // barChart.barsColorsList

        /**
         * Donut Chart
         */
        donutChart.donutColors = intArrayOf(
            Color.parseColor("#FFFFFF"),
            Color.parseColor("#9EFFFFFF"),
            Color.parseColor("#8DFFFFFF")
        )
        donutChart.animation.duration = animationDuration
        donutChart.animate(donutSet)

        /**
         * Horizontal Bar Chart
         */
        horizontalBarChart.animation.duration = animationDuration
        horizontalBarChart.animate(horizontalBarSet)
    }

    private fun createListColorForBarSet(barSet: List<Pair<String, Float>>): List<Int> {
        val listResult : MutableList<Int> = mutableListOf()

        barSet.forEach {
            listResult.add(
                when{
                    it.second <= 3f ->
                        ContextCompat.getColor(requireContext(), R.color.chart_red)
                    it.second >= 7f ->
                        ContextCompat.getColor(requireContext(), R.color.chart_green)
                    else
                        -> ContextCompat.getColor(requireContext(), R.color.chart_yellow)
                }
            )
        }

        return listResult
    }

    companion object {
        private val lineSet = listOf(
            "Jan" to 15f,
            "Feb" to 8.5f,
            "Mar" to 14.7f,
            "Apr" to 7.5f,
            "May" to 10.6f,
            "Jun" to 16.5f
        )

        private val barSet = listOf(
            "JAN" to 1f,
            "FEB" to 6f,
            "MAR" to 10f,
            "APR" to 1f,
            "MAY" to 6f,
            "JUN" to 6f,
            "JUL" to 10f
        )

        private val horizontalBarSet = listOf(
            "PORRO" to 5f,
            "FUSCE" to 6.4f,
            "EGET" to 3f
        )

        private val donutSet = listOf(
            20f,
            80f,
            100f
        )

        private const val animationDuration = 1000L
    }
}