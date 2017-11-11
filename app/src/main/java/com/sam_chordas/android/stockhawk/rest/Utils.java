package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.service.StockTaskService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.sam_chordas.android.stockhawk.service.StockTaskService.STOCKS_STATUS_CONNECTING;
import static com.sam_chordas.android.stockhawk.service.StockTaskService.STOCKS_STATUS_FETCH_ERROR;
import static com.sam_chordas.android.stockhawk.service.StockTaskService.STOCKS_STATUS_NETWORK_DOWN;
import static com.sam_chordas.android.stockhawk.service.StockTaskService.STOCKS_STATUS_OK;
import static com.sam_chordas.android.stockhawk.service.StockTaskService.STOCKS_STATUS_OUTDATED;
import static com.sam_chordas.android.stockhawk.service.StockTaskService.STOCKS_STATUS_REFRESHING;
import static com.sam_chordas.android.stockhawk.service.StockTaskService.STOCKS_STATUS_SAVE_ERROR;
import static com.sam_chordas.android.stockhawk.service.StockTaskService.STOCKS_STATUS_SERVER_DOWN;
import static com.sam_chordas.android.stockhawk.service.StockTaskService.STOCKS_STATUS_SERVER_INVALID;
import static com.sam_chordas.android.stockhawk.service.StockTaskService.STOCKS_STATUS_UNKNOWN;

/**
 * Created by sam_chordas on 10/8/15.
 *
 * Utility methods class.
 */
// begin class Utils
public class Utils {

    private static String LOG_TAG = Utils.class.getSimpleName();

    /**
     *
     * JSON from Yahoo
     *
     {
     "query":{
         "count":4,
         "created":"2017-07-07T14:54:21Z",
         "lang":"en-US",
         "diagnostics":{  },
         "results":{
             "quote":[
                 {
                     "symbol":"YHOO",
                     "Ask":null,
                     "AverageDailyVolume":"19798100",
                     "Bid":null,
                     "AskRealtime":null,
                     "BidRealtime":null,
                     "BookValue":"0.00",
                     "Change_PercentChange":"+0.00 - +0.00%",
                     "Change":"+0.00",
                     "Commission":null,
                     "Currency":null,
                     "ChangeRealtime":null,
                     "AfterHoursChangeRealtime":null,
                     "DividendShare":null,
                     "LastTradeDate":null,
                     "TradeDate":null,
                     "EarningsShare":"-0.03",
                     "ErrorIndicationreturnedforsymbolchangedinvalid":null,
                     "EPSEstimateCurrentYear":"0.71",
                     "EPSEstimateNextYear":"0.72",
                     "EPSEstimateNextQuarter":"0.18",
                     "DaysLow":null,
                     "DaysHigh":null,
                     "YearLow":"35.05",
                     "YearHigh":"57.39",
                     "HoldingsGainPercent":null,
                     "AnnualizedGain":null,
                     "HoldingsGain":null,
                     "HoldingsGainPercentRealtime":null,
                     "HoldingsGainRealtime":null,
                     "MoreInfo":null,
                     "OrderBookRealtime":null,
                     "MarketCapitalization":null,
                     "MarketCapRealtime":null,
                     "EBITDA":"0.00",
                     "ChangeFromYearLow":null,
                     "PercentChangeFromYearLow":null,
                     "LastTradeRealtimeWithTime":null,
                     "ChangePercentRealtime":null,
                     "ChangeFromYearHigh":null,
                     "PercebtChangeFromYearHigh":null,
                     "LastTradeWithTime":null,
                     "LastTradePriceOnly":null,
                     "HighLimit":null,
                     "LowLimit":null,
                     "DaysRange":null,
                     "DaysRangeRealtime":null,
                     "FiftydayMovingAverage":"50.51",
                     "TwoHundreddayMovingAverage":"45.66",
                     "ChangeFromTwoHundreddayMovingAverage":null,
                     "PercentChangeFromTwoHundreddayMovingAverage":null,
                     "ChangeFromFiftydayMovingAverage":null,
                     "PercentChangeFromFiftydayMovingAverage":null,
                     "Name":null,
                     "Notes":null,
                     "Open":null,
                     "PreviousClose":null,
                     "PricePaid":null,
                     "ChangeinPercent":"+0.00%",
                     "PriceSales":null,
                     "PriceBook":null,
                     "ExDividendDate":null,
                     "PERatio":null,
                     "DividendPayDate":null,
                     "PERatioRealtime":null,
                     "PEGRatio":"12.79",
                     "PriceEPSEstimateCurrentYear":null,
                     "PriceEPSEstimateNextYear":null,
                     "Symbol":"YHOO",
                     "SharesOwned":null,
                     "ShortRatio":"0.00",
                     "LastTradeTime":null,
                     "TickerTrend":null,
                     "OneyrTargetPrice":"54.08",
                     "Volume":"0",
                     "HoldingsValue":null,
                     "HoldingsValueRealtime":null,
                     "YearRange":"35.05 - 57.39",
                     "DaysValueChange":null,
                     "DaysValueChangeRealtime":null,
                     "StockExchange":"NMS",
                     "DividendYield":null,
                     "PercentChange":"+0.00%"
                 },
                 {
                     and others
                 }
             ]
         }
     }
     *
     * */

