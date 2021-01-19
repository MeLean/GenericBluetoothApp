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
import android.os.Handler
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.milen.GenericBluetoothApp.Companion.defaultSharedPreferences
import com.milen.bluetoothapp.R
import com.milen.bluetoothapp.extensions.showToastMessage
import com.milen.bluetoothapp.extensions.toDecodedString
import com.milen.bluetoothapp.services.MESSAGE_CONNECT_SUCCESS
import com.milen.bluetoothapp.services.MESSAGE_FAIL_CONNECT
import com.milen.bluetoothapp.services.MyBluetoothService
import com.milen.bluetoothapp.ui.pager.MainFragmentStateAdapter
import com.milen.bluetoothapp.ui.pager.MainFragmentStateAdapter.Page.PAGE_PARED_DEVICES
import com.milen.bluetoothapp.ui.pager.MainFragmentStateAdapter.Page.values
import com.milen.bluetoothapp.utils.EMPTY_STRING
import com.milen.bluetoothapp.view_models.ACTION_DISCOVERY_FAILED
import com.milen.bluetoothapp.view_models.MainViewModel
import com.milen.bluetoothapp.view_models.MainViewModelFactory
import kotlinx.android.synthetic.main.activity_main.*

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

        subscribeForBluetoothAvailability()

        initViewPager(values())

        manageDeepLinksIfAny(intent)
    }

    private fun subscribeForBluetoothAvailability() {
        viewModel.getBluetoothAvailability().observe(this, {
            viewModel.enableBluetoothIfNot(this)
        })
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        manageDeepLinksIfAny(intent)

    }

    private fun manageDeepLinksIfAny(intent: Intent?) {
        viewModel.checkIfDeepLinkItemsInIntent(intent)
        viewModel.getDeepLinkItems().observe(this, { map ->
            var textViewVisibility = GONE
            var textViewText = EMPTY_STRING

            if (!map.isNullOrEmpty()) {
                textViewVisibility = VISIBLE
                textViewText  = viewModel.makePresentationText(map, getString(R.string.deep_linking_called_text))
            }

            main_deep_link_text.apply {
                visibility = textViewVisibility
                text = textViewText
            }
        })
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

            if(viewModel.getBluetoothAdapter()?.isEnabled == true){
                viewModel.setShouldScrollToPage(PAGE_PARED_DEVICES)
            }
        }
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

}
