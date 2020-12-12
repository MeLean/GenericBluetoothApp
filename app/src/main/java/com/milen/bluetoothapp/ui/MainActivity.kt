package com.milen.bluetoothapp.ui

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.milen.bluetoothapp.Constants.BLUETOOTH_START_REQUEST_CODE
import com.milen.bluetoothapp.Constants.PERMISSION_REQUEST_CODE
import com.milen.bluetoothapp.R
import com.milen.bluetoothapp.ui.pager.MainFragmentStateAdapter
import com.milen.bluetoothapp.ui.pager.MainFragmentStateAdapter.Page.*
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

    private fun initViewPager(pages: Array<MainFragmentStateAdapter.Page>) {
        main_view_pager?.let {viewPager ->
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
            if(isAvailable == false && denyCount < 2 ){
                denyCount++
                startBluetoothIntent()
            }
        })
    }

    fun startBluetoothIntent() {
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
}