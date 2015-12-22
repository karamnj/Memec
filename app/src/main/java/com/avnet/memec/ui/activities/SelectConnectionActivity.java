package com.avnet.memec.ui.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
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

import java.util.ArrayList;
import java.util.List;

public class SelectConnectionActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_connection);

        Button wifi = (Button) findViewById(R.id.wifi);
        Button cellular = (Button) findViewById(R.id.cellular);
        Button apCheck = (Button) findViewById(R.id.ap_check);
        Button closeObtainedAP = (Button) findViewById(R.id.close_obtained_ap);
        Button save = (Button) findViewById(R.id.save);
        Button cancel = (Button) findViewById(R.id.cancel);
        final EditText ssidName = (EditText) findViewById(R.id.ssid_name);
        final EditText ssidPwd = (EditText) findViewById(R.id.ssid_pwd);
        final ProgressBar apCheckLoading = (ProgressBar) findViewById(R.id.apcheck_loading);
        final LinearLayout wifiLayout = (LinearLayout) findViewById(R.id.wifi_layout);
        final LinearLayout cellularLayout = (LinearLayout) findViewById(R.id.cellular_layout);
        final FrameLayout apSearch = (FrameLayout) findViewById(R.id.ap_search);
        final FrameLayout apObtained = (FrameLayout) findViewById(R.id.ap_obtained);

        //Click Listeners
        wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiLayout.setVisibility(View.VISIBLE);
                cellularLayout.setVisibility(View.GONE);
            }
        });
        cellular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiLayout.setVisibility(View.GONE);
                cellularLayout.setVisibility(View.VISIBLE);
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
                        ssidName.setText("******");
                        ssidPwd.setText("******");
                        spinner.setPrompt("Open");
                    }
                }, 3000);
            }
        });
        closeObtainedAP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apCheckLoading.setVisibility(View.GONE);
                apSearch.setVisibility(View.VISIBLE);
                apObtained.setVisibility(View.GONE);
                ssidName.setText("");
                ssidPwd.setText("");
                spinner.setPrompt("");
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Credentials Saved Dialog
                final Dialog csaved_dialog = new Dialog(SelectConnectionActivity.this, R.style.Theme_Dialog);
                csaved_dialog.setContentView(R.layout.dialog_credentials_saved);
                csaved_dialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        csaved_dialog.dismiss();
                        Intent intent = new Intent(SelectConnectionActivity.this, ConnectionSettingsActivity.class);
                        intent.putExtra("Connected", true);
                        startActivity(intent);
                        finish();
                    }
                }, 3000);
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

        // Spinner element
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        List<String> categories = new ArrayList<String>();
        categories.add("WEP");
        categories.add("WAP");
        categories.add("WAP2");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();

        spinner.setPrompt(item);
        // Showing selected spinner item
        Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

}
