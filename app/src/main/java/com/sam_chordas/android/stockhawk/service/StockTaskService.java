package com.sam_chordas.android.stockhawk.service;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.RemoteException;
import android.support.annotation.IntDef;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.BuildConfig;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.event.NoSuchStockEvent;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URLEncoder;

/**
 * Created by sam_chordas on 9/30/15.
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */
public class StockTaskService extends GcmTaskService {

    // TODO: 11/11/17 Update to use JobScheduler in new StockHawk 
    /* CONSTANTS */

    /* Integers */

    /** Status when stocks have been fetched successfully. */
    public static final int STOCKS_STATUS_OK = 0;

    /** Status when the stocks server is down, such as
     * when we cannot access it to get data from it. */
    public static final int STOCKS_STATUS_SERVER_DOWN = 1;

    /** Status when the server returns invalid (JSON) data. */
    public static final int STOCKS_STATUS_SERVER_INVALID = 2;

    /** Status when there's an error fetching stocks data, maybe due to an encoding problem. */
    public static final int STOCKS_STATUS_FETCH_ERROR = 3;

    /** Status when we cannot save the fetched data well, maybe due to failed db inserts. */
    public static final int STOCKS_STATUS_SAVE_ERROR = 4;

    /** Status when the stocks are outdated. This is seen when the db has no current stocks. */
    public static final int STOCKS_STATUS_OUTDATED = 5;

    /** Status when we are refreshing the stocks. */
    public static final int STOCKS_STATUS_REFRESHING = 6;

    /** Status when we are attempting to connect to the server. */
    public static final int STOCKS_STATUS_CONNECTING = 7;

    /** Status when we don't know what has happened, maybe when we cannot match any of the other
     * statuses in an if statement in {@link Utils}. */
    public static final int STOCKS_STATUS_UNKNOWN = 8;

    /** Status when the network is down. */
    public static final int STOCKS_STATUS_NETWORK_DOWN = 9;

    /* Strings */

    private String LOG_TAG = StockTaskService.class.getSimpleName();

    /* VARIABLES */

    /* Annotations */

    // SOURCE - Annotations are to be discarded by the compiler.
    @Retention( RetentionPolicy.SOURCE )
    @IntDef( { STOCKS_STATUS_OK, STOCKS_STATUS_SERVER_DOWN, STOCKS_STATUS_SERVER_INVALID,
            STOCKS_STATUS_FETCH_ERROR, STOCKS_STATUS_SAVE_ERROR, STOCKS_STATUS_OUTDATED,
            STOCKS_STATUS_REFRESHING, STOCKS_STATUS_CONNECTING, STOCKS_STATUS_UNKNOWN,
            STOCKS_STATUS_NETWORK_DOWN } )
    /** Enumeration of possible stocks data statuses. */
    public @interface StocksStatus{}
    
    /* Contexts */

    private Context mContext;
    
    /* OkHttpClients */
    
    private OkHttpClient client = new OkHttpClient();
    
    /* Primitives */

    private boolean isUpdate;
    
    /* StringBuilders */
    
    private StringBuilder mStoredSymbols = new StringBuilder();
    
    /* CONSTRUCTOR */

    public StockTaskService() {
    }

    public StockTaskService( Context context ) {
        mContext = context;
    }

    /* Overrides */

