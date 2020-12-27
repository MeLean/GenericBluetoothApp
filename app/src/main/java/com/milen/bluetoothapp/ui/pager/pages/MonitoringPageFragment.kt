package com.milen.bluetoothapp.ui.pager.pages

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.milen.bluetoothapp.R
import com.milen.bluetoothapp.base.interfaces.OnItemClickListener
import com.milen.bluetoothapp.base.ui.pager.pages.BasePageFragment
import com.milen.bluetoothapp.data.entities.BluetoothMessageEntity
import com.milen.bluetoothapp.ui.adapters.BluetoothMessageAdapter
import kotlinx.android.synthetic.main.fragment_monitoring_page.view.*

const val DISCOVERY_CODE = 12312
class MonitoringPageFragment : BasePageFragment() {
    private lateinit var bluetoothMessageAdapter: BluetoothMessageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_monitoring_page, container, false)
        initRecycler(view)
        setOnClickListeners(view)
        return view
    }

    private fun initRecycler(view: View) {
        bluetoothMessageAdapter =
            BluetoothMessageAdapter(object : OnItemClickListener<BluetoothMessageEntity?> {
                override fun onItemClick(view: View, selectedItem: BluetoothMessageEntity?) {
                    /*do nothing*/
                }
            })

        view.bluetooth_message_recycler?.let {
            it.layoutManager = LinearLayoutManager(requireContext())
            it.setHasFixedSize(true)
            it.adapter = bluetoothMessageAdapter
        }

        viewModel.getIncomingMessages().observe(viewLifecycleOwner, {
            bluetoothMessageAdapter.setData(it)
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
            //TODO do nothing for now
        }

        view.btn_make_device_visible.setOnClickListener{
            startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE), DISCOVERY_CODE)
        }
    }

    private fun setAvailability(isAvailable: Boolean) {
        viewModel.setBluetoothAvailability(isAvailable)
    }
}