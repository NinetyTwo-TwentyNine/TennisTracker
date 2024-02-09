package com.example.tennistracker.UI

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tennistracker.R
import com.example.tennistracker.ViewModel.TennisViewModel
import com.example.tennistracker.data.Constants
import com.example.tennistracker.data.Constants.APP_TOAST_BLUETOOTH_DATA_SENDING_NOT_AVAILABLE
import com.example.tennistracker.databinding.ActivityMainBinding

@Suppress("DEPRECATION")
@RequiresApi(Build.VERSION_CODES.S)
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val tennisViewModel: TennisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        for (permission: String in Constants.APP_BLUETOOTH_PERMISSIONS_LIST) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(Constants.APP_BLUETOOTH_PERMISSIONS_LIST.toTypedArray(), REQUEST_CODE_LOC)
                return
            }
        }
        initiateBluetoothSetup()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bluetooth_menu, menu)

        tennisViewModel.performTimerEvent({
            tennisViewModel.dataCanBeSent.observe(this) {thread ->
                val bluetoothItem: View? = findViewById(R.id.bluetooth)
                Log.d("APP_DEBUGGER", "Attempt to set the color.")
                bluetoothItem?.apply {
                    Log.d("APP_DEBUGGER", "Color set called.")
                    this.setBackgroundColor(
                        getColor(when(thread != null && thread.checkConnection()) {
                            true -> R.color.green
                            false -> R.color.red
                        }))
                }
            }
        }, 100L)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.bluetooth -> {
                if (tennisViewModel.isBluetoothAvailable() != null) {
                    if (tennisViewModel.isBluetoothAvailable()!!) {
                        tennisViewModel.enableSearch()
                    } else {
                        Toast.makeText(this, APP_TOAST_BLUETOOTH_DATA_SENDING_NOT_AVAILABLE, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_LOC -> if (grantResults.isNotEmpty()) {
                for (gr in grantResults) {
                    // Check if request is granted or not
                    if (gr != PackageManager.PERMISSION_GRANTED) {
                        tennisViewModel.setBluetoothAvailable(false)
                        Toast.makeText(this, APP_TOAST_BLUETOOTH_DATA_SENDING_NOT_AVAILABLE, Toast.LENGTH_SHORT).show()
                        return
                    }
                }
                initiateBluetoothSetup()
            }
            else -> return
        }
    }

    private fun initiateBluetoothSetup() {
        if (tennisViewModel.isBluetoothAvailable() == null) {
            tennisViewModel.createBluetoothAdapter(this)
        }

        if (tennisViewModel.isBluetoothAvailable()!!) {
            val filter = IntentFilter()
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            filter.addAction(BluetoothDevice.ACTION_FOUND)
            ContextCompat.registerReceiver(
                this,
                receiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
            tennisViewModel.enableSearch()
        } else {
            Toast.makeText(this, "${Constants.APP_TOAST_BLUETOOTH_MISSING}\n$APP_TOAST_BLUETOOTH_DATA_SENDING_NOT_AVAILABLE", Toast.LENGTH_SHORT).show()
        }
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("APP_CHECKER", "Receive event was called:")
            when (intent.action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d("APP_CHECKER", "Discovery started")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    if (!tennisViewModel.checkDeviceValidity()) {
                        Toast.makeText(this@MainActivity, Constants.APP_TOAST_BLUETOOTH_DEVICE_NOT_FOUND, Toast.LENGTH_SHORT).show()
                    }
                    Log.d("APP_CHECKER", "Discovery finished")
                }
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null && device.address == Constants.APP_DEVICE_BLUETOOTH_ADDRESS) {
                        tennisViewModel.addDevice(device)
                        tennisViewModel.connectToDevice(this@MainActivity)
                        tennisViewModel.disableSearch()
                        Log.d("APP_CHECKER", "Device found")
                    }
                }
            }
        }
    }

    companion object {
        const val REQUEST_CODE_LOC = 1
    }
}