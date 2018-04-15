package com.example.intel.getmapcurrentlocation;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,GoogleMap.InfoWindowAdapter,GoogleMap.OnInfoWindowClickListener,
        LocationListener{

    private static final String TAG = "MapLocation";
    private static final int REQUEST_SOURCE_LOCATION = 459;

    public MapView mMapView;
    public GoogleMap mGoogleMap;
    LatLng mCenterLatLng;

    private Boolean mRequestingLocationUpdates;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;


    EditText etLoc;
    Button btnSubmit;
    TextView tvLocFirst,tvLocSec,tvDistance;
    ImageButton ibFirst,ibSec;

    boolean isGPSEnabled = false;
    boolean isGetLocationInfo = false;
    private boolean Storelocation = false;

    private double fusedLatitude = 0.0;
    private double fusedLongitude = 0.0;
    LatLng locFirst,locSec;

    MarkerOptions mp1;

    boolean isCurrentLocation = false;

    ProgressDialog mProgressDialog;
    AddressResultReceiver mResultReceiver;


    private final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    private final static String LOCATION_KEY = "location-key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitView();

        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        MapsInitializer.initialize(MainActivity.this);

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                setMap(googleMap);
            }
        });

        mResultReceiver = new AddressResultReceiver(null);
    }

    private void setMap(GoogleMap googleMap) {

        mGoogleMap = googleMap;

        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                String msg = latLng.latitude + ", " + latLng.longitude;
                Log.d(TAG, "Location Info:" + msg);
                //sharjah = new LatLngBounds(new LatLng(25.2, 55.51), new LatLng(24.6, 55.61));
                /*LatLngBounds ADELAIDE = new LatLngBounds(
                        new LatLng(-35.0, 138.58), new LatLng(-34.9, 138.61));
                mGoogleMap.setLatLngBoundsForCameraTarget(ADELAIDE);*/

            }
        });

        mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                Log.d(TAG, "Location Info 1:" + marker.getPosition());
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                Log.d(TAG, "Location Info 1:" + marker.getPosition());
                return null;
            }
        });

        mGoogleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {

                LatLng latLng = cameraPosition.target;
                if (mCenterLatLng != null) {
                    if (latLng.latitude == mCenterLatLng.latitude && latLng.longitude == mCenterLatLng.longitude) {
                        return;
                    }
                }

                mCenterLatLng = cameraPosition.target;

                Location mLocation = new Location("");
                mLocation.setLatitude(mCenterLatLng.latitude);
                mLocation.setLongitude(mCenterLatLng.longitude);

                fusedLatitude = mCenterLatLng.latitude;
                fusedLongitude = mCenterLatLng.longitude;

                Log.d(TAG, "Display LatLng:" + fusedLatitude + "-" + fusedLongitude);

            }
        });

        mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                Log.d(TAG, "Display LatLng:" + fusedLatitude + "-" + fusedLongitude);
                //Toast.makeText(MainActivity.this,etLoc.getText().toString().trim(),Toast.LENGTH_LONG).show();
                return null;
            }
        });

        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.d(TAG, "Marker Click:" + fusedLatitude + "-" + fusedLongitude);
                //Toast.makeText(MainActivity.this,etLoc.getText().toString().trim(),Toast.LENGTH_LONG).show();
                //showDialog(etLoc.getText().toString().trim());
                return false;
            }
        });

        mGoogleMap.setInfoWindowAdapter(this);
        mGoogleMap.setOnInfoWindowClickListener(this);


    }

    void InitView() {
        mMapView = (MapView) findViewById(R.id.maprec);

        etLoc= (EditText) findViewById(R.id.loc_addresss);
        btnSubmit= (Button) findViewById(R.id.loc_submitit);
        tvLocFirst= (TextView) findViewById(R.id.loca_first);
        tvLocSec= (TextView) findViewById(R.id.loc_secon);
        tvDistance= (TextView) findViewById(R.id.loc_distances);
        ibFirst= (ImageButton) findViewById(R.id.ibFirsts);
        ibSec= (ImageButton) findViewById(R.id.ibSece);

        mRequestingLocationUpdates = false;

        buildGoogleApiClient();
        startUpdatesButtonHandler();

    }


    public void startUpdatesButtonHandler() {

        if (!isPlayServicesAvailable(this))
            return;
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
        } else {
            return;
        }

        if (Build.VERSION.SDK_INT < 23) {
            //setButtonsEnabledState();
            //startLocationUpdates();
            checkLocationEnable();
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //setButtonsEnabledState();
            checkLocationEnable();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                showRationaleDialog();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {

        Log.i(TAG, "Building GoogleApiClient");

        mGoogleApiClient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //setButtonsEnabledState();
                    //startLocationUpdates();
                    checkLocationEnable();
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        mRequestingLocationUpdates = false;
                        Toast.makeText(MainActivity.this, "To enable the function of this application please enable location permission of the application from the setting screen of the terminal.", Toast.LENGTH_SHORT).show();
                    } else {
                        showRationaleDialog();
                    }
                }
                break;
            }
        }
    }

    private void showRationaleDialog() {
        new AlertDialog.Builder(this)
                .setPositiveButton("To give permission", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                    }
                })
                .setNegativeButton("do not do", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "Position information permission was not allowed.", Toast.LENGTH_SHORT).show();
                        mRequestingLocationUpdates = false;
                    }
                })
                .setCancelable(false)
                .setMessage("This application needs to allow use of location information.")
                .show();
    }

    void checkLocationEnable() {

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isGPSEnabled) {
            isGPSEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled) {
                showSettingsAlert(this);
                isGetLocationInfo = false;
            } else {
                startLocationUpdates();
            }
        } else {
            startLocationUpdates();
        }

    }

    /*
     * Get Address From LatLng
     * */

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, final Bundle resultData) {
            if (resultCode == Constants.SUCCESS_RESULT) {
                final Address address = resultData.getParcelable(Constants.RESULT_ADDRESS);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        /*tvLocFirst.setText("Latitude: " + address.getLatitude() + "\n" +
                                "Longitude: " + address.getLongitude() + "\n" +
                                "Address: " + resultData.getString(Constants.RESULT_DATA_KEY));*/
                        tvLocFirst.setText(resultData.getString(Constants.RESULT_DATA_KEY));

                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvLocFirst.setText(resultData.getString(Constants.RESULT_DATA_KEY));
                    }
                });
            }
        }
    }





    public void stopUpdatesButtonHandler() {
        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            //setButtonsEnabledState();
            stopLocationUpdates();
        }
    }

    protected void stopLocationUpdates() {
        Log.i(TAG, "stopLocationUpdates");
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        if (mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) MainActivity.this);
    }

    private void startLocationUpdates() {

        Log.i(TAG, "startLocationUpdates");

        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        } else {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) MainActivity.this);
            }
        }

    }

    public void showSettingsAlert(final Activity activity) {

        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(
                activity);
        alertDialog.setTitle("SETTINGS");
        alertDialog.setCancelable(false);
        alertDialog.setMessage("Enable Location Provider! Go to settings menu?");
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        activity.startActivity(intent);
                        dialog.dismiss();
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();

    }

    private void updateUI() {

        if (mCurrentLocation == null) return;

        fusedLatitude = mCurrentLocation.getLatitude();
        fusedLongitude = mCurrentLocation.getLongitude();

        Log.d(TAG, "fLat:" + fusedLatitude);
        Log.d(TAG, "fLng:" + fusedLongitude);

        locFirst=new LatLng(fusedLatitude,fusedLongitude);

        setMarker();
        if (locFirst != null) {
            getAddresssFromLatLng();
        }

        Storelocation = true;
        stopUpdatesButtonHandler();

    }

    void setMarker() {

        mGoogleMap.clear();

        mp1 = new MarkerOptions();
        mp1.position(locFirst);

        mp1.draggable(true);
        mp1.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_loc_first));
        mGoogleMap.addMarker(mp1);

        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locFirst, 15));

    }

    void getAddresssFromLatLng() {

        Intent intent = new Intent(this, GeocodeAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.FETCH_TYPE_EXTRA, Constants.USE_ADDRESS_LOCATION);

        if (locFirst.latitude == 0.0 || locFirst.longitude== 0.0) {
            Toast.makeText(this,
                    "Please enter both latitude and longitude",
                    Toast.LENGTH_LONG).show();
            return;
        }

        intent.putExtra(Constants.LOCATION_LATITUDE_DATA_EXTRA,locFirst.latitude);
        intent.putExtra(Constants.LOCATION_LONGITUDE_DATA_EXTRA,locFirst.longitude);

        Log.e(TAG, "Starting Service");
        startService(intent);
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged");
        mCurrentLocation = location;
        updateUI();
        //Toast.makeText(this, "Location Updated", Toast.LENGTH_SHORT).show();
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {


        Log.i(TAG, "onConnected");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            //updateUI();
        }

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
            checkLocationEnable();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    public static boolean isPlayServicesAvailable(Context context) {
        // Google Play Service APKが有効かどうかチェックする
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().getErrorDialog((Activity) context, resultCode, 2).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        isPlayServicesAvailable(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Storelocation = false;
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        stopLocationUpdates();
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }



}
