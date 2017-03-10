package com.sam_chordas.android.stockhawk.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.sam_chordas.android.stockhawk.R;

/**
 * Activity showing the details of a particular stock
 */
// begin activity StockDetailActivity
public class StockDetailActivity extends AppCompatActivity {

    /* CONSTANTS */
    
    /* Integers */
    
    /* Strings */

    /**
     * The logger.
     */
    private static final String LOG_TAG = StockDetailActivity.class.getSimpleName();
    
    /* VARIABLES */
    
    /* CONSTRUCTOR */
    
    /* METHODS */
    
    /* Getters and Setters */
    
    /* Overrides */

    @Override
    // begin onCreate
    protected void onCreate( Bundle savedInstanceState ) {

        // 0. super stuff
        // 1. use the correct layout

        // 0. super stuff

        super.onCreate( savedInstanceState );

        // 1. use the correct layout

        setContentView( R.layout.activity_line_graph );

    } // end onCreate
    
    /* Other Methods */

} // end activity StockDetailActivity
