package com.example.boltparty;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private final int PERMISSION_REQUEST_CODE = 1000;
    private BottomSheetBehavior bottomSheetBehavior;
    private TextView txtTitle, txtDistance, txtAddress, txtRestaurantNumber, txtOpeningDateAndTime, txtRatingBar;
    private RatingBar ratingBar;
    private Button btnBooking;
    private View action_bar_view;
    private ShapeableImageView profileImage;
    private TextView txtUserName;
    private Users users;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private Location currentLocation;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LatLng owlCafe, digginCafe;
    private ArrayList<LatLng> arrayList;
    private ArrayList<String> markerTitle, address;
    private ArrayList<Location> destinationLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getCustomActionBar();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        profileImage = action_bar_view.findViewById(R.id.profile_img);
        txtUserName = action_bar_view.findViewById(R.id.txtUsername);
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users = snapshot.getValue(Users.class);
                txtUserName.setText(users.getName());
                if (users.getImageURL().equals("default")) {
                    profileImage.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(MapsActivity.this).load(users.getImageURL()).into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MapsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        initComponent();
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                Location location = locationList.get(locationList.size() - 1);
                Log.d("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                currentLocation = location;
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        int statusInfo = NetworkInfo.getNetworkStatus(this);
        if (statusInfo == NetworkInfo.NOT_CONNECTED) {
            startActivity(new Intent(this, NoNetworkActivity.class));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return;
        }

        super.onBackPressed();
    }

    private void checkGPS() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location requests here.
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvableApiException = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
                                resolvableApiException.startResolutionForResult(MapsActivity.this, LocationRequest.PRIORITY_HIGH_ACCURACY);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the settings so we won't show the dialog.
                            break;
                    }
                }
            }
        });
    }

    private void askPermissionForLocation() {

        locationRequest = new LocationRequest();
        locationRequest.setInterval(120000); // Two minutes interval
        locationRequest.setFastestInterval(120000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d("LOG", "Permission is granted");
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                mMap.setMyLocationEnabled(true);
            } else {
                Log.d("LOG", "Permission is not granted");
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Location Permission Needed")
                            .setMessage("This app needs the Location permission, please accept to use location functionality")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
                                }
                            })
                            .create()
                            .show();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
                }
            }
        } else {
            //Permission is automatically granted on SDK < 23 upon Installation.
            Log.d("LOG", "Permission is granted");
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            mMap.setMyLocationEnabled(true);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case LocationRequest.PRIORITY_HIGH_ACCURACY:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        Log.d("MapsActivity", "onActivityResult: GPS Enabled by user");
                        askPermissionForLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Log.d("MapsActivity", "onActivityResult: User rejected GPS request");
                        this.onBackPressed();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("LOG", "Permission is granted");
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    mMap.setMyLocationEnabled(true);
                }
            } else {
                //Permission NOT granted
                Log.d("LOG", "Permission is not granted");
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    //This block here means Permanently Denied Permission
                    new AlertDialog.Builder(this)
                            .setMessage("You have permanently denied this permission, go to settings to enable this permission")
                            .setCancelable(false)
                            .setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    gotoApplicationSettings();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .create()
                            .show();
                } else {
                    Toast.makeText(this, "You have denied the permission", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void gotoApplicationSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

//    private void getLastLocation() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
//        }
//        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
//
//        locationTask.addOnSuccessListener(location -> {
//            if (location != null) {
//               currentLocation = location;
//            }
//        }).addOnFailureListener(e -> {
//            Log.d("onFailure", e.getLocalizedMessage());
//        });
//    }

    private void initComponent() {
        // get the bottom sheet view
        View bottomSheet = findViewById(R.id.bottom_sheet);

        //init the View item
        txtTitle = bottomSheet.findViewById(R.id.txtTitle);
        txtDistance = bottomSheet.findViewById(R.id.txtDistance);
        txtAddress = bottomSheet.findViewById(R.id.txtAddress);
        txtRestaurantNumber = bottomSheet.findViewById(R.id.txtRestaurantNumber);
        txtOpeningDateAndTime = bottomSheet.findViewById(R.id.txtOpeningDateAndTime);
        txtRatingBar = bottomSheet.findViewById(R.id.txtRatingBar);
        ratingBar = bottomSheet.findViewById(R.id.ratingBar);
        btnBooking = bottomSheet.findViewById(R.id.btnBooking);

        // init the bottom sheet behavior
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // change the state of the bottom sheet
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);


        // set callback for changes
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        bottomSheet.setVisibility(View.GONE);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        bottomSheet.setVisibility(View.VISIBLE);
                        break;
                }
            }


            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    private void getCustomActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        action_bar_view = layoutInflater.inflate(R.layout.custom_actionbar, null);
        actionBar.setCustomView(action_bar_view);
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
        checkGPS();
        askPermissionForLocation();
        getLatLng();
        getMarkerTitle();
        getAddress();
        getDestination();

        // Add a marker in given LatLng and move the camera
        Marker[] markers = new Marker[arrayList.size()];
        for (int i = 0; i < arrayList.size(); i++) {
            markers[i] = mMap.addMarker(new MarkerOptions()
                    .position(arrayList.get(i))
                    .title(String.valueOf(markerTitle.get(i)))
                    .icon(bitmapDescriptorFromVector(this, R.drawable.ic_restaurant)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(arrayList.get(i)));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(10f));
        }

        mMap.setOnMarkerClickListener(marker -> {
            Toast.makeText(MapsActivity.this, "Click on Title for booking", Toast.LENGTH_LONG).show();
            return false;
        });

        mMap.setOnInfoWindowClickListener(marker -> {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }

            if (marker.equals(markers[0])) {
                txtTitle.setText(markerTitle.get(0));
                ratingBar.setRating(4.5f);
                txtRatingBar.setText(" 4.7 (51)");
                txtDistance.setText(String.valueOf(Math.floor((currentLocation.distanceTo(destinationLocation.get(0))) / 1000)) + " KM away");
                txtAddress.setText(address.get(0));
                txtRestaurantNumber.setText("+91 - 8847564898");
                onButtonClickAfterMarkerClick(markerTitle.get(0), 4.5f, " 4.7 (51)", String.valueOf(Math.floor((currentLocation.distanceTo(destinationLocation.get(0))) / 1000)), address.get(0), "+91 - 8847564898");
            } else if (marker.equals(markers[1])) {
                txtTitle.setText(markerTitle.get(1));
                ratingBar.setRating(4f);
                txtRatingBar.setText(" 4 (45)");
                txtDistance.setText(String.valueOf(Math.floor((currentLocation.distanceTo(destinationLocation.get(1))) / 1000)) + " KM away");
                txtAddress.setText(address.get(1));
                txtRestaurantNumber.setText("+91 - 9942584898");
                onButtonClickAfterMarkerClick(markerTitle.get(1), 4f, " 4 (45)", String.valueOf(Math.floor((currentLocation.distanceTo(destinationLocation.get(1))) / 1000)), address.get(1), "+91 - 9942584898");
            }
        });
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

