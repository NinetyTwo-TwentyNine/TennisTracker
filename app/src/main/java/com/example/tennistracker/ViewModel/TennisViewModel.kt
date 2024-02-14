package com.example.tennistracker.ViewModel

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tennistracker.Bluetooth.ConnectedThread
import com.example.tennistracker.Bluetooth.ConnectionThread
import com.example.tennistracker.data.Constants.APP_DATA_BYTE_AMOUNT
import com.example.tennistracker.data.Constants.APP_DATA_RECEPTION_PERIOD
import com.example.tennistracker.data.Constants.APP_DEVICE_BLUETOOTH_ADDRESS
import com.example.tennistracker.data.Constants.APP_TENNIS_MAX_RADIAN
import com.example.tennistracker.data.Constants.APP_TENNIS_MAX_SPEED
import com.example.tennistracker.data.Constants.APP_TENNIS_MAX_STRENGTH
import com.example.tennistracker.data.Constants.APP_TOAST_BLUETOOTH_DEVICE_CONNECTION_FAILED
import com.example.tennistracker.data.Constants.APP_TOAST_BLUETOOTH_DEVICE_CONNECTION_SUCCESSFUL
import com.example.tennistracker.data.TennisHit
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.ArrayList

@RequiresApi(Build.VERSION_CODES.S)
@SuppressLint("MissingPermission")
class TennisViewModel : ViewModel() {
    private var isBluetoothAvailable: Boolean? = null

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothDevice: BluetoothDevice? = null

    private var receivedHitData: TennisHit? = null

    var connectionThread: MutableLiveData<ConnectionThread?> = MutableLiveData(null)
    var connectedThread: MutableLiveData<ConnectedThread?> = MutableLiveData(null)

    val hitData: MutableLiveData<ArrayList<TennisHit>> = MutableLiveData(arrayListOf())

    fun getCurrentSpeed(): Float {
        var sum = 0F
        hitData.value!!.forEach {
            sum += (it.getSpeed() / hitData.value!!.size)
        }
        return sum
    }

    fun getCurrentStrength(): Float {
        var sum = 0F
        hitData.value!!.forEach {
            sum += (it.getStrength() / hitData.value!!.size)
        }
        return sum
    }

    fun getCurrentRadian(): Float {
        var sum = 0F
        hitData.value!!.forEach {
            sum += (it.getRadian() / hitData.value!!.size)
        }
        return sum
    }

    fun getHitData(): List<TennisHit> {
        return hitData.value!!
    }

    fun addHit(hit: TennisHit) {
        hitData.value!!.add(hit)
        hitData.postValue(hitData.value!!)
    }

    fun cleanHitData() {
        hitData.value!!.clear()
    }

    fun setBluetoothAvailable(available: Boolean) {
        isBluetoothAvailable = available
    }

    fun isBluetoothAvailable(): Boolean? {
        return isBluetoothAvailable
    }

