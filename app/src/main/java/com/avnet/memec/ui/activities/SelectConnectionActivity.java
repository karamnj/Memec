package com.avnet.memec.ui.activities;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.avnet.memec.R;
import com.avnet.memec.ui.util.MySingleton;
import com.avnet.memec.ui.util.WiFiApManager;

public class SelectConnectionActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {

    Spinner spinner;
    BluetoothDevice btSelectedDevice;
    WiFiApManager wifiApManager;
    String gattId = "";
    Dialog csaved_dialog;
    Boolean initialLoad = true;
    ArrayAdapter<CharSequence> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_connection);
        wifiApManager = new WiFiApManager(getApplicationContext());

        btSelectedDevice = MySingleton.getInstance().connectedDevice;
        gattId = MySingleton.getInstance().gattID;

        csaved_dialog = new Dialog(SelectConnectionActivity.this, R.style.Theme_Dialog);
        csaved_dialog.setContentView(R.layout.dialog_credentials_saved);

        final Button wifi = (Button) findViewById(R.id.wifi);
        final Button cellular = (Button) findViewById(R.id.cellular);
        Button apCheck = (Button) findViewById(R.id.ap_check);
        Button closeObtainedAP = (Button) findViewById(R.id.close_obtained_ap);
        Button save = (Button) findViewById(R.id.save);
        Button cancel = (Button) findViewById(R.id.cancel);
        final EditText ssidName = (EditText) findViewById(R.id.ssid_name);
        final EditText ssidPwd = (EditText) findViewById(R.id.ssid_pwd);
        final EditText apnName = (EditText) findViewById(R.id.apn_name);
        final EditText usrName = (EditText) findViewById(R.id.usr_name);
        final EditText apnPassword = (EditText) findViewById(R.id.apn_password);
        final ProgressBar apCheckLoading = (ProgressBar) findViewById(R.id.apcheck_loading);
        final LinearLayout wifiLayout = (LinearLayout) findViewById(R.id.wifi_layout);
        final LinearLayout cellularLayout = (LinearLayout) findViewById(R.id.cellular_layout);
        final FrameLayout apSearch = (FrameLayout) findViewById(R.id.ap_search);
        final FrameLayout apObtained = (FrameLayout) findViewById(R.id.ap_obtained);
        final Spinner securityType = (Spinner) findViewById(R.id.spinner);

        //Init
        ssidName.setHint(Html.fromHtml("<i>" + MySingleton.getInstance().charValue[0] + "</i>"));
        ssidPwd.setHint(Html.fromHtml("<i>" + MySingleton.getInstance().charValue[1] + "</i>"));

        apnName.setHint(Html.fromHtml("<i>" + MySingleton.getInstance().charValue[3] + "</i>"));
        usrName.setHint(Html.fromHtml("<i>" + MySingleton.getInstance().charValue[4] + "</i>"));
        apnPassword.setHint(Html.fromHtml("<i>" + MySingleton.getInstance().charValue[5] + "</i>"));

        // Spinner element
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        adapter = ArrayAdapter.createFromResource(this,
                R.array.security_type, R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        String compareValue = "WPA2-PSK";

        if (compareValue!=null) {
            int spinnerPosition = adapter.getPosition(compareValue);
            spinner.setSelection(spinnerPosition);
        }

        //Click Listeners
        wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiLayout.setVisibility(View.VISIBLE);
                wifi.setBackgroundColor(getResources().getColor(R.color.theme_primary_black));
                wifi.setTextColor(getResources().getColor(R.color.white));
                cellularLayout.setVisibility(View.GONE);
                cellular.setBackgroundColor(getResources().getColor(R.color.white));
                cellular.setTextColor(getResources().getColor(R.color.theme_neutral_dark_grey));
            }
        });
        cellular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiLayout.setVisibility(View.GONE);
                wifi.setBackgroundColor(getResources().getColor(R.color.white));
                wifi.setTextColor(getResources().getColor(R.color.theme_neutral_dark_grey));
                cellularLayout.setVisibility(View.VISIBLE);
                cellular.setBackgroundColor(getResources().getColor(R.color.theme_primary_black));
                cellular.setTextColor(getResources().getColor(R.color.white));
            }
        });
        apCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apCheckLoading.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        apCheckLoading.setVisibility(View.GONE);
                        apSearch.setVisibility(View.GONE);
                        apObtained.setVisibility(View.VISIBLE);

                        boolean apSupported = (wifiApManager.isApModeSupported());
                        String ssid = "";
                        String password = "";
                        String type = "";
                        if (apSupported) {
                            ssid = wifiApManager.getWifiApConfiguration(WiFiApManager.WIFI_AP_CONFIG.SSID);
                            password = wifiApManager.getWifiApConfiguration(WiFiApManager.WIFI_AP_CONFIG.PASSWORD);
                            type = "WPA2-PSK";
                        } else {
                            ssid = "Unsupported";
                            password = "Unsupported";
                            type = "WPA2-PSK";
                        }
                        ssidName.setText(ssid);
                        ssidPwd.setText(password);

                        int spinnerPosition = adapter.getPosition(type);
                        spinner.setSelection(spinnerPosition);

                        //spinner.setPrompt(type);
                    }
                }, 1000);
            }
        });
        closeObtainedAP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apCheckLoading.setVisibility(View.GONE);
                apSearch.setVisibility(View.VISIBLE);
                apObtained.setVisibility(View.GONE);
                ssidName.setText(MySingleton.getInstance().charValue[0]);
                ssidPwd.setText(MySingleton.getInstance().charValue[1]);
                int spinnerPosition = adapter.getPosition(MySingleton.getInstance().charValue[2]);
                spinner.setSelection(spinnerPosition);
                /*ssidName.setText("");
                ssidPwd.setText("");
                spinner.setPrompt("");*/
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Credentials Saved Dialog
                Log.i("Enterd Text ::::", ssidName.getText().toString());
                Log.i("spinner Text ::::", securityType.getSelectedItem().toString());
                writeString = new String[6];
                if(wifiLayout.getVisibility() == View.VISIBLE) {
                    if(ssidName.getText().toString()=="" || ssidPwd.getText().toString()==""){
                        Toast.makeText(SelectConnectionActivity.this,"Please fill all the fields",Toast.LENGTH_LONG).show();
                    }else {
                        selectedType = "WIFI";
                        writeString[0] = ssidName.getText().toString().trim();
                        writeString[1] = ssidPwd.getText().toString().trim();
                        writeString[2] = securityType.getSelectedItem().toString();
                        if(Build.VERSION.SDK_INT < 21) {
                            if(checkBT(mBluetoothAdapter)){
                                performSave();
                            }
                        } else {
                            if(checkBT(ba)){
                                performSave();
                            }
                        }
                    }
                }else {
                    if(ssidName.getText().toString()=="" || ssidPwd.getText().toString()=="" || securityType.getSelectedItem().toString()==""){
                        Toast.makeText(SelectConnectionActivity.this,"Please fill all the fields",Toast.LENGTH_LONG).show();
                    }else {
                        selectedType = "CELLULAR";
                        writeString[3] = apnName.getText().toString().trim();
                        writeString[4] = usrName.getText().toString().trim();
                        writeString[5] = apnPassword.getText().toString().trim();
                        if(Build.VERSION.SDK_INT < 21) {
                            if(checkBT(mBluetoothAdapter)){
                                performSave();
                            }
                        } else {
                            if(checkBT(ba)){
                                performSave();
                            }
                        }
                    }
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //onBackPressed();
                Intent intent = new Intent(SelectConnectionActivity.this, ConnectionSettingsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        initialLoad = false;
    }
    private void performSave(){
        Log.d("SelectedType", selectedType);
        if(Build.VERSION.SDK_INT < 21) {
            if(checkBT(mBluetoothAdapter)){
                if(mBluetoothAdapter.isEnabled()) {
                    writeToDevice(writeString, selectedType, btSelectedDevice);
                    csaved_dialog.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            readFromDevice(MySingleton.getInstance().connectedDevice, true, false);
                        }
                    }, 5000);
                }
            }
        } else {
            if(checkBT(ba)){
                if(mBluetoothAdapter.isEnabled()) {
                    writeToDevice(writeString, selectedType, btSelectedDevice);
                    csaved_dialog.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            readFromDevice(MySingleton.getInstance().connectedDevice, true, false);
                        }
                    }, 5000);
                }
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        if(!initialLoad){
            String item = parent.getItemAtPosition(position).toString();
            if(item.equalsIgnoreCase("open")){
                EditText ssidPwd = (EditText) findViewById(R.id.ssid_pwd);
                ssidPwd.setText("");
                ssidPwd.setHint("");
                ssidPwd.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_bg));
                ssidPwd.setEnabled(false);
            }else{
                EditText ssidPwd = (EditText) findViewById(R.id.ssid_pwd);
                ssidPwd.setHint(Html.fromHtml("<i>" + MySingleton.getInstance().charValue[1] + "</i>"));
                        ssidPwd.setBackgroundDrawable(getResources().getDrawable(R.drawable.border));
                ssidPwd.setEnabled(true);
            }
            spinner.setPrompt(item);
            Log.d("Itemselected","Spinner");
        }
        // Showing selected spinner item
        //Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onBackPressed() {
        disconnect();
        closeGatt();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        csaved_dialog.dismiss();
    }
}
