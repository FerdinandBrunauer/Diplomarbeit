package digitalsalzburg.opendata;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import digitalsalzburg.JsonReader;

public class OpenDataUtilities {

	private static final String OPENDATAURL = "https://www.data.gv.at/katalog/api/3/action/";

	public static void main(String[] args) throws JSONException, IOException, ParseException {
		for(List<String> t:searchForPackages(" salzburg")){
			for(String s:t){
				System.out.println(s);
			}
			System.out.println();
		}
		getPackageById("land-sbg_museen-land-salzburg434e9");
	}

	public static String[] getDataGigs(String... organisations) {
		return null;
	}

	public static String[] getOrganisations() {
		return null;
	}
	
	/*
	 * Returns an OpenDataPackage that can be used to get and update resource Files (.kmz)
	 * 
	 * @param id 	may be the name or the id of the package as the api supports both
	 */
	public static OpenDataPackage getPackageById(String id) {
		try{
			JSONObject json = JsonReader.readJsonFromUrl(OPENDATAURL + "package_show?id=" + id);
			JSONObject temp = (JSONObject) json.get("result");
			JSONArray resources = temp.getJSONArray("resources");
			JSONArray tags = temp.getJSONArray("tags");
		
			OpenDataPackage odPackage = new OpenDataPackage(temp.getString("id"));
			odPackage.setName(temp.getString("name"));
			odPackage.setTitle(temp.getString("title"));
			odPackage.setNotes(temp.getString("notes"));
	
			for(int i=0; i<resources.length(); i++){
				JSONObject resourceObject = resources.getJSONObject(i);
				OpenDataResource resource = new OpenDataResource(resourceObject.getString("id"));
				resource.setFormat(resourceObject.getString("format"));
				resource.setUrl(resourceObject.getString("url"));
				resource.setRevisionTimestamp(getTimestampFromDate("yyyy-MM-dd;HH:mm:ss.S",resourceObject.getString("revision_timestamp").replaceAll("T",";")));
				odPackage.addResource(resource);
			}
			
			for(int i=0; i<tags.length(); i++){
				JSONObject tagObject = tags.getJSONObject(i);
				OpenDataTag tag = new OpenDataTag(tagObject.getString("id"));
				tag.setName(tagObject.getString("name"));
				tag.setDisplayName(tagObject.getString("display_name"));
				odPackage.addTag(tag);
			}
			return odPackage;
		}catch(Exception e){
			return null;
		}
		
	}
	
	/*
	 * Return a List of all Package Names
	 */
	public static List<String> getAllPackages() throws JSONException, IOException{
		JSONArray jsonArray = JsonReader.readArrayFromUrl(OPENDATAURL + "package_list", "result");
		List<String> allPackages = new ArrayList<String>();
		for(int i = 0; i < jsonArray.length(); i++){
			allPackages.add(jsonArray.get(i).toString());
		}
		return allPackages;
	}
	
	/*
	 * Returns a List of Package titles and ids that either have the
	 * searchString as Tag or in its name. 
	 * 
	 * The List is sorted alphabetical
	 * 
	 * @param searchString	String to look for.
	 */
	public static List<List<String>> searchForPackages(String searchString) throws JSONException, IOException, ParseException{
		List<List<String>> listOne = searchForPackageName(searchString);
		List<List<String>> listTwo = searchForPackageTag(searchString);
		List<List<String>> combinedList = new ArrayList<List<String>>();
		combinedList.addAll(listOne);
		combinedList.addAll(listTwo);
		
		Set<List<String>> set = new LinkedHashSet<List<String>>();
		set.addAll(combinedList);
		combinedList.clear();
		combinedList.addAll(set);
		
		Collections.sort(combinedList, new Comparator<List<String>>() {    
	        @Override
	        public int compare(List<String> o1, List<String> o2) {
	            return o1.get(0).compareTo(o2.get(0));
	        }
      
		});
		
		return combinedList;
	}
	
	/*
	 * Return a List of all Package names
	 */
	public static List<List<String>> searchForPackageName(String searchString) throws JSONException, IOException, ParseException{
		JSONArray jsonArray = JsonReader.readArrayFromUrl(OPENDATAURL + "package_list", "result");
		List<List<String>> foundPackages = new ArrayList<List<String>>();
		for(int i = 0; i < jsonArray.length(); i++){
			if(jsonArray.get(i).toString().matches("(.*)" + searchString.trim().toLowerCase() + "(.*)")){
				OpenDataPackage odPackage = getPackageById(jsonArray.getString(i));
				if(odPackage != null){
					List<String> tempPackages = new ArrayList<String>();
					tempPackages.add(odPackage.getTitle());
					tempPackages.add(odPackage.getId());
					foundPackages.add(tempPackages);
				}
			}
		}
		return foundPackages;
	}
	
	/*
	 * Returns a List of Package titles and ids that have the
	 * searchString as Tag
	 * 
	 * @param searchString	String to look for.
	 */
	public static List<List<String>> searchForPackageTag(String id) throws JSONException, IOException{
		JSONObject json = JsonReader.readJsonFromUrl(OPENDATAURL + "tag_show?id=" + id.trim());
		JSONObject temp = (JSONObject) json.get("result");
		JSONArray packages = temp.getJSONArray("packages");
		List<List<String>> foundPackages = new ArrayList<List<String>>();
		for(int i = 0; i < packages.length(); i++){
			List<String> tempPackages = new ArrayList<String>();
			JSONObject odPackage = packages.getJSONObject(i);
			tempPackages.add(odPackage.getString("title"));
			tempPackages.add(odPackage.getString("id"));
			foundPackages.add(tempPackages);
		}
		return foundPackages;
	}
	
	/*
	 * Returns a List of Package titles and ids that have the
	 * searchString in its name
	 * 
	 * @param searchString	String to look for.
	 */
	public static List<List<String>> searchForPackageTag(OpenDataTag tag) throws JSONException, IOException{
		JSONObject json = JsonReader.readJsonFromUrl(OPENDATAURL + "tag_show?id=" + tag.getId());
		JSONObject temp = (JSONObject) json.get("result");
		JSONArray packages = temp.getJSONArray("packages");
		List<List<String>> foundPackages = new ArrayList<List<String>>();
		for(int i = 0; i < packages.length(); i++){
			List<String> tempPackages = new ArrayList<String>();
			JSONObject odPackage = packages.getJSONObject(i);
			tempPackages.add(odPackage.getString("title"));
			tempPackages.add(odPackage.getString("id"));
			foundPackages.add(tempPackages);
		}
		return foundPackages;
	}

	public static long getTimestampFromDate(String dateformat, String source) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(dateformat);
		Date date = sdf.parse(source);
		return date.getTime();
	}
}
