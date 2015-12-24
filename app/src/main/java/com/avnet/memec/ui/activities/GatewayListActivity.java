package com.avnet.memec.ui.activities;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
	import android.bluetooth.le.ScanResult;
	import android.bluetooth.le.ScanSettings;
	import android.content.Context;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.avnet.memec.R;

import java.util.ArrayList;
import java.util.List;
import com.avnet.memec.ui.adaptors.GatewayListAdapter;

public class GatewayListActivity extends AppCompatActivity {

    ListView listView ;

    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 10;
    private static final long SCAN_PERIOD = 10000;
    private boolean mScanning=true;
    private Handler mHandler;
    private static final String TAG = "GatewayListActivity";
    private BluetoothGatt mGatt;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    BluetoothAdapter ba;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gateway_list);

        mHandler = new Handler();

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        listView = (ListView) findViewById(R.id.gateway_list);

        GatewayListAdapter mAdapter;

        mAdapter = new GatewayListAdapter(this);
        mAdapter.addSectionHeaderItem("");
        mAdapter.addItem("Gateway01_01");
        mAdapter.addItem("Gateway01_02");
        mAdapter.addItem("Gateway01_03");
        mAdapter.addItem("Gateway01_04");
        mAdapter.addItem("Gateway01_05");

        listView.setAdapter(mAdapter);

        //Gateway Success Dialog
        final Dialog gsuccess_dialog = new Dialog(GatewayListActivity.this, R.style.Theme_Dialog);
        gsuccess_dialog.setContentView(R.layout.dialog_gateway_success);
        //gsuccess_dialog.show();

        //Gateway Failure Dialog
        final Dialog gfailure_dialog = new Dialog(GatewayListActivity.this, R.style.Theme_Dialog);
        gfailure_dialog.setContentView(R.layout.dialog_gateway_failure);
        Button close = (Button) gfailure_dialog.findViewById(R.id.close_dialog);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gfailure_dialog.dismiss();
            }
        });
        //gfailure_dialog.show();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition = position;

                FrameLayout fl = (FrameLayout) view.findViewById(R.id.gateway_frame);
                fl.setBackgroundColor(getResources().getColor(R.color.theme_primary_highlight));

                TextView tv = (TextView) view.findViewById(R.id.gateway_text);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                tv.setTextColor(getResources().getColor(R.color.white));
                // ListView Clicked item value
                String itemValue = (String) listView.getItemAtPosition(position);

                // Show Alert
                Toast.makeText(getApplicationContext(),
                        "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
                        .show();
                //TODO Gateway Sucess Logic
                gsuccess_dialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        gsuccess_dialog.dismiss();
                        Intent intent = new Intent(GatewayListActivity.this, ConnectionSettingsActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, 3000);

            }

        });

        final SwipeRefreshLayout srl = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        srl.setNestedScrollingEnabled(true);
        srl.startNestedScroll(View.SCROLL_AXIS_VERTICAL);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //final ProgressBar spin = (ProgressBar) findViewById(R.id.progress_spin);
                //spin.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        srl.setRefreshing(false);
                        //spin.setVisibility(View.GONE);
                        //Check for devices and Update List View
                    }
                }, 3000);
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

        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        //Check BT
        if(!ba.isEnabled()) {
            //Bluetooth Dialog
            final Dialog dialog = new Dialog(GatewayListActivity.this, R.style.Theme_Dialog);
            dialog.setContentView(R.layout.dialog_layout_bluetooth);
            ToggleButton toggleButton = (ToggleButton) dialog.findViewById(R.id.toggleButtonBT);
            toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
                    //Enable Bluetooth
                    Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(turnOn, 0);
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
        } else {
            mLEScanner = ba.getBluetoothLeScanner();
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            filters = new ArrayList<ScanFilter>();
            scanLeDevice(true);
        }
        Button viewSensors = (Button) findViewById(R.id.view_sensors);
        viewSensors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GatewayListActivity.this, ViewSensorsActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

        private void scanLeDevice(final boolean enable) {

            if(enable){

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mScanning = false;
                        mLEScanner.stopScan(mScanCallback);


                    }
                }, SCAN_PERIOD);

                mScanning = true;
                mLEScanner.startScan(filters, settings, mScanCallback);
                // mLEScanner.startScan(mScanCallback);
            } else {
                mScanning = false;
                mLEScanner.stopScan(mScanCallback);
            }
        }


        private ScanCallback mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                Log.i("callbackType", String.valueOf(callbackType));
                Log.i("result", result.toString());
                BluetoothDevice btDevice = result.getDevice();
                connectToDevice(btDevice);
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


        public void connectToDevice(BluetoothDevice device) {
            if (mGatt == null) {
                mGatt = device.connectGatt(this, false, gattCallback);
                scanLeDevice(false);// will stop after first device detection
            }
        }

        private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                Log.i("onConnectionStateChange", "Status: " + status);
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        Log.i("gattCallback", "STATE_CONNECTED");
                        gatt.discoverServices();
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        Log.e("gattCallback", "STATE_DISCONNECTED");
                        break;
                    default:
                        Log.e("gattCallback", "STATE_OTHER");
                }

            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                List<BluetoothGattService> services = gatt.getServices();
                Log.i("onServicesDiscovered", services.toString());
                gatt.readCharacteristic(services.get(1).getCharacteristics().get
                        (0));

                gatt.readCharacteristic(services.get(1).getCharacteristics().get
                        (1));

            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt,
                                             BluetoothGattCharacteristic
                                                     characteristic, int status) {
                Log.i("onCharacteristicRead", characteristic.toString() + "++++" + characteristic.getService().toString() + "++++" + characteristic.getStringValue(0));
                // gatt.disconnect();
            }
        };


}
