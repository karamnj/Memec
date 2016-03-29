package com.avnet.memec.ui.util;


import android.bluetooth.BluetoothDevice;

public interface BleScanCallback
{
    void onScanStarted();
    void onScanEnded();
    void onDeviceDiscovered(BluetoothDevice device, int rssi, byte[] scanRecord);
}