    public static ArrayList quoteJsonToContentVals( String JSON ) throws JSONException {
        ArrayList< ContentProviderOperation > batchOperations = new ArrayList<>();
        JSONObject jsonObject;
        JSONArray resultsArray;
        jsonObject = new JSONObject( JSON );
        if ( jsonObject != null && jsonObject.length() != 0 ) {
            jsonObject = jsonObject.getJSONObject( "query" );
            int count = Integer.parseInt( jsonObject.getString( "count" ) );
            if ( count == 1 ) {
                jsonObject = jsonObject.getJSONObject( "results" )
                        .getJSONObject( "quote" );
                batchOperations.add( buildBatchOperation( jsonObject ) );
            } else {
                resultsArray = jsonObject.getJSONObject( "results" ).getJSONArray( "quote" );

                if ( resultsArray != null && resultsArray.length() != 0 ) {
                    for ( int i = 0; i < resultsArray.length(); i++ ) {
                        jsonObject = resultsArray.getJSONObject( i );
                        batchOperations.add( buildBatchOperation( jsonObject ) );
                    }
                }
            }
        }
        return batchOperations;
    }

    public static String truncateBidPrice( String bidPrice ) {
        bidPrice = String.format( Locale.getDefault(), "%.2f", Float.parseFloat( bidPrice ) );
        return bidPrice;
    }

    public static String truncateChange( String change, boolean isPercentChange ) {
        String weight = change.substring( 0, 1 );
        String ampersand = "";
        if ( isPercentChange ) {
            ampersand = change.substring( change.length() - 1, change.length() );
            change = change.substring( 0, change.length() - 1 );
        }
        change = change.substring( 1, change.length() );
        double round = ( double ) Math.round( Double.parseDouble( change ) * 100 ) / 100;
        change = String.format( Locale.getDefault(), "%.2f", round );
        StringBuffer changeBuffer = new StringBuffer( change );
        changeBuffer.insert( 0, weight );
        changeBuffer.append( ampersand );
        change = changeBuffer.toString();
        return change;
    }

    public static ContentProviderOperation buildBatchOperation( JSONObject jsonObject )
            throws JSONException {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI );
            /*
             "symbol":"YHOO",
             "Bid":null,
             "Change":"+0.00",
             "ChangeinPercent":"+0.00%",
             * */

        // if the bid price is null, just show zeros as the bid price. otherwise this will
        // cause unnecessary NumberFormatExceptions

