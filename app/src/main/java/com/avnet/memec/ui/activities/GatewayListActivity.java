package com.avnet.memec.ui.activities;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.avnet.memec.R;
import com.avnet.memec.ui.adaptors.GatewayListAdapter;

import java.util.ArrayList;

public class GatewayListActivity extends BaseActivity {

    ListView listView ;
    ArrayList<BluetoothDevice> btDevices;
    SwipeRefreshLayout srl;
    GatewayListAdapter mAdapter;
    public Dialog gsuccess_dialog, gfailure_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gateway_list);
        listView = (ListView) findViewById(R.id.gateway_list);

        initDialogs();
        srl = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        if(getIntent().getBooleanExtra("ConnectionSettingsFlow",false)){
            scanGateway();
        }else if(getIntent().getBooleanExtra("FailureFlow",false)){
            gfailure_dialog.show();
            btDevices = (ArrayList<BluetoothDevice>) getIntent().getSerializableExtra("btDeviceList");
            init();
        }else {
            btDevices = (ArrayList<BluetoothDevice>) getIntent().getSerializableExtra("btDeviceList");
            init();
        }

    }
    private void initDialogs() {
        //Gateway Success Dialog
        gsuccess_dialog = new Dialog(this, R.style.Theme_Dialog);
        gsuccess_dialog.setContentView(R.layout.dialog_gateway_success);
        //gsuccess_dialog.show();

        //Gateway Failure Dialog
        gfailure_dialog = new Dialog(this, R.style.Theme_Dialog);
        gfailure_dialog.setContentView(R.layout.dialog_gateway_failure);
        Button close = (Button) gfailure_dialog.findViewById(R.id.close_dialog);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gfailure_dialog.dismiss();
            }
        });
        //gfailure_dialog.show();
    }
    private void scanGateway(){
        //Gateway Scanning Dialog
        final Dialog gscan_dialog = new Dialog(GatewayListActivity.this, R.style.Theme_Dialog);
        gscan_dialog.setContentView(R.layout.dialog_gateway_scan);
        gscan_dialog.show();

        scanLeDeviceForResult(true);
        srl.setRefreshing(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                srl.setRefreshing(false);
                //spin.setVisibility(View.GONE);
                //Check for devices and Update List View
                btDevices = getBTDeviceList();
                Log.d("btDevices", btDevices.size() + "");
                refreshList();
                gscan_dialog.dismiss();
            }
        }, 2500);
    }

    private void init(){

        mAdapter = new GatewayListAdapter(this);
        mAdapter.addSectionHeaderItem("");
        /*mAdapter.addItem("Gateway01_01");
        mAdapter.addItem("Gateway01_02");
        mAdapter.addItem("Gateway01_03");
        mAdapter.addItem("Gateway01_04");
        mAdapter.addItem("Gateway01_05");*/

        int i = 0;
        while (i < btDevices.size()) {
            BluetoothDevice bt = btDevices.get(i);
//            Log.d("DeviceName", bt.getName());
            if(bt.getName()==null){
                mAdapter.addItem("Undefined");
            }else {
                mAdapter.addItem(bt.getName());
            }
            i++;
        }

        listView.setAdapter(mAdapter);
        listSetup();
    }

    private void listSetup(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {

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
                //Toast.makeText(getApplicationContext(), "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG).show();
                //TODO Gateway Sucess Logic
                TextView successText = (TextView) gsuccess_dialog.findViewById(R.id.gw_success);
                successText.setText("You have successfully been connected to "+tv.getText());
                gsuccess_dialog.show();
                /*new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {*/
                        connectToDevice(btDevices.get(position - 1));
                        //gsuccess_dialog.dismiss();
                /*    }
                }, 1000);*/

            }

        });

        srl.setNestedScrollingEnabled(true);
        srl.startNestedScroll(View.SCROLL_AXIS_VERTICAL);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //final ProgressBar spin = (ProgressBar) findViewById(R.id.progress_spin);
                //spin.setVisibility(View.VISIBLE);
                srl.setRefreshing(false);
                if(Build.VERSION.SDK_INT < 21) {
                    if(checkBT(mBluetoothAdapter)){
                        if (mBluetoothAdapter.isEnabled()) {
                            srl.setRefreshing(true);
                            scanLeDeviceForResult(true);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    srl.setRefreshing(false);
                                    //spin.setVisibility(View.GONE);
                                    //Check for devices and Update List View
                                    btDevices = getBTDeviceList();
                                    refreshList();
                                }
                            }, 2500);
                        }
                    }
                } else {
                    if(checkBT(ba)){
                        if (ba.isEnabled()) {
                            srl.setRefreshing(true);
                            scanLeDeviceForResult(true);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    srl.setRefreshing(false);
                                    //spin.setVisibility(View.GONE);
                                    //Check for devices and Update List View
                                    btDevices = getBTDeviceList();
                                    refreshList();
                                }
                            }, 2500);
                        }
                    }
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

    private void refreshList(){

        mAdapter = new GatewayListAdapter(this);
        mAdapter.addSectionHeaderItem("");

        int i = 0;
        while (i < btDevices.size()) {
            BluetoothDevice bt = btDevices.get(i);
// Log.d("DeviceName", bt.getName());
            if(bt.getName()==null){
                mAdapter.addItem("Undefined");
            }else {
                mAdapter.addItem(bt.getName());
            }
            i++;
        }

        listView.setAdapter(null);
        listView.setAdapter(mAdapter);

        listSetup();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gsuccess_dialog.dismiss();
        gfailure_dialog.dismiss();
    }
}
