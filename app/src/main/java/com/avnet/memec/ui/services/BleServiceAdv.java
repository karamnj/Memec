package com.avnet.memec.ui.services;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.avnet.memec.ui.util.BleFuncProvider;
import com.avnet.memec.ui.util.BleScanCallback;
import com.avnet.memec.ui.util.BtDevice;

public class BleServiceAdv extends Service implements BleScanCallback
{
    private BleFuncProvider bleFuncProvider;
    private BtDevice btDevice;
    //private BleServiceNotification bleServiceNotification;
    private boolean isScanToStop;


    public BleServiceAdv()
    {
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // No need to allow binding to this service
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        Log.d("onCreate", "BleServiceAdv");
        // Handle Bluetooth provider creation
        try {
            bleFuncProvider = new BleFuncProvider(this, this);
        }
        catch (Exception exc){
            Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d("onStartCommand", "BleServiceAdv");
        btDevice = intent.getParcelableExtra(BleServiceConst.BLE_DEVICE);
        //bleServiceNotification = new BleServiceNotification(this, btDevice);
        isScanToStop = false;
        bleFuncProvider.startScan();
        //bleServiceNotification.showConnectNotification(getText(R.string.adv_connected_notification));

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy()
    {
        isScanToStop = true;
        bleFuncProvider.stopScan();
        //bleServiceNotification.hideConnectNotification();
        broadcastUpdate(BleServiceConst.SERVICE_STOPPED);

        super.onDestroy();
    }

    private void broadcastUpdate(final String event)
    {
        final Intent intent = new Intent(event);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String event, byte[] envData)
    {
        Log.d("broadcastUpdate", "BleServiceAdv");
        final Intent intent = new Intent(event);
        intent.putExtra(BleServiceConst.ENV_DATA, envData);
        sendBroadcast(intent);
    }

    @Override
    public void onScanStarted()
    {

    }

    @Override
    public void onScanEnded()
    {
        if(isScanToStop){
            return;
        }
        bleFuncProvider.startScan();
    }

    @Override
    public void onDeviceDiscovered(BluetoothDevice device, int rssi, byte[] scanRecord)
    {
        Log.d("onDeviceDiscovered", "BleServiceAdv");
        // Filter advertising message not coming from selected device
        if(!device.getAddress().equals(btDevice.getBluetoothDevice().getAddress())){
            return;
        }

        //AdvFrameParser advFrameParser = new AdvFrameParser(scanRecord);
        //EnvData envData = advFrameParser.parseFrame();

        if(scanRecord != null){
            broadcastUpdate(BleServiceConst.ACTION_ENV_DATA_AVAILABLE, scanRecord);
        }
    }
}
