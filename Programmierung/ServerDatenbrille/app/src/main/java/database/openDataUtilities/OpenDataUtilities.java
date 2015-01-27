/*
 * Copyright 2015 [Alexander Bendl, Brunauer Ferdinand, Milena Matic]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package database.openDataUtilities;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OpenDataUtilities {

	private static final String OPENDATAURL = "https://www.data.gv.at/katalog/api/3/action/";
	private static final String PACKAGE_SHOW = "package_show?id=";
	private static final String PACKAGE_LIST = "package_list";
	private static final String RESOURCE_SHOW = "resource_show?id=";
	private static final String TAG_SHOW = "tag_show?id=";

	public static String getFileUrl(String id, String format){
		OpenDataPackage test = getPackageById(id);
		List<OpenDataResource> testarr = test.getResources();
		for(OpenDataResource res:testarr){
			if(res.getFormat().equals(format)){
				return res.getUrl();
			}
		}
		return null;
	}
	
	/*
	 * Returns an OpenDataPackage that can be used to get and update resource Files (.kmz)
	 * 
	 * @param id 	may be the name or the id of the package as the api supports both
	 */
	public static OpenDataPackage getPackageById(String id) {
		try{
			JSONObject json = new JSONObject(getRequestResult(OPENDATAURL + PACKAGE_SHOW + id));
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
				resource.setCreationTimestamp(getTimestampFromDate("yyyy-MM-dd;HH:mm:ss.S",resourceObject.getString("created").replaceAll("T",";")));
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
            System.err.print(e);
		}
        return null;
	}
	
	public static OpenDataResource getResourceById(String id){
		try{
			JSONObject json = new JSONObject(getRequestResult(OPENDATAURL + RESOURCE_SHOW + id));
			JSONObject jsonResource = (JSONObject) json.get("result");
	
			OpenDataResource resource = new OpenDataResource(id);
			resource.setFormat(jsonResource.getString("format"));
			resource.setUrl(jsonResource.getString("url"));
			resource.setCreationTimestamp(getTimestampFromDate("yyyy-MM-dd;HH:mm:ss.S",jsonResource.getString("created").replaceAll("T",";")));
			
			return resource;
		}catch(Exception e){
			return null;
		}
	}
	
	/*
	 * Return a List of all Package Names
	 */
	public static List<String> getAllPackages() throws JSONException, IOException{
        JSONObject json = new JSONObject(getRequestResult(OPENDATAURL + PACKAGE_LIST));
        JSONArray jsonArray = json.getJSONArray("result");
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
		JSONObject json = new JSONObject(getRequestResult(OPENDATAURL + PACKAGE_LIST));
        JSONArray jsonArray = json.getJSONArray("result");
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
		JSONObject json = new JSONObject(getRequestResult(OPENDATAURL + TAG_SHOW + id.trim()));
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
        JSONObject json = new JSONObject(getRequestResult(OPENDATAURL + TAG_SHOW + tag.getId()));
        JSONObject temp = (JSONObject) json.get("result");
        JSONArray packages = temp.getJSONArray("packages");
        List<List<String>> foundPackages = new ArrayList<List<String>>();
        for (int i = 0; i < packages.length(); i++) {
            List<String> tempPackages = new ArrayList<String>();
            JSONObject odPackage = packages.getJSONObject(i);
            tempPackages.add(odPackage.getString("title"));
            tempPackages.add(odPackage.getString("id"));
            foundPackages.add(tempPackages);
        }
        return  foundPackages;
	}

	public static long getTimestampFromDate(String dateformat, String source) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(dateformat);
		Date date = sdf.parse(source);
		return date.getTime();
	}

    public static String getRequestResult(String url){
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;

        try {
            response = httpclient.execute(new HttpGet(url));
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
            } else {
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
            System.err.print(e);
        } catch (IOException e) {
            System.err.print(e);
        }
        return responseString;
    }
}
