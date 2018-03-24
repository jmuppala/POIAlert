package hk.ust.cse.comp4521.poialert;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class AddPOI extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "AddPOI";

    private EditText poiName, poiLatitude, poiLongitude;
    private boolean add = true;
    private Button btnSubmit;

    // Class to store info about selected POI
    public class PoiInfo {
        // if you declare a field as transient, GSON will not include it
        // when converting the Java object to Json string
        transient String _id;
        String poiName;
        double latitude;
        double longitude;

        PoiInfo() {
            _id = "";
            poiName = "";
            latitude = 0.0;
            longitude = 0.0;
        }
    }

    PoiInfo poiInfo = new PoiInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_poi);

        poiName = (EditText) findViewById(R.id.poiName);
        poiLatitude = (EditText) findViewById(R.id.poiLatitude);
        poiLongitude = (EditText) findViewById(R.id.poiLongitude);

        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(this);

        Intent in = getIntent();
        // Are we adding a new POI (true) or updating POI (false)
        add = in.getBooleanExtra("Add",true);

        if (!add) {
            poiInfo.poiName = in.getStringExtra("POI Name");
            poiInfo.latitude = in.getDoubleExtra("Latitude",0.0);
            poiInfo.longitude = in.getDoubleExtra("Longitude",0.0);
            poiInfo._id = in.getStringExtra("_id");
        }

        poiName.setText(poiInfo.poiName);
        poiLatitude.setText(Double.toString(poiInfo.latitude));
        poiLongitude.setText(Double.toString(poiInfo.longitude));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_poi, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        // When the submit button is clicked, get the POI information

        poiInfo.poiName = poiName.getText().toString();
        poiInfo.latitude = Double.parseDouble(poiLatitude.getText().toString());
        poiInfo.longitude = Double.parseDouble(poiLongitude.getText().toString());

        Log.i(TAG, "POI " + poiInfo.poiName + " Latitude " + poiInfo.latitude + " Longitude " + poiInfo.longitude);

        if (add) {

            new ModifyPoiAsyncHttpTask().execute("");

        }
        else {

            new ModifyPoiAsyncHttpTask().execute(poiInfo._id);

        }
    }

    private class ModifyPoiAsyncHttpTask extends AsyncTask<String, Void, Integer> {
        private String response = null;

        @Override
        protected Integer doInBackground(String... params) {
            InputStream inputStream = null;
            HttpURLConnection urlConnection = null;
            Integer result = 0;
            URL url;
            try {
                /* forming th java.net.URL object */
                /* for Post request */
                if (add) { // if adding we do POST /places
                    url = new URL(Constants.SERVER_URL);
                }
                else { // if updating we do PUT /places/<id>
                    url = new URL(Constants.SERVER_URL+"/"+params[0]);
                }
                urlConnection = (HttpURLConnection) url.openConnection();

                 /* optional request header */
                urlConnection.setRequestProperty("Content-Type", "application/json");

                /* optional request header */
                urlConnection.setRequestProperty("Accept", "application/json");

                if (add) {
                    urlConnection.setRequestMethod("POST");
                } else {
                    urlConnection.setRequestMethod("PUT");
                }

                // request has a body to be added
                urlConnection.setDoOutput(true);
                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                writeStream(out);

                // urlConnection.connect();
                int statusCode = urlConnection.getResponseCode();
                Log.i(TAG, "statusCode is "+statusCode);

                /* 200 represents HTTP OK */
                if (statusCode == 200) {
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    response = convertInputStreamToString(inputStream);
                    result = 1;
                } else {
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    response = convertInputStreamToString(inputStream);
                    result = 0; //"Failed to add/update data!";
                }
            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
            return result; //"Failed to fetch data!";
        }

        private void writeStream(OutputStream out) {

            Gson gson = new Gson();

            // converts the java object to json string
            String placeJsonInfo = gson.toJson(poiInfo);

            Log.i(TAG, "Place Info Object is "+placeJsonInfo);

            try {
                out.write(placeJsonInfo.getBytes());
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
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

        @Override
        protected void onPostExecute(Integer result) {
            /* Download complete. Lets update UI */
            if(result == 1){
                Log.i(TAG, "Response is "+response);
            }else{
                Log.i(TAG, "Response is "+response);
                Log.e(TAG, "Failed to set data!");
            }
            finish();
        }
    }

}
