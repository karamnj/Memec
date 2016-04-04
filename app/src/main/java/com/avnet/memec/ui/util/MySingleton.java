package com.avnet.memec.ui.util;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.avnet.memec.ui.model.SensorObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MySingleton
{
	private static MySingleton instance;

	public String connectionType;
	public int noOfSensors;
	public int connectionStatus;
	public String[] charValue;
	public String gattID;
	public BluetoothDevice connectedDevice = null;
	public BtDevice btSensorSelected = null;
	public ArrayList<BluetoothGatt> myGattList = null;
	public boolean bthsChecked = false;
	public HashSet<BluetoothDevice> btDeviceHash;
	public HashMap<String,SensorObject> sensorMap;
	public Boolean disconnectFlow = false;

	public static void initInstance()
	{
		if (instance == null)
		{
			// Create the instance
			instance = new MySingleton();
		}
	}

	public static MySingleton getInstance()
	{
		// Return the instance
		return instance;
	}
	
	private MySingleton()
	{
		// Constructor hidden because this is a singleton
	}
	
	public void customSingletonMethod()
	{
		// Custom method
	}
}
