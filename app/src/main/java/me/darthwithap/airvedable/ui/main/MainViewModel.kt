package me.darthwithap.airvedable.ui.main

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.darthwithap.airvedable.data.ble.BleManager

private const val TAG = "MainViewModel"

class MainViewModel : ViewModel() {

    private var _scanResults: MutableLiveData<List<ScanResult>> = MutableLiveData()
    val scanResults: LiveData<List<ScanResult>> = _scanResults
    private val scanResultsList: MutableList<ScanResult> = mutableListOf()
    // bleDevices: MutableList<BleDevice>()
    // isScanning: MutableLiveData<Boolean>()
    // bleConnected: MutableLiveData<String> // name of the ble device connected

    fun startScan() {
        scanResultsList.clear()
        _scanResults.postValue(scanResultsList)
        BleManager.startScan(scanCallback)
        BleManager.isScanning = true
    }

    fun stopScan() {
        BleManager.stopScan(scanCallback)
        BleManager.isScanning = false
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val index =
                scanResultsList.indexOfFirst { it.device.address == result.device.address }

            // A scan result already exists with the same mac address
            if (index != -1) {
                // replacing new result with old device to update rssi value
                scanResultsList[index] = result
            } else {
                // adding the device to the list of scan results
                scanResultsList.add(result)
            }
            _scanResults.postValue(scanResultsList)
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "onScanFailed: $errorCode")
        }
    }

    fun connectGatt(context: Context, device: BluetoothDevice) {
        device.connectGatt(context, false, gattCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                    // TODO: Store a reference to BluetoothGatt
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                    gatt.close()
                }
            } else {
                Log.w(
                    "BluetoothGattCallback",
                    "Error $status encountered for $deviceAddress! Disconnecting..."
                )
                gatt.close()
            }
            Log.d(TAG, "onConnectionStateChange: ${gatt.device}")
        }
    }
}