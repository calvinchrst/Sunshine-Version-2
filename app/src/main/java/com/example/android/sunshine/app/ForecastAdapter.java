package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {
    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE_DAY = 1;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }
    
    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    /*
                Remember that these views are reused as needed.
             */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        if (viewType == VIEW_TYPE_TODAY) layoutId = R.layout.list_item_forecast_today;
        else if (viewType == VIEW_TYPE_FUTURE_DAY) layoutId = R.layout.list_item_forecast;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // get Data from cursor
        String date = Utility.getFriendlyDayString(context,
                cursor.getLong(ForecastFragment.COL_WEATHER_DATE));
        String weatherDesc = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        boolean isMetric = Utility.isMetric(context);
        String high = Utility.formatTemperature(context,
                        cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP), isMetric);
        String low = Utility.formatTemperature(context,
                        cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP), isMetric);

        // bind to View
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.iconView.setImageResource(R.drawable.ic_launcher);
        viewHolder.dateView.setText(date);
        viewHolder.descriptionView.setText(weatherDesc);
        viewHolder.highTempView.setText(high);
        viewHolder.lowTempView.setText(low);
    }

    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view) {
            this.iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            this.dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            this.descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            this.highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            this.lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }
}