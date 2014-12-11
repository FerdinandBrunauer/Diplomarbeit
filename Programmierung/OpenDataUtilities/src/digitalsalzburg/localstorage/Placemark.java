package digitalsalzburg.localstorage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Placemark {
	
	private String id;
	private String name;
	private String link;
	private Location location;
	
	public Placemark(String rawPlacemark){
		this.id = getStringBetween(rawPlacemark, "<Placemark id=\"", "\">").trim();
		this.name = getStringBetween(rawPlacemark, "<name>", "</name>").trim();
		this.link = getStringBetween(rawPlacemark, "\\<td\\>\\<a target\\=\\\"\\_blank\\\" href\\=\\\"", "\\>http\\:\\/\\/");
		this.location = new Location(getStringBetween(rawPlacemark, "\\<coordinates\\>", "\\<\\/coordinates\\>"));
		// TODO
	}
	
	private static String getStringBetween(String input, String start, String end){
		Pattern pattern = Pattern.compile(start + "(.+)" + end);
		Matcher matcher = pattern.matcher(input);
		matcher.find();
		return matcher.group();
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
	
	
	
}
