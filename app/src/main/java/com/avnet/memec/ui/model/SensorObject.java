package com.avnet.memec.ui.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by niranjank on 2/12/16.
 */
public class SensorObject implements Parcelable {

    String deviceName;
    String deviceId;
    ArrayList<SensorData> sensors;

    public SensorObject(){

    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public ArrayList<SensorData> getSensors() {
        return sensors;
    }

    public void setSensors(ArrayList<SensorData> sensors) {
        this.sensors = sensors;
    }

    // Parcelling part
    public SensorObject(Parcel in){
        String[] data = new String[3];

        in.readStringArray(data);
        this.deviceName = data[0];
        this.deviceId = data[1];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.deviceName,
                this.deviceId});
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public SensorObject createFromParcel(Parcel in) {
            return new SensorObject(in);
        }

        public SensorObject[] newArray(int size) {
            return new SensorObject[size];
        }
    };
}
