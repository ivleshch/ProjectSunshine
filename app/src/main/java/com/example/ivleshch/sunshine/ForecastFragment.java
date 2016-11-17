package com.example.ivleshch.sunshine;


import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.ivleshch.sunshine.data.WeatherContract;
import com.example.ivleshch.sunshine.data.WeatherContract.WeatherEntry;
import com.example.ivleshch.sunshine.datagson.CurrentWeather;
import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;


/**
 * Created by Ivleshch on 26.10.2016.
 */
public class ForecastFragment extends Fragment {
    private ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void updateWeather() {
        try {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String location = settings.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
            doGetRequest(location);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("ErrorInsert", "Inserted");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    String[] convertContentValuesToUXFormat(Vector<ContentValues> cvv, String units) {
        String[] resultStrs = new String[cvv.size()];
        for ( int i = 0; i < cvv.size(); i++ ) {
            ContentValues weatherValues = cvv.elementAt(i);
            String highAndLow = formatHighLows(
                    weatherValues.getAsDouble(WeatherEntry.COLUMN_MAX_TEMP),
                    weatherValues.getAsDouble(WeatherEntry.COLUMN_MIN_TEMP),
                    units);
            resultStrs[i] = getReadableDateString(
                    weatherValues.getAsLong(WeatherEntry.COLUMN_DATE)) +
                    " - " + weatherValues.getAsString(WeatherEntry.COLUMN_SHORT_DESC) +
                    " - " + highAndLow;
        }
        return resultStrs;
    }

    void doGetRequest(String... params) throws IOException {

        final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
        final String QUERY_PARAM = "q";
        final String FORMAT_PARAM = "mode";
        final String UNITS_PARAM = "units";
        final String DAYS_PARAM = "cnt";
        final String APPID_PARAM = "APPID";
        final int numDays = 7;

        final Vector<ContentValues> cVVector = new Vector<ContentValues>(numDays);

        String format = "json";

        final String location = params[0];

        Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, params[0])
                .appendQueryParameter(FORMAT_PARAM, format)
                .appendQueryParameter(UNITS_PARAM, "metric")
                .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                //.appendQueryParameter(APPID_PARAM, "598ac5920b2c6b8de6760e270730def3")
                .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                .build();

        URL url = new URL(builtUri.toString());


        Request request = new Request.Builder()
                .url(url)
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
                                            @Override
                                            public void onFailure(Request request, IOException e) {
                                                ((Activity) mForecastAdapter.getContext()).runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        updateList(location);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onResponse(Response response) throws IOException {

                                                final String[] resultStrs;// = new String[7];

                                                Time dayTime = new Time();
                                                dayTime.setToNow();
                                                int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
                                                dayTime = new Time();

                                                if (response.isSuccessful()) {
                                                    String res = response.body().string();

                                                    Gson gson = new Gson();
                                                    CurrentWeather currentWeather = gson.fromJson(res, CurrentWeather.class);

                                                    String cityName = currentWeather.getCity().getName();
                                                    double cityLatitude = currentWeather.getCity().getCoord().getLat();
                                                    double cityLongitude = currentWeather.getCity().getCoord().getLon();

                                                    long locationId = addLocation(location, cityName, cityLatitude, cityLongitude);


                                                    String units = "metric";

                                                    if (currentWeather != null) {
                                                        for (int i = 0; i < numDays; i++) {
                                                            double pressure = currentWeather.getList()[i].getPressure();
                                                            int humidity = currentWeather.getList()[i].getHumidity();
                                                            double windSpeed = currentWeather.getList()[i].getSpeed();
                                                            double windDirection = currentWeather.getList()[i].getDeg();
                                                            int weatherId = currentWeather.getList()[i].getWeather()[0].getId();
                                                            String description = currentWeather.getList()[i].getWeather()[0].getMain();
                                                            long dateTime;

                                                            double high = currentWeather.getList()[i].getTemp().getMax();
                                                            double low = currentWeather.getList()[i].getTemp().getMin();

                                                            dateTime = dayTime.setJulianDay(julianStartDay+i);

                                                            ContentValues weatherValues = new ContentValues();

                                                            weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationId);
                                                            weatherValues.put(WeatherEntry.COLUMN_DATE, dateTime);
                                                            weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, humidity);
                                                            weatherValues.put(WeatherEntry.COLUMN_PRESSURE, pressure);
                                                            weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
                                                            weatherValues.put(WeatherEntry.COLUMN_DEGREES, windDirection);
                                                            weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, high);
                                                            weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, low);
                                                            weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, description);
                                                            weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherId);

                                                            cVVector.add(weatherValues);
                                                        }

                                                        int inserted = 0;
                                                        if (cVVector.size() > 0) {
                                                            ContentValues[] cvArray = new ContentValues[cVVector.size()];
                                                            cVVector.toArray(cvArray);
                                                            inserted = ((Activity) mForecastAdapter.getContext()).getContentResolver().bulkInsert(WeatherEntry.CONTENT_URI, cvArray);
                                                        }
                                                    }

                                                    ((Activity) mForecastAdapter.getContext()).runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            updateList(location);
                                                        }
                                                    });
                                                }
                                            }
                                        }

        );
    }

    long addLocation(String locationSetting, String cityName, double lat, double lon) {
        long locationId;
        Context mContext = ((Activity) mForecastAdapter.getContext());

        Cursor locationCursor = mContext.getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{locationSetting},
                null);

        if (locationCursor.moveToFirst()) {
            int locationIdIndex = locationCursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            locationId = locationCursor.getLong(locationIdIndex);
        } else {
            ContentValues locationValues = new ContentValues();
            locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);

            Uri insertedUri = mContext.getContentResolver().insert(
                    WeatherContract.LocationEntry.CONTENT_URI,
                    locationValues
            );

            locationId = ContentUris.parseId(insertedUri);
        }

        locationCursor.close();
        return locationId;
    }

    void updateList(String location){
        String sortOrder = WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(
                location, System.currentTimeMillis());

        Cursor cur = ((Activity) mForecastAdapter.getContext()).getContentResolver().query(weatherForLocationUri,
                null, null, null, sortOrder);

        Vector<ContentValues> cVVector = new Vector<ContentValues>(cur.getCount());
        if ( cur.moveToFirst() ) {
            do {
                ContentValues cv = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(cur, cv);
                cVVector.add(cv);
            } while (cur.moveToNext());
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String units = sharedPrefs.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_metric));
        String[] resultStrs = convertContentValuesToUXFormat(cVVector,units);

        mForecastAdapter.clear();
        for (String dayForecastStr : resultStrs) {
            mForecastAdapter.add(dayForecastStr);

        }
    }

    private String getReadableDateString(long time){
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date).toString();
    }

    private String formatHighLows(double high, double low, String units) {
        if (units.equals(getString(R.string.pref_units_imperial))) {
            high = (high * 1.8) + 32;
            low = (low * 1.8) + 32;
        }

        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mForecastAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, new ArrayList<String>());

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String forecast = mForecastAdapter.getItem(i);
                Intent intent = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(intent);
            }
        });

        return rootView;
    }
}
