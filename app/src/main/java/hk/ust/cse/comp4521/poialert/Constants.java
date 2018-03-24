package hk.ust.cse.comp4521.poialert;

/**
 * Created by muppala on 19/3/16.
 */
public class Constants {
    // configure to the IP address of your machine if you are running the server on the same machine
    // public static final String SERVER_ID = "192.168.1.111";
    public static final String SERVER_ID = "course.cse.ust.hk/comp4521/";

    public static final String PORT_NUM = "5001"; // default node.js server: 3000, MAMP server: 8888

    // url for WAMP/MAMP/LAMP server
    //public static final String SERVER_URL = "http://"+SERVER_ID+":"+PORT_NUM+"/poiServer/v1/places";

    // url for node server
    // public static final String SERVER_URL = "http://"+SERVER_ID+":"+PORT_NUM;
    public static final String SERVER_URL = "https://"+SERVER_ID+"json/places.json";
}
