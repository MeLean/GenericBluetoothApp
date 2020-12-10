package com.milen.bluetoothapp.view_models

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.milen.GenericBluetoothApp
import com.milen.bluetoothapp.Constants
import com.milen.bluetoothapp.data.sharedPreferences.ApplicationSharedPrefInterface
import com.milen.bluetoothapp.data.sharedPreferences.DefaultApplicationSharedPreferences
import com.milen.bluetoothapp.services.*
import com.milen.bluetoothapp.ui.pager.MainFragmentStateAdapter.*
import com.milen.bluetoothapp.utils.EMPTY_STRING

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val mSharedPrefInterface: ApplicationSharedPrefInterface =
        initAndroidSharedPreferences(application)
    private val mBluetoothAvailability = MutableLiveData<Boolean>()
    private val mCustomCommandsAutoCompleteSet = MutableLiveData<Set<String>>()
    private val mUpValue = MutableLiveData<String>()
    private val mDownValue = MutableLiveData<String>()
    private val mLeftValue = MutableLiveData<String>()
    private val mRightValue = MutableLiveData<String>()
    private val selectedDevice = MutableLiveData<BluetoothDevice?>()
    private val lastCommand = MutableLiveData<String>()
    private val mIncomingMessage = MutableLiveData<String>()
    private val mShouldScroll = MutableLiveData<Page>()

    private val mIncomingMsgHandler: Handler = Handler { msg ->
        if(msg.what == MESSAGE_FAIL_CONNECT){
            //Log.d("TEST_IT", "mIncomingMsgHandler failed to connect!")
            selectedDevice.postValue(null)
        }

        if(msg.what == MESSAGE_CONNECT_SUCCESS){
            //Log.d("TEST_IT", "mIncomingMsgHandler connected!")
            mShouldScroll.value = Page.PAGE_REMOTE_CONTROL
        }


        msg.obj?.let {
            if (it is ByteArray) {
                val msgStr = createStringMsg(it, msg)
                Log.d("TEST_IT", "mIncomingMsgHandler $msgStr what: ${msg.what}")
                mIncomingMessage.postValue(msgStr)
                showIncomingMessage(msgStr)
            }
        }
        true
    }

    private fun createStringMsg(arr: ByteArray, msg: Message): String {
        val msgStr = arr.decodeToString()
        return when(msg.what){
            MESSAGE_READ -> "received: $msgStr"
            MESSAGE_WRITE -> "sent: $msgStr"
            else -> msgStr
        }
    }

    private val mBluetoothService
            = MyBluetoothService.getInstance(bluetoothAdapter, mIncomingMsgHandler)

    init {
        mBluetoothAvailability.value = bluetoothAdapter?.isEnabled == false
        mCustomCommandsAutoCompleteSet.value =
            mSharedPrefInterface.readStringSetOrDefault(Constants.AUTO_COMPLETE_SET)
        mUpValue.value =
            mSharedPrefInterface.readStringOrDefault(Constants.UP_VALUE_KEY, EMPTY_STRING)
        mDownValue.value =
            mSharedPrefInterface.readStringOrDefault(Constants.DOWN_VALUE_KEY, EMPTY_STRING)
        mLeftValue.value =
            mSharedPrefInterface.readStringOrDefault(Constants.LEFT_VALUE_KEY, EMPTY_STRING)
        mRightValue.value =
            mSharedPrefInterface.readStringOrDefault(Constants.RIGHT_VALUE_KEY, EMPTY_STRING)
    }

    fun getShouldScrollToPage() : LiveData<Page> = mShouldScroll
    fun setShouldScrollToPage(page:Page){
        mShouldScroll.value = page
    }

    fun getBluetoothAvailability() : LiveData<Boolean> = mBluetoothAvailability
    fun setBluetoothAvailability(isAvailable: Boolean) {
        mBluetoothAvailability.value = isAvailable

        when(isAvailable){
            true -> mBluetoothService.startService()
            else -> mBluetoothService.stopService()
        }
    }

    fun getCustomCommandsAutoCompleteSet(): LiveData<Set<String>> = mCustomCommandsAutoCompleteSet
    fun addCustomCommand(command: String) {
        mCustomCommandsAutoCompleteSet.value?.let {
            if (!it.contains(command)) {
                val newSet = it.toMutableSet()
                newSet.add(command)
                mSharedPrefInterface.storeStringSet(Constants.AUTO_COMPLETE_SET, newSet)
            }
        }
    }

    fun getIncomingMessage(): LiveData<String> = mIncomingMessage

    fun getUpValue(): LiveData<String> = mUpValue
    fun getDownValue(): LiveData<String> = mDownValue
    fun getLeftValue(): LiveData<String> = mLeftValue
    fun getRightValue(): LiveData<String> = mRightValue

    fun setUpValue(value: String) = mSharedPrefInterface.storeString(Constants.UP_VALUE_KEY, value)
    fun setDownValue(value: String) = mSharedPrefInterface.storeString(Constants.DOWN_VALUE_KEY, value)
    fun setLeftValue(value: String) = mSharedPrefInterface.storeString(Constants.LEFT_VALUE_KEY, value)
    fun setRightValue(value: String) = mSharedPrefInterface.storeString(
        Constants.RIGHT_VALUE_KEY,
        value
    )


    fun getBluetoothDevice(): LiveData<BluetoothDevice?> = selectedDevice
    fun setBluetoothDevice(device: BluetoothDevice?) {
        if(device != null) {
            mBluetoothService.connectToDevice(device)
        }else{
            mBluetoothService.disconnectAllDevices()
        }

        selectedDevice.value = device
    }


    fun getLastCommand(): LiveData<String> = lastCommand

    fun sentCommand(command: String) {
        if (command.isNotBlank()) {
            mBluetoothService.write(command.toByteArray())
            lastCommand.value = command
        }
    }

    private fun initAndroidSharedPreferences(application: Application): ApplicationSharedPrefInterface {
        return DefaultApplicationSharedPreferences(
            application.getSharedPreferences(
                Constants.SHARED_PREF_NAME,
                Context.MODE_PRIVATE
            )
        )
    }

    private fun showIncomingMessage(msg: String) {
        Toast.makeText(
            getApplication<GenericBluetoothApp>().applicationContext,
            msg,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onCleared() {
        super.onCleared()
        //Log.d("TEST_IT", "MainViewModel onCleared")
        setBluetoothDevice(null)
        mBluetoothService.stopService()
    }
}