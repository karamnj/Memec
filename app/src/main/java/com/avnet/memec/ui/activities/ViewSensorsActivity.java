package com.avnet.memec.ui.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.avnet.memec.R;
import com.avnet.memec.ui.adaptors.ViewSensorsAdapter;
import com.avnet.memec.ui.model.SensorObject;
import com.avnet.memec.ui.services.BleServiceConst;
import com.avnet.memec.ui.util.BleFuncProvider;
import com.avnet.memec.ui.util.BleScanCallback;
import com.avnet.memec.ui.util.BtDevice;
import com.avnet.memec.ui.util.MySingleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ViewSensorsActivity extends AppCompatActivity implements BleScanCallback {

    ListView listView;
    ViewSensorsAdapter mAdapter;
    SwipeRefreshLayout srl;
    private BleFuncProvider bleFuncProvider;
    ArrayList<String> devicesFound;

    private static final int REQUEST_ENABLE_BT = 100;
    Button viewGatewayDetails;
    Button backToHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_sensors);

        MySingleton.getInstance().btDeviceHash = new HashSet<BluetoothDevice>();
        MySingleton.getInstance().sensorMap = new HashMap<String, SensorObject>();
        devicesFound = new ArrayList<String>();

        // Handle Bluetooth provider creation
        try {
            bleFuncProvider = new BleFuncProvider(this, this);
        }
        catch (Exception exc){
            Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }

        viewGatewayDetails = (Button) findViewById(R.id.view_gateway_details);
        backToHome = (Button) findViewById(R.id.back_to_home);
        if(MySingleton.getInstance().connectedDevice!=null){
            viewGatewayDetails.setVisibility(View.VISIBLE);
            backToHome.setVisibility(View.GONE);
        }

        listView = (ListView) findViewById(R.id.sensor_list);

        mAdapter = new ViewSensorsAdapter(this);
        mAdapter.addSectionHeaderItem("0 sensor objects found");
        listView.setAdapter(mAdapter);

        srl = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        srl.setProgressViewOffset(false,0,185);
        srl.setRefreshing(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode)
        {
            // Result of the Bluetooth activation procedure
            case REQUEST_ENABLE_BT: {
                if(resultCode != RESULT_OK){ // BT not activated
                    Toast.makeText(this, "BT not enabled", Toast.LENGTH_LONG).show();
                    finish();
                }
                else{   // BT activated: re-instantiate BLEfuncProvider obj
                    try
                    {
                        bleFuncProvider = new BleFuncProvider(this, this);
                    }
                    catch (Exception exc){
                        Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        if(bleFuncProvider.arePermissionGranted(requestCode, permissions, grantResults)){
            startBleScan();
        }
        else{ // Permission not granted by the user
            Toast.makeText(this, "No permission granted to use BLE related functionality",
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // Ask for permission to use for BLE functionality if needed and start the scan accordingly
        if(!bleFuncProvider.isGrantPermissionNeeded(this)){
            startBleScan();
        }
    }

    @Override
    protected void onPause()
    {
        stopBleScan();
        super.onPause();
    }

    public void startBleScan()
    {
        // Ask the user to activate Bluetooth if not enabled yet
        if(!bleFuncProvider.isBluetoothOn()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        MySingleton.getInstance().btDeviceHash = new HashSet<BluetoothDevice>();
        MySingleton.getInstance().sensorMap = new HashMap<String, SensorObject>();

        bleFuncProvider.startScan();
    }

    public void stopBleScan()
    {
        bleFuncProvider.stopScan();
    }

    /*
     * Callback called when BleFuncProvider starts scanning for devices
     */
    @Override
    public void onScanStarted()
    {
        srl.setRefreshing(true);
    }

    /*
     * Callback called when BleFuncProvider finishes scanning for devices
     */
    @Override
    public void onScanEnded()
    {
        srl.setRefreshing(false);
    }

    /*
     * Callback called when a BLE device is detected by BleFuncProvider
     */
    @Override
    public void onDeviceDiscovered(final BluetoothDevice device, int rssi, final byte[] scanRecord)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (scanRecord != null) {
                    int read1 = (scanRecord[5] & 0xFF);
                    int read2 = (scanRecord[6] & 0xFF);
                    Log.d("readSpecificData", read1 + " " + read2);
                    if ((read1 == 25 && read2 == 16) || (read1 == 16 && read2 == 25)) {
                        BluetoothDevice btDevice = device;
                        Log.d("BT-Name",device.getName()+" id:"+device.getAddress()+" uuid:"+device.getUuids());
                        MySingleton.getInstance().btDeviceHash.add(btDevice);
                        //BtDevice bt = new BtDevice(device, true, "VT Sensor - ");
                        SensorObject sensorObject = new SensorObject();
                        String id = device.getAddress().substring(12,14)+device.getAddress().substring(15,17);
                        sensorObject.setDeviceName("VT Sensor - "+id);
                        sensorObject.setDeviceId(device.getAddress());
                        //sensorObject.setSensors(readManufacturerSpecificData(scanRecord, 7));
                        MySingleton.getInstance().sensorMap.put(device.getAddress(), sensorObject);
                    }
                }
                refreshList(getSensorList());
            }
        });
    }

    protected ArrayList<SensorObject> getSensorList(){
        //HashSet<SensorObject> sensorHash = new HashSet<SensorObject>(sensorMap.values());
        ArrayList<SensorObject> sensorList = new ArrayList<SensorObject>(MySingleton.getInstance().sensorMap.values());
        return sensorList;
    }

    protected ArrayList<BluetoothDevice> getBTDeviceList(){
        ArrayList<BluetoothDevice> btDeviceList = new ArrayList<BluetoothDevice>(MySingleton.getInstance().btDeviceHash);
        return btDeviceList;
    }

    private void refreshList(ArrayList<SensorObject> sensorList){

        mAdapter = new ViewSensorsAdapter(this);
        int i = 0;

        Log.d("In flag first", sensorList.size() + " sensor objects found");
        mAdapter.addSectionHeaderItem(sensorList.size() + " sensor objects found");

        while (i < sensorList.size()) {
            SensorObject so = sensorList.get(i);
            /*if(!devicesFound.contains(so.getDeviceId())) {
                devicesFound.add(so.getDeviceId());*/
                Log.d("addSensor", so.getDeviceId());
                //so.setDeviceName("VT Sensor - " + (i + 1));
                Log.d("Activity Sensor", so.getDeviceName());
                //mAdapter.addSensorItem(so.getSensors(), so.getDeviceName());
                mAdapter.addItem(so.getDeviceName());
            //}
            i++;
        }
        if (sensorList.size() > 0) {
            listView.setAdapter(null);
            listView.setAdapter(mAdapter);
        }

        listSetup();
    }

    private void listSetup(){
        srl.setNestedScrollingEnabled(true);
        srl.startNestedScroll(View.SCROLL_AXIS_VERTICAL);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //final ProgressBar spin = (ProgressBar) findViewById(R.id.progress_spin);
                //spin.setVisibility(View.VISIBLE);
                Log.d("in Refresh", "pull");
                //devicesFound = new ArrayList<String>();
                // Ask for permission to use for BLE functionality if needed and start the scan accordingly
                if (!bleFuncProvider.isGrantPermissionNeeded(ViewSensorsActivity.this)) {
                    startBleScan();
                }

            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                boolean enable = false;
                if (listView != null && listView.getChildCount() > 0) {
                    // check if the first item of the list is visible
                    boolean firstItemVisible = listView.getFirstVisiblePosition() == 0;
                    // check if the top of the first item is visible
                    boolean topOfFirstItemVisible = listView.getChildAt(0).getTop() == 0;
                    // enabling or disabling the refresh layout
                    enable = firstItemVisible && topOfFirstItemVisible;
                }
                srl.setEnabled(enable);
            }
        });

        /*listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FrameLayout fl = (FrameLayout) view.findViewById(R.id.gateway_frame);
                fl.setBackgroundColor(getResources().getColor(R.color.theme_primary_highlight));

                TextView tv = (TextView) view.findViewById(R.id.gateway_text);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                tv.setTextColor(getResources().getColor(R.color.white));

                ArrayList<BluetoothDevice> btList = getBTDeviceList();
                BtDevice bt = new BtDevice(btList.get(position - 1), true, "VT Sensor - " + position);
                Log.d("btDevice.getBtName()", bt.getBtName());
                MySingleton.getInstance().btDeviceSelected = bt;

                Intent intent = new Intent(ViewSensorsActivity.this, SensorDetailsActivity.class);
                intent.putExtra(BleServiceConst.BLE_DEVICE, position - 1);
                startActivity(intent);

                fl.setBackgroundColor(getResources().getColor(R.color.white));

                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                tv.setTextColor(getResources().getColor(R.color.theme_neutral_dark_grey));
            }
        });*/

        viewGatewayDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //stopViewSensors = true;
                Intent intent = new Intent(ViewSensorsActivity.this, ConnectionSettingsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        backToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //stopViewSensors = true;
                Intent intent = new Intent(ViewSensorsActivity.this, ScanActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //stopViewSensors = true;
    }
}
