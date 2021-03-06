package com.milen.bluetoothapp.ui.adapters

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.milen.bluetoothapp.R
import com.milen.bluetoothapp.base.adapters.BaseAdapterDelegate
import com.milen.bluetoothapp.base.adapters.BaseViewHolder
import com.milen.bluetoothapp.base.interfaces.OnItemClickListener
import com.milen.bluetoothapp.utils.EMPTY_STRING
import com.milen.bluetoothapp.utils.inflateViewWithoutAttaching
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
        return ViewModel(parent.inflateViewWithoutAttaching(R.layout.item_bluetooth_device))
    }

    @SuppressLint("SetTextI18n")
    override fun bindViewHolder(
        holder: ViewModel,
        model: BluetoothDevice,
        position: Int,
        listener: OnItemClickListener<BluetoothDevice?>,
        isChecked : Boolean
    ) {

        holder.deviceName.text = model.name ?: EMPTY_STRING
        holder.deviceMac.text = model.address
        holder.deviceInfo.text = "${model.javaClass.simpleName} : ${model.type}"

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