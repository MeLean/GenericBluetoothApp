package com.milen.bluetoothapp.ui

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED
import android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_STARTED
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.ACTION_FOUND
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.snackbar.Snackbar
import com.milen.GenericBluetoothApp.Companion.defaultSharedPreferences
import com.milen.bluetoothapp.R
import com.milen.bluetoothapp.data.entities.ConditionNames
import com.milen.bluetoothapp.data.entities.TrackerDetails
import com.milen.bluetoothapp.data.entities.TrackerScores
import com.milen.bluetoothapp.extensions.showToastMessage
import com.milen.bluetoothapp.extensions.toDecodedString
import com.milen.bluetoothapp.services.MESSAGE_CONNECT_SUCCESS
import com.milen.bluetoothapp.services.MESSAGE_FAIL_CONNECT
import com.milen.bluetoothapp.services.MyBluetoothService
import com.milen.bluetoothapp.ui.custom_views.CustomTextInput
import com.milen.bluetoothapp.ui.custom_views.FlowView
import com.milen.bluetoothapp.ui.dialogs.CustomDialogFragment
import com.milen.bluetoothapp.ui.dialogs.FRAG_TAG
import com.milen.bluetoothapp.ui.pager.MainFragmentStateAdapter.Page.PAGE_PARED_DEVICES
import com.milen.bluetoothapp.utils.Utils
import com.milen.bluetoothapp.utils.beGone
import com.milen.bluetoothapp.view_models.ACTION_DISCOVERY_FAILED
import com.milen.bluetoothapp.view_models.MainViewModel
import com.milen.bluetoothapp.view_models.MainViewModelFactory
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt


