package com.milen.bluetoothapp.extensions

import android.app.Activity
import android.widget.Toast


fun Activity.showToastMessage(msg: String) {
    Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
}
