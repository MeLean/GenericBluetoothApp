package com.milen.bluetoothapp.base.services

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


class BluetoothConnectionServices() {
    private var mConnectThread : ConnectThread? = null
    private var mAdapter = BluetoothAdapter.getDefaultAdapter()
    private var mAcceptThread : AcceptThread? = null
    private var mConnectedThread : ConnectedThread? = null
    private val isSecure = false

    companion object {

        // Name for the SDP record when creating server socket
        const val NAME_SECURE = "BluetoothServiceSecure"
        const val NAME_INSECURE = "BluetoothServiceInsecure"

        private val MY_UUID_SECURE: UUID = UUID.fromString("0000110a-0000-1000-8000-00805f9b34fb")
        private val MY_UUID_INSECURE: UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")
    }

    inner class AcceptThread(secure: Boolean) : Thread() {
        // The local server socket
        private var bluetoothServerSocket: BluetoothServerSocket? = null
        private val mSocketType: String = if (secure) "Secure" else "Insecure"

        init {
            // Create a new listening server socket
            try {
                bluetoothServerSocket = if (secure) {
                    mAdapter.listenUsingRfcommWithServiceRecord(
                        NAME_SECURE,
                        MY_UUID_SECURE
                    )
                } else {
                    mAdapter.listenUsingInsecureRfcommWithServiceRecord(
                        NAME_INSECURE,
                        MY_UUID_INSECURE
                    )
                }
            } catch (e: IOException) {
                Log.e("TEST", "Socket Type: " + mSocketType + "listen() failed", e)
            }
        }

        override fun run() {
            Log.d(
                "TEST", "Socket Type: " + mSocketType +
                        "BEGIN mAcceptThread" + this
            )
            name = "AcceptThread$mSocketType"

            // Listen to the server socket if we're not connected
            var socket: BluetoothSocket? = null
            try {
                socket = bluetoothServerSocket?.accept()
            } catch (e: Throwable) {
                Log.e("TEST", "Socket Type: " + mSocketType + "accept() failed", e)
            }

            Log.i("TEST", "Server socked accepted : $mSocketType")
            // If a connection was accepted
            socket?.let {
                connected(it, socket.remoteDevice, mSocketType)
                Log.i("TEST", "END mAcceptThread, socket Type: $mSocketType")
            }
        }

        fun cancel() {
            Log.d("TEST", "Socket Type" + mSocketType + "cancel " + this)
            bluetoothServerSocket?.close()
        }
    }

    inner class ConnectThread(
        private val mmDevice: BluetoothDevice,
        secure: Boolean
    ) : Thread() {
        private val mmSocket: BluetoothSocket?
        private val mSocketType: String

        init {
            var tmp: BluetoothSocket? = null
            mSocketType = if (secure) "Secure" else "Insecure"

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = if (secure) {
                    mmDevice.createRfcommSocketToServiceRecord(
                        MY_UUID_SECURE
                    )
                } else {
                    mmDevice.createInsecureRfcommSocketToServiceRecord(
                        MY_UUID_INSECURE
                    )
                }
            } catch (e: IOException) {
                Log.e("TEST", "Socket Type: " + mSocketType + "create() failed", e)
            }
            mmSocket = tmp
        }

        override fun run() {
            Log.i("TEST", "BEGIN mConnectThread SocketType:$mSocketType")
            name = "ConnectThread$mSocketType"

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery()

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket?.connect()
                Log.d("TEST", "Connection successful!")
            } catch (e: IOException) {
                // Close the socket
                try {
                    mmSocket?.close()
                } catch (e2: IOException) {
                    Log.e(
                        "TEST", "unable to close() " + mSocketType +
                                " socket during connection failure", e2
                    )
                }
                return
            }

            // Start the connected thread
            connected(mmSocket!!, mmDevice, mSocketType)
        }


        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: IOException) {
                Log.e("TEST", "close() of connect $mSocketType socket failed", e)
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    class ConnectedThread(socket: BluetoothSocket, socketType: String) : Thread() {
        private val mmSocket: BluetoothSocket
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?


        init {
            Log.d("TEST", "create ConnectedThread: $socketType")
            mmSocket = socket
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.inputStream
                tmpOut = socket.outputStream
            } catch (e: IOException) {
                Log.e("TEST", "temp sockets not created ${e.localizedMessage}")
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
        }

        override fun run() {
            Log.i("TEST", "BEGIN mConnectedThread")
            val buffer = ByteArray(1024)
            var bytes: Int

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream?.read(buffer) ?: 0

//                    // Send the obtained bytes to the UI Activity
//                    mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer)
//                        .sendToTarget()
                    Log.d("TEST", "bytes read: $bytes")
                    val msg = String(buffer, 0, bytes)
                    Log.d("TEST", "msg: $msg")
                } catch (e: IOException) {
                    Log.e("TEST", "disconnected", e)
                    break
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        fun write(buffer: ByteArray) {
            try {
                mmOutStream?.write(buffer)
                Log.e("TEST", "write!" )
                // Share the sent message back to the UI Activity
//                mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
//                    .sendToTarget()
            } catch (e: IOException) {
                Log.e("TEST", "Exception during write", e)
            }
        }

        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e("TEST", "close() of connect socket failed", e)
            }
        }
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    @Synchronized
    fun startService() {
        Log.d("TEST", "startService")
        mConnectThread?.let{
            it.cancel()
            mConnectThread = null
        }

        if(mAcceptThread == null){
            mAcceptThread = AcceptThread(isSecure)
            mAcceptThread!!.start()
        }
    }

    @Synchronized
    fun startClient(device: BluetoothDevice, uuid: UUID){
        Log.d("TEST", "startClient")
        mConnectThread = ConnectThread(device, isSecure)
    }

    private fun connected(mmSocket: BluetoothSocket, mmDevice: BluetoothDevice, mSocketType: String) {
        Log.d("TEST", "connected to device ${mmDevice.name}")
        mConnectedThread = ConnectedThread(mmSocket, mSocketType)
        mConnectedThread!!.start()
    }

    fun writeToUi(output : ByteArray){
        mConnectedThread?.write(output)
    }
}