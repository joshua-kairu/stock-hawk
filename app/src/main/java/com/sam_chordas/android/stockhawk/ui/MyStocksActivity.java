package com.sam_chordas.android.stockhawk.ui;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.sam_chordas.android.stockhawk.BuildConfig;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.data.remote.CheckConnectivityAsyncTask;
import com.sam_chordas.android.stockhawk.event.ConnectivityEvent;
import com.sam_chordas.android.stockhawk.event.NoSuchStockEvent;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.sam_chordas.android.stockhawk.touch_helper.SimpleItemTouchHelperCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.sam_chordas.android.stockhawk.service.StockTaskService.STOCKS_STATUS_CONNECTING;
import static com.sam_chordas.android.stockhawk.service.StockTaskService.STOCKS_STATUS_NETWORK_DOWN;
import static com.sam_chordas.android.stockhawk.service.StockTaskService.STOCKS_STATUS_OK;
import static com.sam_chordas.android.stockhawk.service.StockTaskService.STOCKS_STATUS_OUTDATED;
import static com.sam_chordas.android.stockhawk.service.StockTaskService.STOCKS_STATUS_UNKNOWN;

public class MyStocksActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks< Cursor >, QuoteAdapterOnClickHandler,
        SharedPreferences.OnSharedPreferenceChangeListener {

    /* CONSTANTS */

    /* Integers */

    private static final int CURSOR_LOADER_ID = 0;

    /* Strings */

    /* VARIABLES */

    boolean isConnected;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private Intent mServiceIntent;
    private ItemTouchHelper mItemTouchHelper;
    private QuoteCursorAdapter mCursorAdapter;
    private Context mContext;
    private Cursor mCursor;

    /* FloatingActionButtons */

    private FloatingActionButton mFab; // ditto

    /* TextViews */

    private TextView mStatusTextView; // ditto

    /* CONSTRUCTOR */

    /* METHODS */

    /* Getters and Setters */

    /* Overrides */

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        mContext = this;
        ConnectivityManager cm =
                ( ConnectivityManager ) mContext.getSystemService( Context.CONNECTIVITY_SERVICE );

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        setContentView( R.layout.activity_my_stocks );
        // The intent service is for executing immediate pulls from the Yahoo API
        // GCMTaskService can only schedule tasks, they cannot execute immediately
        mServiceIntent = new Intent( this, StockIntentService.class );
//        if ( savedInstanceState == null ) {
//            // Run the initialize task service so that some stocks appear upon an empty database
//            mServiceIntent.putExtra( "tag", "init" );
//            if ( isConnected ) {
//                startService( mServiceIntent );
//            } else {
//                networkToast();
//            }
//        }
        RecyclerView recyclerView = ( RecyclerView ) findViewById( R.id.recycler_view );
        recyclerView.setLayoutManager( new LinearLayoutManager( this ) );
        getLoaderManager().initLoader( CURSOR_LOADER_ID, null, this );

        mCursorAdapter = new QuoteCursorAdapter( this, null, this );

        recyclerView.setAdapter( mCursorAdapter );

        mStatusTextView = ( TextView ) findViewById( R.id.status_textView );

        mFab = ( FloatingActionButton ) findViewById( R.id.fab );

//        FloatingActionButton fab = ( FloatingActionButton ) findViewById( R.id.fab );
//        fab.setOnClickListener( new View.OnClickListener() {
//            @Override
//            public void onClick( View v ) {
//                if ( isConnected ) {
//                    new MaterialDialog.Builder( mContext ).title( R.string.symbol_search )
//                            .content( R.string.symbol_search_title )
//                            .inputType( InputType.TYPE_CLASS_TEXT )
//                            .input( R.string.symbol_search_input_hint, R.string.symbol_search_input_prefill,
//                                    new MaterialDialog.InputCallback() {
//
//                                        @Override
//                                        public void onInput( MaterialDialog dialog,
//                                                             CharSequence input ) {
//
//                                            // On FAB click, receive user input. Make sure the stock
//                                            // doesn't already exist in the DB and proceed accordingly
//                                            Cursor c = getContentResolver().query(
//                                                    QuoteProvider.Quotes.CONTENT_URI,
//                                                    new String[]{ QuoteColumns.SYMBOL },
//                                                    QuoteColumns.SYMBOL + "= ?",
//                                                    new String[]{ input.toString() }, null );
//                                            if ( c.getCount() != 0 ) {
//                                                Toast toast =
//                                                        Toast.makeText( MyStocksActivity.this,
//                                                                "This stock is already saved!",
//                                                                Toast.LENGTH_LONG );
//                                                toast.setGravity( Gravity.CENTER, Gravity.CENTER, 0 );
//                                                toast.show();
//                                                return;
//                                            } else {
//                                                // Add the stock to DB
//                                                mServiceIntent.putExtra( "tag", "add" );
//                                                mServiceIntent.putExtra( "symbol", input.toString() );
//                                                startService( mServiceIntent );
//                                            }
//                                        }
//                                    } )
//                            .show();
//                } else {
//                    networkToast();
//                }
//
//            }
//        } );

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback( mCursorAdapter );
        mItemTouchHelper = new ItemTouchHelper( callback );
        mItemTouchHelper.attachToRecyclerView( recyclerView );

        mTitle = getTitle();
        if ( isConnected ) {
//            long period = 3600L;
//            long flex = 10L;
//            String periodicTag = "periodic";
//
//            // create a periodic task to pull stocks once every hour after the app has been opened.
//            // This is so Widget data stays up to date.
//            PeriodicTask periodicTask = new PeriodicTask.Builder()
//                    .setService( StockTaskService.class )
//                    .setPeriod( period )
//                    .setFlex( flex )
//                    .setTag( periodicTag )
//                    .setRequiredNetwork( Task.NETWORK_STATE_CONNECTED )
//                    .setRequiresCharging( false )
//                    .build();
//            // Schedule task with tag "periodic." This ensure that only the stocks present in the DB
//            // are updated.
//            GcmNetworkManager.getInstance( this ).schedule( periodicTask );
        }

        new CheckConnectivityAsyncTask().execute();

    }

    @Override
    // begin onStart
    protected void onStart() {

        // 0. super stuff
        // 1. register for events
        // 2. register for preference changes

        // 0. super stuff

        super.onStart();

        // 1. register for events

        EventBus.getDefault().register( this );

        // 2. register for preference changes

        PreferenceManager.getDefaultSharedPreferences( this ).
                registerOnSharedPreferenceChangeListener( this );

    } // end onStart

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader( CURSOR_LOADER_ID, null, this );
    }

    @Override
    // begin onStop
    protected void onStop() {

        // 0. super stuff
        // 1. unregister for events
        // 2. unregister for preference changes

        // 0. super stuff

        super.onStop();

        // 1. unregister for events

        EventBus.getDefault().unregister( this );

        // 2. unregister for preference changes

        PreferenceManager.getDefaultSharedPreferences( this )
                .unregisterOnSharedPreferenceChangeListener( this );

    } // end onStop

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate( R.menu.my_stocks, menu );
        restoreActionBar();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if ( id == R.id.action_settings ) {
            return true;
        }

        if ( id == R.id.action_change_units ) {

            // this is for changing stock changes from percent value to dollar value

            // 0. get the change units preference
            // 1. flip it and the icon
            // (so if I am seeing dollars and I want to see percents I should tap on percent)
            // 2. store it in the preferences
            // 3. update the content resolver
            // 4. update the widgets

            // 0. get the change units preference

            String changeUnitsPreference = Utils.getChangeUnits( this );

            // 1. flip it and the icon

            // if we're showing dollars the menu item should be a percent
            // if we're showing percents the menu item should be a dollar

            if ( changeUnitsPreference.equals(
                    getString( R.string.pref_change_units_dollars_value ) ) ) {
                changeUnitsPreference = getString( R.string.pref_change_units_percents_value );
                item.setIcon( R.drawable.ic_attach_money_white_24dp ); /* show a dollar since by
                this time the changes will be displayed as percents */
            }

            else {
                changeUnitsPreference = getString( R.string.pref_change_units_dollars_value );
                item.setIcon( R.drawable.ic_percent_white_24dp ); /* show a percent since by
                this time the changes will be displayed as dollars */
            }

            // 2. store it in the preferences

            Utils.setChangeUnits( this, changeUnitsPreference );

            // 3. update the content resolver

            this.getContentResolver().notifyChange( QuoteProvider.Quotes.CONTENT_URI, null );

            // 4. update the widgets

            sendBroadcast( new Intent( BuildConfig.ACTION_DATA_UPDATED ) );

        }

        return super.onOptionsItemSelected( item );
    }

    @Override
    public Loader< Cursor > onCreateLoader( int id, Bundle args ) {
        // This narrows the return to only the stocks that are most current.
        return new CursorLoader( this, QuoteProvider.Quotes.CONTENT_URI,
                QuoteColumns.STOCKS_COLUMNS,
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{ "1" },
                null );
    }

    @Override
    public void onLoadFinished( Loader< Cursor > loader, Cursor data ) {
        mCursorAdapter.swapCursor( data );
        mCursor = data;
    }

    @Override
    public void onLoaderReset( Loader< Cursor > loader ) {
        mCursorAdapter.swapCursor( null );
    }

    /** Click handler for when an item on the quote list is tapped. */
    @Override
    // begin onClick
    public void onClick( int position ) {

        // 0. start the stock detail activity with the correct symbol
        // 0a. confirm we have a cursor
        // 0a0. move the cursor to this item's position
        // 0a1. get the symbol at this position
        // 0a2. add the symbol to the detail intent
        // 0a3. start the detail activity via the detail intent

        // 0. start the stock detail activity with the correct symbol

        // 0a. confirm we have a cursor

        // begin if we have a cursor
        if ( mCursor != null ) {

            // 0a0. move the cursor to this item's position

            mCursor.moveToPosition( position );

            // 0a1. get the symbol at this position

            String symbol = mCursor.getString( QuoteColumns.STOCKS_COLUMN_SYMBOL );

            // 0a2. add the symbol to the detail intent

            Intent detailIntent = new Intent( MyStocksActivity.this,
                    StockDetailActivity.class )
                    .putExtra( StockDetailActivity.KEY_SYMBOL, symbol );

            // 0a3. start the detail activity via the detail intent

            startActivity( detailIntent );

        } // end if we have a cursor

    } // end onClick

    /**
     * Method to let us know when the shared preferences change.
     *
     * In particular, we are interested in the stocks status preference so that we can surface its
     * change to the user.
     * */
    @Override
    // begin method onSharedPreferenceChanged
    public void onSharedPreferenceChanged( SharedPreferences sharedPreferences,
                                           String changedPrefKey ) {

        Log.e( getClass().getSimpleName(), "onSharedPreferenceChanged() called with: sharedPreferences = [" + sharedPreferences + "], changedPrefKey = [" + changedPrefKey + "]" );
        // 0. if the changed pref is the stocks status one
        // 0a. update status view
        // 0b. if the status is stocks outdated, maybe show outdated data too?

        // 0. if the changed pref is the stocks status one
        // 0a. update status view

        // begin if it's stocks status
        if ( changedPrefKey.equals( getString( R.string.pref_stocks_status_key ) ) ) {
            updateStatusView();
        }

        // 0b. if the status is stocks outdated, maybe show outdated data too?

    } // end method onSharedPreferenceChanged

    /* Other Methods */

    public void networkToast() {
        Toast.makeText( mContext, getString( R.string.message_error_no_network ),
                Toast.LENGTH_SHORT ).show();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode( ActionBar.NAVIGATION_MODE_STANDARD );
        actionBar.setDisplayShowTitleEnabled( true );
        actionBar.setTitle( mTitle );
    }

    @Subscribe( threadMode = ThreadMode.MAIN )
    // begin onNoSuchStockEvent
    public void onNoSuchStockEvent( NoSuchStockEvent event ) {

        // 0. toast the user

        // 0. toast the user

        Toast.makeText( mContext,
                getString( R.string.message_no_such_stock_format, event.getStockSymbol() ),
                Toast.LENGTH_SHORT ).show();

    } // end onNoSuchStockEvent

    @Subscribe( threadMode = ThreadMode.MAIN )
    // begin method onConnectivityEvent
    public void onConnectivityEvent( ConnectivityEvent event ) {

        // 0. if we're connected
        // 0a. start the stock intent service to fetch stocks
        // 0b. save connecting status in preferences
        // 0c. display the add fab
        // 0d. schedule periodic stock data update
        // 1. otherwise we're offline
        // 1a. save the offline status in preferences
        // 1b. hide the add fab
        // 2. update the status view

        // 0. if we're connected

        // begin if we're online
        if ( event.isConnected() ) {

            // 0a. start the stock intent service to fetch stocks

            mServiceIntent.putExtra( "tag", "init" );
            startService( mServiceIntent );

            // 0b. save connecting status in preferences

            Utils.setStockStatus( this, STOCKS_STATUS_CONNECTING );

            // 0c. display the add fab

            // begin fab.setOnClickListener
            mFab.setOnClickListener( new View.OnClickListener() {

                @Override
                public void onClick( View v ) {

                    new MaterialDialog.Builder( mContext ).title( R.string.symbol_search )
                            .content( R.string.symbol_search_title )
                            .inputType( InputType.TYPE_CLASS_TEXT )
                            .input( R.string.symbol_search_input_hint,
                                    R.string.symbol_search_input_prefill,
                                    new MaterialDialog.InputCallback() {

                                        @Override
                                        public void onInput( MaterialDialog dialog,
                                                             CharSequence input ) {

                                            // On FAB click, receive user input. Make sure the stock
                                            // doesn't already exist in the DB and proceed
                                            // accordingly
                                            Cursor c = getContentResolver().query(
                                                    QuoteProvider.Quotes.CONTENT_URI,
                                                    new String[]{ QuoteColumns.SYMBOL },
                                                    QuoteColumns.SYMBOL + "= ?",
                                                    new String[]{ input.toString() }, null );
                                            if ( c.getCount() != 0 ) {
                                                Toast toast =
                                                        Toast.makeText( MyStocksActivity.this,
                                                                "This stock is already saved!",
                                                                Toast.LENGTH_LONG );
                                                toast.setGravity( Gravity.CENTER, Gravity.CENTER,
                                                        0 );
                                                toast.show();
                                            } else {
                                                // Add the stock to DB
                                                mServiceIntent.putExtra( "tag", "add" );
                                                mServiceIntent.putExtra( "symbol", input.toString() );
                                                startService( mServiceIntent );
                                            }
                                        }
                                    } )
                            .show();

                    }

                }); // end fab.setOnClickListener

            mFab.setVisibility( View.VISIBLE );

            // 0d. schedule periodic stock data update

            long period = 3600L;
            long flex = 10L;
            String periodicTag = "periodic";

            // create a periodic task to pull stocks once every hour after the app has been opened.
            // This is so Widget data stays up to date.
            PeriodicTask periodicTask = new PeriodicTask.Builder()
                    .setService( StockTaskService.class )
                    .setPeriod( period )
                    .setFlex( flex )
                    .setTag( periodicTag )
                    .setRequiredNetwork( Task.NETWORK_STATE_CONNECTED )
                    .setRequiresCharging( false )
                    .build();
            // Schedule task with tag "periodic." This ensure that only the stocks present in the DB
            // are updated.
            GcmNetworkManager.getInstance( this ).schedule( periodicTask );

        } // end if we're online

        // 1. otherwise we're offline

        // begin else we're offline
        else {

            // 1a. save the offline status in preferences

            Utils.setStockStatus( this, STOCKS_STATUS_NETWORK_DOWN );

            // 1b. hide the add fab

            if ( mFab.getVisibility() == View.VISIBLE ) { mFab.setVisibility( View.GONE ); }

        } // end else we're offline

        // 2. update the status view

        updateStatusView();

    } // end method onConnectivityEvent

    /** Method to update the status view as needed. */
    // begin method updateStatusView
    @SuppressLint( "SwitchIntDef" ) // we have considered the OK status before starting the switch
    private void updateStatusView() {

        // 0. get the stocks status
        // 0a. the default message is there are no stocks
        // 0b. tell the user the current stocks status - could be
        // 0b0. there are stocks
        // 0b0a. hide the status view
        // 0b0a. stop
        // 0b1. the stocks are outdated
        // 0b1a. display the most recent outdated data
        // 0b2. we refreshing the stocks
        // 0b3. we are connecting to the servers
        // 0b4. there are no stocks - the server is down - I/O errors during fetching
        // 0b5. there are no stocks - invalid data received
        // 0b6. there are no stocks - because of a fetching error
        // 0b7. there are no stocks - because of a saving error
        // 0b8. there are no stocks - network unavailability
        // 0b-last. there are no stocks - for some unknown reason

        // 0. get the stocks status

        @StockTaskService.StocksStatus int stocksStatus = Utils.getStocksStatus( this );

        // 0a. the default message is there are no stocks

        int message = R.string.message_error_no_stocks_info;

        // 0b. tell the user the current stocks status - could be

        // begin switching stocks status
        switch ( stocksStatus ) {

            // 0b0. there are stocks

            case STOCKS_STATUS_OK:

                // 0b0a. hide the status view

                mStatusTextView.setVisibility( View.GONE );

                // 0b0a. stop

                return;

            // 0b1. the stocks are outdated

            case STOCKS_STATUS_OUTDATED:

                message = R.string.message_warn_outdated_stocks;

                // 0b1a. display the most recent outdated data

//                hiw

                break;

            case STOCKS_STATUS_NETWORK_DOWN:

                message = R.string.message_error_no_network;

                break;

            // 0c-last. some unknown reason

            default: case STOCKS_STATUS_UNKNOWN:

                message = R.string.message_error_unknown;

                break;

        } // end switching stocks status

        mStatusTextView.setText( message );

        if ( mStatusTextView.getVisibility() == View.GONE ) {
            mStatusTextView.setVisibility( View.VISIBLE );
        }

    } // end method updateStatusView

}
