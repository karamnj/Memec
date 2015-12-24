package com.avnet.memec.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;

import com.avnet.memec.R;
import com.avnet.memec.ui.adaptors.ViewSensorsAdapter;

public class ViewSensorsActivity extends AppCompatActivity {

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_sensors);

        ViewSensorsAdapter mAdapter;

        mAdapter = new ViewSensorsAdapter(this);
        mAdapter.addSectionHeaderItem("5 sensor objects found");
        mAdapter.addItem("Sensor Object 1");
        mAdapter.addItem("Sensor Object 2");
        mAdapter.addItem("Sensor Object 3");
        mAdapter.addItem("Sensor Object 4");
        mAdapter.addItem("Sensor Object 5");

        listView = (ListView) findViewById(R.id.sensor_list);
        listView.setAdapter(mAdapter);

        final SwipeRefreshLayout srl = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        srl.setNestedScrollingEnabled(true);
        srl.startNestedScroll(View.SCROLL_AXIS_VERTICAL);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //final ProgressBar spin = (ProgressBar) findViewById(R.id.progress_spin);
                //spin.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        srl.setRefreshing(false);
                        //spin.setVisibility(View.GONE);
                        //Check for devices and Update List View
                    }
                }, 3000);
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

        Button backToHome = (Button) findViewById(R.id.back_to_home);
        backToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewSensorsActivity.this, ScanActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
