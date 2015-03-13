package htlhallein.at.serverdatenbrille_rewritten.datapoint;

import android.widget.Toast;

import com.google.gson.Gson;

import java.util.Map;

import htlhallein.at.serverdatenbrille_rewritten.MainActivity;
import htlhallein.at.serverdatenbrille_rewritten.R;
import htlhallein.at.serverdatenbrille_rewritten.database.DatabaseHelper;
import htlhallein.at.serverdatenbrille_rewritten.event.datapoint.DatapointEventObject;

public class Validator {

    public static DatapointEventObject validate(String json) {
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
                    // TODO get content from webdata (Validator)
                    // Temperature from PI or something like that
                    return null;
                }
                default: {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    public static DatapointEventObject validate(double latitude, double longitude, int angle) {
        // TODO GPS validator
        return null;
    }

}