//    private String getDistance(double lat1, double lon1, double lat2, double lon2) {
//        lat1 = Math.toRadians(lat1);
//        lon1 = Math.toRadians(lon1);
//        lat2 = Math.toRadians(lat2);
//        lon2 = Math.toRadians(lon2);
//
//        double dlat =  lat1 - lat2;
//        double dlon = lon1 - lon2;
//
//        double a = Math.pow(Math.sin(dlat/2), 2)
//                + Math.cos(lat1) * Math.cos(lat2)
//                * Math.pow(Math.sin(dlon/2), 2);
//
//        double c = 2 * Math.asin(Math.sqrt(a));
//
//        double r = 6371;
//
//        double d = r * c;
//
//        return String.valueOf(d);
//    }

    private void getLatLng() {
        setLatLng();
        arrayList = new ArrayList<LatLng>();
        arrayList.add(owlCafe);
        arrayList.add(digginCafe);
    }

    private void setLatLng() {

        owlCafe = new LatLng(28.764050, 77.188148);
        digginCafe = new LatLng(28.59536780916339, 77.19815234618756);
    }

    private void getMarkerTitle() {
        markerTitle = new ArrayList<String>();
        markerTitle.add("The Owl Cafe");
        markerTitle.add("Diggin Cafe");
    }

    private void getAddress() {
        address = new ArrayList<String>();
        address.add("Aurobindo Marg, Hauz Khas, New Delhi, Delhi - 110016");
        address.add("Santushti Shopping Complex, Shop No - 10, Opp Samrat Hotel, New Delhi, Delhi - 110003");
    }

    private void getDestination() {
        Location owlCafeLocation = new Location(LocationManager.GPS_PROVIDER);
        owlCafeLocation.setLatitude(28.764050);
        owlCafeLocation.setLongitude(77.188148);

        Location digginCafeLocation = new Location(LocationManager.GPS_PROVIDER);
        digginCafeLocation.setLatitude(28.59536780916339);
        digginCafeLocation.setLongitude(77.19815234618756);

        destinationLocation = new ArrayList<Location>();
        destinationLocation.add(owlCafeLocation);
        destinationLocation.add(digginCafeLocation);
    }

    private void onButtonClickAfterMarkerClick(String title, float rating, String ratingText, String distance, String address, String number) {
        btnBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent detailsIntent = new Intent(MapsActivity.this, DetailsActivity.class);
                detailsIntent.putExtra("CafeName", title);
                detailsIntent.putExtra("Rating", rating);

                detailsIntent.putExtra("RatingText", ratingText);
                detailsIntent.putExtra("Distance", distance);
                detailsIntent.putExtra("Address", address);
                detailsIntent.putExtra("Number", number);
                startActivity(detailsIntent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                    startActivity(intent);
                    finishAffinity();
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }
}