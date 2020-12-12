package com.milen.bluetoothapp.ui.adapters

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.milen.bluetoothapp.R
import com.milen.bluetoothapp.base.adapters.BaseAdapterDelegate
import com.milen.bluetoothapp.base.adapters.BaseViewHolder
import com.milen.bluetoothapp.base.OnItemClickListener
import kotlinx.android.synthetic.main.item_bluetooth_device.view.*


class BluetoothDeviceDelegate :
    BaseAdapterDelegate<BluetoothDeviceDelegate.ViewModel, BluetoothDeviceDelegate.ViewModel, BluetoothDevice>() {

    class ViewModel(parent: View) : BaseViewHolder(parent) {
        val deviceHolder: ViewGroup = parent.pared_device_holder
        val deviceName: TextView = parent.pared_device_name
        val deviceMac: TextView = parent.pared_device_mac
        val deviceInfo: TextView = parent.pared_device_info
        val switcher : SwitchMaterial = parent.pared_device_switch
    }

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewModel(inflater.inflate(R.layout.item_bluetooth_device, parent, false))
    }

    override fun bindViewHolder(
        holder: ViewModel,
        model: BluetoothDevice,
        position: Int,
        listener: OnItemClickListener<BluetoothDevice?>,
        isChecked : Boolean
    ) {
        holder.deviceName.text = model.name
        holder.deviceMac.text = model.address
        holder.deviceInfo.text = model.uuids[0].uuid.toString()
        if(isChecked){
            holder.deviceHolder.setBackgroundResource(R.color.colorPrimary)
        }else{
            holder.deviceHolder.setBackgroundResource(android.R.color.transparent)
        }

        holder.switcher.isChecked = isChecked
        holder.switcher.setOnClickListener {
            when((it as SwitchMaterial).isChecked){
                true -> listener.onItemClick(it, model)
                else -> listener.onItemClick(it, null)
            }
        }
        super.bindViewHolder(holder, model, position, listener, isChecked)
    }
}