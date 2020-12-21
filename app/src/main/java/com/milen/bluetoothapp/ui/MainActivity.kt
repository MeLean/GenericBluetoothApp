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
import android.os.Bundle
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.milen.bluetoothapp.Constants.BLUETOOTH_START_REQUEST_CODE
import com.milen.bluetoothapp.Constants.PERMISSION_REQUEST_CODE
import com.milen.bluetoothapp.R
import com.milen.bluetoothapp.ui.pager.MainFragmentStateAdapter
import com.milen.bluetoothapp.ui.pager.MainFragmentStateAdapter.Page.PAGE_PARED_DEVICES
import com.milen.bluetoothapp.ui.pager.MainFragmentStateAdapter.Page.values
import com.milen.bluetoothapp.ui.pager.pages.ACTION_DISCOVERY_FAILED
import com.milen.bluetoothapp.view_models.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_scan_devices_page.*


class MainActivity : AppCompatActivity() {
    private var denyCount = 0
    private val viewModel: MainViewModel by viewModels()

    private val deviceFoundReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_FOUND -> {
                    onDeviceFound(intent)
                }

                ACTION_DISCOVERY_STARTED -> {
                    showMessage(getString(R.string.device_scanning_start))
                }

                ACTION_DISCOVERY_FINISHED -> {
                    showMessage(getString(R.string.device_scanning_finished))
                }

                ACTION_DISCOVERY_FAILED -> {
                    //TODO SHOW POPUP WITH IMAGE
                    showMessage(getString(R.string.device_scanning_failed))
                    startActivityForResult(Intent(ACTION_LOCATION_SOURCE_SETTINGS), 0);
                }
            }
        }
    }

    private val deviceBoundStateChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    Snackbar.make(scan_devices_fab, getString(R.string.device_bound_changed), Snackbar.LENGTH_SHORT)
                        .show()

                    var device: BluetoothDevice? = null
                    if(intent.hasExtra(BluetoothDevice.EXTRA_DEVICE)){
                        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }

                    device?.let {
                        when (it.bondState) {
                            BluetoothDevice.BOND_BONDED -> {
                                viewModel.setParedBluetoothDevice(it)
                                viewModel.setShouldScrollToPage(PAGE_PARED_DEVICES)
                                showMessage(getString(R.string.device_bound_bonded))
                            }
                            BluetoothDevice.BOND_BONDING -> {
                                showMessage(getString(R.string.device_bound_bounding))
                            }
                            BluetoothDevice.BOND_NONE -> {
                                showMessage(getString(R.string.device_bound_none))
                                viewModel.setParedBluetoothDevice(null)
                            }
                        }
                    }


                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        enableBluetoothIfNot()

        initViewPager(values())
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

    private fun initViewPager(pages: Array<MainFragmentStateAdapter.Page>) {
        main_view_pager?.let { viewPager ->
            val viewPagerAdapter = MainFragmentStateAdapter(this, pages)
            viewPager.adapter = viewPagerAdapter

            TabLayoutMediator(main_bottom_tab_layout, main_view_pager) { tab, position ->
                tab.setText(viewPagerAdapter.getStringResIdByPage(pages[position]))
            }.attach()

            viewModel.getShouldScrollToPage().observe(this, { page ->
                main_bottom_tab_layout?.getTabAt(page.ordinal)?.select()
            })

            if(viewModel.bluetoothAdapter?.isEnabled == true){
                viewModel.setShouldScrollToPage(PAGE_PARED_DEVICES)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == BLUETOOTH_START_REQUEST_CODE ) {
            viewModel.setBluetoothAvailability(resultCode == Activity.RESULT_OK)
        }
    }

    private fun enableBluetoothIfNot() {
        viewModel.getBluetoothAvailability().observe(this, { isAvailable ->
            if (isAvailable == false && denyCount < 2) {
                denyCount++
                startBluetoothOnIntent()
            }
        })
    }

    fun startBluetoothOnIntent() {
        startActivityForResult(
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
            BLUETOOTH_START_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    viewModel.setBluetoothPermissionGranted(true)
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

    private fun showMessage(msg: String) {
        Snackbar.make(main_view_pager, msg, Snackbar.LENGTH_SHORT)
            .show()
    }
}
