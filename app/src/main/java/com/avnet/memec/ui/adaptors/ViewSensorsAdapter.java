package com.avnet.memec.ui.adaptors;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.avnet.memec.R;
import com.avnet.memec.ui.activities.SensorDetailsActivity;
import com.avnet.memec.ui.model.SensorData;
import com.avnet.memec.ui.services.BleServiceConst;
import com.avnet.memec.ui.util.BtDevice;
import com.avnet.memec.ui.util.MySingleton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

/**
 * Created by admin on 24/12/15.
 */
public class ViewSensorsAdapter extends BaseAdapter {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;

    private Context context;

    private HashSet<String> hData = new HashSet<String>();
    private HashSet<ArrayList<SensorData>> hSensors = new HashSet<ArrayList<SensorData>>();
    private ArrayList<String> mData = new ArrayList<String>();
    private ArrayList<ArrayList<SensorData>> mSensors = new ArrayList<ArrayList<SensorData>>();
    private TreeSet<Integer> sectionHeader = new TreeSet<Integer>();

    private String noOfSensors = "";
    private HashSet<String> sensorNames =  new HashSet<String>();

    private LayoutInflater mInflater;

    public ViewSensorsAdapter(Context context) {
        this.context = context;
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItem(final String item) {
        hData.add(item);
        sensorNames.add(item);
        notifyDataSetChanged();
    }
    public void clearData() {
        hData.clear();
        sensorNames.clear();
        hSensors.clear();
        sectionHeader.clear();
    }
    public void notifyDataSet() {
        notifyDataSetChanged();
    }
    public void addSensorItem(final ArrayList<SensorData> item, String name) {
        if(name!=null) {
            hData.add(name);
        }else{
            hData.add("Undefined");
        }
        hSensors.add(item);
    }

    public void addSectionHeaderItem(final String item) {
        /*if(!hData.isEmpty()){
            mData = new ArrayList<String>(hData);
            hData.clear();
            int i=1;
            while(i<mData.size()){
                hData.add(mData.get(i));
            }
            sectionHeader.clear();
            sectionHeader.add(hData.size() - 1);
        }else {*/
        clearData();
            noOfSensors = item;
            hData.add(item);
            sectionHeader.add(hData.size() - 1);
        notifyDataSetChanged();
        //}
    }

    @Override
    public int getItemViewType(int position) {
        return sectionHeader.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return hData.size();
    }

    @Override
    public String getItem(int position) {
        mData = new ArrayList<String>(hData);
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        int rowType = getItemViewType(position);
Log.d("GetView",position+"");
        if (convertView == null) {
            holder = new ViewHolder();
            switch (rowType) {
                case TYPE_ITEM:
                    /*convertView = mInflater.inflate(R.layout.list_item_sensors, null);
                    holder.textView = (TextView) convertView.findViewById(R.id.object_id);
                    mData = new ArrayList<String>(hData);
                    holder.textView.setText(mData.get(position));
                    holder.sensorsHolder = (LinearLayout) convertView.findViewById(R.id.sensors_holder);
                    mSensors = new ArrayList<ArrayList<SensorData>>(hSensors);
                    addSensors(mSensors.get(position-1), holder.sensorsHolder);*/
                    convertView = mInflater.inflate(R.layout.list_item_gateway, null);
                    holder.textView = (TextView) convertView.findViewById(R.id.gateway_text);
                    final ArrayList<String> nameData = new ArrayList<String>(sensorNames);
                    holder.textView.setText(nameData.get(position-1));
                    Log.d("SensorName", nameData.get(position - 1) + " Position:" + position);
                    convertView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent event) {
                            FrameLayout fl;
                            TextView tv;
                            final int X = (int) event.getX();
                            final int Y = (int) event.getY();
                            switch(event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    fl = (FrameLayout) view.findViewById(R.id.gateway_frame);
                                    fl.setBackgroundColor(context.getResources().getColor(R.color.theme_primary_highlight));

                                    tv = (TextView) view.findViewById(R.id.gateway_text);
                                    tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                                    tv.setTextColor(context.getResources().getColor(R.color.white));
                                    return true;
                                case MotionEvent.ACTION_MOVE:
                                    if( !((X-event.getX()<10) && (X-event.getX()>-10)) || !((Y-event.getY()<10) && (Y-event.getY()>-10))){
                                        fl = (FrameLayout) view.findViewById(R.id.gateway_frame);
                                        fl.setBackgroundColor(context.getResources().getColor(R.color.white));

                                        tv = (TextView) view.findViewById(R.id.gateway_text);
                                        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                                        tv.setTextColor(context.getResources().getColor(R.color.theme_neutral_dark_grey));
                                    }
                                    break;

                                case MotionEvent.ACTION_UP:

                                    ArrayList<BluetoothDevice> btList = getBTDeviceList();
                                    if(btList.size()>0) {
                                        BtDevice bt = new BtDevice(btList.get(position - 1), true, nameData.get(position - 1));
                                        //BtDevice bt = new BtDevice(btList.get(position - 1), true, "VT Sensor - " + (position));
                                        Log.d("btDevice.getBtName()", bt.getBtName());
                                        MySingleton.getInstance().btSensorSelected = bt;

                                        Intent intent = new Intent(context, SensorDetailsActivity.class);
                                        intent.putExtra(BleServiceConst.BLE_DEVICE, position - 1);
                                        context.startActivity(intent);

                                        fl = (FrameLayout) view.findViewById(R.id.gateway_frame);
                                        fl.setBackgroundColor(context.getResources().getColor(R.color.white));

                                        tv = (TextView) view.findViewById(R.id.gateway_text);
                                        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                                        tv.setTextColor(context.getResources().getColor(R.color.theme_neutral_dark_grey));
                                    }
                                    return true;
                            }
                            return false;
                        }
                    });
                    break;
                case TYPE_SEPARATOR:
                    convertView = mInflater.inflate(R.layout.list_item_pull_with_count, null);
                    holder.textView = (TextView) convertView.findViewById(R.id.sensor_count);
                    //mData = new ArrayList<String>(hData);
                    holder.textView.setText(noOfSensors);
                    Log.d("Seperator", noOfSensors+" Position:"+position);
                    break;
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        return convertView;
    }
    protected ArrayList<BluetoothDevice> getBTDeviceList(){
        ArrayList<BluetoothDevice> btDeviceList = new ArrayList<BluetoothDevice>(MySingleton.getInstance().btDeviceHash);
        return btDeviceList;
    }

