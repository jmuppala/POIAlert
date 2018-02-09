package hk.ust.cse.comp4521.poialert;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DateFormat;
import java.util.Date;

import hk.ust.cse.comp4521.poialert.provider.POIContract;

import static hk.ust.cse.comp4521.poialert.provider.POIContract.POIEntry.*;

public class POIAlertActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private String TAG = "POIAlertActivity";

    // Spinner handle
    private Spinner poispinner;
    SimpleCursorAdapter mAdapter;

    private String pointOfInterest = "";
    private double latitude = 0.0;
    private double longitude = 0.0;
    private long rowID = 0;

    /**
     * Provides the entry point to Google Play services.
     */
    protected FusedLocationProviderClient mFusedLocationClient;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation, mCurrentLocation;


    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates = false;

    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;

    private LocationCallback mLocationCallback;

    TextView poiView;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poialert);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        poiView = (TextView) findViewById(R.id.pointOfInterest);

        // Create an empty adapter we will use to display the loaded data.
        // We display the Song Title and the Artist's name in the List

        mAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_spinner_item, null,
                new String[]{COLUMN_POI},
                new int[]{android.R.id.text1}, 0);

        poispinner = (Spinner) findViewById(R.id.POIspinner);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the spinner to display all the pointOfInterest locations
        poispinner.setAdapter(mAdapter);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, (LoaderCallbacks<Cursor>) this);

        // set the on selected listener for the spinner
        poispinner.setOnItemSelectedListener(new MyOnItemSelectedListener());

        // Build the Google API Fused Location Services client so that connections can be established
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            Log.i(TAG, "No Permission Granted!");
            return;
        }

        Log.i(TAG, "Location can be determined!");

        createLocationRequest();

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            mLastLocation = location;
                            mCurrentLocation = mLastLocation;
                            String message = "Last Location is: " +
                                    "  Latitude = " + String.valueOf(mLastLocation.getLatitude()) +
                                    "  Longitude = " + String.valueOf(mLastLocation.getLongitude());
                            Log.i(TAG, message);
                            Snackbar.make(poiView, message, Snackbar.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "No Last Location Detected!");
                        Snackbar.make(poiView, R.string.no_location_detected, Snackbar.LENGTH_LONG).show();
                    }
                });

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    mCurrentLocation = location;
                    mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                    String message = "Current Location is: " +
                            "  Latitude = " + String.valueOf(mCurrentLocation.getLatitude()) +
                            "  Longitude = " + String.valueOf(mCurrentLocation.getLongitude() +
                            "\nLast Updated = " + mLastUpdateTime);
                    Log.i(TAG, message);
                    Snackbar.make(poiView, message, Snackbar.LENGTH_LONG).show();
                };
            };
        };

    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    protected void onStart() {

        super.onStart();
    }

    @Override
    protected void onRestart() {

        super.onRestart();
    }

    @Override
    protected void onResume() {

        super.onResume();
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {

        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            stopLocationUpdates();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    public class MyOnItemSelectedListener implements OnItemSelectedListener {

        // This listener is responsible to deal with the user's selection from the spinner

        @Override
        public void onItemSelected(AdapterView<?> parent,
                                   View view, int pos, long id) {

            // These is the projection on the POI content provider rows that we will retrieve.
            final String[] POIDb_POI_PROJECTION = new String[] {
                    _ID, // unique id to identify the row
                    COLUMN_POI, // pointOfInterest name
                    COLUMN_LATITUDE, // latitude
                    COLUMN_LONGITUDE, // longitude
            };

            int column_index;

            Uri baseUri = ContentUris.withAppendedId(POIContract.POIProvider.CONTENT_URI, id);

            String select = "((" + COLUMN_POI + " NOTNULL) AND ("
                    + COLUMN_POI + " != '' ))";

            // Get the cursor to the database row corresponding to the selected pointOfInterest
            Cursor poiCursor = getContentResolver().query(baseUri,
                    POIDb_POI_PROJECTION, select, null, null);

            // Get the pointOfInterest's name, latitude and longitude
            poiCursor.moveToFirst();
            pointOfInterest = poiCursor.getString(poiCursor.getColumnIndexOrThrow(COLUMN_POI));
            latitude = poiCursor.getDouble(poiCursor.getColumnIndexOrThrow(COLUMN_LATITUDE));
            longitude = poiCursor.getDouble(poiCursor.getColumnIndexOrThrow(COLUMN_LONGITUDE));
            rowID = id;

            poiCursor.close();

            String message = String.format(
                    "%1$s\n Longitude: %2$s \n Latitude: %3$s",
                    pointOfInterest, longitude, latitude
            );

            TextView tv = (TextView) findViewById(R.id.pointOfInterest);

            // Updates the textview on the main screen to show the selected pointOfInterest
            tv.setText(message);

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }

    }

    // These is the projection on the POI content provider rows that we will retrieve.
    static final String[] POIDb_PROJECTION = new String[] {
            _ID, // unique id to identify the row
            COLUMN_POI, // file handle
    };

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.

        // This is the URI of the Android Media Store that enable's
        // access to all the music files on the device

        Uri baseUri = POIContract.POIProvider.CONTENT_URI;

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        String select = "((" + COLUMN_POI + " NOTNULL) AND ("
                + COLUMN_POI + " != '' ))";

        CursorLoader curloader = new CursorLoader(this, baseUri,
                POIDb_PROJECTION, select, null,
                null);

        return curloader;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)

        mAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_poialert, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        Intent intent = new Intent(this,AddPOI.class);

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case R.id.action_add_poi:
                intent.putExtra("Add",true);
                startActivity(intent);

                break;
            case R.id.action_update_poi:
                intent.putExtra("Add",false);
                intent.putExtra("POI Name", pointOfInterest);
                intent.putExtra("Latitude", latitude);
                intent.putExtra("Longitude", longitude);
                intent.putExtra("RowID", rowID);
                startActivity(intent);

                break;

            case R.id.action_delete_poi:

                Uri baseUri = ContentUris.withAppendedId(POIContract.POIProvider.CONTENT_URI, rowID);

                String select = "((" + COLUMN_POI + " NOTNULL) AND ("
                        + COLUMN_POI + " != '' ))";

                int retval = getContentResolver().delete(baseUri, select, null);
                Log.i(TAG, "Updated " + retval + " rows");

                break;
            case R.id.action_settings:
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}