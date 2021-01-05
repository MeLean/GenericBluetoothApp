package com.milen.bluetoothapp.data.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class BluetoothMessageEntity(
    val what: Int,
    val messageText: String,
    val messageTime: Long
) : Parcelable