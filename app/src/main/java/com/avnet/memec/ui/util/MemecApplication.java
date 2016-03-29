package com.avnet.memec.ui.util;

import android.app.Application;

/**
 * Created by niranjank on 2/13/16.
 */
public class MemecApplication extends Application {

    @Override
    public void onCreate()
    {
        super.onCreate();

        // Initialize the singletons so their instances
        // are bound to the application process.
        initSingletons();
    }

    protected void initSingletons()
    {
        // Initialize the instance of MySingleton
        MySingleton.initInstance();
    }

    public void customAppMethod()
    {
        // Custom application method
    }

}
