package se.miun.joli1407.bathingsites;

import android.Manifest;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.LinkedList;
import java.util.List;

/**
 * Activity that displays bathing sites on map based on the search radius setting.
 */
public class BathingSitesMapActivity extends FragmentActivity implements OnMapReadyCallback, OnRequestPermissionsResultCallback, OnDBAccessCompleted, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient; //Used to register/unregister for location updates
    private final int FINE_LOCATION_PERMISSION_MAP_READY = 1;
    private final int FINE_LOCATION_PERMISSION_LOCATION_UPDATES = 1;
    private LocationRequest mLocationRequest;   //Location request settings
    private LocationCallback mLocationCallback; //On location update callback
    private Location mCurrentLocation = null;   //Most recent location

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bathing_sites_map);

        //Location related setup
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //On location update logic
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null)
                    return;

                //Store current location, move camera and fetch bathing sites from database
                //TODO: Just fetch bathing sites on creation and resume to help performance (constant memory use should be more reliable anyways)
                mCurrentLocation = locationResult.getLastLocation();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 10f));
                BathingSiteRepository.getInstance().getAllBathingSites(BathingSitesMapActivity.this);
            }
        };

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    //Unregister from unnecessary location updates on pause
    @Override
    protected void onPause() {
        super.onPause();
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    //Re-register for location updates on resume
    @Override
    protected void onResume() {
        super.onResume();
        requestLocationUpdates();
    }

    private void requestLocationUpdates(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
        else{
            //Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    FINE_LOCATION_PERMISSION_LOCATION_UPDATES);
        }
    }

    /**
     * When the map is ready.
     * Registers 'this' as marker click listener.
     * Enables my location and makes a first manual request for the current location
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        onMapReadyWithPermission();
    }

    private void onMapReadyWithPermission() {
        //Check permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    //Location could be null
                    if (location != null) {
                        //Move the camera to location and fetch bathing sites
                        mCurrentLocation = location;
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())));
                        BathingSiteRepository.getInstance().getAllBathingSites(BathingSitesMapActivity.this);
                    }
                }
            });
        } else {
            //Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    FINE_LOCATION_PERMISSION_MAP_READY);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == FINE_LOCATION_PERMISSION_MAP_READY){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                onMapReadyWithPermission();
            }
            else{
                Toast toast = Toast.makeText(this, R.string.bathingsitesmap_permission_location_denied, Toast.LENGTH_LONG);
                toast.show();
            }
        }

        if(requestCode == FINE_LOCATION_PERMISSION_LOCATION_UPDATES){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                requestLocationUpdates();
            }
            else{
                Toast toast = Toast.makeText(this, R.string.bathingsitesmap_permission_location_denied, Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    @Override
    public void onDBAccessCompleted(Long result) {

    }

    @Override
    public void onDBAccessCompleted(Long[] result) {

    }

    /**
     * Callback method for clicking on a bathing site marker.
     * Displays a dialog with the bathing site information.
     * @param bathingSite Bathing site represented by the clicked marker
     */
    @Override
    public void onDBAccessCompleted(BathingSite bathingSite) {
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle(R.string.bathingsitesmap_marker_click_title);

        StringBuilder sb = new StringBuilder("");
        sb.append(bathingSite.getName());

        if(bathingSite.getDescription() != null){
            sb.append('\n');
            sb.append(bathingSite.getDescription());
        }

        if(bathingSite.getAddress() != null){
            sb.append('\n');
            sb.append(bathingSite.getAddress());
        }

        if(bathingSite.getLatitude() != null && bathingSite.getLongitude() != null){
            sb.append('\n');
            sb.append(getString(R.string.all_coordinates));
            sb.append(bathingSite.getLatitude());
            sb.append(" ");
            sb.append(bathingSite.getLongitude());
        }

        sb.append('\n');
        sb.append(getString(R.string.all_rating));
        sb.append(bathingSite.getRating());

        if(bathingSite.getTemp() != null){
            sb.append('\n');
            sb.append(getString(R.string.all_temperature));
            sb.append(bathingSite.getTemp());
        }

        if(bathingSite.getDate() != null){
            sb.append('\n');
            sb.append(getString(R.string.all_date));
            sb.append(bathingSite.getDate());
        }

        ad.setMessage(sb.toString());
        ad.setPositiveButton(R.string.all_ok, null);
        ad.show();
    }

    /**
     * Callback method for displaying all relevant bathing sites on map.
     * Executes AsyncTask that displays markers (clears map first) for the bathing sites within the search radius setting.
     * @param bathingSites List of bathing sites
     */
    @Override
    public void onDBAccessCompleted(List<BathingSite> bathingSites) {
        mMap.clear();

       MapDrawer md = new MapDrawer();
       md.execute(bathingSites);
    }

    /**
     * Fetch bathing site represented by the marker, callback for further logic
     * @param marker Marker that was clicked
     * @return True if event was consumed
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.getTag() != null) {
            BathingSiteRepository.getInstance().getBathingSite((Long) marker.getTag(), this);
            return true;
        }

        return false;
    }

    //Draws markers for bathing sites
    private class MapDrawer extends AsyncTask<List<BathingSite>, Void, List<BathingSite>> {

        @Override
        protected List<BathingSite> doInBackground(List<BathingSite>... bathingSites) {
            List<BathingSite> qualifiedSites = new LinkedList<>();  //For holding the bathing sites that fulfills the search distance requirement
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            double searchDistance = Double.parseDouble(sharedPreferences.getString("mapDistance", "0"));    //Bathing site search distance

            //Loop all sites
            for(BathingSite site : bathingSites[0]){
                if(site.getLatitude() == null || site.getLongitude() == null)   //Ignore sites that are missing coordinates //TODO: Could attempt fetching rough coordinates when site is created based on address
                    continue;

                //Calc distance between current location and the bathing site
                float[] distance = new float[2];
                Location.distanceBetween(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), site.getLatitude(), site.getLongitude(), distance);

                if(distance[0] < searchDistance*1000)
                    qualifiedSites.add(site);
            }

            return qualifiedSites;
        }

        @Override
        protected void onPostExecute(List<BathingSite> bathingSites) {
            //Add marker for each bathing site, also set the tag to the id of the site so we can fetch it from the db again (on click)
            MarkerOptions options = new MarkerOptions();
            for(BathingSite site : bathingSites){
                mMap.addMarker(options.position(new LatLng(site.getLatitude(), site.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_beach_umbrella))).setTag(site.getId());
            }
        }
    }
}