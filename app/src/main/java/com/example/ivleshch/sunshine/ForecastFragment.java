package com.example.ivleshch.sunshine;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
import java.util.GregorianCalendar;


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
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    void doGetRequest(String... params) throws IOException {

        final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
        final String QUERY_PARAM = "q";
        final String FORMAT_PARAM = "mode";
        final String UNITS_PARAM = "units";
        final String DAYS_PARAM = "cnt";
        final String APPID_PARAM = "APPID";

        String format = "json";
        String units = "metric";
        final int numDays = 7;

        Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, params[0])
                .appendQueryParameter(FORMAT_PARAM, format)
                .appendQueryParameter(UNITS_PARAM, units)
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

                                            }

                                            @Override
                                            public void onResponse(Response response) throws IOException {

                                                final String[] resultStrs = new String[7];

                                                GregorianCalendar gc = new GregorianCalendar();

                                                if (response.isSuccessful()) {
                                                    String res = response.body().string();

                                                    Gson gson = new Gson();
                                                    CurrentWeather currentWeather = gson.fromJson(res, CurrentWeather.class);

                                                    if (currentWeather != null) {
                                                        for (int i = 0; i < numDays; i++) {
                                                            double high = currentWeather.getList()[i].getTemp().getMax();
                                                            double low = currentWeather.getList()[i].getTemp().getMin();

                                                            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                                                            String units = sharedPrefs.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_metric));

                                                            String highAndLow = formatHighLows(high, low, units);
                                                            Date time = gc.getTime();

                                                            resultStrs[i] = getReadableDateString(time) + " - " + currentWeather.getList()[i].getWeather()[0].getMain() + " : " + highAndLow;
                                                            gc.add(GregorianCalendar.DATE, 1);
                                                        }
                                                    }

                                                    ((Activity) mForecastAdapter.getContext()).runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            mForecastAdapter.clear();
                                                            for (String dayForecastStr : resultStrs) {
                                                                mForecastAdapter.add(dayForecastStr);

                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        }

        );
    }

    private String getReadableDateString(Date time) {
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
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
