package hk.ust.cse.comp4521.poialert;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import hk.ust.cse.comp4521.poialert.rest.RestClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


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

            // Ask the Rest Client to do POST /pois
            RestClient.get().addPoi(poiInfo).enqueue(new Callback<ResponseBody>() {

                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.i(TAG, "Add Response is " + response.body().toString());
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(TAG, "Retrofit Error" + t.toString());
                }
            });
            finish();
        }
        else {

            // Ask the Rest Client to do PUT /pois/<id>
            RestClient.get().updatePoi(poiInfo, poiInfo._id).enqueue(new Callback<ResponseBody>() {

                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.i(TAG, "Update Response is " + response.body().toString());
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(TAG, "Retrofit Error" + t.toString());
                }
            });
            finish();
        }
    }

}
