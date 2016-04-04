package com.avnet.memec.ui.activities;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.avnet.memec.R;
import com.avnet.memec.ui.util.MySingleton;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class ConnectionSettingsActivity extends BaseActivity {

    BluetoothDevice btSelectedDevice;
    String deviceName = "";
    String gattId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_settings);

        btSelectedDevice = MySingleton.getInstance().connectedDevice;
        gattId = MySingleton.getInstance().gattID;

        final TextView connectedGTitle = (TextView) findViewById(R.id.ctd_gateway);
        final TextView gatewayID = (TextView) findViewById(R.id.gateway_id);
        final TextView numOfSensors = (TextView) findViewById(R.id.num_sensors);
        final TextView wanStatus = (TextView) findViewById(R.id.wan_status);
        final TextView serverStatus = (TextView) findViewById(R.id.server_status);
        final TextView activeWAN = (TextView) findViewById(R.id.active_wan);
        final Button wanSettings = (Button) findViewById(R.id.wan_setting);
        //Button connectToWan = (Button) findViewById(R.id.connect_to_wan);
        final Button closeActivity = (Button) findViewById(R.id.close_activity);
        final Button closeConnection = (Button) findViewById(R.id.close_connection);

        final Handler handler = new Handler(Looper.getMainLooper());
        stopHandler = false;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(Build.VERSION.SDK_INT < 21) {
                    if(!checkBT(mBluetoothAdapter)){
                        onBackPressed();
                    }
                } else {
                    if(!checkBT(ba)){
                        onBackPressed();
                    }
                }
                numOfSensors.setText(String.valueOf(MySingleton.getInstance().noOfSensors));

                byte[] bytes = new byte[2];
                ByteBuffer buf = ByteBuffer.wrap(bytes);
                buf.putChar((char) MySingleton.getInstance().connectionStatus);
                switch ((int) buf.get(1)) {
                    case 0:
                        wanStatus.setText("NOT OK");
                        wanStatus.setBackgroundColor(getResources().getColor(R.color.theme_primary_red));
                        break;
                    case 1:
                        wanStatus.setText("UNINITIALIZED");
                        wanStatus.setBackgroundColor(getResources().getColor(R.color.theme_primary_red));
                        break;
                    case 2:
                        wanStatus.setText("DISCONNECTED");
                        wanStatus.setBackgroundColor(getResources().getColor(R.color.theme_primary_red));
                        break;
                    case 3:
                        wanStatus.setText("CONNECTING");
                        wanStatus.setBackgroundColor(getResources().getColor(R.color.theme_primary_amber));
                        break;
                    case 4:
                        wanStatus.setText("CONNECTED");
                        wanStatus.setBackgroundColor(getResources().getColor(R.color.theme_primary_green));
                        break;
                    default:
                        wanStatus.setText("N/A");
                }
                switch ((int) buf.get(0)) {
                    case 0:
                        serverStatus.setText("NOT OK");
                        serverStatus.setBackgroundColor(getResources().getColor(R.color.theme_primary_red));
                        break;
                    case 1:
                        serverStatus.setText("UNINITIALIZED");
                        serverStatus.setBackgroundColor(getResources().getColor(R.color.theme_primary_red));
                        break;
                    case 2:
                        serverStatus.setText("DISCONNECTED");
                        serverStatus.setBackgroundColor(getResources().getColor(R.color.theme_primary_red));
                        break;
                    case 3:
                        serverStatus.setText("CONNECTING");
                        serverStatus.setBackgroundColor(getResources().getColor(R.color.theme_primary_amber));
                        break;
                    case 4:
                        serverStatus.setText("CONNECTED");
                        serverStatus.setBackgroundColor(getResources().getColor(R.color.theme_primary_green));
                        break;
                    default:
                        serverStatus.setText("N/A");
                }
                activeWAN.setText(MySingleton.getInstance().connectionType);
                //Log.d("ConnectionStatus", buf.get(0)+", "+buf.get(1));
                if(!stopHandler){
                    frequentReadFromDevice(MySingleton.getInstance().connectedDevice, false, false);
                    handler.postDelayed(this, 6000);
                }
            }
        }, 2000);
        if(deviceName.equals("")){
            deviceName = btSelectedDevice.getName();
        }
        connectedGTitle.setText("Connected to " + deviceName);
        gatewayID.setText(gattId);

        wanSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT < 21) {
                    if(checkBT(mBluetoothAdapter)){
                        if (mBluetoothAdapter.isEnabled()) {
                            stopHandler = true;
                            Intent intent = new Intent(ConnectionSettingsActivity.this, SelectConnectionActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                } else {
                    if(checkBT(ba)){
                        if (ba.isEnabled()) {
                            stopHandler = true;
                            Intent intent = new Intent(ConnectionSettingsActivity.this, SelectConnectionActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }
            }
        });
        closeActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(ConnectionSettingsActivity.this, R.style.Theme_Dialog);
                dialog.setContentView(R.layout.dialog_gateway_close);
                Button disconnectButton = (Button) dialog.findViewById(R.id.disconnect_gw);
                disconnectButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {*/
                                disconnect();
                                closeGatt();
                            /*}
                        },5000);*/
                        MySingleton.getInstance().disconnectFlow = true;
                        Intent intent = new Intent(ConnectionSettingsActivity.this, GatewayListActivity.class);
                        intent.putExtra("ConnectionSettingsFlow", true);
                        startActivity(intent);
                        finish();
                        dialog.dismiss();
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
            }
        });

        Button viewSensors = (Button) findViewById(R.id.view_sensors);
        viewSensors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConnectionSettingsActivity.this, ViewSensorsActivity.class);
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

    @Override
    public void onBackPressed() {
        Log.d("Back Pressed","true");
        disconnect();
        closeGatt();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopHandler = true;
    }
}
