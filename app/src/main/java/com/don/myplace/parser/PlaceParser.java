package com.don.myplace.parser;

import android.util.Log;

import com.don.myplace.model.SavedPlace;
import com.google.android.gms.location.places.Place;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dli on 12/19/2016.
 */

public class PlaceParser {
    public static List<SavedPlace> parsePlace(String in ) {

        try {
            List<SavedPlace> res = new ArrayList<>();

            JSONObject root = new JSONObject(in);
            JSONArray resList = root.getJSONArray("results");
            JSONObject obj;

            SavedPlace savedPlace;

            for (int i = 0; i < resList.length(); i++) {
                obj = resList.getJSONObject(i);
                savedPlace = new SavedPlace();
                savedPlace.setPlaceId(obj.getString("place_id"));
                savedPlace.setAddress(obj.getString("formatted_address"));
                savedPlace.setTelephone(null);
                savedPlace.setTitle(obj.getString("name"));
                savedPlace.setType(obj.getString("types"));
                res.add(savedPlace);            }

            return res;
        }catch (JSONException e){
            Log.d("PlaceSearchFragment", e.getMessage());
        }
        return null;
    }

    public static String parsePhoneNumber (String in) {
        try {
            JSONObject root = new JSONObject(in);
            JSONObject obj = root.getJSONObject("result");

            return obj.getString("formatted_phone_number");
        }catch (JSONException e){
            Log.d("PlaceSearchFragment", e.getMessage());
        }
        return null;
    }

    public static Map<String, String> parseDistanceAndTime (String in) {

        Map<String, String> res = new HashMap<>();
        try{
            JSONObject root = new JSONObject(in);
            JSONObject resObj = root.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0);
            Log.d("PlaceSearchFragment","you have: "+resObj.toString());
            if(!resObj.getString("status").equals("OK")){
                res.put("distance", "unknown");
                res.put("duration", "unknown");
            }
            else{
                res.put("distance", resObj.getJSONObject("distance").getString("text"));
                res.put("duration", resObj.getJSONObject("duration").getString("text"));
            }

        }catch (JSONException e) {
            Log.d("PlaceSearchFragment", e.getMessage());
        }
        return res;
    }
}
