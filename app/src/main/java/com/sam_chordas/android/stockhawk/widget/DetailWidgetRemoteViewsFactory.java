package com.sam_chordas.android.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.widget.AbsListView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.ui.StockDetailActivity;

import static com.sam_chordas.android.stockhawk.data.QuoteColumns.*;

/**
 * {@link android.widget.RemoteViewsService.RemoteViewsFactory} to generate
 * {@link android.widget.RemoteViews} for the detail widget's list.
 * */
// begin class DetailWidgetRemoteViewsFactory
class DetailWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    /* CONSTANTS */

    /* Integers */

    /* Strings */

    /* VARIABLES */

    /* Contexts */

    private Context mContext; // ditto

    /* Cursors */

    private Cursor mCursor; // ditto

    /* CONSTRUCTOR */

    // begin constructor
    DetailWidgetRemoteViewsFactory( Context context ) {

        // 0. initialize
        // 0a. context

        // 0. initialize

        // 0a. context

        mContext = context;

    } // end constructor

    /**
     * Called when your factory is first constructed. The same factory may be shared across
     * multiple RemoteViewAdapters depending on the intent passed.
     *
     * It's a good place to set up connections/cursors to data sources. However, heavy lifting,
     * e.g. downloading/creating content should be deferred to
     * {@link RemoteViewsService.RemoteViewsFactory#onDataSetChanged()} or
     * {@link android.widget.RemoteViewsService.RemoteViewsFactory#getViewAt(int)}.
     *
     * Taking more than 20 seconds here results in an ANR.
     * */
    @Override
    // begin onCreate
    public void onCreate() {

        // 0. set up the cursor to fetch current stock info

        // 0. set up the cursor to fetch current stock info

        String onlyCurrentStocksSelection = ISCURRENT + " = ?";

        String[] onlyCurrentStocksSelectionArgs = new String[] { "1" }; // current stocks have a 1
                                                                        // in this column

        mCursor = mContext.getContentResolver().query( QuoteProvider.Quotes.CONTENT_URI,
                STOCKS_COLUMNS, onlyCurrentStocksSelection,
                onlyCurrentStocksSelectionArgs, null );

    } // end onCreate

    /**
     * See {@link android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)}.
     * Note: expensive tasks can be safely performed synchronously within this method, and a
     * loading view will be displayed in the interim.
     * See {@link android.widget.RemoteViewsService.RemoteViewsFactory#getLoadingView()}.
     * */
    @Override
    // begin getViewAt
    public RemoteViews getViewAt( int position ) {

        // 0. stop if
        // 0a. the position is invalid, or
        // 0b. the cursor doesn't exist, or
        // 0c. there is nothing at the given position
        // 1. get a remote views based on the XML layout
        // 2. put relevant details in
        // 2a. stock symbol
        // 2b. bid price
        // 2c. up/down status
        // 2d. (percent) change
        // 3. put a content description - // TODO: 5/2/17 Content description here
        // 4. create a fill in intent to respond to taps
        // 4a. when tapped, send a tapped broadcast
        // which will be received by the detail widget provider
        // last. return the remote views

        // 0. stop if

        // 0a. the position is invalid, or
        // 0b. the cursor doesn't exist, or
        // 0c. there is nothing at the given position

        if ( position == AbsListView.INVALID_POSITION || mCursor == null
                || !mCursor.moveToPosition( position ) ) {
            return null;
        }

        // by this time the cursor's successfully moved to the position referred to in the parameter

        // 1. get a remote views based on the XML layout

        RemoteViews views = new RemoteViews( mContext.getPackageName(),
                R.layout.widget_detail_list_item );

        // 2. put relevant details in

        // 2a. stock symbol

        views.setTextViewText( R.id.widget_list_item_tv_stock_symbol,
                mCursor.getString( STOCKS_COLUMN_SYMBOL ) );

        // 2b. bid price

        views.setTextViewText( R.id.widget_list_item_tv_bid_price,
                mCursor.getString( STOCKS_COLUMN_BIDPRICE ) );

        // 2c. up/down status

        // if the stock is up, show a green pill
        if ( mCursor.getInt( STOCKS_COLUMN_ISUP ) == 1 ) {
            views.setInt( R.id.widget_list_item_tv_change, "setBackgroundResource",
                    R.drawable.percent_change_pill_green );
        }

        // else the stock is down, so show a red pill
        else {
            views.setInt( R.id.widget_list_item_tv_change, "setBackgroundResource",
                    R.drawable.percent_change_pill_red );
        }

        // 2d. (percent) change

        if ( Utils.getChangeUnits( mContext ).equals(
                mContext.getString( R.string.pref_change_units_percents_value ) ) ) {
            views.setTextViewText( R.id.widget_list_item_tv_change,
                    mCursor.getString( STOCKS_COLUMN_PERCENT_CHANGE ) );
        } else {
            views.setTextViewText( R.id.widget_list_item_tv_change,
                    mCursor.getString( STOCKS_COLUMN_CHANGE ) );
        }

        // 3. put a content description - // TODO: 5/2/17 Content description here

        // 4. create a fill in intent to respond to taps

        Intent fillInIntent = new Intent().putExtra( StockDetailActivity.KEY_SYMBOL,
                mCursor.getString( STOCKS_COLUMN_SYMBOL ) );

        // 4a. when tapped, send a tapped broadcast
        // which will be received by the detail widget provider

        // setOnClickFillInIntent - fills in any details not in the intent template provided to this
        // AbsListView. In our case the template (found in DetailWidgetProvider) only provided the
        // activity to start (StockDetailActivity). The intent passed here fills in information that
        // will help StockDetailActivity know which stock symbol to use
        views.setOnClickFillInIntent( R.id.widget_list_item, fillInIntent );

        // last. return the remote views

        return views;

    } // end getViewAt

    /**
     * Called when notifyDataSetChanged() is triggered on the remote adapter. This allows a
     * RemoteViewsFactory to respond to data changes by updating any internal references.
     *
     * Note: expensive tasks can be safely performed synchronously within this method. In the
     * interim, the old data will be displayed within the widget.
     * */
    @Override
    // begin onDataSetChanged
    public void onDataSetChanged() {

        // 0. refresh the cursor
        // 0a. close the cursor if it is there
        // 0b. since our content provider is not exported, clear the calling identity so that calls
        // to the provider use our process and permissions
        // 0c. fetch (the new) data from the db
        // 0d. since our content provider is not exported, restore the calling identity so that
        // calls to the provider use our process and permissions

        // 0. refresh the cursor

        // 0a. close the cursor if it is there

        if ( mCursor != null ) { mCursor.close(); }

        // 0b. since our content provider is not exported, clear the calling identity so that calls
        // to the provider use our process and permissions

        final long identityToken = Binder.clearCallingIdentity();

        // 0c. fetch (the new) data from the db

        String onlyCurrentStocksSelection = ISCURRENT + " = ?";

        String[] onlyCurrentStocksSelectionArgs = new String[] { "1" }; // current stocks have a 1
        // in this column

        mCursor = mContext.getContentResolver().query( QuoteProvider.Quotes.CONTENT_URI,
                STOCKS_COLUMNS, onlyCurrentStocksSelection,
                onlyCurrentStocksSelectionArgs, null );

        // 0d. since our content provider is not exported, restore the calling identity so that
        // calls to the provider use our process and permissions

        Binder.restoreCallingIdentity( identityToken );

    } // end onDataSetChanged

    /**
     * @return The number of types of Views that will be returned by this factory.
     *         In our case, just one.
     */
    @Override
    public int getViewTypeCount() {
        return 1;
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the data set whose row id we want.
     * @return The id of the item at the specified position.
     *
     * */
    @Override
    // begin getItemId
    public long getItemId( int position ) {

        // 0. if we can get the stock id of the stock in this given position, return that stock id
        // 1. else just return the parameter position

        // 0. if we can get the stock id of the stock in this given position, return that stock id

        if ( mCursor.moveToPosition( STOCKS_COLUMN_ID ) ) {
            return mCursor.getLong( STOCKS_COLUMN_ID );
        }

        // 1. else just return the parameter position

        return position;

    } // end getItemId

    /**
     * Gets the number of items in the in the data set represented by this Adapter. Since we use
     * a {@link Cursor}, we'll just get the number of items in the cursor, or 0 if there is no
     * cursor
     *
     * @return Count of items, or 0 if the cursor exists not.
     */
    @Override
    public int getCount() { return mCursor == null ? 0 : mCursor.getCount(); }

    /**
     * Indicates whether the item ids are stable across changes to the underlying data.
     *
     * @return True if the same id always refers to the same object.
     * */
    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     * This allows for the use of a custom loading view which appears between the time that
     * {@link #getViewAt(int)} is called and returns. If null is returned, a default loading
     * view will be used.
     *
     * We will use the widget detail list item view as the loading view.
     *
     * @return The RemoteViews representing the desired loading view.
     */
    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews( mContext.getPackageName(), R.layout.widget_detail_list_item );
    }

    /**
     * Called when the last RemoteViewsAdapter that is associated with this factory is
     * unbound.
     */
    @Override
    // begin onDestroy
    public void onDestroy() {

        // 0. close and destroy the cursor

        // 0. close and destroy the cursor

        if ( mCursor != null ) { mCursor.close(); mCursor = null; }

    } // end onDestroy

    /* METHODS */

    /* Getters and Setters */

    /* Overrides */

    /* Other Methods */

} // end class DetailWidgetRemoteViewsFactory
