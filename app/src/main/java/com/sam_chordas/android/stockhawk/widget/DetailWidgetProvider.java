package com.sam_chordas.android.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.BuildConfig;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;
import com.sam_chordas.android.stockhawk.ui.StockDetailActivity;

/**
 * {@link AppWidgetProvider} for the detail widget
 */
// begin class DetailWidgetProvider
public class DetailWidgetProvider extends AppWidgetProvider {

    /* CONSTANTS */
    
    /* Integers */
        
    /* Strings */
    
    /* VARIABLES */
    
    /* CONSTRUCTOR */
    
    /* METHODS */
        
    /* Getters and Setters */
        
    /* Overrides */

    @Override
    // begin onUpdate
    public void onUpdate( Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds ) {

        // 0. for each widget managed by this provider
        // 0a. get a remote views for the detail widget
        // 0b. create a pending intent to launch the stocks activity when the logo is tapped
        // 0c. set up the collection
        // 0d. create the intent template
        // 0e. put the intent template into the remote views to show the detail activity when an
        // individual stock is tapped
        // 0f. set remote views empty view
        // 0g. update widget

        // 0. for each widget managed by this provider

        // begin for through all widgets managed by this provider
        for ( int widgetId : appWidgetIds ) {

            // 0a. get a remote views for the detail widget

            // RemoteViews - a view hierarchy that can be displayed in another process.
            RemoteViews views = new RemoteViews( context.getPackageName(), R.layout.widget_detail );

            // 0b. create a pending intent to launch the stocks activity when the logo is tapped

            // PendingIntent - A description of an Intent and target action to perform with it.
            PendingIntent tapLogoPendingIntent = PendingIntent.getActivity( context, 0,
                    new Intent( context, MyStocksActivity.class ), 0 );

            views.setOnClickPendingIntent( R.id.widget_detail_fl_logo, tapLogoPendingIntent );

            // 0c. set up the collection

            views.setRemoteAdapter( R.id.widget_detail_lv_stocks,
                    new Intent( context, DetailWidgetRemoteViewsService.class ) );

            // 0d. create the intent template

            Intent tapStockIntent = new Intent( new Intent( context, StockDetailActivity.class ) );

            // 0e. put the intent template into the remote views to show the detail activity when an
            // individual stock is tapped

            // TaskStackBuilder - Utility class for constructing synthetic back stacks for
            //  cross-task navigation on Android 3.0 and newer.
            PendingIntent tapStockTemplatePendingIntent = TaskStackBuilder.create( context )
                    // Add a new Intent with the resolved chain of parents for the target activity
                    // to the task stack.
                    .addNextIntentWithParentStack( tapStockIntent )
                    // Obtain a PendingIntent for launching the task constructed by this builder so
                    // far.
                    .getPendingIntent( 0, PendingIntent.FLAG_UPDATE_CURRENT );

            views.setPendingIntentTemplate( R.id.widget_detail_lv_stocks,
                    tapStockTemplatePendingIntent );

            // 0f. set remote views empty view

            views.setEmptyView( R.id.widget_detail_lv_stocks, R.id.widget_detail_tv_empty );

            // 0g. update widget

            appWidgetManager.updateAppWidget( widgetId, views );

        } // end for through all widgets managed by this provider

    } // end onUpdate

    /**
     * Receives the {@link BuildConfig#ACTION_DATA_UPDATED} broadcast and updates the widget
     * accordingly.
     * */
    @Override
    // begin onReceive
    public void onReceive( Context context, Intent intent ) {

        // 0. super stuff
        // 1. if we receive the update action
        // 1a. get the widget manager
        // 1b. get the widget IDs for our detail widget
        // 1c. notify the list that data has changed

        // 0. super stuff

        super.onReceive( context, intent );

        // 1. if we receive the update action

        // begin if we have the update action
        if ( intent != null && intent.getAction().equals( BuildConfig.ACTION_DATA_UPDATED ) ) {

            // 1a. get the widget manager

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance( context );

            // 1b. get the widget IDs for our detail widget

            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName( context, DetailWidgetProvider.class ) );

            // 1c. notify the list that data has changed

            appWidgetManager.notifyAppWidgetViewDataChanged( appWidgetIds,
                    R.id.widget_detail_lv_stocks );

        } // end if we have the update action

    } // end onReceive

    /* Other Methods */

} // end class DetailWidgetProvider
