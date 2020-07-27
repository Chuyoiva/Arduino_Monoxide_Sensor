package com.example.cosensor;

public class Lectura {
    private int id;
    private float ppmVal;
    private int temperature;
    private int humidity;
    private double latitudeY;
    private double longitudeX;
    private String date;
    private String time;

    public Lectura(int _id, float _ppmVal, int _t, int _h, double _laY, double _loX, String _time, String _date){
        id = _id;
        ppmVal = _ppmVal;
        temperature = _t;
        humidity = _h;
        latitudeY = _laY;
        longitudeX = _loX;
        time = _time;
        date = _date;
    }

    public int getId(){
        return id;
    }

    public float getPPMValue(){
        return  ppmVal;
    }

    public int getTemperature(){
        return  temperature;
    }

    public int getHumidity(){
        return  humidity;
    }
    public double getLatitudeY(){
        return  latitudeY;
    }

    public double getLongitudeX(){
        return  longitudeX;
    }

    public String getTime(){
        return  time;
    }

    public String getDate(){
        return  date;
    }
}
