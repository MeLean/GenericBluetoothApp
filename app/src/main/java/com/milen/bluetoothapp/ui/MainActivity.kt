package com.milen.bluetoothapp.ui

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        enableBluetoothIfNot()

        initViewPager(values())
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun initViewPager(pages: Array<MainFragmentStateAdapter.Pages>) {
        main_view_pager?.let {
            it.adapter = MainFragmentStateAdapter(this, pages)

           TabLayoutMediator(main_bottom_tab_layout, main_view_pager) { tab, position ->
                tab.setText(viewModel.getStringResIdByPage(pages[position]))
            }.attach()

            it.currentItem =1
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