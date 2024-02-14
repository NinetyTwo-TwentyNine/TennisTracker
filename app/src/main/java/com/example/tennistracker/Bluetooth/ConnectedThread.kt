package com.example.tennistracker.Bluetooth

import android.bluetooth.BluetoothSocket
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.tennistracker.data.Constants.APP_DATA_RECEPTION_PERIOD
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.collections.ArrayList

@RequiresApi(Build.VERSION_CODES.S)
class ConnectedThread(bluetoothSocket: BluetoothSocket, private val timerFun: (InputStream)->Boolean) : Thread() {
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

        if (this.inputStream != null) {
            setupTimer()
        }
    }

    private fun setupTimer() {
        val eventTimer = Timer()
        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                MainScope().launch {
                    val result = timerFun(inputStream!!)
                    if (result) {
                        Log.d("RECEPTION_CHECKER", "Received something = $result")
                        eventTimer.cancel()
                        setupTimer()
                    }
                }
            }
        }
        eventTimer.scheduleAtFixedRate(timerTask, 1000L, APP_DATA_RECEPTION_PERIOD)
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