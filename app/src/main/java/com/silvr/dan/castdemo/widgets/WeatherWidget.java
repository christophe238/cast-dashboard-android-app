package com.silvr.dan.castdemo.widgets;

import android.content.Context;

import com.silvr.dan.castdemo.Widget;
import com.silvr.dan.castdemo.settingsFragments.WeatherSettings;

import org.json.JSONException;
import org.json.JSONObject;


public class WeatherWidget extends UIWidget {
    public static String HUMAN_NAME = "Weather";

    public WeatherWidget(Context context, Widget widget) {
        super(context, widget);
    }


    @Override
    public JSONObject getContent() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("lat", widget.getOption(WeatherSettings.WEATHER_LAT).value);
        json.put("lng", widget.getOption(WeatherSettings.WEATHER_LNG).value);
        return json;
    }

    @Override
    public String getWidgetPreviewSecondaryHeader() {

        return WeatherSettings.getNameFromCoordinates(context, widget);
    }
}