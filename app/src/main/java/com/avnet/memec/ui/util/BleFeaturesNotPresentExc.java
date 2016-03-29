package com.avnet.memec.ui.util;

/**
 * Class wrapping the exception structure for BLE not present
 */
public class BleFeaturesNotPresentExc extends Exception
{
    private static final String msg = "BLE features not present on this device";


    /**
     * Create BleFeatureNotPresent obj
     */
    public BleFeaturesNotPresentExc(){
        super(msg);
    }

    /**
     * Create BleFeatureNotPresent with inner exception
     *
     * @param innExc Inner exception to associate to BleFeatureNotPresent obj
     */
    public BleFeaturesNotPresentExc(Exception innExc){
        super(msg, innExc);
    }
}
