package com.milen.bluetoothapp.services

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


private const val TAG = "TEST_IT"

// Defines several constants used when transmitting messages between the
// service and the UI.
//const val MESSAGE_READ: Int = 0
const val MESSAGE_WRITE: Int = 1
const val MESSAGE_ERROR: Int = 2
const val MESSAGE_FAIL_CONNECT: Int = 3

const val NAME = "BluetoothServiceSecure"
private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

class MyBluetoothService(
    private val bluetoothAdapter: BluetoothAdapter?,
    // handler that gets info from Bluetooth service
    private val handler: Handler
) {
    companion object {

        @Volatile
        private var INSTANCE: MyBluetoothService? = null

        fun getInstance(
            bluetoothAdapter: BluetoothAdapter?,
            handler: Handler
        ): MyBluetoothService = INSTANCE ?: synchronized(this) {
            MyBluetoothService(bluetoothAdapter, handler).also {
                INSTANCE = it
            }
        }
    }

    private var mConnectThread: ConnectThread? = null
    private var mAcceptThread: AcceptThread? = null
    private var mConnectedThread: ConnectedThread? = null

    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        override fun run() {
            var numBytes: Int // bytes returned from read()
            Log.d(TAG,  "ConnectedThread started")
            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                numBytes = try {
                    mmInStream.read(mmBuffer)
                    Log.d(TAG, "Message received: ${String(mmBuffer)}")
                } catch (e: IOException) {
                    Log.d(TAG, "Input stream was disconnected", e)
                    sendErrorMsg(e)
                    break
                }

                sendMsg(numBytes, mmBuffer, MESSAGE_WRITE)
            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
                Log.d(TAG, "mmOutStream sending data done!")
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)
                sendErrorMsg(e)
                return
            }

            // Share the sent message with the UI activity.
            val writtenMsg = handler.obtainMessage(
                MESSAGE_WRITE, -1, -1, bytes
            )
            writtenMsg.sendToTarget()
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }

    private inner class AcceptThread : Thread() {
        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(NAME, MY_UUID)
        }

        override fun run() {
            // Keep listening until exception occurs or a socket is returned.
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    mmServerSocket?.let {
                        Log.d(TAG, "Socket's accept() started!")
                        it.accept()
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Socket's accept() method failed", e)
                    MESSAGE_ERROR
                    shouldLoop = false
                    null
                }
                socket?.also {
                    manageMyConnectedSocket(it)
                    mmServerSocket?.close()
                    shouldLoop = false
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        fun cancel() {
            try {
                mmServerSocket?.close()
                Log.e(TAG, "mmServerSocket closed")
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }

    private inner class ConnectThread(device: BluetoothDevice) : Thread() {

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(MY_UUID)
        }

        override fun run() {
            Log.d(TAG, "ConnectThread started")
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter?.cancelDiscovery()

            mmSocket?.use { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                try {
                    socket.connect()
                    // The connection attempt succeeded. Perform work associated with
                    // the connection in a separate thread.
                    manageMyConnectedSocket(socket)
                } catch (e : Throwable) {
                    Log.e(TAG, "connection failed: ${e.localizedMessage}", e)
                    sendErrorMsg(e, MESSAGE_FAIL_CONNECT)
                }
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
                Log.d(TAG, "mmSocket closed")
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }

    private fun manageMyConnectedSocket(socket: BluetoothSocket) {
        Log.d(
            TAG,
            "connection started device name: ${socket.remoteDevice.name} isConnected: ${socket.isConnected}"
        )
        mConnectedThread = ConnectedThread(socket)
        mConnectedThread?.start()
    }

    @Synchronized
    fun startService() {
        Log.d(TAG, "startService invoked")
        if (mAcceptThread?.isAlive == true) {
            return
        }

        Log.d(TAG, "mAcceptThread started")
        mAcceptThread = AcceptThread()
        mAcceptThread?.start()
    }

    @Synchronized
    fun stopService() {
        if (mAcceptThread != null) {
            mAcceptThread?.cancel()
            mAcceptThread = null
        }

        cancelAllConnectedDevices()
    }

    @Synchronized
    fun connectToDevice(device: BluetoothDevice) {
        Log.d(TAG, "connectToDevice: $device")

        cancelAllConnectedDevices()

        // Start the thread to connect with the given device
        mConnectThread = ConnectThread(device)
        mConnectThread?.start()
    }

    @Synchronized
    fun write(out: ByteArray?) {
        out?.let{
            Log.d(TAG,  "write service: ${String(it)}")
            mConnectedThread?.write(it)
        }
    }

    @Synchronized
    fun cancelAllConnectedDevices() {
        Log.d(TAG, "cancelAllConnectedDevices")
        mConnectThread?.let {
            it.cancel()
            mConnectThread = null
        }

        mConnectedThread?.let {
            it.cancel()
            mConnectedThread = null
        }
    }

    private fun sendErrorMsg(e: Throwable, what: Int = MESSAGE_ERROR) {
        val msg = e.localizedMessage ?: "error"
        sendMsg(msg.length, msg.toByteArray(), what)
    }

    private fun sendMsg(numBytes: Int, mmBuffer: ByteArray, what: Int) {
        // Send the obtained bytes to the UI activity.
        val readMsg = handler.obtainMessage(
            what,
            numBytes,
            -1,
            mmBuffer
        )
        readMsg.sendToTarget()
    }

}