package com.example.ivleshch.sunshine;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


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
//            FetchWeatherTask weatherTask = new FetchWeatherTask();
//            weatherTask.execute("Cherkasy,Ukraine");
            //{OkHTTP
            try {
//                doGetRequest("http://api.openweathermap.org/data/2.5/forecast/daily?q=Cherkasy,Ukraine&mode=json&units=metric&cnt=7&APPID=598ac5920b2c6b8de6760e270730def3");
                doGetRequest("Cherkasy,Ukraine");
            } catch (IOException e) {
                e.printStackTrace();
            }
            //OkHTTP}
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        int numDays = 7;

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

                String day;
                String description;
                String highAndLow;
                final String[] resultStrs = new String[7];

                GregorianCalendar gc = new GregorianCalendar();

                if (response.isSuccessful()) {
                    String res = response.body().string();
                    try {
                        JSONObject Jobject = new JSONObject(res);
                        JSONArray Jarray = Jobject.getJSONArray("list");
                        for (int i = 0; i < Jarray.length(); i++) {
                            JSONObject object = Jarray.getJSONObject(i);

                            double high = object.getJSONObject("temp").getDouble("max");
                            double low = object.getJSONObject("temp").getDouble("min");
                            description = object.getJSONArray("weather").getJSONObject(0).getString("main");

                            highAndLow = formatHighLows(high, low);

                            Date time = gc.getTime();
                            day = getReadableDateString(time);
                            gc.add(GregorianCalendar.DATE, 1);

                            resultStrs[i] = day + " - " + description + " - " + highAndLow;

                        }
                    } catch (JSONException e) {
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
//                    for (String dayForecastStr : resultStrs) {
//                        mForecastAdapter.add(dayForecastStr);
//
//                    }

                    //  result = getWeatherDataFromJson(res, 7);
//                    mForecastAdapter.clear();
//                    for (String dayForecastStr : result) {
//                        mForecastAdapter.add(dayForecastStr);
//                    }
                }
            }
        });
    }

    private String getReadableDateString(Date time) {
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    private String formatHighLows(double high, double low) {
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        String[] data = {
                "Mon 6/23- Sunny - 31/17",
                "Tue 6/24 - Foggy - 21/8",
                "Wed 6/25 - Cloudy - 22/17",
                "Thurs 6/26 - Rainy - 18/11",
                "Fri 6/27 - Sunny - 21/10",
                "Sat 6/28 - Sunny - 23/18",
                "Sun 6/29 - Sunny - 20/7"
        };
        List<String> weekForecast = new ArrayList<String>(Arrays.asList(data));

        mForecastAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, weekForecast);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);

        return rootView;
    }

    //    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
//            throws JSONException {
//
//        final String OWM_LIST = "list";
//        final String OWM_WEATHER = "weather";
//        final String OWM_TEMPERATURE = "temp";
//        final String OWM_MAX = "max";
//        final String OWM_MIN = "min";
//        final String OWM_DESCRIPTION = "main";
//
//        JSONObject forecastJson = new JSONObject(forecastJsonStr);
//        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
//
////            Time dayTime = new Time();
////            dayTime.setToNow();
//
//        GregorianCalendar gc = new GregorianCalendar();
//
////            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
//
////            dayTime = new Time();
//
//        String[] resultStrs = new String[numDays];
//        for (int i = 0; i < weatherArray.length(); i++) {
//            String day;
//            String description;
//            String highAndLow;
//
//            JSONObject dayForecast = weatherArray.getJSONObject(i);
//
//            long dateTime;
//
//
//            Date time = gc.getTime();
//            day = getReadableDateString(time);
//            gc.add(GregorianCalendar.DATE, 1);
////                dateTime = dayTime.setJulianDay(julianStartDay + i);
////                day = getReadableDateString(dateTime);
//
//            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
//            description = weatherObject.getString(OWM_DESCRIPTION);
//
//            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
//            double high = temperatureObject.getDouble(OWM_MAX);
//            double low = temperatureObject.getDouble(OWM_MIN);
//
//            highAndLow = formatHighLows(high, low);
//            resultStrs[i] = day + " - " + description + " - " + highAndLow;
//        }
//
//        for (String s : resultStrs) {
//            Log.v(FetchWeatherTask.class.getSimpleName(), "Forecast entry: " + s);
//        }
//
//        return resultStrs;
//
//    }

