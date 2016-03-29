package com.avnet.memec.ui.services;

import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.avnet.memec.ui.util.BtDevice;

import java.util.List;

public class BleServiceGatt extends Service
{
    private BtDevice btDevice;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCallback bluetoothGattCallback;
    //private CharaParser charaParser;
    private boolean isServiceStarted;
    //private BleServiceNotification bleServiceNotification;

    // GATT Services UUIDs
    private final static int SERVICE_GENERIC_ACCESS = 0x1800;
    private final static int SERVICE_GENERIC_ATTRIBUTE = 0x1801;
    private final static int SERVICE_DEVICE_INFORMATION = 0x180A;


    public BleServiceGatt()
    {
        //charaParser = new CharaParser();

        bluetoothGattCallback = new BluetoothGattCallback()
        {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
            {
                if(newState == BluetoothProfile.STATE_CONNECTED){
                    //bleServiceNotification.showConnectNotification(getText(R.string.gatt_connected_notification));
                    broadcastUpdate(BleServiceConst.ACTION_GATT_CONNECTED);
                    bluetoothGatt.discoverServices();
                }
                else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                    //bleServiceNotification.hideConnectNotification();
                    broadcastUpdate(BleServiceConst.ACTION_GATT_DISCONNECTED);
                    stopSelf();
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status)
            {
                List<BluetoothGattCharacteristic> characteristics;

                if(status == BluetoothGatt.GATT_SUCCESS){
                    broadcastUpdate(BleServiceConst.ACTION_GATT_SERVICES_DISCOVERED);

                    // Loop through discovered services
                    List<BluetoothGattService> services = bluetoothGatt.getServices();
                    for (BluetoothGattService service : services) {
                        Log.d("Service UUID", service.getUuid().toString());
                        System.out.println("MSB= " + service.getUuid().getMostSignificantBits());
                        System.out.println("LSB= " + service.getUuid().getLeastSignificantBits());

                        characteristics = service.getCharacteristics();

                        // Loop through characteristics of discovered services
                        for(BluetoothGattCharacteristic chara : characteristics){
                            gatt.readCharacteristic(chara);
                        }
                    }
                }
                else{ // Status not success
                    broadcastUpdate(BleServiceConst.ACTION_GATT_SERVICES_DISCOVERED_FAILED);
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
            {
                if(status == BluetoothGatt.GATT_SUCCESS){

                    System.out.println("CHARA UUID = " + characteristic.getUuid());
                    System.out.print("CHARA VALUES = ");
                    byte[] valuesRead = characteristic.getValue();
                    for(byte b : valuesRead){
                        System.out.print(b);
                    }
                    System.out.println("");

                    System.out.println(valuesRead);
                    //EnvData envData = charaParser.parseEnvDataChara(valuesRead);
                    //System.out.println(envData);
                    //broadcastUpdate(BleServiceConst.ACTION_ENV_DATA_AVAILABLE, envData);
                    broadcastUpdate(BleServiceConst.ACTION_ENV_DATA_AVAILABLE, valuesRead);

                    // Enable characteristic on change notification
                    //gatt.setCharacteristicNotification(characteristic, true);
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
            {
                System.out.println("CHARA CHANGED UUID = " + characteristic.getUuid());
            }
        };
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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        btDevice = intent.getParcelableExtra(BleServiceConst.BLE_DEVICE);
        bluetoothGatt = btDevice.getBluetoothDevice().connectGatt(this, false, bluetoothGattCallback);
        isServiceStarted = true;
        //bleServiceNotification = new BleServiceNotification(this, btDevice);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy()
    {
        isServiceStarted = false;
        bluetoothGatt.disconnect();
        //bleServiceNotification.hideConnectNotification();

        super.onDestroy();
    }

    private void broadcastUpdate(final String event)
    {
        final Intent intent = new Intent(event);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String event, byte[] envData)
    {
        final Intent intent = new Intent(event);
        intent.putExtra(BleServiceConst.ENV_DATA, envData);
        sendBroadcast(intent);
    }
    /*private void broadcastUpdate(final String event, EnvData envData)
    {
        final Intent intent = new Intent(event);
        intent.putExtra(BleServiceConst.ENV_DATA, envData);
        sendBroadcast(intent);
    }*/
}
