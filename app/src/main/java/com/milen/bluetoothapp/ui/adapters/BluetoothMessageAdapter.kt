package com.milen.bluetoothapp.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.milen.bluetoothapp.base.adapters.BaseViewHolder
import com.milen.bluetoothapp.base.interfaces.OnItemClickListener
import com.milen.bluetoothapp.data.entities.BluetoothMessageEntity


class BluetoothMessageAdapter(
    private val listener: OnItemClickListener<BluetoothMessageEntity?>
) : RecyclerView.Adapter<BaseViewHolder>() {
    private val itemDelegate = BluetoothMessageDelegate()
    private val differ: AsyncListDiffer<BluetoothMessageEntity> = createAdapterDiffer()

    override fun getItemCount() = differ.currentList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val holder: RecyclerView.ViewHolder = itemDelegate.createViewHolder(parent)
        return holder as BaseViewHolder
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val viewModel = differ.currentList[position]
        itemDelegate.bindViewHolder(holder as BluetoothMessageDelegate.ViewModel, viewModel, position, listener)
    }

    fun setData(data: List<BluetoothMessageEntity>){
        differ.submitList(data)
    }

    private fun createAdapterDiffer(): AsyncListDiffer<BluetoothMessageEntity> {
        return AsyncListDiffer(this, object : DiffUtil.ItemCallback<BluetoothMessageEntity>() {

            override fun areItemsTheSame(old: BluetoothMessageEntity, aNew: BluetoothMessageEntity): Boolean =
                old.messageTime == aNew.messageTime

            override fun areContentsTheSame(old: BluetoothMessageEntity, aNew: BluetoothMessageEntity): Boolean {
                return old.messageText == aNew.messageText
            }
        })
    }
}