//    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
//
//        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
//
//        private String getReadableDateString(Date time) {
//            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
//            return shortenedDateFormat.format(time);
//        }
//
//        private String formatHighLows(double high, double low) {
//            long roundedHigh = Math.round(high);
//            long roundedLow = Math.round(low);
//
//            String highLowStr = roundedHigh + "/" + roundedLow;
//            return highLowStr;
//        }
//
//        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
//                throws JSONException {
//
//            final String OWM_LIST = "list";
//            final String OWM_WEATHER = "weather";
//            final String OWM_TEMPERATURE = "temp";
//            final String OWM_MAX = "max";
//            final String OWM_MIN = "min";
//            final String OWM_DESCRIPTION = "main";
//
//            JSONObject forecastJson = new JSONObject(forecastJsonStr);
//            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
//
////            Time dayTime = new Time();
////            dayTime.setToNow();
//
//            GregorianCalendar gc = new GregorianCalendar();
//
////            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
//
////            dayTime = new Time();
//
//            String[] resultStrs = new String[numDays];
//            for (int i = 0; i < weatherArray.length(); i++) {
//                String day;
//                String description;
//                String highAndLow;
//
//                JSONObject dayForecast = weatherArray.getJSONObject(i);
//
//                long dateTime;
//
//
//                Date time = gc.getTime();
//                day = getReadableDateString(time);
//                gc.add(GregorianCalendar.DATE, 1);
////                dateTime = dayTime.setJulianDay(julianStartDay + i);
////                day = getReadableDateString(dateTime);
//
//                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
//                description = weatherObject.getString(OWM_DESCRIPTION);
//
//                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
//                double high = temperatureObject.getDouble(OWM_MAX);
//                double low = temperatureObject.getDouble(OWM_MIN);
//
//                highAndLow = formatHighLows(high, low);
//                resultStrs[i] = day + " - " + description + " - " + highAndLow;
//            }
//
//            for (String s : resultStrs) {
//                Log.v(LOG_TAG, "Forecast entry: " + s);
//            }
//
//            return resultStrs;
//
//        }
//
//
//        @Override
//        protected String[] doInBackground(String... params) {
//
//            if (params.length == 0) {
//                return null;
//            }
//            HttpURLConnection urlConnection = null;
//            BufferedReader reader = null;
//
//            String forecastJsonStr = null;
//
//            String format = "json";
//            String units = "metric";
//            int numDays = 7;
//
//            try {
////                String baseUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?q=Cherkasy,Ukraine&mode=json&units=metric&cnt=7";
////                String apiKey = "&APPID=598ac5920b2c6b8de6760e270730def3";
////                URL url = new URL(baseUrl.concat(apiKey));
//
//                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
//                final String QUERY_PARAM = "q";
//                final String FORMAT_PARAM = "mode";
//                final String UNITS_PARAM = "units";
//                final String DAYS_PARAM = "cnt";
//                final String APPID_PARAM = "APPID";
//
//                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
//                        .appendQueryParameter(QUERY_PARAM, params[0])
//                        .appendQueryParameter(FORMAT_PARAM, format)
//                        .appendQueryParameter(UNITS_PARAM, units)
//                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
//                        //.appendQueryParameter(APPID_PARAM, "598ac5920b2c6b8de6760e270730def3")
//                        .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
//                        .build();
//
//                URL url = new URL(builtUri.toString());
//
//                Log.v(LOG_TAG, "Built URI " + builtUri.toString());
//
//
//                urlConnection = (HttpURLConnection) url.openConnection();
//                urlConnection.setRequestMethod("GET");
//                urlConnection.connect();
//
//                InputStream inputStream = urlConnection.getInputStream();
//                StringBuffer buffer = new StringBuffer();
//                if (inputStream == null) {
//                    return null;
//                }
//
//                reader = new BufferedReader(new InputStreamReader(inputStream));
//
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    buffer.append(line + "\n");
//                }
//
//                if (buffer.length() == 0) {
//                    return null;
//                }
//
//                forecastJsonStr = buffer.toString();
//                Log.v(LOG_TAG, "Test -" + forecastJsonStr);
//            } catch (IOException e) {
//                Log.e(LOG_TAG, "Error ", e);
//                return null;
//            } finally {
//                if (urlConnection != null) {
//                    urlConnection.disconnect();
//                }
//                if (reader != null) {
//                    try {
//                        reader.close();
//                    } catch (final IOException e) {
//                        Log.e(LOG_TAG, "Error closing stream", e);
//                    }
//                }
//            }
//
//            try {
//                return getWeatherDataFromJson(forecastJsonStr, numDays);
//            } catch (JSONException e) {
//                Log.e(LOG_TAG, e.getMessage(), e);
//                e.printStackTrace();
//            }
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String[] result) {
//            if (result != null) {
//                mForecastAdapter.clear();
//                for (String dayForecastStr : result) {
//                    mForecastAdapter.add(dayForecastStr);
//                }
//            }
//        }
//    }
}
