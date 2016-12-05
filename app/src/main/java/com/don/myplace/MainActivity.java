package com.don.myplace;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import com.google.android.gms.*
import android.widget.ListView;

import com.don.myplace.ListviewAdapter.PlaceAdapter;
import com.don.myplace.model.Place;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<Place> places = new ArrayList<>();

        // test data
        places.add(new Place("title", "type", "address", "telephone"));

        // initialize list view and adapter
        ListView listView = (ListView)findViewById(R.id.placeList);
        PlaceAdapter adapter = new PlaceAdapter(this, R.layout.row_item_layout, places);
        listView.setAdapter(adapter);

        adapter.setNotifyOnChange(true);
    }
}
