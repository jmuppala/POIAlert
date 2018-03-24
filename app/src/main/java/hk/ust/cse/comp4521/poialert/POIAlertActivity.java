package hk.ust.cse.comp4521.poialert;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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


public class POIAlertActivity extends AppCompatActivity {

    private String TAG = "POIAlertActivity";

    // Spinner handle
    private Spinner poispinner;
    ArrayAdapter mAdapter = null;

    private String pointOfInterest = "";
    private double latitude = 0.0;
    private double longitude = 0.0;

    List<PlaceInfo> placeInfoArrayList;

    public class PlaceInfo {
        String poiName;
        double latitude;
        double longitude;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poialert);

        // Create an empty adapter we will use to display the loaded data.
        // We display the Song Title and the Artist's name in the List

        poispinner = (Spinner) findViewById(R.id.POIspinner);

        // Set the spinner to display all the pointOfInterest locations
        poispinner.setAdapter(mAdapter);
        poispinner.setOnItemSelectedListener(new MyOnItemSelectedListener());

        if (isOnline()) {
            new AsyncHttpTask().execute(Constants.SERVER_URL);
        }
        else {
            Toast.makeText(this,getString(R.string.notOnline),Toast.LENGTH_LONG).show();

            TextView tv = (TextView) findViewById(R.id.pointOfInterest);
            tv.setText(R.string.notOnline);
        }
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

            pointOfInterest = placeInfoArrayList.get((int) id).poiName;
            latitude = placeInfoArrayList.get((int) id).latitude;
            longitude = placeInfoArrayList.get((int) id).longitude;

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

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case R.id.action_add_poi:

                break;
            case R.id.action_update_poi:

                break;

            case R.id.action_delete_poi:

                break;
            case R.id.action_settings:
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isOnline() {

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        else {
            return false;
        }
    }

    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            InputStream inputStream = null;
            HttpURLConnection urlConnection = null;
            Integer result = 0;
            try {
                /* forming th java.net.URL object */
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                 /* optional request header */
                urlConnection.setRequestProperty("Content-Type", "application/json");

                /* optional request header */
                urlConnection.setRequestProperty("Accept", "application/json");

                /* for Get request */
                urlConnection.setRequestMethod("GET");
                int statusCode = urlConnection.getResponseCode();

                Log.i(TAG, "status code is " + statusCode);

                /* 200 represents HTTP OK */
                if (statusCode == 200) {
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    String response = convertInputStreamToString(inputStream);
                    parseResult(response);
                    result = 1; // Successful
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

                String [] allPlaceNames = new String[placeInfoArrayList.size()];
                for (int i = 0; i < placeInfoArrayList.size(); i++) {
                    allPlaceNames[i] = placeInfoArrayList.get(i).poiName;
                }
                mAdapter = new ArrayAdapter(POIAlertActivity.this, android.R.layout.simple_spinner_dropdown_item, allPlaceNames);
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

        placeInfoArrayList = new ArrayList<PlaceInfo>();

        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();

        placeInfoArrayList = Arrays.asList(gson.fromJson(result, PlaceInfo[].class));

    }
}