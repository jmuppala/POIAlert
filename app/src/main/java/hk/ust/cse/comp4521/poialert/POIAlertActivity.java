package hk.ust.cse.comp4521.poialert;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class POIAlertActivity extends AppCompatActivity{

    private String TAG = "POIAlertActivity";

    // Spinner handle
    private Spinner poispinner;
    ArrayAdapter mAdapter = null;

    private int selectedPOIindex;

    // POIInfo object stores information for each point
    public class PoiInfo {
        String id;
        String poiName;
        double latitude;
        double longitude;
    }
    // List of POIs
    List<PoiInfo> poiInfoArrayList;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poialert);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Adding a new POI", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Intent intent = new Intent(getApplication(),AddPOI.class);

                // add a new POI. So indicate to addPOI.
                intent.putExtra("Add",true);
                startActivity(intent);

            }
        });

        // Create an empty adapter we will use to display the loaded data.
        // We display the Song Title and the Artist's name in the List

        poispinner = (Spinner) findViewById(R.id.POIspinner);

        // Set the spinner to display all the pointOfInterest locations
        poispinner.setAdapter(mAdapter);
        poispinner.setOnItemSelectedListener(new MyOnItemSelectedListener());

        // fetch all the POIs from the server. Use GET /places
        new POIsAsyncHttpTask().execute();
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

            // remember the poi selected
            selectedPOIindex = (int) id;

            // fetch the detailed info about the selected POI. Use GET /places/<id>
            new SelectedPoiAsyncHttpTask().execute(poiInfoArrayList.get((int) id).id, "Get");

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }

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

        switch (item.getItemId()) {
            case R.id.action_add_poi:

                // add a new POI. So indicate to addPOI.
                intent.putExtra("Add",true);
                startActivity(intent);

                // fetch all the POIs from the server. Use GET /places
                new POIsAsyncHttpTask().execute();
                break;
            case R.id.action_update_poi:

                // user is requesting to update the selected POI. send the info of the poi
                // to addPOI
                intent.putExtra("Add",false);
                intent.putExtra("_id",poiInfoArrayList.get((int) selectedPOIindex).id);
                intent.putExtra("POI Name", poiInfoArrayList.get((int) selectedPOIindex).poiName);
                intent.putExtra("Latitude", poiInfoArrayList.get((int) selectedPOIindex).latitude);
                intent.putExtra("Longitude", poiInfoArrayList.get((int) selectedPOIindex).longitude);
                startActivity(intent);

                // fetch all the POIs from the server. Use GET /places
                new POIsAsyncHttpTask().execute();
                break;

            case R.id.action_delete_poi:
                // delete the selected POI
                new SelectedPoiAsyncHttpTask().execute(poiInfoArrayList.get((int) selectedPOIindex).id, "Delete");

                // fetch all the POIs from the server. Use GET /places
                new POIsAsyncHttpTask().execute();
                break;
            case R.id.action_settings:
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // fetches all the POIs from server. Uses GET /places
    public class POIsAsyncHttpTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            InputStream inputStream = null;
            HttpURLConnection urlConnection = null;
            Integer result = 0;
            try {
                /* forming th java.net.URL object */
                URL url = new URL(Constants.SERVER_URL);
                urlConnection = (HttpURLConnection) url.openConnection();

                 /* optional request header. Asking for info in json format */
                urlConnection.setRequestProperty("Content-Type", "application/json");

                /* optional request header */
                urlConnection.setRequestProperty("Accept", "application/json");

                /* for Get request */
                urlConnection.setRequestMethod("GET");
                int statusCode = urlConnection.getResponseCode();
                Log.i(TAG, "statusCode is "+statusCode);

                /* 200 represents HTTP OK */
                if (statusCode == 200) {
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    String response = convertInputStreamToString(inputStream);
                    parseResult(response);
                    result = 1; // Successful
                    return result;
                } else {
                    result = 0; //"Failed to fetch data!";
                }
            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
            return result; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Integer result) {
            /* Download complete. Lets update UI */
            if(result == 1){

                String [] allPOINames = new String[poiInfoArrayList.size()];
                for (int i = 0; i < poiInfoArrayList.size(); i++) {
                    allPOINames[i] = poiInfoArrayList.get(i).poiName;
                }
                mAdapter = new ArrayAdapter(POIAlertActivity.this, android.R.layout.simple_spinner_dropdown_item, allPOINames);
                poispinner.setAdapter(mAdapter);
            }else{
                Log.e(TAG, "Failed to fetch data!");
            }
        }
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null){
            result += line;
        }

            /* Close Stream */
        if(null!=inputStream){
            inputStream.close();
        }
        return result;
    }

    private void parseResult(String result) {

        poiInfoArrayList = new ArrayList<PoiInfo>();

        Log.i(TAG, "Result from server is " + result);

        // We are using the GSON library for converting JSON
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();

        // converts the incoming JSON string to an array list of PoiInfo objects
        poiInfoArrayList = Arrays.asList(gson.fromJson(result, PoiInfo[].class));

    }

    // Used for fetching a selected POI info using GET or deleting the selected POI using DELETE
    // the Parameter for AsyncTask indicates which action to perform
    private class SelectedPoiAsyncHttpTask extends AsyncTask<String, Void, Integer> {

        private String response = null;

        private boolean deletePlace = false;

        @Override
        protected Integer doInBackground(String... params) {
            InputStream inputStream = null;
            HttpURLConnection urlConnection = null;
            Integer result = 0;
            try {
                /* forming th java.net.URL object */
                // Append the ID of the selected POI to URL to get /places/<id>
                URL url = new URL(Constants.SERVER_URL+"/"+params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                 /* optional request header */
                urlConnection.setRequestProperty("Content-Type", "application/json");

                /* optional request header */
                urlConnection.setRequestProperty("Accept", "application/json");

                /* for Delete or Get request */
                deletePlace = "Delete".equals(params[1]);
                if (deletePlace)
                    urlConnection.setRequestMethod("DELETE");
                else
                    urlConnection.setRequestMethod("GET");

                int statusCode = urlConnection.getResponseCode();
                Log.i(TAG, "statusCode is "+statusCode);

                /* 200 represents HTTP OK */
                if (statusCode == 200) {
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    response = convertInputStreamToString(inputStream);
                    result = 1;
                } else {
                    result = 0; //"Failed to fetch data!";
                }
            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
            return result; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Integer result) {
            /* Download complete. Lets update UI */
            if(result == 1 && response != null && !deletePlace){

                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();

                PoiInfo poiInfo;
                poiInfo = gson.fromJson(response, PoiInfo.class);

                if (poiInfo != null) {
                    poiInfoArrayList.get(selectedPOIindex).latitude = poiInfo.latitude;
                    poiInfoArrayList.get(selectedPOIindex).longitude = poiInfo.longitude;
                }

                String message = String.format(
                        "%1$s\n Longitude: %2$s \n Latitude: %3$s",
                        poiInfoArrayList.get(selectedPOIindex).poiName,
                        poiInfoArrayList.get((int) selectedPOIindex).longitude,
                        poiInfoArrayList.get((int) selectedPOIindex).latitude
                );

                TextView tv = (TextView) findViewById(R.id.pointOfInterest);

                // Updates the textview on the main screen to show the selected pointOfInterest
                tv.setText(message);

            }else{
                Log.e(TAG, "Failed to fetch data!");
            }
        }
    }

}