    @Override
    public int onRunTask( TaskParams params ) {
        Cursor initQueryCursor;
        if ( mContext == null ) {
            mContext = this;
        }
        StringBuilder urlStringBuilder = new StringBuilder();
        try {
            // Base URL for the Yahoo query
            urlStringBuilder.append( "https://query.yahooapis.com/v1/public/yql?q=" );
            urlStringBuilder.append( URLEncoder.encode(
                    "select * from yahoo.finance.quotes where symbol "
                    + "in (", "UTF-8" ) );
        } catch ( UnsupportedEncodingException e ) {
            e.printStackTrace();
            Utils.setStockStatus( mContext, STOCKS_STATUS_FETCH_ERROR );
        }
        if ( params.getTag().equals( "init" ) || params.getTag().equals( "periodic" ) ) {
            isUpdate = true;
            initQueryCursor = mContext.getContentResolver().query( QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{ "Distinct " + QuoteColumns.SYMBOL }, null,
                    null, null );
            if ( initQueryCursor.getCount() == 0 || initQueryCursor == null ) {
                // Init task. Populates DB with quotes for the symbols seen below
                try {
                    urlStringBuilder.append(
                            URLEncoder.encode( "\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\")", "UTF-8" ) );
                } catch ( UnsupportedEncodingException e ) {
                    e.printStackTrace();
                    Utils.setStockStatus( mContext, STOCKS_STATUS_FETCH_ERROR );
                }
            } else if ( initQueryCursor != null ) {
                DatabaseUtils.dumpCursor( initQueryCursor );
                initQueryCursor.moveToFirst();
                for ( int i = 0; i < initQueryCursor.getCount(); i++ ) {
                    mStoredSymbols.append( "\"" +
                            initQueryCursor.getString(
                                    initQueryCursor.getColumnIndex( "symbol" ) ) + "\"," );
                    initQueryCursor.moveToNext();
                }
                mStoredSymbols.replace( mStoredSymbols.length() - 1, mStoredSymbols.length(), ")" );
                try {
                    urlStringBuilder.append( URLEncoder.encode( mStoredSymbols.toString(), "UTF-8" ) );
                } catch ( UnsupportedEncodingException e ) {
                    e.printStackTrace();
                    Utils.setStockStatus( mContext, STOCKS_STATUS_FETCH_ERROR );
                }
            }
        } else if ( params.getTag().equals( "add" ) ) {
            isUpdate = false;
            // get symbol from params.getExtra and build query
            String stockInput = params.getExtras().getString( "symbol" );
            try {
                urlStringBuilder.append( URLEncoder.encode( "\"" + stockInput + "\")", "UTF-8" ) );
            } catch ( UnsupportedEncodingException e ) {
                e.printStackTrace();
                Utils.setStockStatus( mContext, STOCKS_STATUS_FETCH_ERROR );
            }
        }
        // finalize the URL for the API query.
        urlStringBuilder.append( "&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                + "org%2Falltableswithkeys&callback=" );

        String urlString;
        final String getResponse;
        int result = GcmNetworkManager.RESULT_FAILURE;

        urlString = urlStringBuilder.toString();
        try {
            getResponse = fetchData( urlString );
            result = GcmNetworkManager.RESULT_SUCCESS;
            try {
                ContentValues contentValues = new ContentValues();
                // update ISCURRENT to 0 (false) so new data is current
                // this is done before new data is added. All new data added is given an
                // ISCURRENT value of 1 (true), showing that it is fresh.
                // See Utils.buildBatchOperation
                if ( isUpdate ) {
                    contentValues.put( QuoteColumns.ISCURRENT, 0 );
                    mContext.getContentResolver().update( QuoteProvider.Quotes.CONTENT_URI,
                            contentValues, null, null );

                    // update the widget
                    updateWidget();

                }

                // if we've added a new stock, we should let the widget know of that
                if ( params.getTag().equals( "add" ) ) { updateWidget(); }

                mContext.getContentResolver().applyBatch( QuoteProvider.AUTHORITY,
                        Utils.quoteJsonToContentVals( getResponse ) );

                // we have successfully gotten stocks data!
                Utils.setStockStatus( mContext, STOCKS_STATUS_OK );

            }

            // catch errors when contacting remote content provider or performing the batch
            // operation on the db
            catch ( RemoteException | OperationApplicationException e ) {
                Log.e( LOG_TAG, "Error applying batch insert", e );
                Utils.setStockStatus( mContext, STOCKS_STATUS_SAVE_ERROR );
            }

            // catch number formats
            catch ( NumberFormatException e ) {
                if ( getResponse != null ) {
                    EventBus.getDefault().post( new NoSuchStockEvent(
                            Utils.getSymbolFromJSON( getResponse ) ) );
                }
            }

            // catch JSONs
            catch ( JSONException e ) {
                Log.e( LOG_TAG, "String to JSON failed: " + e );
                Utils.setStockStatus( mContext, STOCKS_STATUS_SERVER_INVALID );
            }

        } catch ( IOException e ) { // gotten from fetchData.
            e.printStackTrace();
            Utils.setStockStatus( mContext, STOCKS_STATUS_SERVER_DOWN );
        }

        return result;
    }

    /* Other Methods */

    /**
     * Fetches data from a given url.
     *
     * @param url The url as a String
     * @throws IOException if network is unsuccessful
     * @return The fetched data as a String
     * */
    // begin method fetchData
    String fetchData( String url ) throws IOException {

        Request request = new Request.Builder()
                .url( url )
                .build();

        Response response = client.newCall( request ).execute();
        return response.body().string();

    } // end method fetchData

    /**
     * Helper method to update the collection widget.
     *
     * This is done by sending a BuildConfig.ACTION_DATA_UPDATED broadcast.
     *  */
    // begin method updateWidget
    private void updateWidget() {

        // 0. send the data changed broadcast

        // 0. send the data changed broadcast

        Intent dataUpdatedIntent = new Intent( BuildConfig.ACTION_DATA_UPDATED );
        mContext.sendBroadcast( dataUpdatedIntent );

    } // end method updateWidget

}