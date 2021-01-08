package com.milen.bluetoothapp.ui.pager.pages

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.BOND_BONDED
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.LOLLIPOP
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.milen.bluetoothapp.BuildConfig
import com.milen.bluetoothapp.R
import com.milen.bluetoothapp.base.interfaces.OnItemClickListener
import com.milen.bluetoothapp.base.ui.pager.pages.BasePageFragment
import com.milen.bluetoothapp.ui.MainActivity
import com.milen.bluetoothapp.ui.PERMISSION_REQUEST_CODE
import com.milen.bluetoothapp.ui.adapters.BluetoothDevicesAdapter
import com.milen.bluetoothapp.ui.pager.MainFragmentStateAdapter.Page.PAGE_PARED_DEVICES
import kotlinx.android.synthetic.main.fragment_scan_devices_page.view.*

const val ACTION_DISCOVERY_FAILED = "${BuildConfig.APPLICATION_ID}.ACTION_DISCOVERY_FAILED"

class ScanDevicesPageFragment : BasePageFragment() {
    private lateinit var scanDevicesAdapter: BluetoothDevicesAdapter

    private fun initRecycler(view: View) {
        scanDevicesAdapter =
            BluetoothDevicesAdapter(object : OnItemClickListener<BluetoothDevice?> {
                override fun onItemClick(view: View, selectedItem: BluetoothDevice?) {
                    selectedItem?.let {
                        viewModel.bluetoothAdapter?.cancelDiscovery()
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
            if (viewModel.bluetoothAdapter?.isEnabled != true) {
                when (requireActivity()) {
                    is MainActivity -> (requireActivity() as MainActivity).startBluetoothOnIntent()
                }
                return@setOnClickListener
            }

            launchScanning()
        }
    }

    private fun launchScanning() {
        viewModel.bluetoothAdapter?.let { adapter ->
            viewModel.setBluetoothPermissionGranted(isLocationPermissionGranted())
            viewModel.getBluetoothPermissionGranted().observe(viewLifecycleOwner, { hasPermission ->
                if (hasPermission) {
                    startDiscoveryMode(adapter)
                } else {
                    requestPermissions()
                }
            })
        }
    }

    private fun startDiscoveryMode(it: BluetoothAdapter) {
        it.cancelDiscovery()
        if (!it.startDiscovery()) {
            requireActivity().sendBroadcast(Intent(ACTION_DISCOVERY_FAILED))
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return when (SDK_INT >= LOLLIPOP) {
            true -> checkLollipopPermissions()
            else -> true
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    private fun checkLollipopPermissions(): Boolean {

        val permissionCoarseLocation = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val permissionFineLocation = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        return permissionCoarseLocation == PERMISSION_GRANTED &&
                permissionFineLocation == PERMISSION_GRANTED
    }
}