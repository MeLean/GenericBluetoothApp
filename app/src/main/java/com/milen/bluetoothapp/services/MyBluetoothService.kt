package com.milen.bluetoothapp.services

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.os.Handler
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.*


private const val TAG = "TEST_IT"

// Defines several constants used when transmitting messages between the
// service and the UI.
const val MESSAGE_READ: Int = 0
const val MESSAGE_WRITE: Int = 1
const val MESSAGE_TOAST: Int = 2

const val NAME = "BluetoothServiceSecure"
private val MY_UUID: UUID = UUID.fromString("0000110a-0000-1000-8000-00805f9b34fb")

class MyBluetoothService(
    private val bluetoothAdapter: BluetoothAdapter?,
    // handler that gets info from Bluetooth service
    private val handler: Handler
) {
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
                } catch (e: IOException) {
                    Log.d(TAG, "Input stream was disconnected", e)
                    break
                }

                sendMsg(numBytes, mmBuffer)
            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)

                // Send a failure message back to the activity.
                val writeErrorMsg = handler.obtainMessage(MESSAGE_TOAST)
                val bundle = Bundle().apply {
                    putString("toast", "Couldn't send data to the other device")
                }
                writeErrorMsg.data = bundle
                handler.sendMessage(writeErrorMsg)
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

    private fun sendMsg(numBytes: Int, mmBuffer: ByteArray) {
        // Send the obtained bytes to the UI activity.
        val readMsg = handler.obtainMessage(
            MESSAGE_READ,
            numBytes,
            -1,
            mmBuffer
        )
        readMsg.sendToTarget()
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
                    mmServerSocket?.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "Socket's accept() method failed", e)
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
                } catch (e : Throwable){
                    Log.d(TAG,  "connection failed: ${e.localizedMessage}")
                    val msg = e.localizedMessage ?: "error"
                    sendMsg(msg.length, msg.toByteArray())
                }
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }

    private fun manageMyConnectedSocket(socket: BluetoothSocket) {
        mConnectedThread = ConnectedThread(socket)
        mConnectedThread?.start()
    }

    @Synchronized
    fun startService() {
        Log.d(TAG, "startService")

        cancelAllConnectedDevices()

        if (mAcceptThread == null) {
            mAcceptThread = AcceptThread()
            mAcceptThread?.start()
        }
    }

    @Synchronized
    fun connectToDevice(device: BluetoothDevice) {
        Log.d(TAG, "connect to: $device")

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
        mConnectThread?.let {
            it.cancel()
            mConnectThread = null
        }

        mConnectedThread?.let {
            it.cancel()
            mConnectedThread = null
        }
    }
}