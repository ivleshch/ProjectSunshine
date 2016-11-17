package com.example.ivleshch.sunshine.datagson;

/**
 * Created by Ivleshch on 01.11.2016.
 */
public class ListJson {
    private Weather[] weather;

    private int humidity;

    private double pressure;

    private Temp temp;

    private double speed;

    private double deg;

    public double getSpeed ()
    {
        return speed;
    }

    public double getDeg ()
    {
        return deg;
    }

    public int getHumidity ()
    {
        return humidity;
    }

    public double getPressure ()
    {
        return pressure;
    }

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