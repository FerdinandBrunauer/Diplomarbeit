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

package database;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Placemark {

	private String id;
	private String name;
	private String link;
	private Location location;

	public Placemark(String rawPlacemark) {
		Pattern idPattern = Pattern.compile("<Placemark id=\"(ID\\_\\d+)\"\\>");
		Matcher matcher = idPattern.matcher(rawPlacemark);
		matcher.find();
		this.id = matcher.group(1);

        try {
            Pattern namePattern = Pattern.compile("<name>([a-zA-Z0-9äöü     -\"/\\\\(\\\\'\\.\\),]+)</name>");
            matcher = namePattern.matcher(rawPlacemark);
            matcher.find();
            this.name = matcher.group(1);
        } catch (Exception e){
            this.name = "";
        }

		this.link = StringUtils.unescapeHtml3(getStringBetween(rawPlacemark, "\\<td\\>\\<a target\\=\\\"\\_blank\\\" href\\=\\\"", "\\\"\\>http\\:\\/\\/"));
		this.location = new Location(getStringBetween(rawPlacemark, "\\<coordinates\\>", "\\<\\/coordinates\\>"));
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
