package com.avnet.memec.ui.activities;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ToggleButton;

import com.avnet.memec.R;
import com.avnet.memec.ui.model.SensorData;
import com.avnet.memec.ui.model.SensorObject;
import com.avnet.memec.ui.util.BtDevice;
import com.avnet.memec.ui.util.MemecApplication;
import com.avnet.memec.ui.util.MySingleton;
import com.avnet.memec.ui.util.WiFiApManager;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;


public class BaseActivity extends AppCompatActivity {

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
    protected BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 10;
    private static final long SCAN_PERIOD = 3000;
    private boolean mScanning = true;
    private Handler mHandler;
    private static final String TAG = "BaseActivity";
    protected BluetoothGatt mGatt;
    protected BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    protected ArrayList<BluetoothDevice> btDeviceList;
    protected BluetoothAdapter ba;
    public BluetoothDevice connectedDevice;
    String[] writeString;
    String selectedType;
    private LeScanCallback mLeScanCallback;
    private LeScanCallback mLeScanCallbackAdd;
    private ScanCallback mScanCallback;
    private ScanCallback mScanCallbackAdd;
    public MemecApplication app;
    public BluetoothManager bluetoothManager;
    public Boolean refreshFlag;
    public Boolean stopHandler = true;
    public Boolean stopViewSensors = true;
    public Boolean disconnectFlow = false;
    public WiFiApManager wifiApManager;

    List<BluetoothGattCharacteristic> readCharacteristics, writeCharacteristics;

    protected List<UUID> uuids;
    protected byte[] MfgData;
    private int i = 0;

    protected List<BluetoothGattCharacteristic> storedChars = new ArrayList<>();
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        mHandler = new Handler();

        // Get the application instance
        app = (MemecApplication)getApplication();
        if(MySingleton.getInstance().myGattList==null){
            MySingleton.getInstance().myGattList = new ArrayList<BluetoothGatt>();
            MySingleton.getInstance().charValue = new String[6];
        }
        wifiApManager = new WiFiApManager(getApplicationContext());

        bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        ba = BluetoothAdapter.getDefaultAdapter();

