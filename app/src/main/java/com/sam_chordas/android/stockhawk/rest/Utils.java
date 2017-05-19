package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

    private static String LOG_TAG = Utils.class.getSimpleName();

    public static ArrayList quoteJsonToContentVals( String JSON ) {
        ArrayList< ContentProviderOperation > batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try {
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
        } catch ( JSONException e ) {
            Log.e( LOG_TAG, "String to JSON failed: " + e );
        }
        return batchOperations;
    }

    public static String truncateBidPrice( String bidPrice ) {
        bidPrice = String.format( "%.2f", Float.parseFloat( bidPrice ) );
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
        change = String.format( "%.2f", round );
        StringBuffer changeBuffer = new StringBuffer( change );
        changeBuffer.insert( 0, weight );
        changeBuffer.append( ampersand );
        change = changeBuffer.toString();
        return change;
    }

    public static ContentProviderOperation buildBatchOperation( JSONObject jsonObject ) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI );
        try {
            String change = jsonObject.getString( "Change" );
            builder.withValue( QuoteColumns.SYMBOL, jsonObject.getString( "symbol" ) );
            builder.withValue( QuoteColumns.BIDPRICE, truncateBidPrice(
                    jsonObject.getString( "Bid" ) ) );
            builder.withValue( QuoteColumns.PERCENT_CHANGE, truncateChange(
                    jsonObject.getString( "ChangeinPercent" ), true ) );
            builder.withValue( QuoteColumns.CHANGE, truncateChange( change, false ) );
            builder.withValue( QuoteColumns.ISCURRENT, 1 ); // this is the latest, current-est value
            if ( change.charAt( 0 ) == '-' ) {
                builder.withValue( QuoteColumns.ISUP, 0 );
            } else {
                builder.withValue( QuoteColumns.ISUP, 1 );
            }

        } catch ( JSONException e ) {
            e.printStackTrace();
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

}