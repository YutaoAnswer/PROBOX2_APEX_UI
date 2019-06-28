package com.netxeon.newprobox2.newyahooweather;

import android.graphics.Bitmap;

import org.json.JSONArray;

public class NewYahooWeather {

    private long lon ;
    private long lat ;
    private long wind_chill;
    private long wind_direction;
    private long wind_speed;
    private long atmosphere_humidity;
    private long atmosphere_visibility;
    private long atmosphere_pressure;
    private String city;
    private String region;
    private String country;
    private String desc;
    private int mCurrentCode = 0;
    private long code ;
    private long temp ;

    public long getLon() {
        return lon;
    }

    public void setLon(long lon) {
        this.lon = lon;
    }

    public long getLat() {
        return lat;
    }

    public void setLat(long lat) {
        this.lat = lat;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public long getTemp() {
        return temp;
    }

    public void setTemp(long temp) {
        this.temp = temp;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public long getWind_direction() {
        return wind_direction;
    }

    public void setWind_direction(long wind_direction) {
        this.wind_direction = wind_direction;
    }

    public long getWind_speed() {
        return wind_speed;
    }

    public void setWind_speed(long wind_speed) {
        this.wind_speed = wind_speed;
    }

    public long getWind_chill() {
        return wind_chill;
    }

    public void setWind_chill(long wind_chill) {
        this.wind_chill = wind_chill;
    }

    public long getAtmosphere_humidity() {
        return atmosphere_humidity;
    }

    public void setAtmosphere_humidity(long atmosphere_humidity) {
        this.atmosphere_humidity = atmosphere_humidity;
    }

    public long getAtmosphere_pressure() {
        return atmosphere_pressure;
    }

    public void setAtmosphere_pressure(long atmosphere_pressure) {
        this.atmosphere_pressure = atmosphere_pressure;
    }

    public long getAtmosphere_visibility() {
        return atmosphere_visibility;
    }

    public void setAtmosphere_visibility(long atmosphere_visibility) {
        this.atmosphere_visibility = atmosphere_visibility;
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public int getCurrentCode() {
        return this.mCurrentCode;
    }

    protected void setCurrentCode(int currentCode) {
        this.mCurrentCode = currentCode;
    }


}
