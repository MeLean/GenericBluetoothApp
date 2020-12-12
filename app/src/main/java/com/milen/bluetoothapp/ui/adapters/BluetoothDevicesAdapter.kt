package com.milen.bluetoothapp.ui.adapters

import android.bluetooth.BluetoothDevice
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.milen.bluetoothapp.base.adapters.BaseViewHolder
import com.milen.bluetoothapp.base.OnItemClickListener


class BluetoothDevicesAdapter(private val listener: OnItemClickListener<BluetoothDevice?>) : RecyclerView.Adapter<BaseViewHolder>() {
    private var chosenPosition = NO_POSITION
    private val itemDelegate = BluetoothDeviceDelegate()
    private val differ: AsyncListDiffer<BluetoothDevice> = createAdapterDiffer()

    override fun getItemCount() = differ.currentList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val holder: RecyclerView.ViewHolder = itemDelegate.createViewHolder(parent)
        return holder as BaseViewHolder
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val viewModel = differ.currentList[position]
        val isChecked = position==chosenPosition
        itemDelegate.bindViewHolder(holder as BluetoothDeviceDelegate.ViewModel, viewModel, position, listener, isChecked)
    }

    fun setData(data: List<BluetoothDevice>){
        differ.submitList(data)
    }

    private fun createAdapterDiffer(): AsyncListDiffer<BluetoothDevice> {
        return AsyncListDiffer(this, object : DiffUtil.ItemCallback<BluetoothDevice>() {

            override fun areItemsTheSame(old: BluetoothDevice, aNew: BluetoothDevice): Boolean = old.address == aNew.address

            override fun areContentsTheSame(old: BluetoothDevice, aNew: BluetoothDevice): Boolean {
                return old.name == aNew.name && old.type == old.type
            }
        })
    }

    fun setChosenDevice(device : BluetoothDevice?){
       if(device != null){
           val lastPosition = chosenPosition
           for(i in 0 until differ.currentList.size){
               if(differ.currentList[i].address == device.address){
                   chosenPosition=i
                   notifyItemChanged(lastPosition)
                   notifyItemChanged(i)
               }
           }
       } else {
           val lastPosition = chosenPosition
           chosenPosition = NO_POSITION
           notifyItemChanged(lastPosition)
       }
    }

    fun addDevice(device: BluetoothDevice) {
        val newList = differ.currentList
        newList.add(device)
        setData(newList)
    }

    companion object{
        const val NO_POSITION = -1
    }
}
