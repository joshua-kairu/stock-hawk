package com.sam_chordas.android.stockhawk.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

/**
 * Created by sam_chordas on 10/5/15.
 */
// begin class QuoteColumns
public class QuoteColumns {

    /* Constants */

    /* Columns */

    @DataType( DataType.Type.INTEGER )
    @PrimaryKey
    @AutoIncrement
    public static final String _ID = "_id";

    @DataType( DataType.Type.TEXT )
    @NotNull
    public static final String SYMBOL = "symbol";

    @DataType( DataType.Type.TEXT )
    @NotNull
    public static final String PERCENT_CHANGE = "percent_change";

    @DataType( DataType.Type.TEXT )
    @NotNull
    public static final String CHANGE = "change";

    @DataType( DataType.Type.TEXT )
    @NotNull
    public static final String BIDPRICE = "bid_price";

    @DataType( DataType.Type.TEXT )
    public static final String CREATED = "created";

    @DataType( DataType.Type.INTEGER )
    @NotNull
    public static final String ISUP = "is_up"; // if the stock is rising at this particular moment

    @DataType( DataType.Type.INTEGER )
    @NotNull
    public static final String ISCURRENT = "is_current"; // if this is the current stock value
    // 0 means it is not current, 1 means the current stock value is hot off the press

    /* Arrays */

    /**
     * Projection of the columns we need to show stocks as used in
     * {@link com.sam_chordas.android.stockhawk.ui.MyStocksActivity}.
     * */
    public static final String[] STOCKS_COLUMNS = {
            QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
            QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP };

    /**
     * Projection of the columns we need to show an individual stock's details as used in
     * {@link com.sam_chordas.android.stockhawk.ui.StockDetailActivity}.
     * */
    public static final String[] DETAIL_COLUMNS = { QuoteColumns.BIDPRICE };

    /* Integers */

    // indices for the stocks columns projection
    public static final int STOCKS_COLUMN_ID = 0;
    public static final int STOCKS_COLUMN_SYMBOL = 1;
    public static final int STOCKS_COLUMN_BIDPRICE = 2;
    public static final int STOCKS_COLUMN_PERCENT_CHANGE = 3;
    public static final int STOCKS_COLUMN_CHANGE = 4;
    public static final int STOCKS_COLUMN_ISUP = 5;

    // indices for the detail columns projection
    public static final int DETAIL_COLUMN_BIDPRICE = 0;

} // end class QuoteColumns
