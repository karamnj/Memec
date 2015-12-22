package com.avnet.memec.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.avnet.memec.R;

public class LoadingActivity extends BaseActivity {

    private static final String CLASS_NAME = LoadingActivity.class.getSimpleName();

    private LoadingActivity currentObject;
    ProgressBar bar;
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        currentObject = this;
        Log.d(CLASS_NAME, "Set current object");
        bar = (ProgressBar) findViewById(R.id.loading_progress);
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        bar.setProgress(90);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(LoadingActivity.this, ScanActivity.class);
                startActivity(intent);
                finish();
            }
        }, 3000);

    }
}
