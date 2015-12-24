package com.avnet.memec.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.avnet.memec.R;

public class ScanActivity extends AppCompatActivity {

    private static final String CLASS_NAME = ScanActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        final Button scan = (Button) findViewById(R.id.scan_gateways);
        Button viewSensors = (Button) findViewById(R.id.view_sensors);
        final ProgressBar spin = (ProgressBar) findViewById(R.id.progress_spin);
        spin.setVisibility(View.GONE);

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scan.setBackground(getResources().getDrawable(R.drawable.round_button_progress));
                scan.setText("Scanning for gateways...");
                scan.setTextColor(getResources().getColor(R.color.theme_bg_dark_grey));
                spin.setVisibility(View.VISIBLE);
                spin.bringToFront();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(ScanActivity.this, GatewayListActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, 3000);
            }
        });

        viewSensors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScanActivity.this, ViewSensorsActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}
