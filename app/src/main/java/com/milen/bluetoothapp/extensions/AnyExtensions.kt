package com.milen.bluetoothapp.extensions


fun Any.toDecodedString(): String {
    return when(this) {
        is ByteArray -> decodeToString()
        else -> ""
    }
}
