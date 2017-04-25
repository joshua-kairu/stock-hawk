package com.sam_chordas.android.stockhawk.ui;

import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.db.chart.model.ChartEntry;
import com.db.chart.model.LineSet;
import com.db.chart.renderer.AxisRenderer;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity showing the details of a particular stock
 */
// begin activity StockDetailActivity
public class StockDetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks< Cursor > {

    /* CONSTANTS */
    
    /* Integers */

    private static final int DETAIL_LOADER_ID = 0; // ditto
    
    /* Strings */

    /**
     * The logger.
     */
    private static final String LOG_TAG = StockDetailActivity.class.getSimpleName();

    /** Key for the symbol argument */
    public static final String KEY_SYMBOL = "SYMBOL";

    /* VARIABLES */

    /* LineChartViews */

    @BindView( R.id.linechart )
    LineChartView mStocksLineChartView; // ditto

    /* Strings */

    String mStockSymbol; // ditto

    /* CONSTRUCTOR */
    
    /* METHODS */
    
    /* Getters and Setters */
    
    /* Overrides */

    @Override
    // begin onCreate
    protected void onCreate( Bundle savedInstanceState ) {

        // use a cursor loader
        //
        // plan
        // ====
        // on create loader - the cursor should fetch all rows having a specified symbol
        //                  - we should get only the bid price column from the db
        //                  - get the symbol from the intent
        // on load finished - extract bid price from the cursor and show it in a line chart

        // 0. super stuff
        // 1. use the correct layout
        // 2. bind
        // 3. initialize
        // 3a. loader
        // 3b. the stock symbol

        // 0. super stuff

        super.onCreate( savedInstanceState );

        // 1. use the correct layout

        setContentView( R.layout.activity_line_graph );

        // 2. bind

        ButterKnife.bind( this );

        // 3. initialize

        // 3a. loader

        getSupportLoaderManager().initLoader( DETAIL_LOADER_ID, null, this );

        // 3b. the stock symbol

        mStockSymbol = getIntent() != null ? getIntent().getStringExtra( KEY_SYMBOL ) : "";

    } // end onCreate

    @Override
    // begin onCreateLoader
    public Loader< Cursor > onCreateLoader( int id, Bundle args ) {

        // on create loader - the cursor should fetch all rows having a specified symbol
        //                  - we should get only the bid price column from the db

        // 0. if there is no stock symbol return null
        // 1. otherwise there is a stock symbol so create loader as needed

        // 0. if there is no stock symbol return null

        if ( TextUtils.isEmpty( mStockSymbol ) ) { return null; }

        // 1. otherwise there is a stock symbol so create loader as needed

        return new CursorLoader( this, QuoteProvider.Quotes.withSymbol( mStockSymbol ),
                QuoteColumns.DETAIL_COLUMNS, null, null, QuoteColumns.BIDPRICE + " ASC" );

    } // end onCreateLoader

    @Override
    // begin onLoadFinished
    public void onLoadFinished( Loader< Cursor > loader, Cursor cursor ) {

        // 0. if there is a cursor
        // 0a. get the bid prices from the cursor
        // 0a0. have an array where we'll store the bid prices
        // 0a1. move the cursor to the first row
        // 0a2. collect all the bid prices from db and store them in the array
        // 0b. display the bid prices using the line chart
        // 0b0. create an array of labels used in the line chart
        // 0b1. use the array of labels and bid prices to create the line set to be used in the
        // chart
        // 0b1a. the line should have the accent color
        // 0b2. show the chart
        // 0b2a. use the appropriate step count
        // 0b2b. the upper axis limit should be the next number divisible by five that's bigger
        // than the largest bid price, or the next divisor of five if the max value is divisible by
        // five
        // 0b2c. the lower axis limit should be the previous number divisible by five that's smaller
        // than the smallest bid price, or the previous divisor of five if the min value is
        // divisible by five
        // 0b2-last. maybe don't show the X axis labels for now

        // 0. if there is a cursor

        // begin if the cursor exists
        if ( cursor != null ) {

            // 0a. get the bid prices from the cursor

            // 0a0. have an array where we'll store the bid prices

            float[] bidPrices = new float[ cursor.getCount() ];

            // 0a1. move the cursor to the first row

            cursor.moveToFirst();

            // 0a2. collect all the bid prices from db and store them in the array

            int position = 0;

            // begin while through all bid prices
            while ( !cursor.isAfterLast() ) {

                bidPrices[ position ] = Float.parseFloat(
                        cursor.getString( QuoteColumns.DETAIL_COLUMN_BIDPRICE ) );

                cursor.moveToNext(); position++;

            } // end while through all bid prices

            // 0b. display the bid prices using the line chart

            // 0b0. create an array of labels used in the line chart

            String[] labels = new String[ cursor.getCount() ];

            for ( int i = 0; i < labels.length; i++ ) { labels[ i ] = String.valueOf( i ); }

            // 0b1. use the array of labels and bid prices to create the line set to be used in the
            // chart

            LineSet lineSet = new LineSet( labels, bidPrices );

            // 0b1a. the line should have the accent color

            lineSet.setColor( getResources().getColor( R.color.material_orange_accent_700 ) );

            mStocksLineChartView.addData( lineSet );

            // 0b2. show the chart

            // 0b2a. use the appropriate step count

            mStocksLineChartView.setStep(
                    getResources().getInteger( R.integer.vertical_axis_step ) );

            // 0b2b. the upper axis limit should be the next number divisible by five that's bigger
            // than the largest bid price, or the next divisor of five if the max value is divisible
            // by five

            // 0b2c. the lower axis limit should be the previous number divisible by five that's
            // smaller than the smallest bid price, or the previous divisor of five if the min value
            // is divisible by five

            mStocksLineChartView.setAxisBorderValues( Utils.getChartMinValue( bidPrices ),
                    Utils.getChartMaxValue( bidPrices ) );

            // 0b2-last. maybe don't show the X axis labels for now

            mStocksLineChartView.setXLabels( AxisRenderer.LabelPosition.NONE );

            mStocksLineChartView.show();

        } // end if the cursor exists

    } // end onLoadFinished

    @Override
    public void onLoaderReset( Loader< Cursor > loader ) {

    }

    /* Other Methods */

} // end activity StockDetailActivity
