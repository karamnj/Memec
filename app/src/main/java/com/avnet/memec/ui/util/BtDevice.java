package com.avnet.memec.ui.util;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

public class BtDevice implements Parcelable
{
    private BluetoothDevice bluetoothDevice;
    private boolean isAdvMsgServiceCompatible;
    private String btName;


    public BluetoothDevice getBluetoothDevice(){ return this.bluetoothDevice; }
    public boolean isAdvMsgServiceCompatible() { return isAdvMsgServiceCompatible; }


    public BtDevice(BluetoothDevice bluetoothDevice, boolean isAdvMsgServiceCompatible, String btName)
    {
        this.bluetoothDevice = bluetoothDevice;
        this.isAdvMsgServiceCompatible = isAdvMsgServiceCompatible;
        this.btName = btName;
    }
    public String getBtName(){
        return btName;
    }



    // --- PARCELABLE CODE SECTION ---
    protected BtDevice(Parcel in)
    {
        bluetoothDevice = (BluetoothDevice) in.readValue(BluetoothDevice.class.getClassLoader());
        isAdvMsgServiceCompatible = in.readByte() != 0x00;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeValue(bluetoothDevice);
        dest.writeByte((byte) (isAdvMsgServiceCompatible ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Creator<BtDevice> CREATOR = new Creator<BtDevice>()
    {
        @Override
        public BtDevice createFromParcel(Parcel in)
        {
            return new BtDevice(in);
        }

        @Override
        public BtDevice[] newArray(int size)
        {
            return new BtDevice[size];
        }
    };
}
