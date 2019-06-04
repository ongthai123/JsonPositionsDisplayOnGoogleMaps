package com.example.jsonpositionsdisplayongooglemaps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double latitude = -34;
    private double longitude = 151;
    private static final int REQUEST_LOCATION = 99;
    private RequestQueue requestQueue;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);

        requestQueue = Volley.newRequestQueue(this);
        textView = findViewById(R.id.text_view_result);

        final Button btnGetLocation = findViewById(R.id.btnCurrentLocation);

        final FusedLocationProviderClient locationClient = LocationServices.
                getFusedLocationProviderClient(this);
        btnGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Task<Location> location = locationClient.getLastLocation();
                    location.addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            latitude = task.getResult().getLatitude();
                            longitude = task.getResult().getLongitude();

                            Toast.makeText(getApplicationContext(), task.getResult().getLatitude() + " " + task.getResult().getLongitude(), Toast.LENGTH_SHORT).show();

                            initMap();

                        }
                    });
                } catch (SecurityException ex) {
                    ex.printStackTrace();
                }
            }
        });

        LocationRequest req = new LocationRequest();
//        req.setInterval(2000); // 2 seconds
//        req.setFastestInterval(500); // 500 milliseconds
//        req.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user asynchronously -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted

            locationClient.requestLocationUpdates(req, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

                    latitude = locationResult.getLastLocation().getLatitude();
                    longitude = locationResult.getLastLocation().getLongitude();
//                    Log.e("location:",locationResult.getLastLocation().toString());
                    Toast.makeText(getApplicationContext(), locationResult.getLastLocation().getLatitude() + " " + locationResult.getLastLocation().getLongitude(), Toast.LENGTH_SHORT).show();
//                    Toast.makeText(this, locationResult.getLastLocation().toString(), Toast.LENGTH_LONG).show();
                    initMap();
                }
            }, null);

        }
    }

    private void initMap() {
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        supportMapFragment.getMapAsync(MapsActivity.this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        String url = "https://api.myjson.com/bins/vnobj";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//                        StringBuilder stringBuilder = new StringBuilder();

                        try {
                            JSONArray jsonArray = response.getJSONArray("locations");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject student = jsonArray.getJSONObject(i);

                                String name = student.getString("name");
                                double jsonLatitude = student.getDouble("latitude");
                                double jsonLongitude = student.getDouble("longitude");

//                                textView.append(name + " " + String.valueOf(jsonLatitude) + " " + String.valueOf(jsonLongitude) + "\n\n");
                                // Add a marker in Sydney and move the camera
                                LatLng currentPosition = new LatLng(jsonLatitude, jsonLongitude);
                                mMap.addMarker(new MarkerOptions().position(currentPosition).title(name));
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPosition));

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        requestQueue.add(request);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(5.0f));
    }
}