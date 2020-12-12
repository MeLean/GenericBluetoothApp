package com.milen.bluetoothapp.ui.pager.pages

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.milen.bluetoothapp.R
import com.milen.bluetoothapp.base.interfaces.OnItemClickListener
import com.milen.bluetoothapp.base.ui.pager.pages.BasePageFragment
import com.milen.bluetoothapp.ui.MainActivity
import com.milen.bluetoothapp.ui.adapters.BluetoothDevicesAdapter
import kotlinx.android.synthetic.main.fragment_pared_devices_page.view.*

class ParedDevicesPageFragment : BasePageFragment() {
    private lateinit var bluetoothDevicesAdapter: BluetoothDevicesAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pared_devices_page, container, false)
        initRecycler(view)

        return view
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        bluetoothDevicesAdapter.setData(
            viewModel.bluetoothAdapter?.bondedDevices?.toList() ?: listOf()
        )
    }

    private fun initRecycler(view: View) {
        bluetoothDevicesAdapter =
            BluetoothDevicesAdapter(object : OnItemClickListener<BluetoothDevice?> {
                override fun onItemClick(view: View, selectedItem: BluetoothDevice?) {
                    viewModel.setBluetoothDevice(selectedItem)
                }
            })

        viewModel.getBluetoothDevice()
            .observe(viewLifecycleOwner,
                { device -> updateUiForDevice(device) }
            )

        val recycler = view.pared_device_recycler

        recycler?.let {
            it.layoutManager = LinearLayoutManager(requireContext())
            it.setHasFixedSize(true)
            val devices = viewModel.bluetoothAdapter?.bondedDevices?.toList()
            bluetoothDevicesAdapter.setData(devices ?: listOf())
            it.adapter = bluetoothDevicesAdapter
        }
    }

    private fun updateUiForDevice(device: BluetoothDevice?) {
        bluetoothDevicesAdapter.setChosenDevice(device)
        setTitleToParentActivity(device?.name ?: getString(R.string.app_name))
    }

    private fun setTitleToParentActivity(name: String) {
        if (requireActivity() is MainActivity) {
            requireActivity().title = name
        }
    }
}