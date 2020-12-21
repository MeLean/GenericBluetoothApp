package com.milen.bluetoothapp.ui.pager.pages

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.milen.bluetoothapp.R
import com.milen.bluetoothapp.base.ui.pager.pages.BasePageFragment
import kotlinx.android.synthetic.main.fragment_settings_page.view.*

const val DISCOVERY_CODE = 12312
class MonitoringPageFragment : BasePageFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings_page, container, false)
        subscribeToIncomingMessages(view)
        setOnClickListeners(view)
        return view
    }

    private fun subscribeToIncomingMessages(view: View) {
        viewModel.getIncomingMessage().observe(viewLifecycleOwner, {
            val text = "${view.bluetooth_on.text}\n$it"
            view.bluetooth_on.text = text
        })
    }

    private fun setOnClickListeners(view: View) {
        view.btn_off.setOnClickListener {
            viewModel.bluetoothAdapter?.disable()
            setAvailability(false)
        }

        view.btn_on.setOnClickListener {
            viewModel.bluetoothAdapter?.enable()
            setAvailability(true)
        }

        view.btn_restart_service.setOnClickListener{
           viewModel.restartService()
        }

        view.btn_make_device_visible.setOnClickListener{
            startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE), DISCOVERY_CODE)
        }
    }

    private fun setAvailability(isAvailable: Boolean) {
        viewModel.setBluetoothAvailability(isAvailable)
    }
}