const val BLUETOOTH_START_REQUEST_CODE = 123
const val PERMISSION_REQUEST_CODE = 12345

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            defaultSharedPreferences,
            MyBluetoothService.getInstance(BluetoothAdapter.getDefaultAdapter(), createHandler()),
        )
    }

    private val deviceFoundReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_FOUND -> onDeviceFound(intent)
                ACTION_DISCOVERY_STARTED -> showToastMessage(getString(R.string.device_scanning_start))
                ACTION_DISCOVERY_FINISHED -> showToastMessage(getString(R.string.device_scanning_finished))
                ACTION_DISCOVERY_FAILED -> {
                    showToastMessage(getString(R.string.device_scanning_failed))
                    startActivityForResult(Intent(ACTION_LOCATION_SOURCE_SETTINGS), 0)
                }
            }
        }
    }

    private val deviceBoundStateChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    showToastMessage(getString(R.string.device_bound_changed))

                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    device?.let {
                        when (it.bondState) {
                            BluetoothDevice.BOND_BONDED -> {
                                viewModel.setParedBluetoothDevice(it)
                                viewModel.setShouldScrollToPage(PAGE_PARED_DEVICES)
                                showToastMessage(getString(R.string.device_bound_bonded))
                            }
                            BluetoothDevice.BOND_BONDING -> {
                                showToastMessage(getString(R.string.device_bound_bounding))
                            }
                            BluetoothDevice.BOND_NONE -> {
                                showToastMessage(getString(R.string.device_bound_none))
                                viewModel.setParedBluetoothDevice(null)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun createHandler(): Handler {
        return Handler { msg ->
            when (msg.what) {
                MESSAGE_FAIL_CONNECT -> viewModel.whenMessageFail()
                MESSAGE_CONNECT_SUCCESS -> viewModel.whenMessageSuccess()
            }

            msg.obj?.let {
                viewModel.handleHandlerMessage(msg.what, it)
                showToastMessage(it.toDecodedString())
            }
            true
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        load_chart_example.setOnClickListener{
            startActivity(Intent(this, FilledLineActivity::class.java))
        }



        //drawBarChart(crateFakeTrackers())

        //Corner radius to bottom nav barr
        val radius = resources.getDimension(R.dimen.default_corner_radius)
        val bottomBarBackground = bottomAppBar.background as MaterialShapeDrawable
        bottomBarBackground.shapeAppearanceModel = bottomBarBackground.shapeAppearanceModel
            .toBuilder()
            .setTopRightCorner(CornerFamily.ROUNDED, radius)
            .setTopLeftCorner(CornerFamily.ROUNDED, radius)
            .setBottomLeftCorner(CornerFamily.ROUNDED, radius)
            .setBottomRightCorner(CornerFamily.ROUNDED, radius)
            .build()

        //show more buttons layout animated
        more_buttons_layout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                more_buttons_layout.animateCircularHide()
                more_buttons_layout.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        fab.setOnClickListener {
            if (it.alpha > 0.0f) {
                val centreX= (it.x + it.width / 2).roundToInt()
                val centreY= (it.y + it.height / 2).roundToInt()
                when {
                    more_buttons_layout.isVisible ->
                        more_buttons_layout.animateCircularHide(centreX, centreY)

                    else -> more_buttons_layout.animateCircularReveal(centreX, centreY)
                }
            }
        }

        //hide FAB on Bottom bar scrolled
        val point = Point()
        val startSize = fab.customSize
        nested_view.viewTreeObserver.addOnScrollChangedListener {
            windowManager.defaultDisplay.getSize(point)
            when {
                bottomAppBar.y + bottomAppBar.measuredHeight > point.y -> {
                    fab.customSize = 1; fab.alpha = 0.0f
                }
                else -> {
                    fab.customSize = startSize; fab.alpha = 1f
                }
            }
        }

        //Int FlowView

        flowView.setItems(
            listOf(
                ConditionNames("1", "test 1"),
                ConditionNames("2", "test 2")
            )
        )

        //test override items if we have any
        flowView.setItems(
            listOf(
                ConditionNames("1", "IBDU"),
                ConditionNames("2", "Ulcerative Colitis"),
                ConditionNames("3", "Crohn’s Disease"),
                ConditionNames("4", "Pouchitis"),
                ConditionNames("5", "Celiac"),
                ConditionNames("6", "Microscoptic Colitis"),
                ConditionNames("7", "Not yet diagnosed")
            )
        )

        flowView.setFlowViewOnClickListener(object: FlowView.OnFlowViewItemClickListener{
            override fun onItemClicked(item: ConditionNames) {
                Snackbar.make(
                    this@MainActivity,
                    flowView,
                    "chosen:  ${item.name ?: "NO NAME"}",
                    Snackbar.LENGTH_LONG
                ).also{
                    it.anchorView = fab
                }.show()
            }
        })

        //Playing around with custom views
        custom_valid.setText("yeaaaa!")
        custom_valid.setHintRes(R.string.click_to_scan_for_devices)
        custom_valid.showResultMessage(CustomTextInput.MODE.CTI_VALID, "BRAVO!", R.drawable.cti_circular_tick)

        custom_default.setText("ohoooo!")

        custom_error.showResultMessage(CustomTextInput.MODE.CTI_ERROR, "Error", R.drawable.cti_error_x)

        //bottomSheet popups

        show_popup_btn.setOnClickListener{
            CustomDialogFragment(
                "Feedback submitted!",
                "Thank you for submitting your rating and experience!",
                null,
                null,
                "yes",
                "no",
                null,
                {
                    Snackbar.make(show_popup_btn, "Yeeeaaay!", Snackbar.LENGTH_LONG).also {
                        it.anchorView = fab
                    }.show()
                },
                {}
            ).show(supportFragmentManager, FRAG_TAG)
        }
    }

    private fun crateFakeTrackers(): List<TrackerDetails> {
        val fakeTrackers = mutableListOf<TrackerDetails>()
        val tracker = TrackerDetails()

        val fakeResults: MutableList<TrackerScores> = mutableListOf()
        val fakeScores = listOf(1,6,3,4,3,3,7)
        for (i in 0..6) {
            fakeResults.add(TrackerScores().also {
                it.date = "2021-03-" + (19 + i) +"T00:00:00+00:00"
                it.id = 0
                it.score = fakeScores[i]
            })
        }

        tracker.apply {
            graphColor = "e9ea89"
            title_part = "How you felt about your mood"
            results = fakeResults
            trackerId = 1
            trackerName = "Mood"
            uniqueid = 36
        }

        fakeTrackers.add(tracker)

        return fakeTrackers

    }

    override fun onStart() {
        super.onStart()
        registerDeviceReceivers()
    }

    override fun onStop() {
        super.onStop()
        unregisterDeviceReceiver()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == BLUETOOTH_START_REQUEST_CODE ) {
            when (resultCode) {
                Activity.RESULT_OK -> viewModel.setBluetoothAvailability(true)
                else -> viewModel.enableBluetoothIfNot(this)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    viewModel.setBluetoothPermissionGranted(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                }
            }
        }
    }

    private fun onDeviceFound(intent: Intent) {
        val device: BluetoothDevice? =
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        device?.let {
            viewModel.addFoundDevice(it)
        }
    }

    private fun registerDeviceReceivers() {
        registerReceiver(
            deviceFoundReceiver,
            IntentFilter().also {
                it.addAction(ACTION_FOUND)
                it.addAction(ACTION_DISCOVERY_STARTED)
                it.addAction(ACTION_DISCOVERY_FINISHED)
                it.addAction(ACTION_DISCOVERY_FAILED)
            }
        )

        registerReceiver(
            deviceBoundStateChangedReceiver,
            IntentFilter().also { it.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED) }
        )
    }

    private fun unregisterDeviceReceiver() {
        unregisterReceiver(deviceFoundReceiver)
        unregisterReceiver(deviceBoundStateChangedReceiver)
    }

    val sevendays: MutableList<String> = mutableListOf()
    var results: List<TrackerScores> = listOf()
    var trackerdetails: List<TrackerDetails> = listOf()

    fun drawBarChart(trackerdetails: List<TrackerDetails>) {
//        trackerdetails = new ArrayList<>();
        this.trackerdetails = trackerdetails
        trackerdetails[0].results?.let {
            results = it
        }
        for (j in results.indices) {
            sevendays.add(Utils.changeDateFormatDate(results[j].date, "dd/MM", true))
        }

//        chart.setDrawBarShadow(false)
//        chart.setDrawValueAboveBar(false)
        chart.getDescription().setEnabled(false)
        chart.setMaxVisibleValueCount(8)
        chart.setPinchZoom(false)
        chart.setDragEnabled(false)
        chart.setScaleEnabled(false)
        chart.setDrawGridBackground(false)
        val xAxisLabel = ArrayList<String>()

        for (i in 0..6) {
            if (i < sevendays.size) {
                xAxisLabel.add(sevendays.get(i))
            } else {
                xAxisLabel.add("")
            }
        }
        val xAxis: XAxis = chart.getXAxis()

        xAxis.valueFormatter =
            IAxisValueFormatter { value, axis -> xAxisLabel[value.toInt()] }
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f // only intervals of 1 day
        xAxis.textColor = ContextCompat.getColor(this, R.color.gray_hint)
        xAxis.textSize = 11f
        xAxis.axisLineColor = ContextCompat.getColor(this, android.R.color.transparent)
        xAxis.gridColor = ContextCompat.getColor(this, (R.color.light_transparent_green))

        val leftAxis: YAxis = chart.axisRight
        leftAxis.setLabelCount(8, true)
        leftAxis.granularity = 1f
        leftAxis.axisMinimum = 0f
        leftAxis.gridLineWidth = .6f
        leftAxis.textColor = ContextCompat.getColor(this, R.color.gray_hint)
        leftAxis.gridColor = ContextCompat.getColor(this, R.color.colorPrimaryApp)
        leftAxis.enableGridDashedLine(20f, 20f, 0f)
        val yAxisLabel = ArrayList<String>()
        for (i in 0..7) {
            yAxisLabel.add("☹️")
            yAxisLabel.add(" ")
            yAxisLabel.add(" ")
            yAxisLabel.add("\uD83D\uDE10")
            yAxisLabel.add(" ")
            yAxisLabel.add(" ")
            yAxisLabel.add("\uD83D\uDE42")
            yAxisLabel.add(" ")
        }
        leftAxis.valueFormatter =
            IAxisValueFormatter { value, axis -> yAxisLabel[value.toInt()] }
        leftAxis.axisMaximum = 7f
        leftAxis.textColor = ContextCompat.getColor(this, R.color.gray_hint)
        leftAxis.textSize = 24f
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        leftAxis.axisLineColor = ContextCompat.getColor(this, android.R.color.transparent)
        leftAxis.gridColor = ContextCompat.getColor(this, R.color.light_transparent_green)
        leftAxis.setDrawLabels(true)

//        val rightAxis: YAxis = chart.getAxisRight()
//        rightAxis.setDrawGridLines(false)
//        rightAxis.setDrawLabels(false)
//        rightAxis.axisLineColor = ContextCompat.getColor(this, android.R.color.transparent)
//        rightAxis.gridColor = ContextCompat.getColor(this, R.color.light_transparent_green)
        val l: Legend = chart.getLegend()
        l.isEnabled = false
        l.setDrawInside(false)
        setData(sevendays.size, 200)
    }

    private fun setData(count: Int, range: Int) {
        val entries: MutableList<Entry> = ArrayList()
        //        entries.add(new BarEntry(0,0));
        if (trackerdetails.size != 0) {
            for (i in 0..6) {
                if (i < sevendays.size) {
                    entries.add(
                        Entry(
                            i.toFloat(),
                            trackerdetails[0].results?.get(i)?.score?.toFloat() ?: 0f
                        )
                    )
                } else {
                    entries.add(Entry(i.toFloat(), 0f))
                }
            }
        }
        val set = LineDataSet(entries, "LineDataSet")
        set.color = Color.parseColor("#" + trackerdetails[0].graphColor)
        val data = LineData(set)
        //data.barWidth = 0.3f // set custom bar width
        data.setDrawValues(false)
        chart.isDrawBordersEnabled()
        chart.setBorderColor(ContextCompat.getColor(this, R.color.light_transparent_green))
        chart.setData(data)
        //chart.setFitBars(true) // make the x-axis fit exactly all bars
        chart.setExtraRightOffset(0f)
        chart.animateY(1000)
        chart.invalidate()
    }

}


fun View.animateCircularReveal(x : Int = 0, y : Int = 0) {
    val finalRadius = max(this.width, this.height).toFloat()
    val circularReveal =
        ViewAnimationUtils.createCircularReveal(this, x,  y, 0f, finalRadius * 1.1f)
    circularReveal.duration = 300
    circularReveal.start()
    this.visibility = View.VISIBLE
}

fun View.animateCircularHide(x : Int = 0, y : Int = 0) {
    val startRadius = max(this.width, this.height).toFloat()
    val circularHide =
        ViewAnimationUtils.createCircularReveal(this, x, y, startRadius, 0.0f)
    circularHide.duration = 300
    circularHide.start()
    this.visibility = View.GONE
}