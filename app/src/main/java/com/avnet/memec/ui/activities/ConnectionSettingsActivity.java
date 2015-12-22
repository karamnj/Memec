package com.avnet.memec.ui.activities;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.avnet.memec.R;

public class ConnectionSettingsActivity extends AppCompatActivity {

    boolean connected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_settings);

        connected = getIntent().getBooleanExtra("Connected", false);

        TextView connectedGTitle = (TextView) findViewById(R.id.ctd_gateway);
        TextView gatewayID = (TextView) findViewById(R.id.gateway_id);
        TextView numOfSensors = (TextView) findViewById(R.id.num_sensors);
        TextView wanStatus = (TextView) findViewById(R.id.wan_status);
        Button wanSettings = (Button) findViewById(R.id.wan_setting);
        Button connectToWan = (Button) findViewById(R.id.connect_to_wan);
        Button closeActivity = (Button) findViewById(R.id.close_activity);
        Button closeConnection = (Button) findViewById(R.id.close_connection);

        if(connected){
            wanSettings.setVisibility(View.GONE);
            connectToWan.setVisibility(View.GONE);
            closeConnection.setVisibility(View.VISIBLE);
            wanStatus.setBackgroundColor(getResources().getColor(R.color.theme_primary_green));
            wanStatus.setText("Connected");
        }

        wanSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConnectionSettingsActivity.this, SelectConnectionActivity.class);
                startActivity(intent);
                finish();
            }
        });
        closeActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConnectionSettingsActivity.this, GatewayListActivity.class);
                startActivity(intent);
                finish();
            }
        });

        /*//Floating Action Button
        FloatingActionButton fab = (FloatingActionButton) this.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConnectionSettingsActivity.this, LoadingActivity.class);
                startActivity(intent);
                finish();
            }
        });*/
    }
}
