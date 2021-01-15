package com.milen.bluetoothapp.ui.pager.pages

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.BOND_BONDED
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.milen.bluetoothapp.R
import com.milen.bluetoothapp.base.interfaces.OnItemClickListener
import com.milen.bluetoothapp.base.ui.pager.pages.BasePageFragment
import com.milen.bluetoothapp.ui.adapters.BluetoothDevicesAdapter
import com.milen.bluetoothapp.ui.pager.MainFragmentStateAdapter.Page.PAGE_PARED_DEVICES
import kotlinx.android.synthetic.main.fragment_scan_devices_page.view.*


class ScanDevicesPageFragment : BasePageFragment() {
    private lateinit var scanDevicesAdapter: BluetoothDevicesAdapter

    private fun initRecycler(view: View) {
        scanDevicesAdapter =
            BluetoothDevicesAdapter(object : OnItemClickListener<BluetoothDevice?> {
                override fun onItemClick(view: View, selectedItem: BluetoothDevice?) {
                    selectedItem?.let {
                        viewModel.getBluetoothAdapter()?.cancelDiscovery()
                        manageFoundDevicePicked(it)
                    }
                }
            })

        viewModel.getFoundDevice().observe(viewLifecycleOwner,
            { devices -> updateUiForDevice(devices.toList()) }
        )

        val recycler = view.scan_device_recycler

        recycler?.let {
            it.layoutManager = LinearLayoutManager(requireContext())
            it.setHasFixedSize(true)
            it.adapter = scanDevicesAdapter
        }
    }

    private fun manageFoundDevicePicked(it: BluetoothDevice): Any {
        return when (it.bondState) {
            BOND_BONDED -> {
                viewModel.setParedBluetoothDevice(it)
                viewModel.setShouldScrollToPage(PAGE_PARED_DEVICES)
            }

            else -> it.createBond()
        }
    }

    private fun updateUiForDevice(devices: List<BluetoothDevice>) {
        scanDevicesAdapter.setData(devices)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_scan_devices_page, container, false)
        initRecycler(view)
        initClickListeners(view)
        return view
    }

    private fun initClickListeners(view: View) {
        view.scan_devices_fab.setOnClickListener {
            if (viewModel.getBluetoothAdapter()?.isEnabled != true) {
                viewModel.enableBluetoothIfNot(requireActivity())
                return@setOnClickListener
            }

            launchScanning()
        }
    }

    private fun launchScanning() {
        viewModel.getBluetoothAdapter()?.let { adapter ->
            viewModel.checkBluetoothPermissionGranted(requireActivity())
            viewModel.getBluetoothPermissionGranted().observe(viewLifecycleOwner, { hasPermission ->
                if (hasPermission) {
                    viewModel.startDiscoveryMode(adapter, requireActivity())
                } else {
                    viewModel.requestPermissions(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ),
                        requireActivity()
                    )
                }
            })
        }
    }
}