    private void addSensors(ArrayList<SensorData> list, LinearLayout holder){
        SensorData sensorData;
        View sensorView;
        int i = 0;
        while(i < list.size()){
            sensorData = list.get(i);
            sensorView = mInflater.inflate(R.layout.list_item_sensor_item, holder, false);
            TextView name = (TextView) sensorView.findViewById(R.id.sensor_name);
            ImageView image = (ImageView) sensorView.findViewById(R.id.sensor_image);
            TextView x = (TextView) sensorView.findViewById(R.id.x);
            TextView y = (TextView) sensorView.findViewById(R.id.y);
            TextView z = (TextView) sensorView.findViewById(R.id.z);

            name.setText(sensorData.getSensorType());
            image.setImageDrawable(getDrawable(sensorData.getSensorIcon()));
            x.setText(sensorData.getParam1());
            if(sensorData.getParam2()!=null){
                y.setText(sensorData.getParam2());
            }else{
                y.setVisibility(View.GONE);
            }
            if(sensorData.getParam3()!=null){
                z.setText(sensorData.getParam3());
            }else{
                z.setVisibility(View.GONE);
            }
            holder.addView(sensorView);
            i++;
        }
    }

    public Drawable getDrawable(String name) {
        int resourceId = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        return context.getResources().getDrawable(resourceId);
    }

    public static class ViewHolder {
        public TextView textView;
        public LinearLayout sensorsHolder;
    }

}
