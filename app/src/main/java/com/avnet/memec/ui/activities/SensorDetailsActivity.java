package com.avnet.memec.ui.activities;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avnet.memec.R;
import com.avnet.memec.ui.model.SensorData;
import com.avnet.memec.ui.services.BleServiceAdv;
import com.avnet.memec.ui.services.BleServiceConst;
import com.avnet.memec.ui.services.BleServiceGatt;
import com.avnet.memec.ui.util.BtDevice;
import com.avnet.memec.ui.util.MySingleton;

import java.util.ArrayList;
import java.util.Arrays;


public class SensorDetailsActivity extends ViewSensorsActivity
{
    //private UiEnvDataActivityHelper uiHelper;
    private BroadcastReceiver bleBroadcastReceiver;
    private IntentFilter receiverIntentFilter;
    private BtDevice btDevice;
    private LayoutInflater mInflater;
    private ViewHolder holder = null;

    private static final int AVPROTOCOL_READING_TYPE_ACCELERATION = 1;
    private static final int AVPROTOCOL_READING_TYPE_GYROSCOPE = 2;
    private static final int AVPROTOCOL_READING_TYPE_MAGNETIC_FIELD = 3;
    private static final int AVPROTOCOL_READING_TYPE_LIGHT = 4;
    private static final int AVPROTOCOL_READING_TYPE_TEMPERATURE = 5;
    private static final int AVPROTOCOL_READING_TYPE_NOISE = 6;
    private static final int AVPROTOCOL_READING_TYPE_BATTERY_VOLTAGE = 7;
    private static final int AVPROTOCOL_READING_TYPE_RSSI = 8;
    private static final int AVPROTOCOL_READING_TYPE_CAP_SENSE = 9;
    private static final int AVPROTOCOL_READING_TYPE_HUMIDITY = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d("EnvDataActivity", "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_details);

        initializeBleBroadcastReceiver();

        btDevice = MySingleton.getInstance().btSensorSelected;
        Log.d("btDeviceName",btDevice.getBtName());

        holder = new ViewHolder();
        holder.textView = (TextView) findViewById(R.id.object_id);
        holder.textView.setText(btDevice.getBtName());
        holder.sensorsHolder = (LinearLayout) findViewById(R.id.sensors_holder);

        if(!isBleServiceRunning()){
            startBleService();
        }
    }

