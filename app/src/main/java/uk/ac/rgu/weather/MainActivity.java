package uk.ac.rgu.weather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.ac.rgu.weather.data.HourForecast;
import uk.ac.rgu.weather.data.HourForecastRecyclerViewAdapter;

public class MainActivity extends AppCompatActivity {

    public void buttonPressed(View button){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupRecyclerView();
    }

    List<HourForecast> hourForecasts;
    HourForecastRecyclerViewAdapter adapter;
    private void setupRecyclerView() {

        hourForecasts = new ArrayList<HourForecast>();

        // create the adapter for the RecyclerView
         adapter = new HourForecastRecyclerViewAdapter(getApplicationContext(), hourForecasts);

        // get the RecyclerView
        RecyclerView rvForecast = findViewById(R.id.forecast_list);

        // wireup the RecyclerView with the adapter
        rvForecast.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rvForecast.setAdapter(adapter);
    }

    public void getWeather(String location){
        Toast.makeText(getApplicationContext(), "Getting weather for " + location, Toast.LENGTH_LONG).show();

        // now make the HTTP request to get the forecast
        // build our URI
        Uri uri = Uri.parse("https://api.weatherapi.com/v1/forecast.json?key=a3b9cc3fb35943d5826152257210311");
        Uri.Builder uriBuilder = uri.buildUpon();
        uriBuilder.appendQueryParameter("q", location);
        uriBuilder.appendQueryParameter("days", String.valueOf(3));
        // create the final URL
        uri = uriBuilder.build();

        // use Volley to make the request
        StringRequest request = new StringRequest(
                Request.Method.GET,
                uri.toString(),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d("WeatherApp", response);

                        // for processing the date coming from Weather API
                        SimpleDateFormat dateInParser = new SimpleDateFormat("yyyy-MM-dd");

                        // for processing Dates for display in our app
                        SimpleDateFormat dateOutFormatter = new SimpleDateFormat("dd MMM");

                        // process response to get a list of HourForecast objects
                        // in the JSON, we're interested in forecast>[forecastday]>[hour]
                        try {
                            JSONObject rootObject = new JSONObject(response);
                            JSONObject forecastObj = rootObject.getJSONObject("forecast");
                            JSONArray forecastDayArray = forecastObj.getJSONArray("forecastday");
                            hourForecasts.clear();
                            for (int i = 0, j = forecastDayArray.length(); i < j; i++){
                                JSONObject forecastDayObject = forecastDayArray.getJSONObject(i);
                                JSONArray hoursArray = forecastDayObject.getJSONArray("hour");
                                for (int ii = 0, jj = hoursArray.length(); ii < jj; ii++){
                                    JSONObject hourObj = hoursArray.getJSONObject(ii);

                                    // time is in the format YYYY-MM-DD HH:mm
                                    // need to split into day and hour
                                    String time = hourObj.getString("time");
                                    int hour = Integer.parseInt(time.substring(11,13));
                                    Date date = dateInParser.parse(time.substring(0,10));
                                    String dateStr = dateOutFormatter.format(date);

                                    // get the temp
                                    int temp = (int)Math.round(hourObj.getDouble("temp_c"));

                                    // get the humidity
                                    int humidity = hourObj.getInt("humidity");

                                    // get the weather from condition>text
                                    JSONObject conditionObj = hourObj.getJSONObject("condition");
                                    String weather = conditionObj.getString("text");
                                    String weatherIcon = "https:" + conditionObj.getString("icon");

                                    // create an HourForecast with the extracted information
                                    HourForecast hf = new HourForecast();
                                    hf.setDate(dateStr);
                                    hf.setHour(hour);
                                    hf.setTemperature(temp);
                                    hf.setHumidity(humidity);
                                    hf.setWeather(weather);
                                    hf.setWeatherIcon(weatherIcon);
                                    hourForecasts.add(hf);
                                }
                            }
                        } catch (JSONException | ParseException e) {
                            e.printStackTrace();
                        }
                        adapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Download Error", Toast.LENGTH_LONG);
                Log.e("WeatherApp", error.getLocalizedMessage());
            }
        });
        // now make the request
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }
}