package com.milen.bluetoothapp.ui

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.milen.bluetoothapp.Constants.BLUETOOTH_START_REQUEST_CODE
import com.milen.bluetoothapp.R
import com.milen.bluetoothapp.ui.pager.MainFragmentStateAdapter
import com.milen.bluetoothapp.ui.pager.MainFragmentStateAdapter.Pages.*
import com.milen.bluetoothapp.view_models.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var denyCount = 0
    private val viewModel: MainViewModel by viewModels()

    //TODO Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device?.name
                    val deviceHardwareAddress = device?.address // MAC address
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

        registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
    }

    override fun onDestroy() {
        super.onDestroy()
        // unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun initViewPager(pages: Array<MainFragmentStateAdapter.Pages>) {
        main_view_pager?.let {
            val viewPagerAdapter = MainFragmentStateAdapter(this, pages)
            it.adapter = viewPagerAdapter

           TabLayoutMediator(main_bottom_tab_layout, main_view_pager) { tab, position ->
                tab.setText(viewPagerAdapter.getStringResIdByPage(pages[position]))
            }.attach()

            if(viewModel.bluetoothAdapter?.isEnabled == true){
                it.setCurrentItem(PAGE_PARED_DEVICES.ordinal, true)
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
            if(!isAvailable && denyCount < 2 ){
                denyCount++
                startActivityForResult(
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                    BLUETOOTH_START_REQUEST_CODE
                )
            }
        })
    }
}