package com.example.ivleshch.sunshine.datagson;

/**
 * Created by Ivleshch on 01.11.2016.
 */
public class ListJson {
    private Weather[] weather;

    private Temp temp;

    public Weather[] getWeather() {
        return weather;
    }

    public void setWeather(Weather[] weather) {
        this.weather = weather;
    }

    public Temp getTemp() {
        return temp;
    }

    public void setTemp(Temp temp) {
        this.temp = temp;
    }

}