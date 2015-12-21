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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_settings);

        TextView connectedGTitle = (TextView) findViewById(R.id.ctd_gateway);
        EditText gatewayID = (EditText) findViewById(R.id.gateway_id);
        EditText numOfSensors = (EditText) findViewById(R.id.num_sensors);
        EditText wanStatus = (EditText) findViewById(R.id.wan_status);
        Button wanSettings = (Button) findViewById(R.id.wan_setting);
        Button connectToWan = (Button) findViewById(R.id.connect_to_wan);
        Button closeActivity = (Button) findViewById(R.id.close_activity);

        //Floating Action Button
        FloatingActionButton fab = (FloatingActionButton) this.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConnectionSettingsActivity.this, LoadingActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
