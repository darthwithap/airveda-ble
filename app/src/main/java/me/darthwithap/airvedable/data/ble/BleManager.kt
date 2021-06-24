package me.darthwithap.airvedable.data.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import me.darthwithap.airvedable.BaseApplication

private const val TAG = "BleManager"

object BleManager {

    private val bluetoothManager =
        BaseApplication.getContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    val adapter: BluetoothAdapter by lazy {
        bluetoothManager.adapter
    }

    var isScanning: Boolean = false

    private val bleScanner: BluetoothLeScanner by lazy {
        adapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
        .build()

    fun startScan(scanCallback: ScanCallback) {
        bleScanner.startScan(null, scanSettings, scanCallback)
    }

    fun stopScan(scanCallback: ScanCallback) {
        bleScanner.stopScan(scanCallback)
    }
}
