package com.sam_chordas.android.stockhawk.rest;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperAdapter;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperViewHolder;

import static com.sam_chordas.android.stockhawk.data.QuoteColumns.STOCKS_COLUMN_BIDPRICE;
import static com.sam_chordas.android.stockhawk.data.QuoteColumns.STOCKS_COLUMN_ISUP;
import static com.sam_chordas.android.stockhawk.data.QuoteColumns.STOCKS_COLUMN_SYMBOL;

/**
 * Created by sam_chordas on 10/6/15.
 * Credit to skyfishjy gist:
 * https://gist.github.com/skyfishjy/443b7448f59be978bc59
 * for the code structure
 */
public class QuoteCursorAdapter extends CursorRecyclerViewAdapter< QuoteCursorAdapter.ViewHolder >
        implements ItemTouchHelperAdapter {

    private static Context mContext;
    private static Typeface robotoLight;
    private boolean isPercent;

    public QuoteCursorAdapter( Context context, Cursor cursor ) {
        super( context, cursor );
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
        robotoLight = Typeface.createFromAsset( mContext.getAssets(), "fonts/Roboto-Light.ttf" );
        View itemView = LayoutInflater.from( parent.getContext() )
                .inflate( R.layout.list_item_quote, parent, false );
        ViewHolder vh = new ViewHolder( itemView );
        return vh;
    }

    @Override
    public void onBindViewHolder( final ViewHolder viewHolder, final Cursor cursor ) {
        viewHolder.symbol.setText( cursor.getString( STOCKS_COLUMN_SYMBOL ) );
        viewHolder.bidPrice.setText( cursor.getString( STOCKS_COLUMN_BIDPRICE ) );
        if ( cursor.getInt( STOCKS_COLUMN_ISUP ) == 1 ) {
            viewHolder.change.setBackgroundResource( R.drawable.percent_change_pill_green );
        }
        else {
            viewHolder.change.setBackgroundResource( R.drawable.percent_change_pill_red );
        }
        if ( Utils.getChangeUnits( mContext ).equals(
                mContext.getString( R.string.pref_change_units_percents_value ) ) ) {
            viewHolder.change.setText( cursor.getString( cursor.getColumnIndex( "percent_change" ) ) );
        } else {
            viewHolder.change.setText( cursor.getString( cursor.getColumnIndex( "change" ) ) );
        }
    }

    @Override
    public void onItemDismiss( int position ) {
        Cursor c = getCursor();
        c.moveToPosition( position );
        String symbol = c.getString( c.getColumnIndex( QuoteColumns.SYMBOL ) );
        mContext.getContentResolver().delete( QuoteProvider.Quotes.withSymbol( symbol ), null, null );
        notifyItemRemoved( position );
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
            implements ItemTouchHelperViewHolder, View.OnClickListener {
        public final TextView symbol;
        public final TextView bidPrice;
        public final TextView change;

        public ViewHolder( View itemView ) {
            super( itemView );
            symbol = ( TextView ) itemView.findViewById( R.id.stock_symbol );
            symbol.setTypeface( robotoLight );
            bidPrice = ( TextView ) itemView.findViewById( R.id.bid_price );
            change = ( TextView ) itemView.findViewById( R.id.change );
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor( Color.LTGRAY );
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor( 0 );
        }

        @Override
        public void onClick( View v ) {

        }
    }
}
