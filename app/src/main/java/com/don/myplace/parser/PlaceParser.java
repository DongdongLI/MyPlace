package com.don.myplace.parser;

import com.google.android.gms.location.places.Place;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dli on 12/19/2016.
 */

public class PlaceParser {
    public static List<String> parse(String in ) {

        try {
            List<String> res = new ArrayList<>();

            JSONObject root = new JSONObject(in);
            JSONArray resList = root.getJSONArray("results");
            JSONObject obj;
            for (int i = 0; i < resList.length(); i++) {
                obj = resList.getJSONObject(i);
                res.add(obj.getString("place_id"));
            }

            return res;
        }catch (JSONException e){

        }
        return null;
    }
}
