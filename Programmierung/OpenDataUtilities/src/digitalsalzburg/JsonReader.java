package digitalsalzburg;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonReader {
	private static JSONObject json;

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static JSONObject readJsonFromUrl(String url) {
		try {
		InputStream is = new URL(url).openStream();
			try {
				BufferedReader rd = new BufferedReader(new InputStreamReader(is,Charset.forName("UTF-8")));
				String jsonText = readAll(rd);
				json = new JSONObject(jsonText);
				return json;
			}
			catch (JSONException e){
				
				// LOG 
				return null;
			} finally {
				is.close();
			}
		}
		catch(IOException e){
			// LOG 
			return null;
		}
		
	}

	public static String readStringFromUrl(String url) throws IOException, JSONException {
		return readJsonFromUrl(url).get("result").toString();
	}
	
	public static JSONArray readArrayFromUrl(String url, String arrayName) throws JSONException, IOException{
		return readJsonFromUrl(url).getJSONArray(arrayName);
	}

	public static void main(String[] args) throws IOException, JSONException {
		readJsonFromUrl("https://www.data.gv.at/katalog/api/3/action/package_show?id=land-sbg_museen-land-salzburg434e9");
		System.out.println(json.get("result"));
	}
}