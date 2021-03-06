package pinguparty.de.tourguide;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.telemetry.location.LostLocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.location.LocationEnginePriority;
import com.mapbox.services.android.telemetry.permissions.PermissionsListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;
import com.mapbox.services.commons.models.Position;
import com.mapbox.mapboxsdk.style.layers.Layer;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import pinguparty.de.tourguide.web.models.Person;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import pinguparty.de.tourguide.web.WebRepo;
import retrofit2.converter.gson.GsonConverterFactory;

import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;

public class MainActivity extends AppCompatActivity implements LocationEngineListener, PermissionsListener {

    //initialvariablen
    private PermissionsManager permissionsManager;
    private LocationLayerPlugin locationPlugin;
    private LocationEngine locationEngine;
    private MapboxMap mapboxMap;
    private MapView mapView;

    //Variablen für Navigation
    //private MapboxNavigation navigation = new MapboxNavigation(this, "pk.eyJ1Ijoia3JldHpzY2htYXJ2aW4iLCJhIjoiY2pjbHFwZnl4MGFmcjJ3bnZtcXV6M2M3cSJ9.oPwCChtV9rMOTKEB9FLp3Q");
    private DirectionsRoute currentRoute;
    private Point originPoint;
    private Point destinationPoint;
    private static final String TAG = "DirectionsActivity";
    private NavigationMapRoute navigationMapRoute;
    private Marker target;

