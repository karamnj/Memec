package com.avnet.memec.ui.util;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;


/**
 * Class wrapping the logic to handle BLE communication from connecting to retrieving data
 */
public class BleFuncProvider implements BluetoothAdapter.LeScanCallback
{
    private Context appContext;
    private BluetoothAdapter bluetoothAdapter;
    private BleScanCallback bleScanCallback;
    private Handler uiHandler;
    private Runnable scanTopRunnable;

    private static final long SCAN_PERIOD = 5000;  // Duration of the BLE scan in ms

    private static final int PERMISSIONS_REQUEST_READ_LOCATION = 100;


    /**
     * Create BLEfuncProvider obj passing the system bluetooth adapter
     *
     * @param appContext Application context
     * @param bleScanCallback BleScanCallback interface
     * @throws BleFeaturesNotPresentExc
     */
    public BleFuncProvider(Context appContext, BleScanCallback bleScanCallback) throws BleFeaturesNotPresentExc
    {
        this.appContext = appContext;
        this.bleScanCallback = bleScanCallback;
        this.uiHandler = new Handler(Looper.getMainLooper());   // Create the handler using main looper
                                                                // to run it directly on the UI thread

        if(!isBleFeatureAvailable()){
            throw new BleFeaturesNotPresentExc();
        }

        // Retrieve the system Bluetooth adapter
        BluetoothManager bluetoothManager = (BluetoothManager)appContext.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // Runnable to stop the scan after specified time
        this.scanTopRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                stopScan();
            }
        };
    }


    /**
     * Check BLE features availability for this device
     *
     * @return True if available, otherwise false
     */
    private boolean isBleFeatureAvailable(){
        return appContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * Check Bluetooth adapter status
     *
     * @return True if Bluetooth ON, otherwise false
     */
    public boolean isBluetoothOn()
    {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    /**
     * Start scanning for BLE devices. Results of the scan are passed via BluetoothAdapter.LeScanCallback
     */
    public void startScan(){
        // Set the BLE devices scan to stop after SCAN_PERIOD ms.
        uiHandler.postDelayed(scanTopRunnable, SCAN_PERIOD);

        // Start the BLE scan
        bluetoothAdapter.startLeScan(this);
        bleScanCallback.onScanStarted();
    }

    /**
     * Stop scanning for BLE devices. Result of the operation is passed via BluetoothAdapter.LeScanCallback
     */
    public void stopScan()
    {
        // Avoid runnable to fire
        uiHandler.removeCallbacks(scanTopRunnable);

        // Stop the BLE scan
        bluetoothAdapter.stopLeScan(this);
        bleScanCallback.onScanEnded();
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord)
    {
        bleScanCallback.onDeviceDiscovered(device, rssi, scanRecord);
    }

    public boolean isGrantPermissionNeeded(Activity appActivityForReply)
    {
        if (ContextCompat.checkSelfPermission(appActivityForReply,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(appActivityForReply,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(appActivityForReply,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_READ_LOCATION);

            return true;
        }
        // Permissions already granted, no need to ask
        return false;
    }

    public boolean arePermissionGranted(int requestCode, String[] permissions, int[] grantResults)
    {
        if(requestCode == PERMISSIONS_REQUEST_READ_LOCATION){
            if(grantResults.length < 2){ // Request cancelled
                return false;
            }

            for(int grantRes : grantResults){
                if(grantRes != PackageManager.PERMISSION_GRANTED){ // User did not grant permission
                    return false;
                }
            }
            return true;
        }

        // Permission response not related to Location request
        return false;
    }
}
