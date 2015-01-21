public class Main1 {

	public static void main(String[] args) throws Exception {
		Location x = new Location(52.5235, 13.4115); // Berlin
		Location y = new Location(48.2092, 16.3728); // Wien

		double ausrichtung = Methods.getCourseAngle(x, y);
		System.out.println("Ausrichtung = \"" + ausrichtung + "°\"");
	}
}
