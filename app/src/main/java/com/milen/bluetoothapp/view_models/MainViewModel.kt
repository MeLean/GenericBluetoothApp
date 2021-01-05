package com.milen.bluetoothapp.view_models

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.milen.GenericBluetoothApp
import com.milen.bluetoothapp.Constants
import com.milen.bluetoothapp.data.entities.BluetoothMessageEntity
import com.milen.bluetoothapp.data.sharedPreferences.ApplicationSharedPrefInterface
import com.milen.bluetoothapp.data.sharedPreferences.DefaultApplicationSharedPreferences
import com.milen.bluetoothapp.services.DeepLinkItemExtractorService
import com.milen.bluetoothapp.services.MESSAGE_CONNECT_SUCCESS
import com.milen.bluetoothapp.services.MESSAGE_FAIL_CONNECT
import com.milen.bluetoothapp.services.MyBluetoothService
import com.milen.bluetoothapp.ui.pager.MainFragmentStateAdapter.Page
import com.milen.bluetoothapp.utils.EMPTY_STRING

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val mSharedPrefInterface: ApplicationSharedPrefInterface =
        initAndroidSharedPreferences(application)
    private val mDeepLinkService = DeepLinkItemExtractorService()

    private val mBluetoothAvailability = MutableLiveData<Boolean?>()
    private val mCustomCommandsAutoCompleteSet = MutableLiveData<Set<String>>()
    private val mUpValue = MutableLiveData<String>()
    private val mDownValue = MutableLiveData<String>()
    private val mLeftValue = MutableLiveData<String>()
    private val mRightValue = MutableLiveData<String>()
    private val mSelectedParedDevice = MutableLiveData<BluetoothDevice?>()
    private val mFoundDevices = MutableLiveData<MutableSet<BluetoothDevice>>()
    private val mLastCommand = MutableLiveData<String>()
    private val mIncomingMessages = MutableLiveData<MutableList<BluetoothMessageEntity>>()
    private val mShouldScroll = MutableLiveData<Page>()
    private val mBluetoothPermissionGranted = MutableLiveData<Boolean>()
    private val mDeepLinkItems = MutableLiveData<Map<String, String>?>()

    private val mIncomingMsgHandler: Handler = Handler { msg ->
        if(msg.what == MESSAGE_FAIL_CONNECT){
            mSelectedParedDevice.postValue(null)
        }

        if(msg.what == MESSAGE_CONNECT_SUCCESS){
            mShouldScroll.value = Page.PAGE_REMOTE_CONTROL
        }

        msg.obj?.let {
            if (it is ByteArray) {
                val msgStr = it.decodeToString()
                mIncomingMessages.postValue(mIncomingMessages.value?.also{ messages ->
                    messages.add(BluetoothMessageEntity(msg.what, msgStr, System.currentTimeMillis()))
                })

                showIncomingMessage(msgStr)
            }
        }
        true
    }

    private val mBluetoothService
            = MyBluetoothService.getInstance(bluetoothAdapter, mIncomingMsgHandler)

    init {
        mBluetoothAvailability.value = bluetoothAdapter?.isEnabled
        setBluetoothAvailability(bluetoothAdapter?.isEnabled == true)
        mFoundDevices.value = mutableSetOf()
        mIncomingMessages.value = mutableListOf()
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

    fun getBluetoothAvailability() : LiveData<Boolean?> = mBluetoothAvailability
    fun setBluetoothAvailability(isAvailable: Boolean) {
        mBluetoothAvailability.value = isAvailable

        when(isAvailable){
            true -> mBluetoothService.startService()
            else -> mBluetoothService.stopService()
        }
    }

    fun addFoundDevice(foundDevice : BluetoothDevice){
       mFoundDevices.value = mFoundDevices.value?.also {
           it.add(foundDevice)
       }
    }

    fun getFoundDevice() : LiveData<MutableSet<BluetoothDevice>> =
        mFoundDevices

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

    fun getIncomingMessages(): LiveData<MutableList<BluetoothMessageEntity>> = mIncomingMessages

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


    fun getParedBluetoothDevice(): LiveData<BluetoothDevice?> = mSelectedParedDevice
    fun setParedBluetoothDevice(device: BluetoothDevice?) {
        if(device != null) {
            mBluetoothService.connectToDevice(device)
        }else{
            mBluetoothService.disconnectAllDevices()
        }

        mSelectedParedDevice.value = device
    }


    fun getLastCommand(): LiveData<String> = mLastCommand

    fun sentCommand(command: String) {
        if (command.isNotBlank()) {
            mBluetoothService.write(command.toByteArray())
            mLastCommand.value = command
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

    fun getBluetoothPermissionGranted() : LiveData<Boolean> = mBluetoothPermissionGranted
    fun setBluetoothPermissionGranted(permissionState: Boolean) {
        mBluetoothPermissionGranted.value = permissionState
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
        setParedBluetoothDevice(null)
        mBluetoothService.stopService()
    }

    fun checkIfDeepLinkItemsInIntent(intent: Intent?) {
        if (intent != null && Intent.ACTION_VIEW == intent.action) {
            mDeepLinkService.extractQueryParams(
                intent.data,
                object : DeepLinkItemExtractorService.OnItemsExtracted {
                    override fun onItemsExtracted(items: Map<String, String>?) {
                        mDeepLinkItems.value = items
                    }
                }
            )
        } else {
            mDeepLinkItems.value = null
        }

    }

    fun getDeepLinkItems(): LiveData<Map<String, String>?> = mDeepLinkItems


}
