package com.avnet.memec.ui.activities;

import android.os.Bundle;
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
        final ProgressBar spin = (ProgressBar) findViewById(R.id.progress_spin);
        spin.setVisibility(View.GONE);

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scan.setBackground(getResources().getDrawable(R.drawable.round_button_progress));
                scan.setText("Scanning for gateways...");
                scan.setTextColor(getResources().getColor(R.color.theme_neutral_dark_grey));
                spin.setVisibility(View.VISIBLE);
            }
        });

    }
}
