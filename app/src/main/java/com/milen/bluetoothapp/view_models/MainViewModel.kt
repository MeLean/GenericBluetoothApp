package com.milen.bluetoothapp.view_models

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.milen.bluetoothapp.Constants
import com.milen.bluetoothapp.R
import com.milen.bluetoothapp.base.sharedPreferences.AndroidSharedPreferences
import com.milen.bluetoothapp.base.sharedPreferences.DefaultAndroidSharedPreferences
import com.milen.bluetoothapp.ui.pager.MainFragmentStateAdapter
import com.milen.bluetoothapp.utils.EMPTY_STRING

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val sharedPreferences: AndroidSharedPreferences =
        initAndroidSharedPreferences(application)
    private val bluetoothAvailability = MutableLiveData<Boolean>()
    private val customCommandsAutoCompleteSet = MutableLiveData<Set<String>>()
    private val upValue = MutableLiveData<String>()
    private val downValue = MutableLiveData<String>()
    private val leftValue = MutableLiveData<String>()
    private val rightValue = MutableLiveData<String>()


    init {
        bluetoothAvailability.value = bluetoothAdapter?.isEnabled == true
        customCommandsAutoCompleteSet.value =
            sharedPreferences.readStringSetOrDefault(Constants.AUTO_COMPLETE_SET)
        upValue.value =
            sharedPreferences.readStringOrDefault(Constants.UP_VALUE_KEY, EMPTY_STRING)
        downValue.value =
            sharedPreferences.readStringOrDefault(Constants.DOWN_VALUE_KEY, EMPTY_STRING)
        leftValue.value =
            sharedPreferences.readStringOrDefault(Constants.LEFT_VALUE_KEY, EMPTY_STRING)
        rightValue.value =
            sharedPreferences.readStringOrDefault(Constants.RIGHT_VALUE_KEY, EMPTY_STRING)
    }

    fun getBluetoothAvailability() : LiveData<Boolean> = bluetoothAvailability
    fun setBluetoothAvailability(isAvailable: Boolean) {
        bluetoothAvailability.value = isAvailable
    }

    fun getCustomCommandsAutoCompleteSet(): LiveData<Set<String>> = customCommandsAutoCompleteSet
    fun addCustomCommand(command: String) {
        customCommandsAutoCompleteSet.value?.let {
            if (!it.contains(command)) {
                val newSet = it.toMutableSet()
                newSet.add(command)
                sharedPreferences.storeStringSet(Constants.AUTO_COMPLETE_SET, newSet)
            }
        }
    }

    fun getUpValue(): LiveData<String> = upValue
    fun getDownValue(): LiveData<String> = downValue
    fun getLeftValue(): LiveData<String> = leftValue
    fun getRightValue(): LiveData<String> = rightValue

    fun setUpValue(value: String) = sharedPreferences.storeString(Constants.UP_VALUE_KEY, value)
    fun setDownValue(value: String) = sharedPreferences.storeString(Constants.DOWN_VALUE_KEY, value)
    fun setLeftValue(value: String) = sharedPreferences.storeString(Constants.LEFT_VALUE_KEY, value)
    fun setRightValue(value: String) = sharedPreferences.storeString(Constants.RIGHT_VALUE_KEY, value)



    private var selectedDevice = MutableLiveData<BluetoothDevice?>()
    fun getBluetoothDevice(): LiveData<BluetoothDevice?> = selectedDevice
    fun setBluetoothDevice(device: BluetoothDevice?) {
        selectedDevice.value = device
    }

    private var lastCommand = MutableLiveData<String>()
    fun getLastCommand(): LiveData<String> = lastCommand

    fun sentCommand(command: String) {
        //TODO IMPLEMENT SENDING COMMAND
        if (command.isNotBlank()) {
            lastCommand.value = command
        }
    }

    @StringRes
    fun getStringResIdByPage(page: MainFragmentStateAdapter.Pages): Int {
        return when (page) {
            MainFragmentStateAdapter.Pages.PAGE_SETTINGS -> R.string.page_settings
            MainFragmentStateAdapter.Pages.PAGE_PARED_DEVICES -> R.string.page_pared_devices
            MainFragmentStateAdapter.Pages.PAGE_REMOTE_CONTROL -> R.string.page_remote_control
        }
    }

    private fun initAndroidSharedPreferences(application: Application): AndroidSharedPreferences {
        return DefaultAndroidSharedPreferences(
            application.getSharedPreferences(
                "GenericBluetoothAppSharedPref",
                Context.MODE_PRIVATE
            )
        )
    }


}