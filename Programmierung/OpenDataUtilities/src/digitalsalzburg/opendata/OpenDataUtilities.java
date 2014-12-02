package digitalsalzburg.opendata;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import digitalsalzburg.JsonReader;

public class OpenDataUtilities {

	private static final String OPENDATAURL = "https://www.data.gv.at/katalog/api/3/action/";

	public static void main(String[] args) throws JSONException, IOException {
		for(String s:searchForPackages(" Museen")){
			System.out.println(s);
		}
	}

	public static String[] getDataGigs(String... organisations) {
		return null;
	}

	public static String[] getOrganisations() {
		return null;
	}
	
	public static List<String> getAllPackages() throws JSONException, IOException{
		JSONArray jsonArray = JsonReader.readArrayFromUrl(OPENDATAURL + "package_list");
		List<String> allPackages = new ArrayList<String>();
		for(int i = 0; i < jsonArray.length(); i++){
			allPackages.add(jsonArray.get(i).toString());
		}
		return allPackages;
	}
	
	public static List<String> searchForPackages(String searchString) throws JSONException, IOException{
		JSONArray jsonArray = JsonReader.readArrayFromUrl(OPENDATAURL + "package_list");
		List<String> foundPackages = new ArrayList<String>();
		for(int i = 0; i < jsonArray.length(); i++){
			if(jsonArray.get(i).toString().matches("(.*)" + searchString.trim().toLowerCase() + "(.*)")){
				foundPackages.add(jsonArray.get(i).toString());
			}
		}
		return foundPackages;
	}

	public static long getTimestampFromDate(String dateformat, String source) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(dateformat);
		Date date = sdf.parse(source);
		return date.getTime();
	}
}
