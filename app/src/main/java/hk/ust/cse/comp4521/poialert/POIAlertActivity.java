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

import hk.ust.cse.comp4521.poialert.rest.RestClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        getAllPOIs();

    }

    protected void getAllPOIs() {

        // use the RestClient to do GET /pois and return list of POIs
        // When data is fetched the callback function is called.
        RestClient.get().getPois().enqueue(new Callback<ArrayList<PoiInfo>>() {
            @Override
            public void onResponse(Call<ArrayList<PoiInfo>> call, Response<ArrayList<PoiInfo>> response) {
                // The return value is an array list which is assigned to the array list of POIs
                poiInfoArrayList = response.body();

                if (poiInfoArrayList.size() > 0) {

                    String[] allPOINames = new String[poiInfoArrayList.size()];
                    for (int i = 0; i < poiInfoArrayList.size(); i++) {
                        allPOINames[i] = poiInfoArrayList.get(i).poiName;
                    }
                    mAdapter = new ArrayAdapter(POIAlertActivity.this, android.R.layout.simple_spinner_dropdown_item, allPOINames);
                    poispinner.setAdapter(mAdapter);
                } else {
                    Log.w(TAG, "Failed to fetch data!");
                }
            }

            @Override
            public void onFailure(Call<ArrayList<PoiInfo>> call, Throwable t) {
                Log.e(TAG, "Retrofit Error" + t.toString());
            }

        });
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

            // We are calling the REST client to do GET /pois/<id>
            RestClient.get().getPoi(poiInfoArrayList.get((int) id).id).enqueue(new Callback<PoiInfo>() {
                @Override
                public void onResponse(Call<PoiInfo> call, Response<PoiInfo> response) {

                    PoiInfo poiInfo = response.body();
                    // call back returns an poiInfo object.
                    if (poiInfo != null) {
                        // save the information to the specific object for that POI
                        poiInfoArrayList.get(selectedPOIindex).latitude = poiInfo.latitude;
                        poiInfoArrayList.get(selectedPOIindex).longitude = poiInfo.longitude;
                    }

                    String message = String.format(
                            "%1$s\n Longitude: %2$s \n Latitude: %3$s",
                            poiInfoArrayList.get((int) selectedPOIindex).poiName,
                            poiInfoArrayList.get((int) selectedPOIindex).longitude,
                            poiInfoArrayList.get((int) selectedPOIindex).latitude
                    );

                    TextView tv = (TextView) findViewById(R.id.pointOfInterest);

                    // Updates the textview on the main screen to show the selected pointOfInterest
                    tv.setText(message);
                }

                @Override
                public void onFailure(Call<PoiInfo> call, Throwable t) {
                    Log.e(TAG, "Retrofit Error" + t.toString());
                }
            });

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
                getAllPOIs();
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
                getAllPOIs();
                break;

            case R.id.action_delete_poi:
                // delete the selected POI
                // Call the Rest Client to execute DELETE /pois/<id>
                RestClient.get().deletePoi(poiInfoArrayList.get((int) selectedPOIindex).id).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Log.i(TAG, "Response " + response.body().toString());
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(TAG, "Retrofit Error" + t.toString());
                    }
                });
                getAllPOIs();
                break;
            case R.id.action_settings:
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}