package com.example.android.sunshine.app;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = "#SunshineApp";
    private ShareActionProvider mShareActionProvider;
    private String mForecastStr;
    private static final int DETAIL_LOADER = 0;
    private static final String[] DETAIL_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_HUMIDITY = 5;
    static final int COL_WIND_SPEED = 6;
    static final int COL_DEGREES = 7;
    static final int COL_PRESSURE = 8;
    static final int COL_WEATHER_CONDITION_ID = 9;

    private ImageView mIconView;
    private TextView mDateTextView;
    private TextView mFriendlyDateTextView;
    private TextView mDescriptionTextView;
    private TextView mHighTempTextView;
    private TextView mLowTempTextView;
    private TextView mHumidityTextView;
    private TextView mWindView;
    private TextView mPressureView;

    public DetailFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(DETAIL_LOADER, savedInstanceState, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        mDateTextView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        mFriendlyDateTextView = (TextView) rootView.findViewById(R.id.detail_day_textview);
        mDescriptionTextView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        mHighTempTextView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        mLowTempTextView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        mHumidityTextView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        mWindView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        mPressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detailfragment, menu);

        // set up ShareActionProvider
        MenuItem shareItem = menu.findItem(R.id.action_share);
        mShareActionProvider= (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        if (mForecastStr != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null?");
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastStr + " " + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent intent = getActivity().getIntent();
        if (intent == null) return null;

        return new CursorLoader(getActivity(), intent.getData(), DETAIL_COLUMNS, null, null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (! cursor.moveToFirst()) return;

        // getData from Cursor
        int weatherImageResource = Utility.getArtResourceForWeatherCondition(
                cursor.getInt(COL_WEATHER_CONDITION_ID));
        Long fullDate = cursor.getLong(COL_WEATHER_DATE);
        String dayName = Utility.getDayName(getActivity(), fullDate);
        String monthDay = Utility.getFormattedMonthDay(getActivity(), fullDate);
        String weatherDesc = cursor.getString(COL_WEATHER_DESC);
        boolean isMetric = Utility.isMetric(loader.getContext());
        String weatherMax = Utility.formatTemperature(
                getActivity(), cursor.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        String weatherMin = Utility.formatTemperature(
                getActivity(), cursor.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
        String humidity = getActivity().getString(R.string.format_humidity,
                cursor.getDouble(COL_HUMIDITY));
        String wind = Utility.formatWind(getActivity(),
                cursor.getDouble(COL_WIND_SPEED), cursor.getDouble(COL_DEGREES),
                isMetric);
        String pressure = getActivity().getString(R.string.format_pressure,
                cursor.getDouble(COL_PRESSURE));

        // Set to appropriate view
        mFriendlyDateTextView.setText(dayName);
        mDateTextView.setText(monthDay);
        mHighTempTextView.setText(weatherMax);
        mLowTempTextView.setText(weatherMin);
        mIconView.setImageResource(weatherImageResource);
        mDescriptionTextView.setText(weatherDesc);
        mHumidityTextView.setText(humidity);
        mWindView.setText(wind);
        mPressureView.setText(pressure);

        mForecastStr = String.format("%s, %s - %s - %s/%s", dayName, monthDay, weatherDesc,
                weatherMin, weatherMax);
        // if onCreateOptionsMenu has already happened, we need to update the share intent
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}

