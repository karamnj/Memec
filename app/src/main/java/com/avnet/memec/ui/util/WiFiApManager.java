package com.avnet.memec.ui.util;

import android.content.Context;
import android.content.ContextWrapper;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by halsteada on 05/09/2015.
 */
public class WiFiApManager extends ContextWrapper{

    private WifiManager wifiManager;
    private static Method getWifiApConfiguration;
    private static Method getWifiApState;
    private static Method setWifiApEnabled;
    private static Method getWifiState;
    private static Method setWiFiState;

    public static final int SECURITY_NONE = 0;
    public static final int SECURITY_WEP = 1;
    public static final int SECURITY_PSK = 2;
    public static final int SECURITY_EAP = 3;

    public enum WIFI_AP_CONFIG { SSID, PASSWORD, TYPE}

    public WiFiApManager(Context base)
    {
        super(base);
    }

    public boolean isApModeSupported()
    {
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        for(Method method:WifiManager.class.getDeclaredMethods()) {
            String methodName = method.getName();

            if(methodName.contains("Wifi") )
            {
                Log.d("VT", methodName);
            }
            if (methodName.equals("getWifiApConfiguration")) {
                getWifiApConfiguration = method;
            }
            if (methodName.equals("getWifiApState"))
            {
                getWifiApState = method;
            }
            if (methodName.equals("getWifiState"))
            {
                getWifiState = method;
            }
            if (methodName.equals("setWiFiState"))
            {
                setWiFiState = method;
            }
            if (methodName.equals("setWifiApEnabled")) {
                setWifiApEnabled = method;
            }
        }

        if( ( getWifiApState==null) || ( getWifiApState==null) ) return false;
        else return true;

    }

    public int getWiFiState()
    {
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        int ret = wifiManager.getWifiState();
        return ret;
    }
    public void setWiFiState(boolean enabled)
    {
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(enabled);
    }
    public int getWiFiApState()
    {
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        try
        {
            return (Integer) getWifiApState.invoke(wifiManager);
        }
        catch (Exception e)
        {
            Log.v("VT", e.toString(), e); // shouldn't happen
            return -1;

        }
    }
    public boolean setWifiApEnabled(boolean enabled) {
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration wifiConfiguration;
        try {
            wifiConfiguration = (WifiConfiguration) getWifiApConfiguration.invoke(wifiManager);
            return (Boolean) setWifiApEnabled.invoke(wifiManager, wifiConfiguration, enabled);
        } catch (Exception e) {
            Log.v("VT", e.toString(), e); // shouldn't happen
            return false;
        }
    }
    public String getWifiApConfiguration(WIFI_AP_CONFIG info)
    {
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration wifiConfiguration;

        try
        {
            wifiConfiguration = (WifiConfiguration) getWifiApConfiguration.invoke(wifiManager);
            Log.d("VT", wifiConfiguration.SSID);
            Log.d("VT", wifiConfiguration.preSharedKey);
            Log.d("VT", getSecurityType(wifiConfiguration));

            switch(info)
            {
                case SSID:
                    return wifiConfiguration.SSID;
                case PASSWORD:
                    return wifiConfiguration.preSharedKey;
                case TYPE:
                    return getSecurityType(wifiConfiguration);
                default:
                    return "No AP config type found";
            }

        }
        catch (Exception e)
        {
            Log.v("VT",e.toString(),e); // shouldn't happen
            return "Error";
        }

    }
    /*private static String getSecurity(final WifiConfiguration network) {
        if(network.allowedGroupCiphers.get(WifiConfiguration.GroupCipher.CCMP)) {
            return "WPA2-PSK";
        }
        else if(network.allowedGroupCiphers.get(WifiConfiguration.GroupCipher.TKIP)) {
            return "WPA-PSK";
        }
        else if(network.allowedGroupCiphers.get(WifiConfiguration.GroupCipher.WEP40)
                || network.allowedGroupCiphers.get(WifiConfiguration.GroupCipher.WEP104)) {
            return "WEP";
        }
        else return "Open";
    }*/
    public static int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK))
            return SECURITY_PSK;

        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) || config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X))
            return SECURITY_EAP;

        return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
    }
    public static String getSecurityType(WifiConfiguration config) {
        switch (getSecurity(config)) {
            case SECURITY_WEP:
                return "WEP";
            case SECURITY_PSK:
                if (config.allowedProtocols.get(WifiConfiguration.Protocol.RSN))
                    return "WPA2-PSK";
                else
                    return "WPA-PSK";
            default:
                return "Open";
        }
    }
    /*//check whether wifi hotspot on or off
    public static boolean isApOn(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        try {
            Method method = wifimanager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifimanager);
        }
        catch (Throwable ignored) {}
        return false;
    }
    // toggle wifi hotspot on or off
    public static boolean configApState(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiConfiguration wificonfiguration = null;
        try {
            // if WiFi is on, turn it off
            if(isApOn(context)) {
                wifimanager.setWifiEnabled(false);
            }
            Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(wifimanager, wificonfiguration, !isApOn(context));
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }*/

}