    //UI-Variablen
    private Button startNavButton;
    private Button activitySwitchButton;
    private FloatingActionButton commitPoiButton;
    private FloatingActionButton resetCameraButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, "pk.eyJ1Ijoia3JldHpzY2htYXJ2aW4iLCJhIjoiY2pjbHFwZnl4MGFmcjJ3bnZtcXV6M2M3cSJ9.oPwCChtV9rMOTKEB9FLp3Q");
        setContentView(R.layout.activity_main);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {
                Layer mapText = mapboxMap.getLayer("country-label-lg");
                mapText.setProperties(textField("{name_de}"));              //Deutsche Bezeichnungen auf der Karte
                MainActivity.this.mapboxMap = mapboxMap;
                enableLocationPlugin();
                Intent intent = getIntent();
                double lat = intent.getDoubleExtra("Lat",0);
                double lng = intent.getDoubleExtra("Long",0);
                String name = intent.getStringExtra("Name");
                String desc = intent.getStringExtra("Desc");
                //Log.d("foobar","Intent hat funktioniert: Lat: " + lat + " Long: " + lng);
                if(lat != 0 && lng != 0 && name != null) {
                    target = mapboxMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lng,lat))
                            .title(name)
                            .snippet(desc));            //Lat/long Vertauscht... irgendwo aufm weg... ich glaube bei der Hardcoded Liste
                    @SuppressLint("MissingPermission")
                    LatLngBounds latLngBounds = new LatLngBounds.Builder()
                                        .include(target.getPosition())
                                        .include(new LatLng(locationEngine.getLastLocation().getLatitude(),locationEngine.getLastLocation().getLongitude()))
                                        .build();
                    mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,500),1000);
                    originPoint = (Point.fromLngLat(locationEngine.getLastLocation().getLongitude(),locationEngine.getLastLocation().getLatitude()));
                    destinationPoint = (Point.fromLngLat(target.getPosition().getLongitude(),target.getPosition().getLatitude()));
                    addNavigation(originPoint,destinationPoint);
                }

                startNavButton = findViewById(R.id.startNavButton);         //TODO: failsafe einbauen
                startNavButton.setOnClickListener( new View.OnClickListener(){
                    public void onClick(View v){
                         /*
                         if(target != null) {
                            mapboxMap.removeMarker(target);
                            target = null;
                        }else{
                            target= mapboxMap.addMarker(new MarkerOptions().position(new LatLng(50.025230,8.176498)));
                        } */ //TEST

                        Point ori =originPoint;
                        Point des = destinationPoint;

                        String awsPoolID = null;        //Mögliche Eingabe von Amazon Polly ID

                        boolean simulate = true;

                        NavigationViewOptions options = NavigationViewOptions.builder()
                                .origin(ori)
                                .destination(des)
                                .awsPoolId(awsPoolID)
                                .shouldSimulateRoute(simulate)
                                .build();

                        Log.d("foobar","ori: " + ori.latitude() + " " + ori.longitude() + "des: " + des.latitude() + des.longitude());
                        NavigationLauncher.startNavigation(MainActivity.this,options);
                    }
                });

            }
        });

        resetCameraButton = findViewById(R.id.resetCameraButton);
        resetCameraButton.setOnClickListener( new View.OnClickListener(){
            @SuppressLint("MissingPermission")
            public void onClick(View v){
                Location lastLoc = locationEngine.getLastLocation();
                setCameraPosition(lastLoc);
            }
        });


        activitySwitchButton = findViewById(R.id.activitySwitchButton);
        activitySwitchButton.setOnClickListener( new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            public void onClick(View v){
                Location lastLoc = locationEngine.getLastLocation();
                Intent intent = new Intent(MainActivity.this, PoiListActivity.class);
                intent.putExtra("USER_POSITION", lastLoc);
                startActivity(intent);

            }
        });


        commitPoiButton = findViewById(R.id.commitPoiButton);
        commitPoiButton.setOnClickListener( new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            public void onClick(View v){
                Location lastLoc = locationEngine.getLastLocation();
                Intent intent = new Intent (MainActivity.this,CommitPoiActivity.class);
                intent.putExtra("Lat", lastLoc.getLatitude());
                intent.putExtra("Long", lastLoc.getLongitude());
                startActivity(intent);
            }
        });



        try {
            loadPerson();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("foobar", "IO-Exception");
        }
    }

    private void addNavigation(Point originPoint, Point destinationPoint){
        //navigation.setLocationEngine(locationEngine);

        NavigationRoute.builder()
                .accessToken(Mapbox.getAccessToken())
                .origin(originPoint)
                .destination(destinationPoint)
                .profile(DirectionsCriteria.PROFILE_WALKING)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        Log.d("foobar","Response Code: " + response.code());
                        if(response.body() == null){
                            Log.d("foobar", "No Routes Found (maybe User/AT)");
                        } else if(response.body().routes().size() < 1) {
                            Log.d("foobar", "No routes found");
                            return;
                        }

                        currentRoute = response.body().routes().get(0);

                        if (navigationMapRoute != null){
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView,mapboxMap,R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        Log.d("foobar", "ERROR: " + t.getMessage());
                    }
                });

    }

    private void loadPerson() throws IOException {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://swapi.co")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        WebRepo service = retrofit.create(WebRepo.class);
        Call<Person> personCall = service.getPerson(1);
        personCall.enqueue(new Callback<Person>() {
            @Override
            public void onResponse(Call<Person> call, Response<Person> response) {
                if (response.isSuccessful()) {               //laut stackoverflow in retrofit 2.0.2 if(response.getResponseCode == 200) oder so....
                    Person person = response.body();
                    Log.d("foobar" , "loadPerson: " + response.body().getName());
                } else {
                    Log.d("foobar", "response not Successful..." + response.code());
                }
            }

            @Override
            public void onFailure(Call<Person> call, Throwable t) {
                Person person = null;
                Log.d("foobar","Enqueue failure =(");
            }
        });
    }

    @SuppressWarnings({"MissingPermission"})
    private void enableLocationPlugin() {
        //if Permissions are not enabled -> request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            initializeLocationEngine();

            locationPlugin = new LocationLayerPlugin(mapView, mapboxMap, locationEngine);
            locationPlugin.setLocationLayerEnabled(LocationLayerMode.COMPASS);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @SuppressWarnings({"MissingPermission"})
    private void initializeLocationEngine() {
        locationEngine = new LostLocationEngine(MainActivity.this);
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            setCameraPosition(lastLocation);
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }

    private void setCameraPosition(Location location) {
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()),
                16
        ));
    }
    private void setCameraPosition(Position position) {
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(position.getLatitude(),position.getLongitude()),
                16
        ));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    protected void onStart() {
        super.onStart();
        if (locationEngine != null){
            locationEngine.requestLocationUpdates();
        }
        if (locationPlugin != null){
            locationPlugin.onStart();
        }
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @SuppressWarnings("MissingPermission")
    @Override
    protected void onStop() {
        super.onStop();
        if(locationEngine != null){
            locationEngine.removeLocationUpdates();
        }
        if (locationPlugin != null){
            locationPlugin.onStop();
        }
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (locationEngine != null){
            locationEngine.deactivate();
        }
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null) {
            setCameraPosition(location);
            locationEngine.removeLocationEngineListener(this);
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationPlugin();
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
