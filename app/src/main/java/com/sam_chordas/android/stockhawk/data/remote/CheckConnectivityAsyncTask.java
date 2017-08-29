package com.sam_chordas.android.stockhawk.data.remote;

import android.os.AsyncTask;

import com.sam_chordas.android.stockhawk.event.ConnectivityEvent;
import com.sam_chordas.android.stockhawk.rest.Utils;

import org.greenrobot.eventbus.EventBus;

/**
 * {@link AsyncTask} to check if we are connected to the internet
 */
// begin class CheckConnectivityAsyncTask
public class CheckConnectivityAsyncTask extends AsyncTask <Void, Void, Boolean> {

    /* CONSTANTS */
    
    /* VARIABLES */
    
    /* CONSTRUCTOR */
    
    /* METHODS */
    
    /* Getters and Setters */
    
    /* Overrides */

    @Override
    // begin doInBackground
    protected Boolean doInBackground( Void... aVoid ) {

        // 0. check if there is a connection

        // 0. check if there is a connection

        return Utils.isInternetUp();

    } // end doInBackground

    @Override
    // begin onPostExecute
    protected void onPostExecute( Boolean connected ) {

        // 0. post an event with the connected state

        // 0. post an event with the connected state

        EventBus.getDefault().post( new ConnectivityEvent( connected ) );

    } // end onPostExecute

    /* Other Methods */

} // end class CheckConnectivityAsyncTask
