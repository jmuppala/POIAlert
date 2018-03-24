package hk.ust.cse.comp4521.poialert.rest;

import java.util.ArrayList;
import hk.ust.cse.comp4521.poialert.AddPOI;
import hk.ust.cse.comp4521.poialert.POIAlertActivity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * Created by muppala on 24/3/2018.
 */

public interface poiServerAPI {

    @GET("/pois")
    Call<ArrayList<POIAlertActivity.PoiInfo>> getPois();

    @GET("/pois/{id}")
    Call<POIAlertActivity.PoiInfo> getPoi(@Path("id") String id);

    @DELETE("/pois/{id}")
    Call<ResponseBody> deletePoi(@Path("id") String id);

    @POST("/pois")
    Call<ResponseBody> addPoi(@Body AddPOI.PoiInfo poiInfo);

    @PUT("/pois/{id}")
    Call<ResponseBody> updatePoi(@Body AddPOI.PoiInfo poiInfo, @Path("id") String id);

}
