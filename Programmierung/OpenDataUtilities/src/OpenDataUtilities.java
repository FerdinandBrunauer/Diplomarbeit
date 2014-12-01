import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OpenDataUtilities {

	public static void main(String[] args) {

	}

	public static String[] getDataGigs(String... organisations) {
		return null;
	}

	public static String[] getOrganisations() {
		return null;
	}

	public static long getTimestampFromDate(String dateformat, String source) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(dateformat);
		Date date = sdf.parse(source);
		return date.getTime();
	}
}