        //Check BT
        if(!MySingleton.getInstance().bthsChecked) {
            MySingleton.getInstance().bthsChecked = true;
            if (Build.VERSION.SDK_INT < 21) {
                checkBT(mBluetoothAdapter);
            } else {
                checkBT(ba);
            }
            checkHS();
        }
        if(Build.VERSION.SDK_INT < 21) {
            initScanCallBackFor18();
            Log.d("Version","<21");
        }else{
            initScanCallBackFor21();
            Log.d("Version", "21>");
        }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_base, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void checkHS(){
        boolean apSupported = (wifiApManager.isApModeSupported());
        if(apSupported) {
            int wState = wifiApManager.getWiFiApState();
            Log.d("wifiAPState", " " + wState);
            if (wState!=13){
                Log.d("wifiAPState", "disabling");
                //wifiApManager.configApState(this);
                Log.d("wifiState", WifiManager.WIFI_STATE_ENABLED+" "+wifiApManager.getWiFiState());
                if (wifiApManager.getWiFiState() == WifiManager.WIFI_STATE_ENABLED) {
                    Log.d("wifiState", "disabling");
                    wifiApManager.setWiFiState(false);
                }
                //Hotspot Dialog
                final Dialog dialog = new Dialog(this, R.style.Theme_Dialog);
                dialog.setContentView(R.layout.dialog_layout_hotspot);
                final ToggleButton toggleButton = (ToggleButton) dialog.findViewById(R.id.toggleButtonHS);
                final FrameLayout toggleButtonWrap = (FrameLayout) dialog.findViewById(R.id.toggleButtonHSWrap);
                toggleButtonWrap.setOnTouchListener(new View.OnTouchListener() {

                    private float initialTouchX;
                    private float initialTouchY;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                initialTouchX = event.getRawX();
                                initialTouchY = event.getRawY();
                                return true;
                            case MotionEvent.ACTION_UP:
                                if ((Math.abs(initialTouchX - event.getRawX()) < 5) && (Math.abs(initialTouchY - event.getRawY()) < 5)) {
                                    v.performClick();
                                }
                                return true;
                            case MotionEvent.ACTION_MOVE:
                                float distX = event.getRawX() - initialTouchX;
                                float distY = event.getRawY() - initialTouchY;
                                if (distX < -150 && Math.abs(distX)>Math.abs(distY)) {
                                    Log.d("Swipe", "User swiped left");

                                } else if (distX > 150 && Math.abs(distX)>Math.abs(distY)) {
                                    Log.d("Swipe", "User swiped right");
                                    //Enable Hotspot
                                    toggleButton.setChecked(true);
                                    wifiApManager.setWifiApEnabled(true);
                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            dialog.dismiss();
                                        }
                                    }, 500);
                                }
                                return true;
                        }
                        return false;
                    }

                });
                toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
                        //Enable Hotspot
                        wifiApManager.setWifiApEnabled(true);
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                            }
                        }, 500);
                    }
                });
                Button close_dialog = (Button) dialog.findViewById(R.id.close_dialog);
                close_dialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        }
    }

    protected boolean checkBT(final BluetoothAdapter ba) {
        if(MySingleton.getInstance().connectedDevice==null) {
            if (!ba.isEnabled()) {
                //Bluetooth Dialog
                final Dialog dialog = new Dialog(this, R.style.Theme_Dialog);
                dialog.setContentView(R.layout.dialog_layout_bluetooth);
                final ToggleButton toggleButton = (ToggleButton) dialog.findViewById(R.id.toggleButtonBT);
                final FrameLayout toggleButtonWrap = (FrameLayout) dialog.findViewById(R.id.toggleButtonBTWrap);
                toggleButtonWrap.setOnTouchListener(new View.OnTouchListener() {

                    private float initialTouchX;
                    private float initialTouchY;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                initialTouchX = event.getRawX();
                                initialTouchY = event.getRawY();
                                return true;
                            case MotionEvent.ACTION_UP:
                                if ((Math.abs(initialTouchX - event.getRawX()) < 5) && (Math.abs(initialTouchY - event.getRawY()) < 5)) {
                                    v.performClick();
                                }
                                return true;
                            case MotionEvent.ACTION_MOVE:
                                float distX = event.getRawX() - initialTouchX;
                                float distY = event.getRawY() - initialTouchY;
                                if (distX < -150 && Math.abs(distX)>Math.abs(distY)) {
                                    Log.d("Swipe", "User swiped left");

                                } else if (distX > 150 && Math.abs(distX)>Math.abs(distY)) {
                                    Log.d("Swipe", "User swiped right");
                                    //Enable Bluetooth
                                    toggleButton.setChecked(true);
                                    Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                    startActivityForResult(turnOn, 0);
                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            dialog.dismiss();
                                        }
                                    }, 500);
                                }
                                return true;
                        }
                        return false;
                    }

                });
                toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
                        //Enable Bluetooth
                        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(turnOn, 0);
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                            }
                        }, 500);
                    }
                });
                /*Button close_dialog = (Button) dialog.findViewById(R.id.close_dialog);
                close_dialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });*/
                dialog.show();
                return false;
            }
        } else{
            if (!ba.isEnabled()) {
                disconnect();
                closeGatt();
                onBackPressed();
                return false;
            }
        }
        return true;
    }

    protected void scanLeDevice(final boolean enable) {

        filters = new ArrayList<ScanFilter>();
        btDeviceList = new ArrayList<BluetoothDevice>();
        MySingleton.getInstance().btDeviceHash = new HashSet<BluetoothDevice>();
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    if(Build.VERSION.SDK_INT < 21){
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        mLEScanner.stopScan(mScanCallback);
                    }

                    btDeviceList = new ArrayList<BluetoothDevice> (MySingleton.getInstance().btDeviceHash);
                    Intent intent = new Intent(BaseActivity.this, GatewayListActivity.class);
                    intent.putExtra("btDeviceList", btDeviceList);
                    startActivity(intent);
                    finish();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            if(Build.VERSION.SDK_INT < 21){
                /*UUID uuid = UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50000");
                UUID[] uuidArray = new UUID[]{uuid};
                mBluetoothAdapter.startLeScan(uuidArray, mLeScanCallback);*/
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                mLEScanner.startScan(filters, settings, mScanCallback);
            }

            // mLEScanner.startScan(mScanCallback);
        } else {
            mScanning = false;
            if(Build.VERSION.SDK_INT < 21){
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }
        }
    }

    protected void scanLeDeviceForResult(final boolean enable){

        btDeviceList = new ArrayList<BluetoothDevice>();
        MySingleton.getInstance().btDeviceHash = new HashSet<BluetoothDevice>();
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    if(Build.VERSION.SDK_INT < 21){
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        mLEScanner.stopScan(mScanCallback);
                    }
                }
            }, 3000);

            mScanning = true;
            if(Build.VERSION.SDK_INT < 21){
                /*UUID uuid = UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50000");
                UUID[] uuidArray = new UUID[]{uuid};
                mBluetoothAdapter.startLeScan(uuidArray, mLeScanCallback);*/
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                mLEScanner.startScan(filters, settings, mScanCallback);
            }
            // mLEScanner.startScan(mScanCallback);
        } else {
            mScanning = false;
            if(Build.VERSION.SDK_INT < 21){
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }
        }
    }
    public static boolean hasMyService(byte[] scanRecord) {

        // UUID we want to filter by (without hyphens)
        final String myServiceID = "0000D5A502009085E5115E520A01F840";

        try{
            byte[] temp = scanRecord;
            Collections.reverse(Arrays.asList(temp));
            return bytesToHex(temp).contains(myServiceID);

        } catch (Exception e){
            return false;
        }

    }
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    protected void scanLeDeviceForAdvertisements(final boolean enable){

        btDeviceList = new ArrayList<BluetoothDevice>();
        MySingleton.getInstance().btDeviceHash = new HashSet<BluetoothDevice>();
        MySingleton.getInstance().sensorMap = new HashMap<String, SensorObject>();
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    if(Build.VERSION.SDK_INT < 21){
                        mBluetoothAdapter.stopLeScan(mLeScanCallbackAdd);
                    } else {
                        mLEScanner.stopScan(mScanCallbackAdd);
                    }
                }
            }, 3000);

            mScanning = true;
            if(Build.VERSION.SDK_INT < 21){
                mBluetoothAdapter.startLeScan(mLeScanCallbackAdd);
            } else {
                mLEScanner.startScan(filters, settings, mScanCallbackAdd);
            }
            // mLEScanner.startScan(mScanCallback);
        } else {
            mScanning = false;
            if(Build.VERSION.SDK_INT < 21){
                mBluetoothAdapter.stopLeScan(mLeScanCallbackAdd);
            } else {
                mLEScanner.stopScan(mScanCallbackAdd);
            }
        }
    }

    protected ArrayList<BluetoothDevice> getBTDeviceList(){
        btDeviceList = new ArrayList<BluetoothDevice>(MySingleton.getInstance().btDeviceHash);
        return btDeviceList;
    }
    protected ArrayList<SensorObject> getSensorList(){
        //HashSet<SensorObject> sensorHash = new HashSet<SensorObject>(sensorMap.values());
        ArrayList<SensorObject> sensorList = new ArrayList<SensorObject>(MySingleton.getInstance().sensorMap.values());
        return sensorList;
    }

    private void initScanCallBackFor18(){
        mLeScanCallback =
                new LeScanCallback() {
                    @Override
                    public void onLeScan(final BluetoothDevice device, int rssi,
                                         final byte[] scanRecord) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("onLeScan", device.toString() + "/ " + Arrays.toString(scanRecord));
                                List<UUID> uuids = new ArrayList<UUID>();
                                //uuids = parseUuids(scanRecord);
                                //UUID uuid = UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50000");
                                //UUID uuid = uuids.get(0);
                                //Log.d("onLeScan UUID", uuid.toString());
                                byte[] temp = scanRecord;
                                Collections.reverse(Arrays.asList(temp));
                                Log.d("onLeScan ScanRecord", bytesToHex(temp));

                                //printScanRecord(scanRecord);
                                //if(device.toString().contains("40f8010a-525e-11e5-8590-0002a5d50000")) {

                                if(hasMyService(scanRecord)) {
                                    BluetoothDevice btDevice = device;
                                    MySingleton.getInstance().btDeviceHash.add(btDevice);
                                }
                                //}
                                //connectToDevice(device);
                            }
                        });
                    }
                };
        mLeScanCallbackAdd =
                new LeScanCallback() {
                    @Override
                    public void onLeScan(final BluetoothDevice device, int rssi,
                                         final byte[] scanRecord) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("onLeScanAdd", device.toString());
                                //printScanRecord(scanRecord);
                                //if(readManufacturerSpecificData(scanRecord)!=null) {
                                    if(scanRecord!=null) {
                                        int read1 = (scanRecord[5] & 0xFF);
                                        int read2 = (scanRecord[6] & 0xFF);
                                        Log.d("readSpecificData",read1+" "+read2);
                                        if ((read1 == 25 && read2 == 16) || (read1 == 16 && read2 == 25)) {
                                            BluetoothDevice btDevice = device;
                                            MySingleton.getInstance().btDeviceHash.add(btDevice);
                                            //BtDevice bt = new BtDevice(device, true, "VT Sensor - ");
                                            SensorObject sensorObject = new SensorObject();
                                            sensorObject.setDeviceName("VT Sensor - ");
                                            sensorObject.setDeviceId(device.getAddress());
                                            Log.d("Version <21","7");
                                            sensorObject.setSensors(readManufacturerSpecificData(scanRecord, 7));
                                            MySingleton.getInstance().sensorMap.put(device.getAddress(), sensorObject);

                                            /*//TODO To test
                                            readSpecificDataKitkat(scanRecord);*/
                                        }
                                    }
                                //}
                                //connectToDevice(device);
                            }
                        });
                    }
                };
    }
    @TargetApi(21)
    private void initScanCallBackFor21(){

        mLEScanner = ba.getBluetoothLeScanner();
        settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build();
        mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                Log.i("callbackType", String.valueOf(callbackType));
                Log.i("result", result.toString());
                if(result.toString().contains("40f8010a-525e-11e5-8590-0002a5d50000")) {
                    BluetoothDevice btDevice = result.getDevice();
//            Log.d("BTDevice", btDevice.getName());
                    MySingleton.getInstance().btDeviceHash.add(btDevice);
                }
        /*if(!btDeviceList.contains(btDevice)) {
            btDeviceList.add(btDevice);
        }*/
                //connectToDevice(btDevice);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                for (ScanResult sr : results) {
                    Log.i("ScanResult - Results", sr.toString());
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.e("Scan Failed", "Error Code: " + errorCode);
            }
        };

        mScanCallbackAdd = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                //printScanRecord(result.toString().getBytes());

                ScanRecord rec = result.getScanRecord();
                Log.d("Rec", rec.toString());

                SparseArray<byte[]> mf = rec.getManufacturerSpecificData();
                byte[] scanResult = sparseToByteArray(mf);
                if(scanResult!=null) {
                    int read1 = (scanResult[0] & 0xFF);
                    int read2 = (scanResult[1] & 0xFF);
                    Log.d("readSpecificData",read1+" "+read2);
                    if ((read1 == 25 && read2 == 16) || (read1 == 16 && read2 == 25)) {
                        BluetoothDevice btDevice = result.getDevice();
//            Log.d("BTDevice", btDevice.getName());
                        MySingleton.getInstance().btDeviceHash.add(btDevice);
                        SensorObject sensorObject = new SensorObject();
                        sensorObject.setDeviceName("VT Sensor - ");
                        sensorObject.setDeviceId(result.getDevice().getAddress());
                        Log.d("Version 21>", "2");
                        sensorObject.setSensors(readManufacturerSpecificData(scanResult, 2));
                        MySingleton.getInstance().sensorMap.put(result.getDevice().getAddress(), sensorObject);

                        /*//TODO To test
                        readSpecificDataKitkat(scanResult);
                        Log.d("Complete data","");
                        readSpecificDataKitkat(rec.getBytes());*/
                    }
                }

        /*if(!btDeviceList.contains(btDevice)) {
            btDeviceList.add(btDevice);
        }*/
                //connectToDevice(btDevice);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                for (ScanResult sr : results) {
                    //printScanRecord(sr.toString().getBytes());
                    ScanRecord rec = sr.getScanRecord();
                    Log.d("Rec",rec.toString());

                    SparseArray<byte[]> mf = rec.getManufacturerSpecificData();
                    byte[] scanResult = sparseToByteArray(mf);
                    if(scanResult!=null) {
                        if (((scanResult[0] >> 4) & 0xF) > 0) {
                            SensorObject sensorObject = new SensorObject();
                            sensorObject.setDeviceName(sr.getDevice().getName());
                            sensorObject.setDeviceId(sr.getDevice().getAddress());
                            Log.d("Version 21>", "2");
                            sensorObject.setSensors(readManufacturerSpecificData(scanResult, 2));
                            MySingleton.getInstance().sensorMap.put(sr.getDevice().getAddress(), sensorObject);

                            /*//TODO To test
                            readSpecificDataKitkat(rec.getBytes());*/
                        }
                    }
                    //parseAdvertisementPacket(sr.toString().getBytes());
                    //Log.i("ScanResult - Results", sr.toString());
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.e("Scan Failed", "Error Code: " + errorCode);
            }
        };
    }

    public String sparseToString(SparseArray<byte[]> array) {
        if (array == null) {
            return "null";
        }
        if (array.size() == 0) {
            return "{}";
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append('{');
        for (int i = 0; i < array.size(); ++i) {
            buffer.append(array.keyAt(i)).append("=").append(Arrays.toString(array.valueAt(i)));
        }
        buffer.append('}');
        return buffer.toString();
    }
    public byte[] sparseToByteArray(SparseArray<byte[]> array) {
        byte[] ad = null;
        if (array == null) {
            return ad;
        }
        if (array.size() == 0) {
            return ad;
        }
        for (int i = 0; i < array.size(); ++i) {
            int key = array.keyAt(i);
            byte[] value = array.valueAt(i);
            int length = array.valueAt(i).length+2;

            byte[] bytes = new byte[2];
            ByteBuffer buf = ByteBuffer.wrap(bytes);
            buf.putChar((char) key);
            ad = new byte[length];
            ad[0] = buf.get(0);
            ad[1] = buf.get(1);
            for(int k = 2, j = 0; k < length; k++, j++){
                ad[k] = value[j];
            }
        }
        return ad;
    }

    public void connectToDevice(BluetoothDevice device) {
        refreshFlag = true;
        MySingleton.getInstance().connectedDevice = device;
        if (mGatt == null) {
//                if(device.getName() == "Visible Things"){
//
//                }
            mGatt = device.connectGatt(this, false, gattCallback);
            scanLeDevice(false);// will stop after first device detection
        }
    }



    public final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        List<BluetoothGattCharacteristic> readChars = new ArrayList<>();
        boolean flag;

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    MySingleton.getInstance().myGattList.add(gatt);
                    flag = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (!flag) {
                                        Intent intent = new Intent(BaseActivity.this, GatewayListActivity.class);
                                        intent.putExtra("FailureFlow", true);
                                        intent.putExtra("btDeviceList", btDeviceList);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            }, 3000);
                        }
                    });
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED "+stopHandler+"/"+disconnectFlow+"/"+MySingleton.getInstance().disconnectFlow);
                    disconnect();
                    closeGatt();
                    //if(MySingleton.getInstance().myGattList.contains(gatt)) {
                        if (!stopHandler && disconnectFlow) {
                            if (Build.VERSION.SDK_INT < 21) {
                                if (!checkBT(mBluetoothAdapter)) {
                                    onBackPressed();
                                }
                            } else {
                                if (!checkBT(ba)) {
                                    onBackPressed();
                                }
                            }
                            Intent intent = new Intent(BaseActivity.this, GatewayListActivity.class);
                            intent.putExtra("FailureFlow", true);
                            intent.putExtra("btDeviceList", btDeviceList);
                            startActivity(intent);
                            finish();
                        } else if (!stopHandler && !disconnectFlow) {
                            Intent intent = new Intent(BaseActivity.this, GatewayListActivity.class);
                            intent.putExtra("ConnectionSettingsFlow", true);
                            intent.putExtra("btDeviceList", btDeviceList);
                            startActivity(intent);
                            finish();
                        }else if (stopHandler && !disconnectFlow && MySingleton.getInstance().disconnectFlow) {
                            Intent intent = new Intent(BaseActivity.this, GatewayListActivity.class);
                            intent.putExtra("ConnectionSettingsFlow", true);
                            intent.putExtra("btDeviceList", btDeviceList);
                            startActivity(intent);
                            finish();
                        } else if (stopHandler && !disconnectFlow) {
                            Intent intent = new Intent(BaseActivity.this, GatewayListActivity.class);
                            intent.putExtra("FailureFlow", true);
                            intent.putExtra("btDeviceList", btDeviceList);
                            startActivity(intent);
                            finish();
                        }else if (!disconnectFlow) {
                            Intent intent = new Intent(BaseActivity.this, GatewayListActivity.class);
                            intent.putExtra("btDeviceList", btDeviceList);
                            startActivity(intent);
                            finish();
                        } else {
                            Intent intent = new Intent(BaseActivity.this, GatewayListActivity.class);
                            intent.putExtra("FailureFlow", true);
                            intent.putExtra("btDeviceList", btDeviceList);
                            startActivity(intent);
                            finish();
                        }
                    /*}else{
                        gatt.connect();
                    }*/
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            flag = true;
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("onServicesDiscovered", services.toString());
//                gatt.readCharacteristic(services.get(1).getCharacteristics().get
//                        (0));
            UUID uuid = UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50000");
            BluetoothGattService mCustomService = gatt.getService(UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50000"));
            if (mCustomService == null) {
                Log.i("BLE Service not found", " services not found ");
                return;
            }

            do {
                BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50001"));
                readChars.add(mReadCharacteristic);

                mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50002"));
                readChars.add(mReadCharacteristic);

                mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50003"));
                readChars.add(mReadCharacteristic);

                mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50004"));
                readChars.add(mReadCharacteristic);

                mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50005"));
                readChars.add(mReadCharacteristic);

                mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50006"));
                readChars.add(mReadCharacteristic);

                mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50007"));
                readChars.add(mReadCharacteristic);


                mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50020"));
                readChars.add(mReadCharacteristic);

                mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50021"));
                readChars.add(mReadCharacteristic);

                mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50022"));
                readChars.add(mReadCharacteristic);
            }while(readChars.size()<9);

            requestCharacteristics(gatt);


            storedChars = readChars;
            int sz = storedChars.size();
            Log.i("onServicesDiscovered:::", String.valueOf(sz));

            Log.i("characterist service 1", services.get(0).getUuid().toString());
            Log.i("characteri  service 2", services.get(1).getUuid().toString());
            Log.i("characterist service 3", services.get(2).getUuid().toString());
            Log.i("characterist service 4", services.get(3).getUuid().toString());



        }


        public boolean readCharateristicFromService(BluetoothGatt gatt, BluetoothGattCharacteristic mReadCharacteristic) {
            boolean status;

            status = gatt.readCharacteristic(mReadCharacteristic);
//                    Log.i("ReadCharacteristics"," characteristics found ");
//                   // Log.i("ReadCharacteristics", mReadCharacteristic.getValue().toString());
            Log.i("ReadCharacteristics", mReadCharacteristic.getUuid().toString());
//                   // Log.i("ReadCharacteristics", mReadCharacteristic.getValue().toString());
//
//

            return status;

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
           int arraySize;
            Log.i("onCharacteristicRead", characteristic.getUuid().toString()  + "++++" + characteristic.getService().toString() + "++++" + characteristic.getStringValue(0) + "++++" + status);
            //gatt.disconnect();

            String value = characteristic.getUuid().toString();
            Log.i("onCharacteristicRead", "before if condition " + value);

            arraySize = readChars.size();

            switch (arraySize) {
                case 1:
                    MySingleton.getInstance().charValue[0] = characteristic.getStringValue(0);
                    break;
                case 2:
                    MySingleton.getInstance().charValue[1] = characteristic.getStringValue(0);
                    break;
                case 3:
                    MySingleton.getInstance().charValue[2] = characteristic.getStringValue(0);
                    break;
                case 4:
                    MySingleton.getInstance().gattID = characteristic.getStringValue(0);
                    break;
                case 5:
                    MySingleton.getInstance().connectionType = characteristic.getStringValue(0);
                    break;
                case 6:
                    /*byte[] bytes = new byte[2];
                    ByteBuffer buf = ByteBuffer.wrap(bytes);
                    buf.putChar((char) connectionStatus);
                    Log.d("StatusVal8",String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)));
                    Log.d("StatusVal16",String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0)));*/
                    MySingleton.getInstance().connectionStatus = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                    break;
                case 7:
                    MySingleton.getInstance().noOfSensors = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    break;
                case 8:
                    MySingleton.getInstance().charValue[3] = characteristic.getStringValue(0);
                    break;
                case 9:
                    MySingleton.getInstance().charValue[4] = characteristic.getStringValue(0);
                    break;
                case 10:
                    MySingleton.getInstance().charValue[5] = characteristic.getStringValue(0);
                    break;
            }

            readChars.remove(readChars.get(readChars.size() - 1));

            if (readChars.size() > 0) {
                requestCharacteristics(gatt);
            } else {
                Log.d("GattRead","Disconnect");
                /*if (mGatt != null) {
                    mGatt.disconnect();
                    mGatt.close();
                }*/
                if(refreshFlag) {
                    Intent intent = new Intent(BaseActivity.this, ConnectionSettingsActivity.class);
                    startActivity(intent);
                    finish();
                }
                //gatt.disconnect();
            }

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            //read the characteristic data
            byte[] data = characteristic.getValue();
            Log.i("onCharacteristicChanged", data.toString());

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("onCharacteristicWrite", characteristic.toString() + "++++" + characteristic.getService().toString() + "++++" + characteristic.getStringValue(0) + "++++" + status);

        }

        public void requestCharacteristics(BluetoothGatt gatt) {
            gatt.readCharacteristic(readChars.get(readChars.size() - 1));
        }


    };

    public boolean writeToDevice(String[] writeString, String type, BluetoothDevice connDevice) {
        MySingleton.getInstance().connectedDevice = connDevice;
        boolean status = false;
        Log.i("writeToDevice::::", "inside the write function");
        this.writeString = writeString;
        this.selectedType = type;
        try {
            Log.i("writeToDevice::::", "inside the try block");
            Log.i("writeToDevice::::", String.valueOf(storedChars.size()));

            mGatt = connDevice.connectGatt(this, false, gattWriteCallback);

        } catch (Exception e) {
            e.printStackTrace();
        }


        return status;
    }
    public boolean readFromDevice(BluetoothDevice connDevice, Boolean refreshFlag, Boolean disconnectFlag) {
        this.refreshFlag = refreshFlag;
        this.disconnectFlow = disconnectFlag;
        boolean status = false;
        Log.i("readFromDevice::::", "inside the write function");
        try {
            Log.i("readFromDevice::::", "inside the try block");

            mGatt = connDevice.connectGatt(this, false, gattCallback);
            scanLeDevice(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }
    public boolean frequentReadFromDevice(BluetoothDevice connDevice, Boolean refreshFlag, Boolean disconnectFlag) {
        this.refreshFlag = refreshFlag;
        this.disconnectFlow = refreshFlag;
        boolean status = false;
        Log.i("readFromDevice::::", "inside the write function");
        try {
            Log.i("readFromDevice::::", "inside the try block");

            mGatt = connDevice.connectGatt(this, true, gattFrequentCallback);
            scanLeDevice(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

    public final BluetoothGattCallback gattFrequentCallback = new BluetoothGattCallback() {
        List<BluetoothGattCharacteristic> readChars = new ArrayList<>();

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    MySingleton.getInstance().myGattList.add(gatt);
                    gatt.discoverServices();

                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    //mGatt = MySingleton.getInstance().connectedDevice.connectGatt(BaseActivity.this, true, gattFrequentCallback);
                    Log.e("gattFrequentCallback", "STATE_DISCONNECTED "+stopHandler+"/"+disconnectFlow);
                    disconnect();
                    closeGatt();
                    //if(MySingleton.getInstance().myGattList.contains(gatt)) {
                        if (!stopHandler && disconnectFlow) {
                            if (Build.VERSION.SDK_INT < 21) {
                                if (!checkBT(mBluetoothAdapter)) {
                                    onBackPressed();
                                }
                            } else {
                                if (!checkBT(ba)) {
                                    onBackPressed();
                                }
                            }
                            Intent intent = new Intent(BaseActivity.this, GatewayListActivity.class);
                            intent.putExtra("FailureFlow", true);
                            intent.putExtra("btDeviceList", btDeviceList);
                            startActivity(intent);
                            finish();
                        } else if (!stopHandler && !disconnectFlow) {
                            Intent intent = new Intent(BaseActivity.this, GatewayListActivity.class);
                            intent.putExtra("FailureFlow", true);
                            intent.putExtra("btDeviceList", btDeviceList);
                            startActivity(intent);
                            finish();
                        } else if (stopHandler && !disconnectFlow) {
                            Intent intent = new Intent(BaseActivity.this, GatewayListActivity.class);
                            intent.putExtra("FailureFlow", true);
                            intent.putExtra("btDeviceList", btDeviceList);
                            startActivity(intent);
                            finish();
                        }else if (!disconnectFlow) {
                            Intent intent = new Intent(BaseActivity.this, GatewayListActivity.class);
                            intent.putExtra("btDeviceList", btDeviceList);
                            startActivity(intent);
                            finish();
                        } else {
                            Intent intent = new Intent(BaseActivity.this, GatewayListActivity.class);
                            intent.putExtra("FailureFlow", true);
                            intent.putExtra("btDeviceList", btDeviceList);
                            startActivity(intent);
                            finish();
                        }
                   /* }else{
                        gatt.connect();
                    }*/
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("onServicesDiscovered", services.toString());
//                gatt.readCharacteristic(services.get(1).getCharacteristics().get
//                        (0));
            UUID uuid = UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50000");
            BluetoothGattService mCustomService = gatt.getService(UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50000"));
            if (mCustomService == null) {
                Log.i("BLE Service not found", " services not found ");
                return;
            }

            BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50005"));
            readChars.add(mReadCharacteristic);

            mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50006"));
            readChars.add(mReadCharacteristic);

            mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50007"));
            readChars.add(mReadCharacteristic);

            requestCharacteristics(gatt);


            storedChars = readChars;
            int sz = storedChars.size();
            Log.i("onServicesDiscovered:::", String.valueOf(sz));

            Log.i("characterist service 1", services.get(0).getUuid().toString());
            Log.i("characteri  service 2", services.get(1).getUuid().toString());
            Log.i("characterist service 3", services.get(2).getUuid().toString());
            Log.i("characterist service 4", services.get(3).getUuid().toString());


        }


        public boolean readCharateristicFromService(BluetoothGatt gatt, BluetoothGattCharacteristic mReadCharacteristic) {
            boolean status;

            status = gatt.readCharacteristic(mReadCharacteristic);
//                    Log.i("ReadCharacteristics"," characteristics found ");
//                   // Log.i("ReadCharacteristics", mReadCharacteristic.getValue().toString());
            Log.i("ReadCharacteristics", mReadCharacteristic.getUuid().toString());
//                   // Log.i("ReadCharacteristics", mReadCharacteristic.getValue().toString());
//
//
            return status;
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            int arraySize;
            Log.i("onCharacteristicRead", characteristic.getUuid().toString()  + "++++" + characteristic.getService().toString() + "++++" + characteristic.getStringValue(0) + "++++" + status);
            //gatt.disconnect();

            arraySize = readChars.size();

            switch (arraySize) {
                case 1:
                    MySingleton.getInstance().connectionType = characteristic.getStringValue(0);
                    break;
                case 2:
                    MySingleton.getInstance().connectionStatus = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                    break;
                case 3:
                    MySingleton.getInstance().noOfSensors = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    break;
            }

            readChars.remove(readChars.get(readChars.size() - 1));

            if (readChars.size() > 0) {
                requestCharacteristics(gatt);
            } else {
                Log.d("GattFreqRead","Disconnect");
                if (mGatt != null) {
                    mGatt.disconnect();
                    mGatt.close();
                }
                if(refreshFlag) {
                    Intent intent = new Intent(BaseActivity.this, ConnectionSettingsActivity.class);
                    startActivity(intent);
                    finish();
                }
                //gatt.disconnect();
            }

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            //read the characteristic data
            byte[] data = characteristic.getValue();
            Log.i("onCharacteristicChanged", data.toString());

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("onCharacteristicWrite", characteristic.toString() + "++++" + characteristic.getService().toString() + "++++" + characteristic.getStringValue(0) + "++++" + status);

        }

        public void requestCharacteristics(BluetoothGatt gatt) {
            gatt.readCharacteristic(readChars.get(readChars.size() - 1));
        }


    };

    public final BluetoothGattCallback gattWriteCallback = new BluetoothGattCallback() {
        List<BluetoothGattCharacteristic> chars = new ArrayList<>();
        boolean flag;

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    MySingleton.getInstance().myGattList.add(gatt);
                    flag = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (!flag) {
                                        Intent intent = new Intent(BaseActivity.this, GatewayListActivity.class);
                                        intent.putExtra("FailureFlow", true);
                                        intent.putExtra("btDeviceList", btDeviceList);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            }, 2000);
                        }
                    });
                    gatt.discoverServices();
//                    Intent intent = new Intent(BaseActivity.this, ConnectionSettingsActivity.class);
//                    intent.putExtra("SelectedDevice", connectedDevice);
//                    startActivity(intent);
//                    finish();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            disconnect();
                            closeGatt();

                        }
                    });
                    Log.e("gattWriteCallback", "STATE_DISCONNECTED " + stopHandler + "/" + disconnectFlow);
                    if(stopHandler && !disconnectFlow) {
                        Intent intent = new Intent(BaseActivity.this, GatewayListActivity.class);
                        intent.putExtra("FailureFlow", true);
                        intent.putExtra("btDeviceList", btDeviceList);
                        startActivity(intent);
                        finish();
                    }
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            flag = true;
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("onServicesDiscovered", services.toString());
//                gatt.readCharacteristic(services.get(1).getCharacteristics().get
//                        (0));
            UUID uuid = UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50000");
            BluetoothGattService mCustomService = gatt.getService(UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50000"));
            if (mCustomService == null) {
                Log.i("BLE Service not found", " services not found ");
                return;
            }
            BluetoothGattCharacteristic mWriteCharacteristic;
            chars = new ArrayList<>();

            if(selectedType.equalsIgnoreCase("wifi")) {
                mWriteCharacteristic = mCustomService.getCharacteristic(UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50001"));
                chars.add(mWriteCharacteristic);

                mWriteCharacteristic = mCustomService.getCharacteristic(UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50002"));
                chars.add(mWriteCharacteristic);

                mWriteCharacteristic = mCustomService.getCharacteristic(UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50003"));
                chars.add(mWriteCharacteristic);

                if(!MySingleton.getInstance().connectionType.equalsIgnoreCase(selectedType)) {
                    mWriteCharacteristic = mCustomService.getCharacteristic(UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50005"));
                    chars.add(mWriteCharacteristic);
                }
                Log.d("ActualType/SelectedType", MySingleton.getInstance().connectionType + "/" + selectedType+" size:"+chars.size());

            } else if(selectedType.equalsIgnoreCase("cellular")) {
                mWriteCharacteristic = mCustomService.getCharacteristic(UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50020"));
                chars.add(mWriteCharacteristic);

                mWriteCharacteristic = mCustomService.getCharacteristic(UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50021"));
                chars.add(mWriteCharacteristic);

                mWriteCharacteristic = mCustomService.getCharacteristic(UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50022"));
                chars.add(mWriteCharacteristic);

                if(!MySingleton.getInstance().connectionType.equalsIgnoreCase(selectedType)) {
                    mWriteCharacteristic = mCustomService.getCharacteristic(UUID.fromString("40f8010a-525e-11e5-8590-0002a5d50005"));
                    chars.add(mWriteCharacteristic);
                    Log.d("ActualType overwride", ""+ chars.size());
                }
                Log.d("ActualType/SelectedType", MySingleton.getInstance().connectionType+"/"+selectedType+" size:"+chars.size());

            }



            storedChars = chars;
            int sz = storedChars.size();
            Log.i("onServicesDiscovered:::", String.valueOf(sz));
//            try {
//
//
//                for (int i = 0; i < chars.size(); i++) {
//                    Log.i("writeToDevice::::", "inside the for block");
//
//                    BluetoothGattCharacteristic mWCharacteristic = chars.get(i);
//                    Log.i("writeToDevice::::", mWCharacteristic.getUuid().toString());
//                    if (mWCharacteristic.getUuid().toString() == "40f8010a-525e-11e5-8590-0002a5d50001") {
//                        Log.i("writeToDevice 1::::", "inside 0002a5d50001");
//                        mWCharacteristic.setValue(URLEncoder.encode(ssid, "utf-8"));
//                        mGatt.writeCharacteristic(mWCharacteristic);
//                    }
//                    else if(mWCharacteristic.getUuid().toString() == "40f8010a-525e-11e5-8590-0002a5d50002"){
//                        Log.i("writeToDevice 1::::", "inside 0002a5d50002");
//                        mWCharacteristic.setValue(URLEncoder.encode(ssidpwd, "utf-8"));
//                        mGatt.writeCharacteristic(mWCharacteristic);
//
//                    } else if(mWCharacteristic.getUuid().toString() == "40f8010a-525e-11e5-8590-0002a5d50003"){
//                        Log.i("writeToDevice 1::::", "inside 0002a5d50003");
//                        mWCharacteristic.setValue(URLEncoder.encode(securityType, "utf-8"));
//                        mGatt.writeCharacteristic(mWCharacteristic);
//                    }
//                }
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }

//                if(gatt.readCharacteristic(mReadCharacteristic) == true ) {
//                    Log.i("ReadCharacteristics"," characteristics found ");
//                    // Log.i("ReadCharacteristics", mReadCharacteristic.getValue().toString());
//                    Log.i("ReadCharacteristics", mReadCharacteristic.getUuid().toString());
//                    // Log.i("ReadCharacteristics", mReadCharacteristic.getValue().toString());
//                    //Log.i("ReadCharacteristics", mReadCharacteristic.getInstanceId());
//                }

            // requestCharacteristics(gatt);
            //gatt.readCharacteristic(services.get(1).getCharacteristic(uuid));
            //BluetoothGattCharacteristic bluetoothGattCharacteristic = services.get(0).getCharacteristic(uuid);

            //calling the write functionality

            pushCharacteristics(gatt, chars.size());

            Log.i("characterist service 1", services.get(0).getUuid().toString());
            Log.i("characteri  service 2", services.get(1).getUuid().toString());
            Log.i("characterist service 3", services.get(2).getUuid().toString());
            Log.i("characterist service 4", services.get(3).getUuid().toString());

//                gatt.readCharacteristic(services.get(3).getCharacteristics().get(0));
//                gatt.readCharacteristic(services.get(3).getCharacteristics().get(1));
//                gatt.readCharacteristic(services.get(3).getCharacteristics().get(2));
//                gatt.readCharacteristic(services.get(3).getCharacteristics().get(3));


//                gatt.readCharacteristic(services.get(1).getCharacteristics().get
//                        (1));

        }


        public boolean readCharateristicFromService(BluetoothGatt gatt, BluetoothGattCharacteristic mReadCharacteristic) {
            boolean status;

            status = gatt.readCharacteristic(mReadCharacteristic);
//                    Log.i("ReadCharacteristics"," characteristics found ");
//                   // Log.i("ReadCharacteristics", mReadCharacteristic.getValue().toString());
            Log.i("ReadCharacteristics", mReadCharacteristic.getUuid().toString());
//                   // Log.i("ReadCharacteristics", mReadCharacteristic.getValue().toString());
//
//

            return status;

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i("onCharacteristicRead", characteristic.toString() + "++++" + characteristic.getService().toString() + "++++" + characteristic.getStringValue(0) + "++++" + status);
            //gatt.disconnect();
            chars.remove(chars.get(chars.size() - 1));

            if (chars.size() > 0) {
                requestCharacteristics(gatt);
            } else {
                // gatt.disconnect();
            }

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            //read the characteristic data
            byte[] data = characteristic.getValue();
            Log.i("onCharacteristicChanged", data.toString());

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("onCharacteristicWrite", characteristic.toString() + "++++" + characteristic.getService().toString() + "++++" + characteristic.getStringValue(0) + "++++" + status);
            chars.remove(chars.get(chars.size() - 1));

            if (chars.size() > 0) {
                pushCharacteristics(gatt, chars.size());
            } else {

               //mGatt = MySingleton.getInstance().connectedDevice.connectGatt(BaseActivity.this, false, gattCallback);
            }

        }

        public void requestCharacteristics(BluetoothGatt gatt) {
            gatt.readCharacteristic(chars.get(chars.size() - 1));
        }

        public void pushCharacteristics(BluetoothGatt gatt, int index) {
            BluetoothGattCharacteristic mWriteCharacteristic = chars.get(chars.size()-1);

            if(selectedType.equalsIgnoreCase("wifi")) {
                if (index == 3) {
                    Log.i("writeCharacteristics","  3 ");
                    mWriteCharacteristic.setValue(writeString[2]);
                    if(gatt.writeCharacteristic(mWriteCharacteristic) == true){
                        Log.i("writeCharacteristics","  Success :: ");
                    }
                } else if (index == 2) {
                    Log.i("writeCharacteristics","  2 ");
                    mWriteCharacteristic.setValue(writeString[1]);
                    if(gatt.writeCharacteristic(mWriteCharacteristic) == true){
                        Log.i("writeCharacteristics","  Success :: ");
                    }
                } else if (index == 1) {
                    Log.i("writeCharacteristics","  1 "+writeString[0]);
                    mWriteCharacteristic.setValue(writeString[0]);
                    if(gatt.writeCharacteristic(mWriteCharacteristic) == true){
                        Log.i("writeCharacteristics","  Success :: ");
                    }
                } else if (index == 4) {
                    Log.i("writeCharacteristics","  4 ");
                    mWriteCharacteristic.setValue(selectedType);
                    if(gatt.writeCharacteristic(mWriteCharacteristic) == true){
                        Log.i("writeCharacteristics","  Success :: ");
                    }
                }
            } else if(selectedType.equalsIgnoreCase("cellular")) {
                Log.i("writeCharac cellular","  Success :: ");

                if (index == 3) {
                    Log.i("writeCharac cellular","  index 3 :: ");
                    mWriteCharacteristic.setValue(writeString[5]);
                    if(gatt.writeCharacteristic(mWriteCharacteristic) == true){
                        Log.i("writeCharac cellular","  Success :: ");
                    }
                } else if (index == 2) {
                    Log.i("writeCharac cellular","  index 2 :: ");
                    Log.d("writeString[4]",writeString[4]);
                    mWriteCharacteristic.setValue(writeString[4]);
                    if(gatt.writeCharacteristic(mWriteCharacteristic) == true){
                        Log.i("writeCharac cellular","  Success :: ");
                    }
                } else if (index == 1) {
                    Log.d("writeString[3]",writeString[3]);
                    Log.i("writeCharac cellular", "  index 1 :: ");
                    mWriteCharacteristic.setValue(writeString[3]);
                    if(gatt.writeCharacteristic(mWriteCharacteristic) == true){
                        Log.i("writeCharac cellular","  Success :: ");
                    }
                } else if (index == 4) {
                    Log.i("writeCharac cellular","  index 4 :: ");
                    mWriteCharacteristic.setValue(selectedType);
                    if(gatt.writeCharacteristic(mWriteCharacteristic) == true){
                        Log.i("writeCharac cellular","  Success :: ");
                    }
                }
            }
        }
    };

    public void printScanRecord (byte[] scanRecord) {

        // Simply print all raw bytes
        try {
            String decodedRecord = new String(scanRecord,"UTF-8");
            Log.d("DEBUG","decoded String : " + ByteArrayToString(scanRecord));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // Parse data bytes into individual records
        List<AdRecord> records = AdRecord.parseScanRecord(scanRecord);


        // Print individual records
        if (records.size() == 0) {
            Log.i("DEBUG", "Scan Record Empty");
        } else {
            Log.i("DEBUG", "Scan Record: " + TextUtils.join(",", records));
        }

    }


    public static String ByteArrayToString(byte[] ba)
    {
        StringBuilder hex = new StringBuilder(ba.length * 2);
        for (byte b : ba)
            hex.append(b + " ");

        return hex.toString();
    }


    public static class AdRecord {

        public AdRecord(int length, int type, byte[] data) {
            String decodedRecord = "";
            try {
                decodedRecord = new String(data,"UTF-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            Log.d("DEBUG", "Length: " + length + " Type : " + type + " Data : " + ByteArrayToString(data));
        }

        // ...

        public static List<AdRecord> parseScanRecord(byte[] scanRecord) {
            List<AdRecord> records = new ArrayList<AdRecord>();

            int index = 0;
            while (index < scanRecord.length) {
                int length = scanRecord[index++];
                //Done once we run out of records
                if (length == 0) break;

                int type = scanRecord[index];
                //Done if our record isn't a valid type
                if (type == 0) break;

                byte[] data = Arrays.copyOfRange(scanRecord, index + 1, index + length);

                records.add(new AdRecord(length, type, data));
                //Advance
                index += length;
            }

            return records;
        }

        // ...
    }
    private void readSpecificDataKitkat(byte[] raw_data){

        int index = 0;
        if(raw_data!=null) {
            while (index < raw_data.length) {
                int reading_type = ((raw_data[index] >> 4) & 0xF);
                int scaling_value = (raw_data[index] & 0xF) - 8;
                int scaling_factor = (int) power_of_two(scaling_value); // floating point not supported, so need to work out separate fields for magnitudes;
                //Integer.valueOf(String.valueOf(scaling_factor));
                //ub_ustdio_printf("Found reading type: %u, scaling factor: %u, index: %u\n", reading_type, scaling_factor, index);
                double sensor_data;
                if(index<=100)
                Log.d("Raw Data Custom", String.valueOf(raw_data[index] & 0xF)+" index:"+index+" rType:"+ reading_type +" sFactor");
                //Log.d("Initial ", reading_type + " " + scaling_factor + " " + index );
                index++;
            }
            Log.d("Index Count",String.valueOf(index));
        }
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
    /*private List<UUID> parseUUIDs(final byte[] advertisedData) {
        List<UUID> uuids = new ArrayList<UUID>();

        int offset = 0;
        while (offset < (advertisedData.length - 2)) {
            int len = advertisedData[offset++];
            if (len == 0)
                break;

            int type = advertisedData[offset++];
            switch (type) {
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                    while (len > 1) {
                        int uuid16 = advertisedData[offset++];
                        uuid16 += (advertisedData[offset++] << 8);
                        len -= 2;
                        uuids.add(UUID.fromString(String.format(
                                "%08x-0000-1000-8000-00805f9b34fb", uuid16)));
                    }
                    break;
                case 0x06:// Partial list of 128-bit UUIDs
                case 0x07:// Complete list of 128-bit UUIDs
                    // Loop through the advertised 128-bit UUID's.
                    while (len >= 16) {
                        try {
                            // Wrap the advertised bits and order them.
                            ByteBuffer buffer = ByteBuffer.wrap(advertisedData,
                                    offset++, 16).order(ByteOrder.LITTLE_ENDIAN);
                            long mostSignificantBit = buffer.getLong();
                            long leastSignificantBit = buffer.getLong();
                            uuids.add(new UUID(leastSignificantBit,
                                    mostSignificantBit));
                        } catch (IndexOutOfBoundsException e) {
                            // Defensive programming.
                            Log.e("Memec", e.toString());
                            continue;
                        } finally {
                            // Move the offset to read the next uuid.
                            offset += 15;
                            len -= 16;
                        }
                    }
                    break;
                default:
                    offset += (len - 1);
                    break;
            }
        }

        return uuids;
    }*/

    public void closeGatt() {
        Log.d("Gatt Close", "Entering");
        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;
    }
    @Override
    public void onBackPressed() {
        disconnect();
        closeGatt();
        super.onBackPressed();
    }

    public void disconnect() {
        ArrayList<BluetoothGatt> gt = MySingleton.getInstance().myGattList;
        int i = 0;
        while(i<gt.size()){
            if(gt.get(i)!=null){
                Log.i("myGatt", gt.get(i).toString());
                gt.get(i).disconnect();
            }
            i++;
        }
        MySingleton.getInstance().myGattList = new ArrayList<BluetoothGatt>();
        MySingleton.getInstance().charValue = new String[6];
        MySingleton.getInstance().connectionStatus = 0;
        MySingleton.getInstance().connectionType = null;
        MySingleton.getInstance().noOfSensors = 0;
        MySingleton.getInstance().connectedDevice = null;

        if (mBluetoothAdapter == null || mGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mGatt.disconnect();
    }
}
