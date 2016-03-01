package sk.codekitchen.smartfuel.util;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sk.codekitchen.smartfuel.model.SFDB;

/**
 *
 *
 * @author Attila Večerek
 */
public class TestServerAPI {

	public static final String LOG_TAG = TestServerAPI.class.getSimpleName();

	@Test
	public void testConnection() throws Throwable {
		Map<String, String> params = new HashMap<>();
		params.put("userID", "1");

		JSONObject server_json = new ServerAPI("test/connection").sendRequest(params);

		JSONObject local_json = new JSONObject(params);
		//perform server-side alterations
		local_json.put("success", true);
		local_json.put("db_version", String.valueOf(SFDB.DATABASE_VERSION));

		JSONAssert.assertEquals(server_json, local_json, false);
	}

	@Test
	public void testUTF_8() throws Throwable {
		Map<String, String> params = new HashMap<>();
		params.put("utf-8_char", "abcde ďľščťžýáíéóöőúüűäôň§");

		JSONObject server_json = new ServerAPI("test/utf_8").sendRequest(params);

		JSONObject local_json = new JSONObject(params);
		//perform server-side alterations
		local_json.put("success", true);
		local_json.put("db_version", String.valueOf(SFDB.DATABASE_VERSION));

		JSONAssert.assertEquals(local_json, server_json, false);
	}

	@Test
	public void testJSONPostRequest() throws Throwable {
		JSONObject data = new JSONObject();
		data.put("userID", 1);
		data.put("total_points", 8911);
		data.put("current_points", 4583);

		Map<String, String> params = new HashMap<>();
		params.put("userID", "1");
		params.put("json_data", data.toString());

		JSONObject server_json = new ServerAPI("test/json_post_request").sendRequest(params);
		params.remove("userID");
		params.remove("json_data");

		JSONObject local_json = new JSONObject(params);
		//perform server-side alterations
		local_json.put("success", true);
		local_json.put("db_version", String.valueOf(SFDB.DATABASE_VERSION));
		local_json.put("user_id", "1");
		local_json.put("data", data);

		JSONAssert.assertEquals(server_json, local_json, false);
	}

	@Test
	public void testJSONArrayResponse() throws Throwable {
		JSONArray activities =
				new ServerAPI("test/road_activities/1")
						.sendRequest().getJSONArray("data");

		assert (activities != null);
	}

	@Test
	public void testMultipleFileUpload() throws Throwable {
		List<File> files = new ArrayList<>();
		files.add(Utils.getFileFromPath(this, Utils.TEST_FILE_WITH_SPEED));
		files.add(Utils.getFileFromPath(this, Utils.TEST_FILE_WITHOUT_SPEED));

        Map<String, String> params = new HashMap<>();
        params.put("country", "SVK");
        params.put("region", "Nitra");

        try {
            new ServerAPI("test/multipart_http_request")
                    .sendMultipartRequest(params, files);
        } catch (Exception e) {
            Assert.fail("Request should have been sent and received a response code 200, " + e.getMessage());
        }
	}
}
