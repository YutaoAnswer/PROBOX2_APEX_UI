package com.netxeon.newprobox2.newyahooweather;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.netxeon.newprobox2.utils.Logger;
import com.netxeon.newprobox2.weather.WeatherUtilsV2;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

public class NewYahooWeatherHandler extends AsyncTask<Void, Void, NewYahooWeather> {

    private Handler mHandler;
    private String mCity;

    public NewYahooWeatherHandler(Handler handler, String city) {
        this.mHandler = handler;
        this.mCity = city;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.i(Logger.TAG_WEATHER, "NewYahooWeatherHandler.onPreExecute() ");

    }

    @Override
    protected NewYahooWeather doInBackground(Void... arg0) {
        Log.i(Logger.TAG_WEATHER, "NewYahooWeatherHandler.doInBackground() city:" + mCity);
        NewYahooWeather info;
        String jsonStr = getWeatherJsonString();

        if (jsonStr != null) {
            try {
                info = new NewYahooWeather();
                JSONObject jsonObj = new JSONObject(jsonStr);
                JSONObject location = jsonObj.getJSONObject("location");
                info.setLat(location.getLong("lat"));
                info.setLat(location.getLong("lat"));
                info.setCity(location.getString("city"));
                info.setRegion(location.getString("region"));
                info.setCountry(location.getString("country"));

                JSONObject current_observation = jsonObj.getJSONObject("current_observation");

                JSONObject wind = current_observation.getJSONObject("wind");
                info.setWind_chill(wind.getLong("chill"));
                info.setWind_direction(wind.getLong("direction"));
                info.setWind_speed(wind.getLong("speed"));

                JSONObject atmosphere = current_observation.getJSONObject("atmosphere");
                info.setAtmosphere_humidity(atmosphere.getLong("humidity"));
                info.setAtmosphere_visibility(atmosphere.getLong("visibility"));
                info.setAtmosphere_pressure(atmosphere.getLong("pressure"));

                JSONObject condition = current_observation.getJSONObject("condition");
                info.setDesc(condition.getString("text"));
                info.setCode(condition.getLong("code"));
                info.setTemp(condition.getLong("temperature"));

            } catch (final JSONException e) {
                Log.e(Logger.TAG_WEATHER, "Json parsing error: " + e.getMessage());
                info = null;
            }

        } else {
            Log.e(Logger.TAG_WEATHER, "Couldn't get json from server.");
            info = null;
        }

        return info;
    }

    @Override
    protected void onPostExecute(NewYahooWeather result) {
        super.onPostExecute(result);
        if (result != null) {
            Message msg = new Message();
            msg.what = WeatherUtilsV2.MSG_WEATHER_OK_NEW;
            msg.obj = result;
            mHandler.sendMessage(msg);

        } else {
            mHandler.sendEmptyMessage(WeatherUtilsV2.MSG_WEATHER_FAILED);
        }
    }

    private String getWeatherJsonString() {
        String MY_CONSUMER_KEY = "dj0yJmk9RDZPWVVoRW94QzMxJmQ9WVdrOVpFZHRSR1ZhTkdNbWNHbzlNQS0tJnM9Y29uc3VtZXJzZWNyZXQmeD0xZA--";
        String MY_CONSUMER_SECRET = "14e570834dc0dc15ad90bb164f8ba26f24d5b494";
        OAuthConsumer consumer = new CommonsHttpOAuthConsumer(MY_CONSUMER_KEY,
                MY_CONSUMER_SECRET);

        String result;
        StringBuilder sb = null;
        String line;
        String uri;
        int woeid;
        try {
            woeid = Integer.parseInt(mCity);
        } catch (NumberFormatException e) {
            woeid = -1;
        }
        if (woeid != -1) {
            uri = "https://weather-ydn-yql.media.yahoo.com/forecastrss?woeid=" + woeid + "&format=json";
        } else {
            uri = "https://weather-ydn-yql.media.yahoo.com/forecastrss?location=" + mCity + "&format=json";
        }
        try {
//            HttpGet request = new HttpGet("https://weather-ydn-yql.media.yahoo.com/forecastrss?location=" + mCity + "&format=json");
            HttpGet request = new HttpGet(uri);
            consumer.sign(request);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(request);
            InputStream stream = response.getEntity().getContent();

            Log.e(Logger.TAG_WEATHER, "Response: " + response.getStatusLine().getStatusCode() + " "
                    + response.getStatusLine().getReasonPhrase());

            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (sb != null) {
            result = sb.toString();
        } else {
            result = null;
        }
        Log.e(Logger.TAG_WEATHER, "Response entity: " + result);
        return result;
    }
}
