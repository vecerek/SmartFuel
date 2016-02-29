package sk.codekitchen.smartfuel.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.IOException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import sk.codekitchen.smartfuel.model.SFDB;

/**
 * Handles Http Requests to the server.
 * @author Attila Večerek
 */
public class ServerAPI {

	private static final String USER_AGENT = "SmartFuel/1.0";
	private static final String SERVER = "http://smartfuel.walkoflife.sk/";
    private static final MediaType MEDIA_TYPE_XML = MediaType.parse("text/xml");
    private static final String FILE_PREFIX = "FILE_";

    private String url;
	public int responseCode;

	private HttpURLConnection con;
    private final OkHttpClient client = new OkHttpClient();

	public ServerAPI(String action) throws IOException {
		this(action, SERVER);
	}

	public ServerAPI(String action, String server) throws IOException {
        if (action.charAt(0) == '/') action = action.substring(1);
		url = server + action;
		con = (HttpURLConnection) (new URL(url)).openConnection();

		//add request header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Charset", "UTF-8");
		con.setUseCaches(false);
	}

	public JSONObject sendRequest() throws IOException, JSONException {
		return sendRequest(null);
	}

	public JSONObject sendRequest(Map<String, String> params) throws IOException, JSONException {
		if (params == null) params = new HashMap<>();
		//Always puts the local database's version to avoid any incompatibility issues
		params.put("db_version", String.valueOf(SFDB.DATABASE_VERSION));

		String urlParams = "";
		//loop the map
		for (Map.Entry<String, String> param : params.entrySet()) {
			if (!urlParams.equals(""))
				urlParams += "&";
			urlParams += param.getKey() + "=" + URLEncoder.encode(param.getValue(), "UTF-8");
		}

		con.setDoInput(true);
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(String.valueOf(urlParams));
		wr.flush();
		wr.close();

		responseCode = con.getResponseCode();

		BufferedReader in = new BufferedReader(
				new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		Object json = new JSONTokener(response.toString()).nextValue();

		if (json instanceof JSONArray) {
			return new JSONObject().put("data", json);
		} else if (json instanceof JSONObject) {
			if(((JSONObject) json).has("response_code")) {
				responseCode = ((JSONObject) json).getInt("response_code");
			}
			return (JSONObject) json;
		} else {
			throw new IOException("Response is not a JSON");
		}
	}

    /**
     * Sends a multipart request to the specified URL.
     *
     * Files can be accessed via the key FILE_X, where X is the order number, starting with 1.Comments the Adds comment regarding the
     *
     * @param params POST request-like params
     * @param files array of files to send
     * @throws IOException if host unreachable
     *                     if response code not 200
     */
	public void sendMultipartRequest(Map<String, String> params, List<File> files) throws IOException {

        MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder();
        requestBodyBuilder.setType(MultipartBody.FORM);

        for (Map.Entry<String, String> param : params.entrySet()) {
            requestBodyBuilder.addFormDataPart(param.getKey(), param.getValue());
        }

        int fileCounter = 1;
        for (File file : files) {
            requestBodyBuilder.addFormDataPart(FILE_PREFIX + Integer.toString(fileCounter),
                    file.getName(), RequestBody.create(MEDIA_TYPE_XML, file));
            fileCounter++;
        }

        RequestBody requestBody = requestBodyBuilder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected response code " + response);

        System.out.println(response.body().string());
	}
}
