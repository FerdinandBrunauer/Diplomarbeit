package htlhallein.at.serverdatenbrille.datapoint;

import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import htlhallein.at.serverdatenbrille.MainActivity;
import htlhallein.at.serverdatenbrille.R;
import htlhallein.at.serverdatenbrille.database.DatabaseHelper;
import htlhallein.at.serverdatenbrille.event.datapoint.DatapointEventObject;

public class Validator {

    public static DatapointEventObject validate(String json) {
        try {
            Map<String, Object> jsonMap = new Gson().fromJson(json, Map.class);
            if (jsonMap.containsKey("datapointtype")) {
                String datapointType = (String) jsonMap.get("datapointtype");
                switch (datapointType) {
                    case "location": {
                        if (jsonMap.containsKey("latitude") && jsonMap.containsKey("longitude")) {
                            double latitude = Double.parseDouble(jsonMap.get("latitude").toString());
                            double longitude = Double.parseDouble(jsonMap.get("longitude").toString());
                            String datapointContent = DatabaseHelper.getDatapointcontentFromLocation(latitude, longitude);
                            if (datapointContent == null) {
                                Toast.makeText(MainActivity.getContext(), MainActivity.getContext().getString(R.string.no_datapoint_at_that_location), Toast.LENGTH_LONG).show();
                                return null;
                            } else {
                                return new DatapointEventObject(datapointContent);
                            }
                        } else {
                            return null;
                        }
                    }
                    case "webdata": {
                        if(jsonMap.containsKey("link")) {
                            String link = (String) jsonMap.get("link");
                            try {
                                String webContent = new ValidatorWebrequestTask().execute(link).get(10000, TimeUnit.MILLISECONDS);
                                // Toast.makeText(MainActivity.getContext(), "Inhalt geschickt!", Toast.LENGTH_LONG).show();
                                Log.d(Validator.class.toString(), "Sended Content of Link: \"" + jsonMap.get("link").toString());
                            } catch (Exception e) {
                                Toast.makeText(MainActivity.getContext(), "Fehler beim Herunterladen des Inhaltes des angegebenen Linkes!", Toast.LENGTH_LONG).show();
                                Log.d(Validator.class.toString(), "Error loading content. Link: \"" + jsonMap.get("link").toString() + "\" Error: \"" + e.getMessage() + "\"");
                            }
                        } else {
                            return null;
                        }
                    }
                    default: {
                        return null;
                    }
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
