package com.example.tennistracker.data

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.S)
object Constants {
    const val APP_TOAST_BLUETOOTH_DATA_SENDING_NOT_AVAILABLE = "No data can be sent to physical device."
    const val APP_TOAST_BLUETOOTH_MISSING = "Sadly, you have no Bluetooth support."
    const val APP_TOAST_BLUETOOTH_DEVICE_NOT_FOUND = "No valid device was found."
    const val APP_TOAST_BLUETOOTH_DEVICE_CONNECTION_SUCCESSFUL = "Successful connection!"
    const val APP_TOAST_BLUETOOTH_DEVICE_CONNECTION_FAILED = "Failed to connect to the device."
    const val APP_DEVICE_BLUETOOTH_ADDRESS = "00:20:10:08:0B:EF"

    const val APP_TENNIS_MAX_STRENGTH = 120
    const val APP_TENNIS_MAX_SPEED = 200
    const val APP_TENNIS_MAX_RADIAN = 270

    val APP_BLUETOOTH_PERMISSIONS_LIST = listOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
}