    fun createBluetoothAdapter(context: Context) {
        val bluetoothManager: BluetoothManager = ContextCompat.getSystemService(context, BluetoothManager::class.java)!!
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            setBluetoothAvailable(false)
        }
        setBluetoothAvailable(bluetoothAdapter != null)
    }

    fun enableSearch() {
        if (!bluetoothAdapter!!.isDiscovering) {
            bluetoothAdapter!!.startDiscovery()
            Log.d("APP_CHECKER", "Discovery was started!")
        }
    }

    fun disableSearch() {
        if (bluetoothAdapter!!.isDiscovering) {
            bluetoothAdapter!!.cancelDiscovery()
            Log.d("APP_CHECKER", "Discovery was canceled!")
        }
    }

    fun addDevice(device: BluetoothDevice) {
        if (device.address != APP_DEVICE_BLUETOOTH_ADDRESS) { throw(Exception("Attempt to save a device with an invalid address.")) }
        bluetoothDevice = device
        Log.d("APP_CHECKER", "Device added: ${device.name} (${device.address}).")
    }

    fun checkDeviceValidity(): Boolean {
        return (bluetoothDevice != null && bluetoothDevice?.address == APP_DEVICE_BLUETOOTH_ADDRESS)
    }

    fun connectToDevice(context: Context) {
        val connectedThreads: ArrayList<ConnectedThread> = arrayListOf()
        if (bluetoothDevice == null || bluetoothDevice?.address != APP_DEVICE_BLUETOOTH_ADDRESS) {
            throw(Exception("Attempt to connect to an invalid device."))
        }

        val currentConnectionThread = ConnectionThread(bluetoothDevice!!,
            successFun = { socket ->
                (context as Activity).runOnUiThread {
                    Toast.makeText(context, APP_TOAST_BLUETOOTH_DEVICE_CONNECTION_SUCCESSFUL, Toast.LENGTH_SHORT).show()
                }

                val currentConnectedThread = ConnectedThread(socket) { stream ->
                    threadTimerFun(stream)
                }
                currentConnectedThread.start()
                connectedThreads.add(currentConnectedThread)

                connectedThread.value = currentConnectedThread
                Log.d("APP_CHECKER", "Connection to device ${bluetoothDevice!!.name} (${bluetoothDevice!!.bluetoothClass}) established.")
            },
            failFun = {
                (context as Activity).runOnUiThread {
                    Toast.makeText(context, APP_TOAST_BLUETOOTH_DEVICE_CONNECTION_FAILED, Toast.LENGTH_SHORT).show()
                }
                connectedThread.value = null
                Log.d("APP_CHECKER", "Connection to device ${bluetoothDevice!!.name} (${bluetoothDevice!!.bluetoothClass}) failed.")
            })
        currentConnectionThread.start()
        connectionThread.value = currentConnectionThread
    }

    private fun threadTimerFun(stream: InputStream): Boolean {
        if (stream.available() > 0) {
            performTimerEvent({
                val bytesArray: ByteArray = stream.readNBytes(APP_DATA_BYTE_AMOUNT)
                Log.d("DATA_OBSERVER", "Receiving data: ${bytesArray[1]}, ${bytesArray[2]}, ${bytesArray[3]}")

                receivedHitData = TennisHit(
                    speed = bytesArray[1].toFloat() / 255 * APP_TENNIS_MAX_SPEED,
                    strength = bytesArray[2].toFloat() / 255 * APP_TENNIS_MAX_STRENGTH,
                    radian = bytesArray[3].toFloat() / 255 * APP_TENNIS_MAX_RADIAN
                )

                Log.d(
                    "DATA_OBSERVER",
                    "speed = ${receivedHitData!!.getSpeed()}, strength = ${receivedHitData!!.getStrength()}, radian = ${receivedHitData!!.getRadian()}."
                )
                addHit(receivedHitData!!)
            }, APP_DATA_RECEPTION_PERIOD)
            return true
        } else {
            return false
        }
    }

    fun uploadData(dataPackage: ArrayList<Byte>): Boolean {
        return if (connectedThread.value != null && connectionThread.value != null && connectionThread.value!!.isConnected) {
            connectedThread.value!!.writePackage(dataPackage.toByteArray())
            true
        } else {
            false
        }
    }

    private fun getCheckedByte(command: Int): Byte {
        val bytes = ByteBuffer.allocate(Int.SIZE_BYTES).putInt(command).array()
        if (bytes[0].toInt() != 0 || bytes[1].toInt() != 0 || bytes[2].toInt() != 0) {
            Log.d("APP_CHECKER", "Byte1 = ${bytes[0].toInt()}, Byte2 = ${bytes[1].toInt()},  Byte3 = ${bytes[2].toInt()}, Byte4 = ${bytes[3].toInt()}")
            throw(RuntimeException("This number is too big to send."))
        }
        return bytes[3]
    }

    fun performTimerEvent(timerFun: () -> Unit, time: Long) {
        val eventTimer = Timer()
        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                MainScope().launch {
                    timerFun()
                }
            }
        }
        eventTimer.schedule(timerTask, time)
    }

    fun performTimerEventScheduled(timerFun: () -> Unit, time: Long) {
        val eventTimer = Timer()
        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                MainScope().launch {
                    timerFun()
                }
            }
        }
        eventTimer.scheduleAtFixedRate(timerTask, 1000L, time)
    }
}