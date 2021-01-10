package com.milen.bluetoothapp.view_models

import android.Manifest
import android.app.Activity
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.milen.GenericBluetoothApp
import com.milen.bluetoothapp.BuildConfig
import com.milen.bluetoothapp.data.entities.BluetoothMessageEntity
import com.milen.bluetoothapp.data.sharedPreferences.ApplicationSharedPreferences
import com.milen.bluetoothapp.data.sharedPreferences.DefaultApplicationSharedPreferences
import com.milen.bluetoothapp.services.DeepLinkItemExtractorService
import com.milen.bluetoothapp.services.MESSAGE_CONNECT_SUCCESS
import com.milen.bluetoothapp.services.MESSAGE_FAIL_CONNECT
import com.milen.bluetoothapp.services.MyBluetoothService
import com.milen.bluetoothapp.ui.BLUETOOTH_START_REQUEST_CODE
import com.milen.bluetoothapp.ui.PERMISSION_REQUEST_CODE
import com.milen.bluetoothapp.ui.pager.MainFragmentStateAdapter.Page
import com.milen.bluetoothapp.utils.EMPTY_STRING

const val ACTION_DISCOVERY_FAILED = "${BuildConfig.APPLICATION_ID}.ACTION_DISCOVERY_FAILED"
const val AUTO_COMPLETE_SET = "autocomplete_string_set"
const val UP_VALUE_KEY = "up_value_key"
const val DOWN_VALUE_KEY = "down_value_key"
const val LEFT_VALUE_KEY = "left_value_key"
const val RIGHT_VALUE_KEY = "right_value_key"
const val SHARED_PREF_NAME = "GenericBluetoothAppSharedPref"

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private var denyCount = 0
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val sharedPreferences: ApplicationSharedPreferences =
        initAndroidSharedPreferences(application)
    private val deepLinkService = DeepLinkItemExtractorService()

    private val bluetoothAvailability = MutableLiveData<Boolean?>()
    private val customCommandsAutoCompleteSet = MutableLiveData<Set<String>>()
    private val upValue = MutableLiveData<String>()
    private val downValue = MutableLiveData<String>()
    private val leftValue = MutableLiveData<String>()
    private val rightValue = MutableLiveData<String>()
    private val selectedParedDevice = MutableLiveData<BluetoothDevice?>()
    private val foundDevices = MutableLiveData<MutableSet<BluetoothDevice>>()
    private val lastCommand = MutableLiveData<String>()
    private val incomingMessages = MutableLiveData<MutableList<BluetoothMessageEntity>>()
    private val shouldScroll = MutableLiveData<Page>()
    private val bluetoothPermissionGranted = MutableLiveData<Boolean>()
    private val deepLinkItems = MutableLiveData<Map<String, String>?>()

    private val incomingMsgHandler: Handler = Handler { msg ->
        if(msg.what == MESSAGE_FAIL_CONNECT){
            selectedParedDevice.postValue(null)
        }

        if(msg.what == MESSAGE_CONNECT_SUCCESS){
            shouldScroll.value = Page.PAGE_REMOTE_CONTROL
        }

        msg.obj?.let {
            if (it is ByteArray) {
                val msgStr = it.decodeToString()
                incomingMessages.postValue(incomingMessages.value?.also{ messages ->
                    messages.add(BluetoothMessageEntity(msg.what, msgStr, System.currentTimeMillis()))
                })

                showIncomingMessage(msgStr)
            }
        }
        true
    }

    private val bluetoothService
            = MyBluetoothService.getInstance(bluetoothAdapter, incomingMsgHandler)

    init {
        bluetoothAvailability.value = bluetoothAdapter?.isEnabled
        setBluetoothAvailability(bluetoothAdapter?.isEnabled == true)
        foundDevices.value = mutableSetOf()
        incomingMessages.value = mutableListOf()
        customCommandsAutoCompleteSet.value =
            sharedPreferences.readStringSetOrDefault(AUTO_COMPLETE_SET)
        upValue.value =
            sharedPreferences.readStringOrDefault(UP_VALUE_KEY, EMPTY_STRING)
        downValue.value =
            sharedPreferences.readStringOrDefault(DOWN_VALUE_KEY, EMPTY_STRING)
        leftValue.value =
            sharedPreferences.readStringOrDefault(LEFT_VALUE_KEY, EMPTY_STRING)
        rightValue.value =
            sharedPreferences.readStringOrDefault(RIGHT_VALUE_KEY, EMPTY_STRING)
    }

    fun getShouldScrollToPage() : LiveData<Page> = shouldScroll
    fun setShouldScrollToPage(page:Page){
        shouldScroll.value = page
    }

    fun getBluetoothAvailability() : LiveData<Boolean?> = bluetoothAvailability
    fun setBluetoothAvailability(isAvailable: Boolean) {
        bluetoothAvailability.value = isAvailable

        when(isAvailable){
            true -> bluetoothService.startService()
            else -> bluetoothService.stopService()
        }
    }

    fun addFoundDevice(foundDevice : BluetoothDevice){
       foundDevices.value = foundDevices.value?.also {
           it.add(foundDevice)
       }
    }

    fun getFoundDevice() : LiveData<MutableSet<BluetoothDevice>> = foundDevices

    fun getCustomCommandsAutoCompleteSet(): LiveData<Set<String>> = customCommandsAutoCompleteSet
    fun addCustomCommand(command: String) {
        customCommandsAutoCompleteSet.value?.let {
            if (!it.contains(command)) {
                val newSet = it.toMutableSet()
                newSet.add(command)
                sharedPreferences.storeStringSet(AUTO_COMPLETE_SET, newSet)
            }
        }
    }

    fun getIncomingMessages(): LiveData<MutableList<BluetoothMessageEntity>> = incomingMessages

    fun getUpValue(): LiveData<String> = upValue
    fun getDownValue(): LiveData<String> = downValue
    fun getLeftValue(): LiveData<String> = leftValue
    fun getRightValue(): LiveData<String> = rightValue

    fun setUpValue(value: String) = sharedPreferences.storeString(UP_VALUE_KEY, value)
    fun setDownValue(value: String) = sharedPreferences.storeString(DOWN_VALUE_KEY, value)
    fun setLeftValue(value: String) = sharedPreferences.storeString(LEFT_VALUE_KEY, value)
    fun setRightValue(value: String) = sharedPreferences.storeString(RIGHT_VALUE_KEY, value)

    fun getParedBluetoothDevice(): LiveData<BluetoothDevice?> = selectedParedDevice
    fun setParedBluetoothDevice(device: BluetoothDevice?) {
        if (device != null) {
            bluetoothService.connectToDevice(device)
        } else {
            bluetoothService.disconnectAllDevices()
        }

        selectedParedDevice.value = device
    }

    fun getLastCommand(): LiveData<String> = lastCommand

    fun sentCommand(command: String) {
        if (command.isNotBlank()) {
            bluetoothService.write(command.toByteArray())
            lastCommand.value = command
        }
    }

    private fun showIncomingMessage(msg: String) {
        Toast.makeText(
            getApplication<GenericBluetoothApp>().applicationContext,
            msg,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun initAndroidSharedPreferences(application: Application): ApplicationSharedPreferences {
        return DefaultApplicationSharedPreferences(
            application.getSharedPreferences(
                SHARED_PREF_NAME,
                Context.MODE_PRIVATE
            )
        )
    }

    fun startDiscoveryMode(adapter: BluetoothAdapter, activity: Activity) {
        adapter.cancelDiscovery()
        if (!adapter.startDiscovery()) {
            activity.sendBroadcast(Intent(ACTION_DISCOVERY_FAILED))
        }
    }
    fun getBluetoothPermissionGranted(): LiveData<Boolean> = bluetoothPermissionGranted
    fun setBluetoothPermissionGranted(isPermissionGranted: Boolean) {
        bluetoothPermissionGranted.value = isPermissionGranted
    }

    fun checkBluetoothPermissionGranted(activity: Activity) {
        bluetoothPermissionGranted.value = isLocationPermissionGranted(activity)
    }

    fun enableBluetoothIfNot(activity: Activity) {
        if (bluetoothAdapter?.isEnabled == false && denyCount < 2) {
            denyCount++
            startBluetoothOnIntent(activity)
        }
    }

    private fun startBluetoothOnIntent(activity: Activity) {
        activity.startActivityForResult(
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
            BLUETOOTH_START_REQUEST_CODE
        )
    }

    private fun isLocationPermissionGranted(activity: Activity): Boolean {
        return when (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            true -> checkLollipopPermissions(activity)
            else -> true
        }
    }

    fun requestPermissions(permissions: Array<String>, activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            permissions,
            PERMISSION_REQUEST_CODE
        )
    }

    private fun checkLollipopPermissions(activity: Activity): Boolean {

        val permissionCoarseLocation = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val permissionFineLocation = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        return permissionCoarseLocation == PackageManager.PERMISSION_GRANTED &&
                permissionFineLocation == PackageManager.PERMISSION_GRANTED
    }

    override fun onCleared() {
        super.onCleared()
        setParedBluetoothDevice(null)
        bluetoothService.stopService()
    }

    fun checkIfDeepLinkItemsInIntent(intent: Intent?) {
        if (intent != null && Intent.ACTION_VIEW == intent.action) {
            deepLinkService.extractQueryParams(
                intent.data,
                object : DeepLinkItemExtractorService.OnItemsExtracted {
                    override fun onItemsExtracted(items: Map<String, String>?) {
                        deepLinkItems.value = items
                    }
                }
            )
        } else {
            deepLinkItems.value = null
        }

    }

    fun getDeepLinkItems(): LiveData<Map<String, String>?> = deepLinkItems

    fun makePresentationText(map: Map<String, String>, leadingStr: String): String {
        return "$leadingStr ${makeStrFromMap(map)}"
    }

    private fun makeStrFromMap(map: Map<String, String>): String {
        return map.keys.joinToString {
            "$it:${map[it]}"
        }
    }

}
