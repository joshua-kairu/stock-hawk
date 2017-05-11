package com.sam_chordas.android.stockhawk.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * {@link RemoteViewsService} to populate the detail widget
 * */
// RemoteViewsService - The service to be connected to for a remote adapter to request RemoteViews.
//  Users should extend the RemoteViewsService to provide the appropriate RemoteViewsFactory's used
//  to populate the remote collection view (ListView, GridView, etc).
// begin class DetailWidgetRemoteViewsService
public class DetailWidgetRemoteViewsService extends RemoteViewsService {

    /* CONSTANTS */

    /* Integers */

    /* Strings */

    /* VARIABLES */

    /* CONSTRUCTOR */

    /* METHODS */

    /* Getters and Setters */

    /* Overrides */

    /**
     * Implemented by the derived {@link RemoteViewsService} to generate appropriate factories for
     * the data.
     * */
    @Override
    // begin onGetViewFactory
    public RemoteViewsFactory onGetViewFactory( Intent intent ) {

        // 0. return a factory for the detail widget's remote views

        // 0. return a factory for the detail widget's remote views

        return new DetailWidgetRemoteViewsFactory( this.getApplicationContext() );

    } // end onGetViewFactory

    /* Other Methods */

} // end class DetailWidgetRemoteViewsService
