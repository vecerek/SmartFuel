package sk.codekitchen.smartfuel.util;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Attila Večerek
 */
public class ReverseGeocoder {

    private static final String baseURL = "api.tomtom.com";
    private static final String versionNumber = "2";
    private static final String apiKey = "8na699bmsuh3t268tk2cp3ws";

    private String responseFormat = "json";
    private int radius = 15; // 15 meters
    private Location position;

    private String query;
    private boolean isProcessed = false;
    private Exception error = null;

    private int speedLimitKph;
    private int speedLimitMph;
    private String roadUse;

    private final OkHttpClient client = new OkHttpClient();

    public ReverseGeocoder() {}

    public ReverseGeocoder setPosition(Location location) {
        this.position = location;
        return this;
    }

    public ReverseGeocoder setRadius(int radius) {
        this.radius = radius;
        return this;
    }

    public ReverseGeocoder setResponse(String format) {
        this.responseFormat = format;
        return this;
    }

    public int getSpeedLimit(boolean isMph) throws Exception {
        if (error != null) throw error;
        if (!isProcessed) call();
        return isMph ? speedLimitMph : speedLimitKph;
    }

    public String getRoadUse() throws Exception {
        if (error != null) throw error;
        if (!isProcessed) call();
        return roadUse;
    }

    /**
     * GET https://<baseURL>/search/<versionNumber>/reverseGeocode/<position>.<ext>?key=<apiKey>
     *     [&spatialKeys=<spatialKeys>][&returnSpeedLimit=<returnSpeedLimit>][&heading=<heading>]
     *     [&radius=<radius>][&streetNumber=<streetNumber>][&returnRoadUse=<returnRoadUse>]
     *     [&roadUse=<roadUse>]
     */
    private void buildQueryURL() {
        query = "https://" + baseURL + "/search/" + versionNumber + "/reverseGeocode/" +
                Double.toString(position.getLatitude()) + "," +
                Double.toString(position.getLongitude()) + "." +
                responseFormat + "?key=" + apiKey +
                "&returnSpeedLimit=true" +
                "&radius=" + Integer.toString(radius) +
                "&returnRoadUse=true";
    }

    private void call() throws Exception {
        buildQueryURL();

        Request request = new Request.Builder()
                .url(query)
                .build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            error = new IOException("Unexpected code " + response);
            throw error;
        }

        JSONObject responseJSON = new JSONObject(response.body().string());
        try {
            JSONObject address = responseJSON
                    .getJSONArray("addresses")
                    .getJSONObject(0);

            String speedLimit = address
                    .getJSONObject("address")
                    .getString("speedLimit");

            JSONArray roadUseArr = address.getJSONArray("roadUse");
            roadUse = roadUseArr.length() > 0 ? roadUseArr.getString(0) : "";

            if (speedLimit != null) {
                if (speedLimit.contains("KPH")) {
                    float dSpeedLimitKph = Float.parseFloat(speedLimit.replace("KPH", ""));
                    speedLimitKph = (int) dSpeedLimitKph;
                    speedLimitMph = (int) Math.ceil(Units.Speed.Kph.toMph(dSpeedLimitKph));
                } else if (speedLimit.contains("MPH")) {
                    float dSpeedLimitMph = Float.parseFloat(speedLimit.replace("MPH", ""));
                    speedLimitMph = (int) dSpeedLimitMph;
                    speedLimitKph = (int) Math.ceil(Units.Speed.Mph.toKmph(dSpeedLimitMph));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            error = e;
            throw error;
        }
    }
}
