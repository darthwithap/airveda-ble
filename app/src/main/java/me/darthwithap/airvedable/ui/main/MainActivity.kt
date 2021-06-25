package me.darthwithap.airvedable.ui.main

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import es.dmoral.toasty.Toasty
import me.darthwithap.airvedable.R
import me.darthwithap.airvedable.data.ble.BleManager
import me.darthwithap.airvedable.databinding.ActivityMainBinding
import me.darthwithap.airvedable.utils.LOCATION_PERMISSION_REQ_CODE
import me.darthwithap.airvedable.utils.hasPermission

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var viewModel: MainViewModel? = null
    private var positionItemClicked: Int = -1
    private lateinit var scanButton: Button
    private lateinit var adapter: ScanAdapter
    private var scanResults = arrayListOf<ScanResult>()

    private val isLocationPermissionGranted
        get() = hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION) // should be in the viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        binding.rvScanResults.layoutManager = LinearLayoutManager(this)
        adapter = ScanAdapter(scanResults) {
            Toasty.success(this, it.device.name).show()
            // TODO on scan result item click
            if (BleManager.isScanning) {
                viewModel?.stopScan()
            }
            viewModel?.connectGatt(this, it.device)
        }
        binding.rvScanResults.adapter = adapter
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {
        scanButton = binding.btnScan
        scanButton.setOnClickListener {
            // TODO WILL BE A viewModel.toggleScan() method
            if (BleManager.isScanning) {
                scanButton.text = getString(R.string.start_scan)
                viewModel?.stopScan()
            } else {
                scanButton.text = getString(R.string.stop_scan)
                startBleScan()
            }
        }
        viewModel?.scanResults?.observe({ lifecycle }) {
            scanResults.clear()
            scanResults.addAll(it)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()
        if (BleManager.adapter.isEnabled) {
            promptEnableBluetooth()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            if (!BleManager.adapter.isEnabled) {
                promptEnableBluetooth()
            }
        }
    }

    private fun startBleScan() {
        if (!isLocationPermissionGranted) {
            requestLocationPermission()
        } else {
            viewModel?.startScan()
        } // TODO toggleScan inside viewModel
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQ_CODE -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    requestLocationPermission()
                } else startBleScan()
            }
        }
    }

    private fun promptEnableBluetooth() {
        if (!BleManager.adapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableBtIntent)
        }
    }

    private fun requestLocationPermission() {
        if (isLocationPermissionGranted) {
            return
        }
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Location permission required")
                .setMessage(
                    "Starting from Android M (6.0), apps require location " +
                            "access in order to scan for BLE devices."
                )
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    createLocationRequest()
                }
                .create()
                .show()
        }
    }

    private fun createLocationRequest() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQ_CODE
        )
    }

    private fun setScanButtonClickable(clickable: Boolean) {
        scanButton.isClickable = clickable
    }

    private fun grantLocationServices() {
        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    private fun showError(e: String) {
        Toasty.error(this, e).show()
    }

    private fun showSuccess(s: String) {
        Toasty.success(this, s).show()
    }

    private fun showProgressBar(visibility: Boolean) {
        // progressBar.visibility = if (visibility) View.VISIBLE else View.GONE
    }
}