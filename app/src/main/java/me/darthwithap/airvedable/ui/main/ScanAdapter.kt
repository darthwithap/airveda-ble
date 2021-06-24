package me.darthwithap.airvedable.ui.main

import android.bluetooth.le.ScanResult
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.darthwithap.airvedable.BaseApplication
import me.darthwithap.airvedable.R
import me.darthwithap.airvedable.databinding.ListItemScanResultBinding

private const val TAG = "ScanAdapter"

class ScanAdapter(
    private val results: ArrayList<ScanResult>,
    private val onDeviceClick: (device: ScanResult) -> Unit
) : RecyclerView.Adapter<ScanAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanAdapter.ViewHolder {
        val binding = ListItemScanResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ScanAdapter.ViewHolder, position: Int) {
        holder.bind(results[position])
    }

    override fun getItemCount() = results.size

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(result: ScanResult) {
            ListItemScanResultBinding.bind(itemView).apply {
                with(result) {
                    tvDeviceName.text = device.name ?: "Unnamed"
                    tvMacAddress.text = device.address
                    tvSignalStrength.text =
                        BaseApplication.getContext().getString(R.string.rssi, rssi.toString())
                    root.setOnClickListener {
                        Log.d(TAG, "bind: clicked: ${this.device.name}")
                        onDeviceClick(this)
                    }
                }
            }
        }
    }
}