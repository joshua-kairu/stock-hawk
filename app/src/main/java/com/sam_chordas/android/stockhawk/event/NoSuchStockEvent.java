package com.sam_chordas.android.stockhawk.event;

/**
 * Event for when there is no stock defined by a given symbol
 */
// begin class NoSuchStockEvent
public class NoSuchStockEvent {

    /* CONSTANTS */
    
    /* Integers */
        
    /* Strings */
    
    /* VARIABLES */

    /* Strings */

    private String mStockSymbol; // ditto

    /* CONSTRUCTOR */

    // default constructor
    public NoSuchStockEvent( String stockSymbol ) {
        this.mStockSymbol = stockSymbol;
    }

    /* METHODS */
        
    /* Getters and Setters */

    public String getStockSymbol() {
        return mStockSymbol;
    }

    /* Overrides */
        
    /* Other Methods */

} // end class NoSuchStockEvent
