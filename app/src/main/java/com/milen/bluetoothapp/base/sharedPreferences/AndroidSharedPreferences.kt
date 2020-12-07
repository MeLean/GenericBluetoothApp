package com.milen.bluetoothapp.base.sharedPreferences

import android.bluetooth.BluetoothDevice
import android.content.SharedPreferences

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.milen.bluetoothapp.utils.EMPTY_STRING
import java.lang.reflect.Type

interface AndroidSharedPreferences {

    fun hasCachedKey(key: String): Boolean

    fun readStringOrDefault(key: String, default: String? = EMPTY_STRING): String?

    fun readBooleanOrDefault(key: String, default: Boolean = true): Boolean

    fun readIntOrDefault(key: String, default: Int = 0): Int

    fun storeString(key: String, value: String?)

    fun storeBoolean(key: String, value: Boolean)

    fun storeInt(key: String, value: Int)

    fun storeStringSet(key: String, value: Set<String>)

    fun<T> storeObject(key: String, objectToStore: T)

    fun readStoredDevice(key: String): BluetoothDevice?

    fun readStringSetOrDefault(key: String, default: Set<String> = setOf()): Set<String>

//    fun readStoredRemoteControlsNavNamesOfDefault(key: String): RemoteControlNavValues?
}

class DefaultAndroidSharedPreferences(private val sharedPreferences: SharedPreferences): AndroidSharedPreferences {

    override fun hasCachedKey(key: String): Boolean = sharedPreferences.contains(key)

    override fun readStringOrDefault(key: String, default: String?): String? = sharedPreferences.getString(key, default)

    override fun readBooleanOrDefault(key: String, default: Boolean): Boolean = sharedPreferences.getBoolean(key, default)

    override fun readIntOrDefault(key: String, default: Int): Int = sharedPreferences.getInt(key, default)

    override fun readStringSetOrDefault(key: String, default: Set<String>): Set<String> = sharedPreferences.getStringSet(key, default) ?: default

    override fun storeString(key: String, value: String?) = sharedPreferences.edit().putString(key, value).apply()

    override fun storeBoolean(key: String, value: Boolean) = sharedPreferences.edit().putBoolean(key, value).apply()

    override fun storeInt(key: String, value: Int) = sharedPreferences.edit().putInt(key, value).apply()

    override fun storeStringSet(key: String, value: Set<String>) = sharedPreferences.edit().putStringSet(key, value).apply()

    override fun <T> storeObject(key: String, objectToStore: T) {
        val gson = Gson()
        val stringValue = gson.toJson(objectToStore)
        storeString(key, stringValue)
    }

    override fun readStoredDevice(key: String): BluetoothDevice? {
        val gson = Gson()
        val cachedString = readStringOrDefault(key, null)
        val type: Type = object : TypeToken<BluetoothDevice>() {}.type
        return if (cachedString == null) null else gson.fromJson<BluetoothDevice>(cachedString, type)
    }
//
//    override fun readStoredRemoteControlsNavNamesOfDefault(key: String): RemoteControlNavValues {
//        val gson = Gson()
//        val cachedString = readStringOrDefault(key, null)
//        val type: Type = object : TypeToken<RemoteControlNavValues>() {}.type
//        return if (cachedString == null) RemoteControlNavValues() else gson.fromJson(cachedString, type)
//    }
}