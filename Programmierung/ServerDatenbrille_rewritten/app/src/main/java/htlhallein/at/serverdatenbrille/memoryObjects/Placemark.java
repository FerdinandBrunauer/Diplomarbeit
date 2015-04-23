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

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import htlhallein.at.serverdatenbrille.opendata.kmzUtil.StringUtils;

public class Placemark {

    private String id;
    private String name;
    private String link;
    private Location location;
    private String description;

    public Placemark(String rawPlacemark) {
        Pattern idPattern = Pattern.compile("<Placemark id=\\\"([\\s\\S]+)\\\">");
        Matcher matcher = idPattern.matcher(rawPlacemark);
        matcher.find();
        this.id = matcher.group(1);

        try {
            Pattern namePattern = Pattern.compile("<name>([a-zA-Z0-9äöü     -\"/\\\\(\\\\'\\.\\),]+)</name>");
            matcher = namePattern.matcher(rawPlacemark);
            matcher.find();
            this.name = matcher.group(1);
        } catch (Exception e) {
            this.name = "";
        }

        Pattern descriptionPattern = Pattern.compile("<description>([\\s\\S]*?)<\\/description>");
        matcher = descriptionPattern.matcher(rawPlacemark);
        matcher.find();
        this.description = matcher.group(1);

        try {
            this.link = StringUtils.unescapeHtml3(getStringBetween(rawPlacemark, "\\<td\\>\\<a target\\=\\\"\\_blank\\\" href\\=\\\"", "\\\"\\>http\\:\\/\\/"));
        }catch (Exception e){
            Log.d(this.getClass().toString(), "No Link in description");
            this.link = "";
        }

        String s = getStringBetween(rawPlacemark, "\\<coordinates\\>", "\\<\\/coordinates\\>");
        String[] temp = s.split(",");
        String t = temp[1].trim() + "," + temp[0].trim();
        this.location = new Location(t);
    }

    private static String getStringBetween(String input, String start, String end) {
        Pattern pattern = Pattern.compile(start + "(.+)" + end);
        Matcher matcher = pattern.matcher(input);
        matcher.find();
        return matcher.group(1);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return link;
    }

    public Location getLocation() {
        return location;
    }

    public String getDescription() { return description; }

    public void print() {
        String leftAlignFormat = "| %-15s | %-50s |%n";
        System.out.println("+-----------------+----------------------------------------------------+");
        System.out.println("| Variable        | Value                                              |");
        System.out.println("+-----------------+----------------------------------------------------+");
        System.out.format(leftAlignFormat, "ID", this.id);
        System.out.format(leftAlignFormat, "Name", this.name);
        System.out.format(leftAlignFormat, "Link", this.link);
        System.out.format(leftAlignFormat, "Location", this.location.toString());
        System.out.println("+-----------------+----------------------------------------------------+\n");
    }

}
