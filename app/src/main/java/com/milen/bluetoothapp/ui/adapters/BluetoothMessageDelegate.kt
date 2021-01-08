package com.milen.bluetoothapp.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.milen.bluetoothapp.R
import com.milen.bluetoothapp.base.adapters.BaseAdapterDelegate
import com.milen.bluetoothapp.base.adapters.BaseViewHolder
import com.milen.bluetoothapp.base.interfaces.OnItemClickListener
import com.milen.bluetoothapp.data.entities.BluetoothMessageEntity
import com.milen.bluetoothapp.services.MESSAGE_ERROR
import com.milen.bluetoothapp.services.MESSAGE_READ
import com.milen.bluetoothapp.services.MESSAGE_WRITE
import com.milen.bluetoothapp.utils.inflateViewWithoutAttaching
import kotlinx.android.synthetic.main.item_bluetooth_message.view.*
import java.util.*


class BluetoothMessageDelegate :
    BaseAdapterDelegate<BluetoothMessageDelegate.ViewModel, BluetoothMessageDelegate.ViewModel, BluetoothMessageEntity>() {

    class ViewModel(parent: View) : BaseViewHolder(parent) {
        val messageTime: TextView = parent.message_time_stamp_tv
        val messageText: TextView = parent.message_text_tv

    }

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return ViewModel(parent.inflateViewWithoutAttaching(R.layout.item_bluetooth_message))
    }

    @SuppressLint("SetTextI18n")
    override fun bindViewHolder(
        holder: ViewModel,
        model: BluetoothMessageEntity,
        position: Int,
        listener: OnItemClickListener<BluetoothMessageEntity?>,
        isChecked : Boolean
    ) {

        holder.messageText.text = createMessage(holder.itemView.context, model)
        holder.messageTime.text = Date(model.messageTime).toString()

        super.bindViewHolder(holder, model, position, listener, isChecked)
    }

    private fun createMessage(ctx: Context, model: BluetoothMessageEntity): String {
        return  when(model.what){
            MESSAGE_ERROR-> createText(ctx, R.string.error_msg, model.messageText)
            MESSAGE_WRITE -> createText(ctx, R.string.send_meg, model.messageText)
            MESSAGE_READ -> createText(ctx, R.string.received_msg, model.messageText)
            else  -> model.messageText
        }
    }

    private fun createText (
        ctx: Context,
        resId: Int,
        text: String
    ) : String {
        return "${ctx.getString(resId)} $text"
    }
}