package pinguparty.de.tourguide;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.commons.models.Position;

import java.io.IOException;

import pinguparty.de.tourguide.web.models.Poi;

public class CommitPoiActivity extends AppCompatActivity {

    private Poi newPoi = new Poi(null,null);

    private Button commitButton;
    private Button getPosButton;
    private Button choosePosButton;
    private EditText nameEdit;
    private EditText descEdit;
    private EditText latEdit;
    private EditText lngEdit;
    private Position newpos;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commit_poi);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        nameEdit = findViewById(R.id.nameEditText);
        descEdit = findViewById(R.id.descEditText);
        latEdit = findViewById(R.id.latEditText);
        lngEdit = findViewById(R.id.longEditText);

        Intent intent = getIntent();
        Double sentLocLat = intent.getDoubleExtra("LatSent",0);
        Double sentLocLong = intent.getDoubleExtra("LongSent",0);
        if(sentLocLat != 0 && sentLocLong != 0) {
            latEdit.setText(sentLocLat.toString());
            lngEdit.setText(sentLocLong.toString());
        }

        getPosButton = findViewById(R.id.getPosButton);
        getPosButton.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = getIntent();
                Double lastLocLat = intent.getDoubleExtra("Lat",0);
                Double lastLocLong = intent.getDoubleExtra("Long", 0);
                latEdit.setText(lastLocLat.toString());
                lngEdit.setText(lastLocLong.toString());
            }
        });

        /*choosePosButton = findViewById(R.id.choosePosButton);
        choosePosButton.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //Choose Position Funktion
                Intent intent = new Intent(CommitPoiActivity.this, PoiChooserActivity.class);
                startActivity(intent);
            }
        }); */

        commitButton = findViewById(R.id.commitButton);
        commitButton.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //Log.d("foobar","nameEdit: " + nameEdit.getText().toString());
                //Log.d("foobar","descEdit: " + descEdit.getText().toString());
                //Log.d("foobar","latEdit: " + latEdit.getText().toString());           //Waren zwischenzeitlich zum testen gebraucht
                //Log.d("foobar","longEdit: " + lngEdit.getText().toString());
                newpos = Position.fromLngLat(Double.parseDouble(lngEdit.getText().toString()),Double.parseDouble(latEdit.getText().toString()));
                newPoi.setPoi(nameEdit.getText().toString(),descEdit.getText().toString(), newpos);

                //TODO POST Befehl für neuen POI hier einfügen!
                // createPoi(newPoi);
                Log.d("foobar","Neuen POI angelegt: Name: " + newPoi.getName() + " Beschreibung: " + newPoi.getDesc() + " Lat: " + newPoi.getPositionLat() + " Long: " + newPoi.getPositionLong());
            }
        });

    }

}
