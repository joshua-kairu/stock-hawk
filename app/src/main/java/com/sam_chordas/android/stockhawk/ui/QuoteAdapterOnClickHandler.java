package com.sam_chordas.android.stockhawk.ui;

/**
 * Handles taps on the Quotes {@link android.support.v7.widget.RecyclerView}.
 */
// begin interface QuoteAdapterOnClickHandler
public interface QuoteAdapterOnClickHandler {
    
    /* METHODS */

    /**
     * Tells that a tap has happened to an item in a given position.
     *
     * @param position The position of the item on which the tap has happened.
     * */
    void onClick( int position );

} // end interface QuoteAdapterOnClickHandler
