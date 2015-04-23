package htlhallein.at.serverdatenbrille.opendata;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import htlhallein.at.serverdatenbrille.memoryObjects.OpenDataPackage;
import htlhallein.at.serverdatenbrille.memoryObjects.OpenDataResource;
import htlhallein.at.serverdatenbrille.memoryObjects.OpenDataTag;

public class OpenDataUtil {
    public static final String[] supportedFiles = {"KMZ","KML"};
    private static final String OPENDATAURL = "https://www.data.gv.at/katalog/api/3/action/";
    private static final String PACKAGE_LIST = "package_list";
    private static final String TAG_SHOW = "tag_show?id=";
    private static final String PACKAGE_SHOW = "package_show?id=";
    private static final String RESOURCE_SHOW = "resource_show?id=";
    private static HttpClient httpclient = new DefaultHttpClient();
    private static HttpResponse response;
    private static ByteArrayOutputStream outputStream;

    public static List<List<String>> searchForPackages(String searchString) throws JSONException, IOException, ParseException {
        List<List<String>> combinedList = new ArrayList<>();
        combinedList.addAll(searchForPackageName(searchString));
        combinedList.addAll(searchForPackageTag(searchString));

        Set<List<String>> set = new LinkedHashSet<>();
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

    public static List<List<String>> searchForPackageName(String searchString) throws JSONException, IOException, ParseException {
        JSONObject json = new JSONObject(getRequestResult(OPENDATAURL + PACKAGE_LIST));
        JSONArray jsonArray = json.getJSONArray("result");
        List<List<String>> foundPackages = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.get(i).toString().matches("(.*)" + searchString.trim().toLowerCase() + "(.*)")) {
                OpenDataPackage odPackage = getPackageById(jsonArray.getString(i));
                if (odPackage != null) {
                    List<String> tempPackages = new ArrayList<>();
                    tempPackages.add(odPackage.getTitle());
                    tempPackages.add(odPackage.getId());
                    foundPackages.add(tempPackages);
                }
            }
        }
        return foundPackages;
    }

    public static List<List<String>> searchForPackageTag(String id) throws JSONException, IOException {
        JSONObject json = new JSONObject(getRequestResult(OPENDATAURL + TAG_SHOW + id.trim()));
        JSONObject temp = (JSONObject) json.get("result");
        JSONArray packages = temp.getJSONArray("packages");
        List<List<String>> foundPackages = new ArrayList<>();
        for (int i = 0; i < packages.length(); i++) {
            List<String> tempPackages = new ArrayList<>();
            JSONObject odPackage = packages.getJSONObject(i);
            tempPackages.add(odPackage.getString("title"));
            tempPackages.add(odPackage.getString("id"));
            foundPackages.add(tempPackages);
        }
        return foundPackages;
    }

    public static String getRequestResult(String url) {
        try {
            System.gc();
            response = httpclient.execute(new HttpGet(url));
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                outputStream = new ByteArrayOutputStream();
                response.getEntity().writeTo(outputStream);
                outputStream.close();
                return outputStream.toString();
            } else {
                response.getEntity().getContent().close();
                Log.e(OpenDataUtil.class.toString(), "getRequestResult URL unreachable! URL: \"" + url);
                return null;
            }
        } catch (ClientProtocolException e) {
            Log.e(OpenDataUtil.class.toString(), "getRequestResult ClientProtocolException: " + e);
            return null;
        } catch (IOException e) {
            Log.e(OpenDataUtil.class.toString(), "getRequestResult IOException: " + e);
            return null;
        }
    }

    public static OpenDataPackage getPackageById(String id) {
        try {
            JSONObject json = new JSONObject(getRequestResult(OPENDATAURL + PACKAGE_SHOW + id));
            JSONObject temp = (JSONObject) json.get("result");
            JSONArray resources = temp.getJSONArray("resources");
            JSONArray tags = temp.getJSONArray("tags");

            OpenDataPackage odPackage = new OpenDataPackage(temp.getString("id"));
            odPackage.setName(temp.getString("name"));
            odPackage.setTitle(temp.getString("title"));
            odPackage.setNotes(temp.getString("notes"));

            for (int i = 0; i < resources.length(); i++) {
                JSONObject resourceObject = resources.getJSONObject(i);
                OpenDataResource resource = new OpenDataResource(resourceObject.getString("id"));
                resource.setFormat(resourceObject.getString("format"));
                resource.setUrl(resourceObject.getString("url"));
                resource.setCreationTimestamp(getTimestampFromDate("yyyy-MM-dd;HH:mm:ss", resourceObject.getString("created").replaceAll("T", ";")));
                odPackage.addResource(resource);
            }

            for (int i = 0; i < tags.length(); i++) {
                JSONObject tagObject = tags.getJSONObject(i);
                OpenDataTag tag = new OpenDataTag(tagObject.getString("id"));
                tag.setName(tagObject.getString("name"));
                tag.setDisplayName(tagObject.getString("display_name"));
                odPackage.addTag(tag);
            }
            return odPackage;
        } catch (Exception e) {
            Log.wtf("Error", "getPackageById", e);
        }
        return null;
    }

    public static long getTimestampFromDate(String dateformat, String source) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(dateformat);
        Date date = sdf.parse(source);
        return date.getTime();
    }

    public static OpenDataResource getResourceById(String id) {
        try {
            JSONObject json = new JSONObject(getRequestResult(OPENDATAURL + RESOURCE_SHOW + id));
            JSONObject jsonResource = (JSONObject) json.get("result");

            OpenDataResource resource = new OpenDataResource(id);
            resource.setFormat(jsonResource.getString("format"));
            resource.setUrl(jsonResource.getString("url"));
            resource.setCreationTimestamp(getTimestampFromDate("yyyy-MM-dd;HH:mm:ss.S", jsonResource.getString("created").replaceAll("T", ";")));

            return resource;
        } catch (Exception e) {
            return null;
        }
    }

    public static String downloadFromUrl(String DownloadUrl, String fileName) {

        try {
            File root = android.os.Environment.getExternalStorageDirectory();

            File dir = new File(root.getAbsolutePath() + "/datenbrille/download/");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            URL url = new URL(DownloadUrl);
            File file = new File(dir, fileName);

            Log.d(OpenDataUtil.class.toString(), "download from url downloaded file name:" + fileName);

            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

            ByteArrayBuffer byteArrayBuffer = new ByteArrayBuffer(5000);
            int current;
            while ((current = bufferedInputStream.read()) != -1) {
                byteArrayBuffer.append((byte) current);
            }


            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(byteArrayBuffer.toByteArray());
            fileOutputStream.flush();
            fileOutputStream.close();

            return file.getAbsolutePath();

        } catch (IOException e) {
            Log.e(OpenDataUtil.class.toString(), "downloadFromUrl Error: " + e);
            return null;
        }
    }

    public static void clean() {
        httpclient = null;
    }

}