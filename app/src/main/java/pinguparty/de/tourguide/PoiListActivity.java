package pinguparty.de.tourguide;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;


import com.mapbox.services.commons.models.Position;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import pinguparty.de.tourguide.R;
import pinguparty.de.tourguide.web.PoiAdapter;
import pinguparty.de.tourguide.web.models.Poi;

public class PoiListActivity extends AppCompatActivity {

    private List<Poi> poiList = new ArrayList<>();
    private RecyclerView recyclerView;
    private PoiAdapter poiAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        //Location lastLocString = intent.getStringExtra("USER_POSITION");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.PoiList);

        poiAdapter = new PoiAdapter(poiList);
        RecyclerView.LayoutManager poiLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(poiLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(poiAdapter);

        getNearPois();

        /*
        Hier müsste dann die User Position an den Server geschickt werden,
        woraufhin dieser eine Liste mit POIs zurückgibt.
        Diese wird hier manuell erstellt.
         */

    }
    private void getNearPois(){
        Poi poi = new Poi("TH Bingen",Position.fromLngLat(49.9521871,7.9242871),"Die Hochschule an der wir studieren");
        poiList.add(poi);
        poi = new Poi("Dreifaltigkeitskirche",Position.fromLngLat(50.02203,8.1732013), "Eine Kirche in meinem Dorf");
        poiList.add(poi);
        poi = new Poi("Mainzer Dom",Position.fromLngLat(49.9988137,8.273672));
        poiList.add(poi);
        poi = new Poi("Marvins Haus",Position.fromLngLat(50.0243426,8.1738577));
        poiList.add(poi);
        poi = new Poi("Mount Doom",Position.fromLngLat(50.0097887,8.180651));
        poiList.add(poi);

        poiAdapter.notifyDataSetChanged();
    }
}