        String change = jsonObject.getString( "Change" );
        String bidPrice = jsonObject.getString( "Bid" );
        String threeZeros = "0.00";
        builder.withValue( QuoteColumns.SYMBOL, jsonObject.getString( "symbol" ) );
        builder.withValue( QuoteColumns.BIDPRICE,
                truncateBidPrice( bidPrice.equals( "null" ) ? threeZeros : bidPrice ) );
        builder.withValue( QuoteColumns.PERCENT_CHANGE,
                truncateChange( jsonObject.getString( "ChangeinPercent" ), true ) );
        builder.withValue( QuoteColumns.CHANGE,
                truncateChange( change, false ) );
        builder.withValue( QuoteColumns.ISCURRENT, 1 ); /* meaning that this is the latest,
            current-est value */
        if ( change.charAt( 0 ) == '-' ) {
            builder.withValue( QuoteColumns.ISUP, 0 );
        } else {
            builder.withValue( QuoteColumns.ISUP, 1 );
        }

        return builder.build();
    }

    /**
     * Helper method to get the highest bid price in an array as a truncated integer. For example,
     * for the highest bid price 30.24 this method returns 30.
     *
     * @param bidPrices float array of the bid prices
     *
     * @return The highest bid price in an array as a truncated integer.
     * */
    // begin method getHighestBidPriceAsInt
    private static int getHighestBidPriceAsInt( float[] bidPrices ) {

        // 0. get the highest bid price in the array
        // 1. return a truncated version

        // 0. get the highest bid price in the array

        float highestBidPrice = 0;

        for ( float bidPrice : bidPrices ) {
            if ( highestBidPrice < bidPrice ) {
                highestBidPrice = bidPrice;
            }
        }

        // 1. return a truncated version

        return ( int ) Math.floor( highestBidPrice );

    } // end method getHighestBidPriceAsInt

    /**
     * Helper method to get the lowest bid price in an array as a truncated integer. For example,
     * for the highest bid price 7.78 this method returns 7.
     *
     * @param bidPrices float array of the bid prices
     *
     * @return The lowest bid price in an array as a truncated integer.
     * */
    // begin method getLowestBidPriceAsInt
    private static int getLowestBidPriceAsInt( float[] bidPrices ) {

        // 0. get the highest bid price in the array
        // 1. return a truncated version

        // 0. get the highest bid price in the array

        float lowestBidPrice = Float.MAX_VALUE;

        for ( float bidPrice : bidPrices ) {
            if ( lowestBidPrice > bidPrice ) {
                lowestBidPrice = bidPrice;
            }
        }

        // 1. return a truncated version

        return ( int ) Math.floor( lowestBidPrice );

    } // end method getLowestBidPriceAsInt

    /**
     * Helper method to get the max value of a chart's vertical axis depending on the chart's
     * bid prices data. The max value should be the next number divisible by five that's bigger
     * than the largest bid price, or the next divisor of five if the max value is divisible by five
     *
     * @param bidPrices float array of the bid prices
     *
     * @return The max value of a chart's vertical axis
     * */
    // begin method getChartMaxValue
    public static int getChartMaxValue( float[] bidPrices ) {

        // the next number divisible by five that's bigger than the largest bid price is gotten by
        // 0. integer-dividing the integer value of the largest number by 5
        // 1. adding 1 to this result
        // 2. multiplying this result by 5
        // for example, if the largest bid is 77, the max value should be 80
        // 0. 77 / 5 = 15
        // 1. 15 + 1 = 16
        // 2. 16 * 5 = 80. QED

        // 0. if the largest bid is not divisible by five, return the max chart value using the
        // above method
        // 1. else the largest bid is divisible by five, so return the next divisor of five

        // 0. if the largest bid is not divisible by five, return the max chart value using the
        // above method

        int largestBid = getHighestBidPriceAsInt( bidPrices );

        if ( largestBid % 5 != 0  ) { return ( ( largestBid / 5 ) + 1 ) * 5; }

        // 1. else the largest bid is divisible by five, so return the next divisor of five

        else { return largestBid + 5; }

    } // end method getChartMaxValue

    /**
     * Helper method to get the minimum value of a chart's vertical axis depending on the chart's
     * bid prices data. The minimum value should be the previous number divisible by five that's
     * smaller than the smallest bid price, or the previous divisor of five if the min value is
     * divisible by five
     *
     * @param bidPrices float array of the bid prices
     *
     * @return The minimum value of a chart's vertical axis
     * */
    // begin method getChartMinValue
    public static int getChartMinValue( float[] bidPrices ) {

        // the previous number divisible by five that's smaller than the smallest bid price is
        // gotten by
        // 0. integer-dividing the integer value of the smallest number by 5
        // 1. multiplying this result by 5
        // for example, if the smallest bid is 77, the min value should be 75
        // 0. 77 / 5 = 15
        // 1. 15 * 5 = 75. QED

        // 0. if the smallest bid is not divisible by five, return the min chart value using the
        // above method
        // 1. else the smallest bid is divisible by five, so return the previous divisor of five

        // 0. if the smallest bid is not divisible by five, return the min chart value using the
        // above method

        int smallestBid = getLowestBidPriceAsInt( bidPrices );

        if ( smallestBid % 5 != 0 ) { return ( smallestBid / 5 ) * 5; }

        // 1. else the smallest bid is divisible by five, so return the previous divisor of five

        else { return smallestBid - 5; }

    } // end method getChartMinValue

    /**
     * Helper method to get a stock symbol from its parent JSON, which is passed in as a string.
     * Useful for when we want to report that a stock symbol doesn't exist - such as in the catch
     * block in
     * {@link com.sam_chordas.android.stockhawk.service.StockTaskService#onRunTask(TaskParams)}.
     *
     * Most of this is copied from {@link Utils#quoteJsonToContentVals(String)} and
     * {@link Utils#buildBatchOperation(JSONObject)}.
     *
     * @param JSON The JSON string having the symbol
     *
     * @return The symbol as a string
     */
    // begin method getSymbolFromJSON
    public static String getSymbolFromJSON ( String JSON ) {

        // 0. get the query JSON object
        // 1. get the symbol from the query object
        // 2. return the symbol

        // 0. get the query JSON object

        JSONObject jsonObject;

        // begin trying to work with the JSON
        try {

            jsonObject = new JSONObject( JSON );

            if ( jsonObject.length() != 0 ) {

                jsonObject = jsonObject.getJSONObject( "query" );

                int count = Integer.parseInt( jsonObject.getString( "count" ) );

                // 1. get the symbol from the query object
                // 2. return the symbol

                if ( count == 1 ) {
                    return jsonObject.getJSONObject( "results" ).getJSONObject( "quote" )
                            .getString( "symbol" );
                }
            }

        } // end trying to work with the JSON

        // catch JSON issues
        catch ( JSONException e ) { Log.e( LOG_TAG, "Error parsing JSON", e ); }

        return null; // because we have no other option by now

    } // end method getSymbolFromJSON

    /**
     * Sets the change units preference.
     *
     * @param context Android {@link Context}
     * @param changeUnitsValueString The string we will store in the preferences as the preferred
     *                               change units
     * */
    // begin method setChangeUnits
    public static void setChangeUnits( Context context, String changeUnitsValueString ) {

        PreferenceManager.getDefaultSharedPreferences( context ).edit()
                .putString( context.getString( R.string.pref_change_units_key ),
                        changeUnitsValueString )
                .apply();

    } // end method setChangeUnits

    /**
     * Gets the user's preferred change units, or dollars by default.
     *
     * @param context Android {@link Context}
     *
     * @return The user's preferred change units, or dollars by default.
     * */
    // begin method getChangeUnits
    public static String getChangeUnits( Context context ) {

        return PreferenceManager.getDefaultSharedPreferences( context )
                .getString( context.getString( R.string.pref_change_units_key ),
                        context.getString( R.string.pref_change_units_dollars_value ) );

    } // end method getChangeUnits

    /**
     * Returns the current stock status, as defined by the interface
     * {@link com.sam_chordas.android.stockhawk.service.StockTaskService.StocksStatus}
     *
     * @param context Current context
     *
     * @return Current stock status, a member of the {@link android.support.annotation.IntDef}
     * {@link com.sam_chordas.android.stockhawk.service.StockTaskService.StocksStatus}
     * */
    @StockTaskService.StocksStatus
    // begin method getStocksStatus
    public static int getStocksStatus( Context context ) {

        // 0. get the stocks status from preferences
        // 1. return the appropriate status

        // 0. get the stocks status from preferences

        int stocksStatus = PreferenceManager.getDefaultSharedPreferences( context ).getInt(
                context.getString( R.string.pref_stocks_status_key ),
                STOCKS_STATUS_UNKNOWN );

        // 1. return the appropriate status

        // begin switch to determine status
        switch ( stocksStatus ) {

            case STOCKS_STATUS_OK: return STOCKS_STATUS_OK;

            case STOCKS_STATUS_SERVER_DOWN: return STOCKS_STATUS_SERVER_DOWN;

            case STOCKS_STATUS_SERVER_INVALID: return STOCKS_STATUS_SERVER_INVALID;

            case STOCKS_STATUS_FETCH_ERROR: return STOCKS_STATUS_FETCH_ERROR;

            case STOCKS_STATUS_SAVE_ERROR: return STOCKS_STATUS_SAVE_ERROR;

            case STOCKS_STATUS_OUTDATED: return STOCKS_STATUS_OUTDATED;

            case STOCKS_STATUS_REFRESHING: return STOCKS_STATUS_REFRESHING;

            case STOCKS_STATUS_CONNECTING: return STOCKS_STATUS_CONNECTING;

            case STOCKS_STATUS_NETWORK_DOWN: return STOCKS_STATUS_NETWORK_DOWN;

            default: return STOCKS_STATUS_UNKNOWN;

        } // end switch to determine status

    } // end method getStocksStatus

    /**
     * Stores the stocks status in preferences.
     *
     * @param context Current {@link Context}
     * @param stockStatus The current stocks status, a member of
     * {@link com.sam_chordas.android.stockhawk.service.StockTaskService.StocksStatus}
     * */
    // begin method setStockStatus
    public static void setStockStatus( Context context,
                                       @StockTaskService.StocksStatus int stockStatus ) {

        // 0. store the status in preferences

        // 0. store the status in preferences

        PreferenceManager.getDefaultSharedPreferences( context ).edit().putInt(
                context.getString( R.string.pref_stocks_status_key ), stockStatus ).apply();

    } // end method setStockStatus

    /**
     * Helper method to determine if the Internet is up.
     *
     * It does this by checking the result of a ping.
     *
     * Thanks to http://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-times-out/27312494#27312494
     *
     * @return A boolean depending on the success of the ping.
     * */
    // begin method isInternetUp
    public static boolean isInternetUp() {

        // 0. get a runtime
        // 1. start a ping
        // 2. there is net if the ping returns 0
        // last. exceptions and other things mean no net

        // 0. get a runtime

        Runtime runtime = Runtime.getRuntime();

        // begin trying to ping
        try {

            // 1. start a ping

            Process pingProcess = runtime.exec( "/system/bin/ping -c 1 8.8.8.8" );

            // waitFor - Causes the calling thread to wait for the native process
            //  associated with this object to finish executing.
            int exitValue = pingProcess.waitFor();

            // 2. there is net if the ping returns 0

            return ( exitValue == 0 );

        } // end trying to ping

        catch ( InterruptedException | IOException e ) { e.printStackTrace(); }

        // last. exceptions and other things mean no net

        return false;

    } // end method isInternetUp

} // end class Utils