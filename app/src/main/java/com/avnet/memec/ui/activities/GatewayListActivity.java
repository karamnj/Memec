package com.avnet.memec.ui.activities;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.avnet.memec.R;

public class GatewayListActivity extends AppCompatActivity {

    ListView listView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gateway_list);

        listView = (ListView) findViewById(R.id.gateway_list);

        String[] gateway_values = new String[] { "Gateway01_01",
                "Gateway01_02",
                "Gateway01_03",
                "Gateway01_04",
                "Gateway01_05"
        };

        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, gateway_values);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item_gateway, R.id.gateway_text, gateway_values);

        listView.setAdapter(adapter);

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

            }

        });

        FloatingActionButton fab = (FloatingActionButton) this.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GatewayListActivity.this, LoadingActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
