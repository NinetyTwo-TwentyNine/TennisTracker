package com.example.tennistracker.Bluetooth

import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class ConnectedThread(bluetoothSocket: BluetoothSocket) : Thread() {
    private val inputStream: InputStream?
    private val outputStream: OutputStream?

    override fun run() {}

    init {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            inputStream = bluetoothSocket.inputStream
            outputStream = bluetoothSocket.outputStream
        } catch (e: IOException) {
            e.printStackTrace()
        }
        this.inputStream = inputStream
        this.outputStream = outputStream
    }

    fun checkConnection(): Boolean {
        return (inputStream != null && outputStream != null)
    }

    fun write(byte: Byte): Boolean {
        if (outputStream != null) {
            val byteArray: ArrayList<Byte> = arrayListOf()
            byteArray.add(byte)

            return try {
                outputStream.write(byteArray.toByteArray())
                outputStream.flush()
                true
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d("APP_DEBUGGER", "Failed attempt to write to the device.")
                cancel()
                false
            }
        } else {
            return false
        }
    }

    fun writePackage(dataPackage: ByteArray): Boolean {
        if (outputStream != null) {
            return try {
                outputStream.write(dataPackage)
                outputStream.flush()
                true
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d("APP_DEBUGGER", "Failed attempt to write to the device.")
                cancel()
                false
            }
        } else {
            return false
        }
    }

    fun cancel() {
        try {
            inputStream!!.close()
            outputStream!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}