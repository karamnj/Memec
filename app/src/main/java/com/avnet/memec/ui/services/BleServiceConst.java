package com.avnet.memec.ui.services;


public class BleServiceConst
{
    // Broadcast actions
    public final static String ACTION_GATT_CONNECTED = "com.avnet.memec.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.avnet.memec.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.avnet.memec.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED_FAILED = "com.avnet.memec.ACTION_GATT_SERVICES_DISCOVERED_FAILED";
    public final static String ACTION_ENV_DATA_AVAILABLE = "com.avnet.memec.ACTION_ENV_DATA_AVAILABLE";
    public final static String SERVICE_STOPPED = "com.avnet.memec.SERVICE_STOPPED";

    // Intent flags
    public final static String ENV_DATA = "com.avnet.memec.ENV_DATA";
    public final static String BLE_DEVICE = "com.avnet.memec.BLE_DEVICE";
}
