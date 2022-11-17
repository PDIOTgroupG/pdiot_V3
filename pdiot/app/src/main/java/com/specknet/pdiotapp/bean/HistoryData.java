package com.specknet.pdiotapp.bean;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistoryData {

    private String name;
    private String activity;
    private String date;


    public String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(

                "yyyy-MM-dd", Locale.getDefault());

        Date date = new Date();

        return dateFormat.format(date);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "HistoryData{" +
                "name='" + name + '\'' +
                ", activity='" + activity + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
