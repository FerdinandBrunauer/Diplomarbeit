package digitalsalzburg.localstorage;

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

		Pattern namePattern = Pattern.compile("\\<name\\>([\\wüäöß\\s\\d-\\\"\\(\\\\'\\.)\\,]+)\\<\\/name\\>");
		matcher = namePattern.matcher(rawPlacemark);
		matcher.find();
		this.name = matcher.group(1);

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
