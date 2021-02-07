package com.milen

import android.app.Application
import android.content.Context
import com.milen.bluetoothapp.data.sharedPreferences.ApplicationSharedPreferences
import com.milen.bluetoothapp.data.sharedPreferences.DefaultApplicationSharedPreferences
import com.milen.bluetoothapp.view_models.SHARED_PREF_NAME

class GenericBluetoothApp : Application() {

    companion object {
        lateinit var defaultSharedPreferences: ApplicationSharedPreferences
    }

    override fun onCreate() {
        super.onCreate()
        initAndroidSharedPreferences()
    }

    private fun initAndroidSharedPreferences() {
        defaultSharedPreferences = DefaultApplicationSharedPreferences(
            getSharedPreferences(
                SHARED_PREF_NAME,
                Context.MODE_PRIVATE
            )
        )
    }
}
class GenericBluetoothApp : Application()