package com.milen.bluetoothapp.ui.pager.pages

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.milen.bluetoothapp.Constants.BLUETOOTH_DISCOVERY_REQUEST_CODE
import com.milen.bluetoothapp.Constants.BLUETOOTH_DISCOVERY_SECONDS_COUNT
import com.milen.bluetoothapp.R
import com.milen.bluetoothapp.base.ui.BasePageFragment
import kotlinx.android.synthetic.main.fragment_settings_page.view.*

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


    @SuppressLint("SetTextI18n")
    private fun subscribeToIncomingMessages(view: View) {
        viewModel.getIncomingMessage().observe(viewLifecycleOwner, {
            val previousText = view.bluetooth_on.text
            view.bluetooth_on.text = "$previousText\n$it"
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

        view.btn_discovery.setOnClickListener {
            startActivityForResult(
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                    putExtra(
                        BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
                        BLUETOOTH_DISCOVERY_SECONDS_COUNT
                    )
                },
                BLUETOOTH_DISCOVERY_REQUEST_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == BLUETOOTH_DISCOVERY_REQUEST_CODE &&
            resultCode != Activity.RESULT_CANCELED){
            setAvailability(true)
        }
    }

    private fun setAvailability(isAvailable: Boolean) {
        viewModel.setBluetoothAvailability(isAvailable)
    }
}