    private void addSensors(ArrayList<SensorData> list, LinearLayout holder){
        ArrayList<LinearLayout> sensor = new ArrayList<LinearLayout>();
        ArrayList<TextView> sensorName = new ArrayList<TextView>();
        ArrayList<ImageView> sensorImage = new ArrayList<ImageView>();
        ArrayList<TextView> x = new ArrayList<TextView>();
        ArrayList<TextView> y = new ArrayList<TextView>();
        ArrayList<TextView> z = new ArrayList<TextView>();
        for(int i = 1; i <= list.size(); i++){
            sensor.add((LinearLayout) findViewById(getResources().getIdentifier("sensor" + i, "id", this.getPackageName())));
            sensorName.add((TextView) findViewById(getResources().getIdentifier("sensor_name" + i, "id", this.getPackageName())));
            sensorImage.add((ImageView) findViewById(getResources().getIdentifier("sensor_image" + i, "id", this.getPackageName())));
            x.add((TextView) findViewById(getResources().getIdentifier("x" + i, "id", this.getPackageName())));
            y.add((TextView) findViewById(getResources().getIdentifier("y" + i, "id", this.getPackageName())));
            z.add((TextView) findViewById(getResources().getIdentifier("z" + i, "id", this.getPackageName())));
        }
        SensorData sensorData;
        mInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int i = 0;
        while(i < list.size()){
            sensorData = list.get(i);
            sensor.get(i).setVisibility(View.VISIBLE);
            sensorName.get(i).setText(sensorData.getSensorType());
            sensorImage.get(i).setImageDrawable(getDrawable(sensorData.getSensorIcon()));
            x.get(i).setText(sensorData.getParam1());
            if(sensorData.getParam2()!=null){
                y.get(i).setText(sensorData.getParam2());
            }else{
                y.get(i).setVisibility(View.GONE);
            }
            if(sensorData.getParam3()!=null){
                z.get(i).setText(sensorData.getParam3());
            }else{
                z.get(i).setVisibility(View.GONE);
            }
            i++;
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if(!isBleServiceRunning()){
            finish();
        }

        registerBleBroadcastReceiver();
    }

    @Override
    protected void onPause()
    {
        unregisterBleBroadcastReceiver();

        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        stopBleService();
        super.onDestroy();
    }

    private void initializeBleBroadcastReceiver()
    {
        // Set filter for service broadcast messages
        receiverIntentFilter = new IntentFilter();
        receiverIntentFilter.addAction(BleServiceConst.ACTION_GATT_CONNECTED);
        receiverIntentFilter.addAction(BleServiceConst.ACTION_GATT_DISCONNECTED);
        receiverIntentFilter.addAction(BleServiceConst.ACTION_GATT_SERVICES_DISCOVERED);
        receiverIntentFilter.addAction(BleServiceConst.ACTION_GATT_SERVICES_DISCOVERED_FAILED);
        receiverIntentFilter.addAction(BleServiceConst.ACTION_ENV_DATA_AVAILABLE);
        receiverIntentFilter.addAction(BleServiceConst.SERVICE_STOPPED);

        bleBroadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                switch(intent.getAction()){
                    case BleServiceConst.ACTION_GATT_CONNECTED: {
                        onActionGattConnect(context, intent);
                        break;
                    }
                    case BleServiceConst.ACTION_GATT_DISCONNECTED:{
                        onActionGattDisconnect(context, intent);
                        break;
                    }
                    case BleServiceConst.ACTION_GATT_SERVICES_DISCOVERED:{
                        onActionGattServicesDiscovered(context, intent);
                        break;
                    }
                    case BleServiceConst.ACTION_GATT_SERVICES_DISCOVERED_FAILED:{
                        onActionGattServicesDiscoveredFailed(context, intent);
                        break;
                    }
                    case BleServiceConst.ACTION_ENV_DATA_AVAILABLE:{
                        onActionGattDataAvailable(context, intent);
                        break;
                    }
                    case BleServiceConst.SERVICE_STOPPED:{
                        onServiceStopped(context, intent);
                        break;
                    }
                }
            }
        };
    }

    private void onActionGattConnect(Context context, Intent intent)
    {
        System.out.println(BleServiceConst.ACTION_GATT_CONNECTED);
    }

    private void onActionGattDisconnect(Context context, Intent intent)
    {
        System.out.println(BleServiceConst.ACTION_GATT_DISCONNECTED);
        Toast.makeText(context, btDevice.getBluetoothDevice().getName() +
                " disconnected", Toast.LENGTH_LONG).show();
        stopBleService();
        finish();
    }

    private void onActionGattServicesDiscovered(Context context, Intent intent)
    {
        System.out.println(BleServiceConst.ACTION_GATT_SERVICES_DISCOVERED);
    }

    private void onActionGattServicesDiscoveredFailed(Context context, Intent intent)
    {
        System.out.println(BleServiceConst.ACTION_GATT_SERVICES_DISCOVERED_FAILED);
        Toast.makeText(context, "Failed discovering GATT services", Toast.LENGTH_LONG).show();
        stopBleService();
        finish();
    }

    private void onActionGattDataAvailable(Context context, Intent intent)
    {
        Log.d("GattDataAvailable","OnAction");
        System.out.println(BleServiceConst.ACTION_ENV_DATA_AVAILABLE);
        byte[] data = intent.getByteArrayExtra(BleServiceConst.ENV_DATA);
        addSensors(readManufacturerSpecificData(data,7), holder.sensorsHolder);
    }

    private void onServiceStopped(Context context, Intent intent)
    {
        System.out.println(BleServiceConst.SERVICE_STOPPED);
        finish();
    }

    private void registerBleBroadcastReceiver()
    {
        registerReceiver(bleBroadcastReceiver, receiverIntentFilter);
    }

    private void unregisterBleBroadcastReceiver()
    {
        unregisterReceiver(bleBroadcastReceiver);
    }

    private void startBleService()
    {
        Log.d("startService",btDevice.getBtName());
        startService(getIntentForBleServiceHandling());
    }

    private void stopBleService()
    {
        stopService(getIntentForBleServiceHandling());
    }

    private Intent getIntentForBleServiceHandling()
    {
        Intent intent = new Intent(this, btDevice.isAdvMsgServiceCompatible() ? BleServiceAdv.class : BleServiceGatt.class);
        intent.putExtra(BleServiceConst.BLE_DEVICE, btDevice);
        return intent;
    }

    private boolean isBleServiceRunning()
    {
        // Check using OS running service information
        // N.B.: to handle also process kill by android system
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (BleServiceGatt.class.getName().equals(service.service.getClassName()) ||
                    BleServiceAdv.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<SensorData> readManufacturerSpecificData(byte[] raw_data, int ind){

        ArrayList<SensorData> sensors = new ArrayList<SensorData>();

        SensorData sensor;
        Log.d("Raw Data", Arrays.toString(raw_data));
        int index = ind;
        int length;
        if(index==7){
            length = 27;
        }else{
            length = raw_data.length;
        }
        if(raw_data!=null) {
            while (index < length) {
                Log.d("Raw Data Kitkat-Lolipop", String.valueOf(raw_data[index] & 0xF));
                int reading_type = ((raw_data[index] >> 4) & 0xF);
                int scaling_value = (raw_data[index] & 0xF) - 8;
                int scaling_factor = (int) power_of_two(scaling_value); // floating point not supported, so need to work out separate fields for magnitudes;
                //Integer.valueOf(String.valueOf(scaling_factor));
                //ub_ustdio_printf("Found reading type: %u, scaling factor: %u, index: %u\n", reading_type, scaling_factor, index);
                double sensor_data;
                Log.d("Initial ", reading_type + " " + scaling_factor + " " + index + " " + (raw_data[index] & 0xF));
                int x, y, z;

                switch (reading_type) {
                    case AVPROTOCOL_READING_TYPE_ACCELERATION:
                        x = raw_data[++index] * scaling_factor;
                        y = raw_data[++index] * scaling_factor;
                        z = raw_data[++index] * scaling_factor;
                        sensor = new SensorData();
                        sensor.setSensorType("Acceleration");
                        sensor.setSensorIcon("acceleration");
                        sensor.setParam1(x + " g");
                        sensor.setParam2(y + " g");
                        sensor.setParam3(z + " g");
                        sensors.add(sensor);
                        Log.d("Sensor Type", "AVPROTOCOL_READING_TYPE_ACCELERATION " + x + " " + y + " " + z);
                        ++index;
                        continue;
                    case AVPROTOCOL_READING_TYPE_GYROSCOPE:
                        x = raw_data[++index] * scaling_factor;
                        y = raw_data[++index] * scaling_factor;
                        z = raw_data[++index] * scaling_factor;
                        sensor = new SensorData();
                        sensor.setSensorType("Gyroscope");
                        sensor.setSensorIcon("gyroscope");
                        sensor.setParam1(x + " deg/s");
                        sensor.setParam2(y + " deg/s");
                        sensor.setParam3(z + " deg/s");
                        sensors.add(sensor);
                        Log.d("Sensor Type", "AVPROTOCOL_READING_TYPE_GYROSCOPE " + x + " " + y + " " + z);
                        ++index;
                        continue;
                    case AVPROTOCOL_READING_TYPE_MAGNETIC_FIELD:
                        x = raw_data[++index] * scaling_factor;
                        y = raw_data[++index] * scaling_factor;
                        z = raw_data[++index] * scaling_factor;
                        sensor = new SensorData();
                        sensor.setSensorType("Magnetic Field");
                        sensor.setSensorIcon("magnetic");
                        sensor.setParam1(x + " 10^-6T");
                        sensor.setParam2(y + " 10^-6T");
                        sensor.setParam3(z + " 10^-6T");
                        sensors.add(sensor);
                        Log.d("Sensor Type", "AVPROTOCOL_READING_TYPE_MAGNETIC_FIELD " + x + " " + y + " " + z);
                        ++index;
                        continue;
                    case AVPROTOCOL_READING_TYPE_LIGHT:
                        x = raw_data[++index] * scaling_factor;
                        y = raw_data[++index] * scaling_factor;
                        z = raw_data[++index] * scaling_factor;
                        sensor = new SensorData();
                        sensor.setSensorType("Light");
                        sensor.setSensorIcon("light");
                        sensor.setParam1(x + " klux");
                        sensor.setParam2(y + " mW/cm2");
                        sensor.setParam3(z + " mW/cm2");
                        sensors.add(sensor);
                        Log.d("Sensor Type", "AVPROTOCOL_READING_TYPE_LIGHT (ambient infrared uv) " + x + " " + y + " " + z);
                        ++index;
                        continue;
                    case AVPROTOCOL_READING_TYPE_TEMPERATURE:
                        x = raw_data[++index] * scaling_factor;
                        sensor = new SensorData();
                        sensor.setSensorType("Temperature");
                        sensor.setSensorIcon("temperature");
                        sensor.setParam1(x + " Celsius");
                        sensor.setParam2(null);
                        sensor.setParam3(null);
                        sensors.add(sensor);
                        Log.d("Sensor Type", "AVPROTOCOL_READING_TYPE_TEMPERATURE " + x);
                        ++index;
                        continue;
                    case AVPROTOCOL_READING_TYPE_NOISE:
                        x = raw_data[++index] * scaling_factor;
                        sensor = new SensorData();
                        sensor.setSensorType("Noise");
                        sensor.setSensorIcon("noise");
                        sensor.setParam1(x + " dB");
                        sensor.setParam2(null);
                        sensor.setParam3(null);
                        sensors.add(sensor);
                        Log.d("Sensor Type", "AVPROTOCOL_READING_TYPE_NOISE " + x);
                        ++index;
                        continue;
                    case AVPROTOCOL_READING_TYPE_BATTERY_VOLTAGE:
                        x = raw_data[++index] * scaling_factor;
                        sensor = new SensorData();
                        sensor.setSensorType("Battery Voltage");
                        sensor.setSensorIcon("battery");
                        sensor.setParam1(x + " V");
                        sensor.setParam2(null);
                        sensor.setParam3(null);
                        sensors.add(sensor);
                        Log.d("Sensor Type", "AVPROTOCOL_READING_TYPE_BATTERY_VOLTAGE " + x);
                        //++index; //values
                        ++index;
                        continue;
                    case AVPROTOCOL_READING_TYPE_RSSI:
                        x = raw_data[++index] * scaling_factor;
                        sensor = new SensorData();
                        sensor.setSensorType("RSSI");
                        sensor.setSensorIcon("radio");
                        sensor.setParam1(x + " dBm");
                        sensor.setParam2(null);
                        sensor.setParam3(null);
                        sensors.add(sensor);
                        Log.d("Sensor Type", "AVPROTOCOL_READING_TYPE_RSSI " + x);
                        ++index;
                        continue;
                    case AVPROTOCOL_READING_TYPE_CAP_SENSE:
                        x = raw_data[++index] * scaling_factor;
                        y = raw_data[++index] * scaling_factor;
                        sensor = new SensorData();
                        sensor.setSensorType("Cap Sense");
                        sensor.setSensorIcon("cap");
                        sensor.setParam1(x + " %");
                        sensor.setParam2(y + " %");
                        sensor.setParam3(null);
                        sensors.add(sensor);
                        Log.d("Sensor Type", "AVPROTOCOL_READING_TYPE_CAP_SENSE " + x + " " + y);
                        ++index;
                        continue;
                    case AVPROTOCOL_READING_TYPE_HUMIDITY:
                        x = raw_data[++index] * scaling_factor;
                        sensor = new SensorData();
                        sensor.setSensorType("Humidity");
                        sensor.setSensorIcon("humidity");
                        sensor.setParam1(x + " %");
                        sensor.setParam2(null);
                        sensor.setParam3(null);
                        sensors.add(sensor);
                        Log.d("Sensor Type", "AVPROTOCOL_READING_TYPE_HUMIDITY " + x);
                        ++index;
                        continue;

                    default:
                        ++index;
                        Log.d("Sensor Type", "Default");
                }
            }
        }
        return sensors;
    }

    private double power_of_two(int scaling_value) {
        return Math.pow(2,scaling_value);
    }

    public Drawable getDrawable(String name) {
        int resourceId = getResources().getIdentifier(name, "drawable", getPackageName());
        return getResources().getDrawable(resourceId);
    }

    public static class ViewHolder {
        public TextView textView;
        public LinearLayout sensorsHolder;
    }
}
