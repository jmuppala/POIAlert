package hk.ust.cse.comp4521.poialert;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import hk.ust.cse.comp4521.poialert.provider.POIContract;

import static hk.ust.cse.comp4521.poialert.provider.POIContract.POIEntry.*;

public class POIAlertActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>  {

    private String TAG = "POIAlertActivity";

    // Spinner handle
    private Spinner poispinner;
    SimpleCursorAdapter mAdapter;

    private String pointOfInterest = "";
    private double latitude = 0.0;
    private double longitude = 0.0;
    private long rowID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poialert);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

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
    }

    @Override
    protected void onPause() {

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