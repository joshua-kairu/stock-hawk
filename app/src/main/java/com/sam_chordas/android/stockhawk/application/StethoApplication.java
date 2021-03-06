package com.sam_chordas.android.stockhawk.application;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * An {@link android.app.Application} instance to assist with {@link com.facebook.stetho.Stetho}.
 *
 * Thanks to these two sites for explanations:
 * a) https://code.tutsplus.com/tutorials/debugging-android-apps-with-facebooks-stetho--cms-24205
 * b) http://facebook.github.io/stetho/
 * c) https://developer.android.com/studio/build/multidex.html#mdex-gradle - Multi-dex usage
 */
// begin class StethoApplication
public class StethoApplication extends Application {

    /* CONSTANTS */
    
    /* Integers */
    
    /* Strings */

    /* VARIABLES */
    
    /* CONSTRUCTOR */
    
    /* METHODS */
    
    /* Getters and Setters */
    
    /* Overrides */

    @Override
    // begin onCreate
    public void onCreate() {

        // 0. super stuff
        // 1. initialize stetho with defaults

        // 0. super stuff

        super.onCreate();

        // 1. initialize stetho with defaults

        Stetho.initializeWithDefaults( this );

    } // end onCreate

    /* Other Methods */

} // end class StethoApplication
