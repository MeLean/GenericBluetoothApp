package com.milen.bluetoothapp.ui.pager.pages

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.LOLLIPOP
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.milen.bluetoothapp.Constants.PERMISSION_REQUEST_CODE
import com.milen.bluetoothapp.R
import com.milen.bluetoothapp.base.OnItemClickListener
import com.milen.bluetoothapp.base.ui.pager.pages.BasePageFragment
import com.milen.bluetoothapp.ui.MainActivity
import com.milen.bluetoothapp.ui.adapters.BluetoothDevicesAdapter
import kotlinx.android.synthetic.main.fragment_scan_devices_page.*
import kotlinx.android.synthetic.main.fragment_scan_devices_page.view.*

class ScanDevicesPageFragment : BasePageFragment() {
    private lateinit var scanDevicesAdapter: BluetoothDevicesAdapter
    private val deviceFoundReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(EXTRA_DEVICE)
                    device?.let {
                        scanDevicesAdapter.addDevice(it)
                    }
                }
            }
        }
    }

    private val deviceBoundStateChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_BOND_STATE_CHANGED -> {
                    Snackbar.make(scan_devices_fab, "device bound changed!", Snackbar.LENGTH_SHORT).show()
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(EXTRA_DEVICE)
                    device?.let {
                        when(it.bondState){
                            BOND_BONDED -> {
                                Snackbar.make(scan_devices_fab, "device bound bonded!", Snackbar.LENGTH_SHORT).show()
                            }
                            BOND_BONDING -> {
                                Snackbar.make(scan_devices_fab, "device bound bonding!", Snackbar.LENGTH_SHORT).show()
                            }
                            BOND_NONE -> {
                                Snackbar.make(scan_devices_fab, "device bound none!", Snackbar.LENGTH_SHORT).show()
                            }
                        }
                    }


                }
            }
        }
    }

    private fun initRecycler(view: View) {
        scanDevicesAdapter =
            BluetoothDevicesAdapter(object : OnItemClickListener<BluetoothDevice?> {
                override fun onItemClick(view: View, selectedItem: BluetoothDevice?) {
                    Snackbar.make(scan_devices_fab, "device name ${selectedItem?.name}", Snackbar.LENGTH_LONG).show()
                    selectedItem?.let {
                        viewModel.bluetoothAdapter?.cancelDiscovery()
                        it.createBond()
                    }
                }
            })

//        viewModel.getScanBluetoothDevice()
//            .observe(viewLifecycleOwner,
//                { device -> updateUiForDevice(device) }
//            )

        val recycler = view.scan_device_recycler

        recycler?.let {
            it.layoutManager = LinearLayoutManager(requireContext())
            it.setHasFixedSize(true)
            val devices = listOf<BluetoothDevice>()//null ?: listOf()
            scanDevicesAdapter.setData(devices)
            it.adapter = scanDevicesAdapter
        }
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

    override fun onResume() {
        super.onResume()
        requireActivity().registerReceiver(
            deviceFoundReceiver,
            IntentFilter(ACTION_FOUND)
        )
        requireActivity().registerReceiver(
            deviceBoundStateChangedReceiver,
            IntentFilter(ACTION_BOND_STATE_CHANGED)
        )
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(deviceFoundReceiver)
        requireActivity().unregisterReceiver(deviceBoundStateChangedReceiver)
    }


    private fun initClickListeners(view: View) {
        view.scan_devices_fab.setOnClickListener {
            if(viewModel.bluetoothAdapter?.isEnabled != true ) {
                when (requireActivity()) {
                    is MainActivity -> (requireActivity() as MainActivity).startBluetoothIntent()
                }
                return@setOnClickListener
            }

            launchScanningIntent()
        }
    }

    private fun launchScanningIntent() {
        viewModel.bluetoothAdapter?.let {
            if (!isLocationPermissionNotGranted()) {
                it.cancelDiscovery()
                it.startDiscovery()
                Snackbar.make(scan_devices_fab, getString(R.string.device_scanning_start), Snackbar.LENGTH_LONG).show()

            }else{
                viewModel.getBluetoothPermissionGranted().observe(viewLifecycleOwner, {
                    launchScanningIntent()
                })
            }
        }
    }

    private fun isLocationPermissionNotGranted(): Boolean {
        var permsNotGranted = false
        if (SDK_INT > LOLLIPOP) {
            val permissionCoarseLocation = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            val permissionFineLocation = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )

            permsNotGranted = permissionCoarseLocation != PackageManager.PERMISSION_GRANTED ||
                    permissionFineLocation != PackageManager.PERMISSION_GRANTED

            if (permsNotGranted) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    PERMISSION_REQUEST_CODE
                )
            }
        }

        return permsNotGranted
    }
}