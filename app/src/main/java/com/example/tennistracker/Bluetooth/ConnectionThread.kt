package com.example.tennistracker.Bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.IOException

class ConnectionThread(private val device: BluetoothDevice, private val failFun: ()->Unit, private val successFun: (BluetoothSocket)->Unit) : Thread() {
    private var bluetoothSocket: BluetoothSocket? = null
    private var success = false

    init {
        try {
            val method = device::class.members.single {it.name == "createRfcommSocket"}
            bluetoothSocket = method.call(device, 1) as BluetoothSocket
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getBluetoothSocket(): BluetoothSocket? {
        return bluetoothSocket
    }

    @SuppressLint("MissingPermission")
    override fun run() {
        try {
            bluetoothSocket!!.connect()
            success = true
        } catch (e: IOException) {
            e.printStackTrace()
            MainScope().launch {
                failFun()
            }
            cancel()
        }
        if (success) {
            MainScope().launch {
                successFun(bluetoothSocket!!)
            }
        }
    }

    val isConnected: Boolean
        get() = bluetoothSocket!!.isConnected

    fun cancel() {
        try {
            bluetoothSocket!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}