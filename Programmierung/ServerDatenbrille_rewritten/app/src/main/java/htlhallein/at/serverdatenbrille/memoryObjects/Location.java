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

package htlhallein.at.serverdatenbrille.memoryObjects;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Location {

    private double latitude;
    private double longitude;

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Location(String stringBetween) {
        Pattern locationPattern = Pattern.compile("(\\d+\\.\\d+),(\\d+\\.\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher locationMatcher = locationPattern.matcher(stringBetween);
        while (locationMatcher.find()) {
            this.latitude = Double.parseDouble(locationMatcher.group(1));
            this.longitude = Double.parseDouble(locationMatcher.group(2));
        }
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return "Latitude: " + roundDouble(this.latitude, 2) + ", Longitude: " + roundDouble(this.longitude, 2);
    }

    private String roundDouble(double value, int countDigits) {
        double rounder = Double.parseDouble("1" + "000000000000000000000".substring(0, countDigits));
        return String.valueOf(Math.round(value * rounder) / rounder);
    }

}
