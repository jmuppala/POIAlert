package hk.ust.cse.comp4521.poialert.rest;

import hk.ust.cse.comp4521.poialert.Constants;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by muppala on 24/3/2018.
 */
public class RestClient {

    private static poiServerAPI restClient;

    static {
        setupRestClient();
    }

    private RestClient() {}

    public static poiServerAPI get() {
        return restClient;
    }

    private static void setupRestClient() {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(Constants.SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.build();
        restClient = retrofit.create(poiServerAPI.class);
    }
}
