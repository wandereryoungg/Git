package com.example.coolweather.util;

import android.text.TextUtils;

import com.example.coolweather.HeWeatherBean;
import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {

    public static HeWeatherBean handWeatherResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String content = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(content,HeWeatherBean.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray jsonArray = new JSONArray(response);
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject object = jsonArray.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(object.getString("name"));
                    province.setProvinceId(object.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
            return false;
    }

    public static boolean handleCityResponse(String response, int provinceId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray jsonArray = new JSONArray(response);
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject object = jsonArray.getJSONObject(i);
                    City city = new City();
                    city.setCityName(object.getString("name"));
                    city.setCityId(object.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCountyResponse(String response, int cityId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray jsonArray = new JSONArray(response);
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject object = jsonArray.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(object.getString("name"));
                    county.setWeatherId(object.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
