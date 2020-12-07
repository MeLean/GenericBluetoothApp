package com.milen.bluetoothapp.view_models

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.milen.bluetoothapp.Constants
import com.milen.bluetoothapp.R
import com.milen.bluetoothapp.base.sharedPreferences.AndroidSharedPreferences
import com.milen.bluetoothapp.base.sharedPreferences.DefaultAndroidSharedPreferences
import com.milen.bluetoothapp.services.MyBluetoothService
import com.milen.bluetoothapp.ui.pager.MainFragmentStateAdapter
import com.milen.bluetoothapp.utils.EMPTY_STRING

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val mBluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val mSharedPreferences: AndroidSharedPreferences =
        initAndroidSharedPreferences(application)
    private val mBluetoothAvailability = MutableLiveData<Boolean>()
    private val mCustomCommandsAutoCompleteSet = MutableLiveData<Set<String>>()
    private val mUpValue = MutableLiveData<String>()
    private val mDownValue = MutableLiveData<String>()
    private val mLeftValue = MutableLiveData<String>()
    private val mRightValue = MutableLiveData<String>()

    private val mIncomingMsgHandler: Handler = Handler { msg ->
        msg.obj?.let {
            if (it is ByteArray) {
                val msgStr = String(it)
                setIncomingMessage(msgStr)
            }
        }
        true
    }

    private val mBluetoothService = MyBluetoothService(mBluetoothAdapter, mIncomingMsgHandler)
    private val mIncomingMessage = MutableLiveData<String>()

    init {
        mBluetoothService.startService()
        mBluetoothAvailability.value = mBluetoothAdapter?.isEnabled == true
        mCustomCommandsAutoCompleteSet.value =
            mSharedPreferences.readStringSetOrDefault(Constants.AUTO_COMPLETE_SET)
        mUpValue.value =
            mSharedPreferences.readStringOrDefault(Constants.UP_VALUE_KEY, EMPTY_STRING)
        mDownValue.value =
            mSharedPreferences.readStringOrDefault(Constants.DOWN_VALUE_KEY, EMPTY_STRING)
        mLeftValue.value =
            mSharedPreferences.readStringOrDefault(Constants.LEFT_VALUE_KEY, EMPTY_STRING)
        mRightValue.value =
            mSharedPreferences.readStringOrDefault(Constants.RIGHT_VALUE_KEY, EMPTY_STRING)
    }

    fun getBluetoothAvailability() : LiveData<Boolean> = mBluetoothAvailability
    fun setBluetoothAvailability(isAvailable: Boolean) {
        mBluetoothAvailability.value = isAvailable
    }

    fun getCustomCommandsAutoCompleteSet(): LiveData<Set<String>> = mCustomCommandsAutoCompleteSet
    fun addCustomCommand(command: String) {
        mCustomCommandsAutoCompleteSet.value?.let {
            if (!it.contains(command)) {
                val newSet = it.toMutableSet()
                newSet.add(command)
                mSharedPreferences.storeStringSet(Constants.AUTO_COMPLETE_SET, newSet)
            }
        }
    }


    fun getIncomingMessage(): LiveData<String> = mIncomingMessage
    fun setIncomingMessage(msg: String) {
        mIncomingMessage.value = msg
    }

    fun getUpValue(): LiveData<String> = mUpValue
    fun getDownValue(): LiveData<String> = mDownValue
    fun getLeftValue(): LiveData<String> = mLeftValue
    fun getRightValue(): LiveData<String> = mRightValue

    fun setUpValue(value: String) = mSharedPreferences.storeString(Constants.UP_VALUE_KEY, value)
    fun setDownValue(value: String) = mSharedPreferences.storeString(Constants.DOWN_VALUE_KEY, value)
    fun setLeftValue(value: String) = mSharedPreferences.storeString(Constants.LEFT_VALUE_KEY, value)
    fun setRightValue(value: String) = mSharedPreferences.storeString(
        Constants.RIGHT_VALUE_KEY,
        value
    )



    private var selectedDevice = MutableLiveData<BluetoothDevice?>()
    fun getBluetoothDevice(): LiveData<BluetoothDevice?> = selectedDevice
    fun setBluetoothDevice(device: BluetoothDevice?) {
        selectedDevice.value = device
    }

    private var lastCommand = MutableLiveData<String>()
    fun getLastCommand(): LiveData<String> = lastCommand

    fun sentCommand(command: String) {
        if (command.isNotBlank()) {
            //TODO SEND COMMAND
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