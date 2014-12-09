import java.util.ArrayList;

public class Main {

	public static void main(String[] args) {
		ArrayList<Location> locations = getCityLocations();
		System.out.println("Anzahl der Kombinationen: " + getAnzahlKombinationen(locations.size()));
		final int locationsSize = locations.size();

		long startTimeEasy = 0, endTimeEasy = 0, executionTimeEasy = 0;
		long startTimeBetter = 0, endTimeBetter = 0, executionTimeBetter = 0;
		long startTimeHavarine = 0, endTimeHavarine = 0, executionTimeHavarine = 0;
		long startTimeVincenty = 0, endTimeVincenty = 0, executionTimeVincenty = 0;

		startTimeEasy = System.currentTimeMillis();
		for (int x = 0; x < locationsSize; x++) {
			for (int y = x; y < locationsSize; y++) {
				Methods.easy(locations.get(x), locations.get(y));
			}
		}
		endTimeEasy = System.currentTimeMillis();

		startTimeBetter = System.currentTimeMillis();
		for (int x = 0; x < locationsSize; x++) {
			for (int y = x; y < locationsSize; y++) {
				Methods.better(locations.get(x), locations.get(y));
			}
		}
		endTimeBetter = System.currentTimeMillis();

		startTimeHavarine = System.currentTimeMillis();
		for (int x = 0; x < locationsSize; x++) {
			for (int y = x; y < locationsSize; y++) {
				Methods.haversine(locations.get(x), locations.get(y));
			}
		}
		endTimeHavarine = System.currentTimeMillis();

		startTimeVincenty = System.currentTimeMillis();
		for (int x = 0; x < locationsSize; x++) {
			for (int y = x; y < locationsSize; y++) {
				Methods.vincenty(locations.get(x), locations.get(y));
			}
		}
		endTimeVincenty = System.currentTimeMillis();

		executionTimeEasy = endTimeEasy - startTimeEasy;
		executionTimeBetter = endTimeBetter - startTimeBetter;
		executionTimeHavarine = endTimeHavarine - startTimeHavarine;
		executionTimeVincenty = endTimeVincenty - startTimeVincenty;

		String leftAlignFormat = "| %-15s | %-12d |%n";
		System.out.format("+-----------------+--------------+%n");
		System.out.printf("| Methode         | Zeit in ms   |%n");
		System.out.format("+-----------------+--------------+%n");
		System.out.format(leftAlignFormat, "Easy", executionTimeEasy);
		System.out.format(leftAlignFormat, "Better", executionTimeBetter);
		System.out.format(leftAlignFormat, "Havarine", executionTimeHavarine);
		System.out.format(leftAlignFormat, "Vincenty", executionTimeVincenty);
		System.out.format("+-----------------+--------------+%n");
		System.out.println();

		Location wien = new Location(+48.2092, +16.3728);
		Location brasilia = new Location(-15.7801, -47.9292);
		leftAlignFormat = "| %-15s | %-12s |%n";
		System.out.println("Wien --> Brasilia");
		System.out.format("+-----------------+--------------+%n");
		System.out.printf("| Methode         | Distanz      |%n");
		System.out.format("+-----------------+--------------+%n");
		System.out.format(leftAlignFormat, "Easy", formatDouble(Methods.easy(wien, brasilia)));
		System.out.format(leftAlignFormat, "Better", formatDouble(Methods.better(wien, brasilia)));
		System.out.format(leftAlignFormat, "Havarine", formatDouble(Methods.haversine(wien, brasilia)));
		System.out.format(leftAlignFormat, "Vincenty", formatDouble(Methods.vincenty(wien, brasilia)));
		System.out.format("+-----------------+--------------+%n");

		Location washington = new Location(+38.8921, -77.0241);
		Location tokio = new Location(+35.6785, +139.6823);
		leftAlignFormat = "| %-15s | %-12s |%n";
		System.out.println("Washington --> Tokio");
		System.out.format("+-----------------+--------------+%n");
		System.out.printf("| Methode         | Distanz      |%n");
		System.out.format("+-----------------+--------------+%n");
		System.out.format(leftAlignFormat, "Easy", formatDouble(Methods.easy(washington, tokio)));
		System.out.format(leftAlignFormat, "Better", formatDouble(Methods.better(washington, tokio)));
		System.out.format(leftAlignFormat, "Havarine", formatDouble(Methods.haversine(washington, tokio)));
		System.out.format(leftAlignFormat, "Vincenty", formatDouble(Methods.vincenty(washington, tokio)));
		System.out.format("+-----------------+--------------+%n");
		
		Location berlin = new Location(+52.5235, +13.4115);
		leftAlignFormat = "| %-15s | %-12s |%n";
		System.out.println("Wien --> Berlin");
		System.out.format("+-----------------+--------------+%n");
		System.out.printf("| Methode         | Distanz      |%n");
		System.out.format("+-----------------+--------------+%n");
		System.out.format(leftAlignFormat, "Easy", formatDouble(Methods.easy(wien, berlin)));
		System.out.format(leftAlignFormat, "Better", formatDouble(Methods.better(wien, berlin)));
		System.out.format(leftAlignFormat, "Havarine", formatDouble(Methods.haversine(wien, berlin)));
		System.out.format(leftAlignFormat, "Vincenty", formatDouble(Methods.vincenty(wien, berlin)));
		System.out.format("+-----------------+--------------+%n");
		
		Location moskau = new Location(+55.7558, +37.6176);
		leftAlignFormat = "| %-15s | %-12s |%n";
		System.out.println("Moskau --> Tokio");
		System.out.format("+-----------------+--------------+%n");
		System.out.printf("| Methode         | Distanz      |%n");
		System.out.format("+-----------------+--------------+%n");
		System.out.format(leftAlignFormat, "Easy", formatDouble(Methods.easy(moskau, tokio)));
		System.out.format(leftAlignFormat, "Better", formatDouble(Methods.better(moskau, tokio)));
		System.out.format(leftAlignFormat, "Havarine", formatDouble(Methods.haversine(moskau, tokio)));
		System.out.format(leftAlignFormat, "Vincenty", formatDouble(Methods.vincenty(moskau, tokio)));
		System.out.format("+-----------------+--------------+%n");
	}

	private static int getAnzahlKombinationen(int count) {
		return (count * (count - 1)) / 2;
	}

	private static String formatDouble(double value) {
		double hundred = 100.0d;
		return String.valueOf(Math.round(value * hundred) / hundred) + "km";
	}

	private static ArrayList<Location> getCityLocations() {
		ArrayList<Location> locations = new ArrayList<Location>();
		locations.add(new Location(+41.3317, +19.8172)); // Tirane / Tirana
		locations.add(new Location(+42.5075, +1.5218)); // Andorra la Vella
		locations.add(new Location(+53.9678, +27.5766)); // Minsk
		locations.add(new Location(+50.8371, +4.3676)); // Bruxelles / Brüssel
		locations.add(new Location(+43.8608, +18.4214)); // Sarajevo
		locations.add(new Location(+42.7105, +23.3238)); // Sofiya / Sofia
		locations.add(new Location(+55.6763, +12.5681)); // Kobenhavn /
															// Kopenhagen
		locations.add(new Location(+52.5235, +13.4115)); // Berlin
		locations.add(new Location(+59.4389, +24.7545)); // Tallinn
		locations.add(new Location(+60.1699, +24.9384)); // Helsinki
		locations.add(new Location(+48.8567, +2.3510)); // Paris
		locations.add(new Location(+37.9792, +23.7166)); // Athinai / Athen
		locations.add(new Location(+51.5002, -0.1262)); // London
		locations.add(new Location(+53.3441, -6.2675)); // Dublin
		locations.add(new Location(+64.1353, -21.8952)); // Reykjavik
		locations.add(new Location(+41.8955, +12.4823)); // Roma / Rom
		locations.add(new Location(+45.8150, +15.9785)); // Zagreb
		locations.add(new Location(+56.9465, +24.1049)); // Riga
		locations.add(new Location(+47.1411, +9.5215)); // Vaduz
		locations.add(new Location(+54.6896, +25.2799)); // Vilnius
		locations.add(new Location(+49.6100, +6.1296)); // Luxembourg /
														// Luxemburg
		locations.add(new Location(+35.9042, +14.5189)); // Valletta
		locations.add(new Location(+42.0024, +21.4361)); // Skopje
		locations.add(new Location(+47.0167, +28.8497)); // Chisinau
		locations.add(new Location(+43.7325, +7.4189)); // Monaco
		locations.add(new Location(+42.4602, +19.2595)); // Podgorica
		locations.add(new Location(+52.3738, +4.8910)); // Amsterdam
		locations.add(new Location(+59.9138, +10.7387)); // Oslo
		locations.add(new Location(+48.2092, +16.3728)); // Vienna / Wien
		locations.add(new Location(+52.2297, +21.0122)); // Warszawa / Warschau
		locations.add(new Location(+38.7072, -9.1355)); // Lisboa / Lissabon
		locations.add(new Location(+44.4479, +26.0979)); // Bucuresti / Bukarest
		locations.add(new Location(+55.7558, +37.6176)); // Moskva / Moskau
		locations.add(new Location(+43.9424, +12.4578)); // San Marino
		locations.add(new Location(+59.3328, +18.0645)); // Stockholm
		locations.add(new Location(+46.9480, +7.4481)); // Bern
		locations.add(new Location(+44.8048, +20.4781)); // Beograd / Belgrad
		locations.add(new Location(+48.2116, +17.1547)); // Bratislava
		locations.add(new Location(+46.0514, +14.5060)); // Ljubljana
		locations.add(new Location(+40.4167, -3.7033)); // Madrid
		locations.add(new Location(+50.0878, +14.4205)); // Praha / Prag
		locations.add(new Location(+50.4422, +30.5367)); // Kyiv / Kiew
		locations.add(new Location(+47.4984, +19.0408)); // Budapest
		locations.add(new Location(+62.0177, -6.7719)); // Torshavn (auf
														// Streymoy)
		locations.add(new Location(+36.1377, -5.3453)); // Gibraltar
		locations.add(new Location(+49.4660, -2.5522)); // Saint Peter Port
		locations.add(new Location(+54.1670, -4.4821)); // Douglas
		locations.add(new Location(+49.1919, -2.1071)); // Saint Helier
		locations.add(new Location(+42.6740, +21.1788)); // Prishtine / Pristina
		locations.add(new Location(+78.2186, +15.6488)); // Longyearbyen
		locations.add(new Location(+34.5155, +69.1952)); // Kabol / Kabul
		locations.add(new Location(+40.1596, +44.5090)); // Yerevan / Eriwan
		locations.add(new Location(+40.3834, +49.8932)); // Baki / Baku
		locations.add(new Location(+26.1921, +50.5354)); // Al Manamah / Manama
		locations.add(new Location(+23.7106, +90.3978)); // Dhaka
		locations.add(new Location(+27.4405, +89.6730)); // Thimphu
		locations.add(new Location(+4.9431, +114.9425)); // Bandar Seri Begawan
		locations.add(new Location(+39.9056, +116.3958)); // Beijing / Peking
		locations.add(new Location(+41.7010, +44.7930)); // T'bilisi / Tiflis
		locations.add(new Location(+28.6353, +77.2250)); // New Delhi /
															// Neu-Delhi
		locations.add(new Location(-6.1862, +106.8063)); // Jakarta (auf Java)
		locations.add(new Location(+33.3157, +44.3922)); // Baghdad / Bagdad
		locations.add(new Location(+35.7061, +51.4358)); // Tehran / Teheran
		locations.add(new Location(+31.7857, +35.2007)); // Yerushalayim /
															// Jerusalem
		locations.add(new Location(+35.6785, +139.6823)); // Tokyo / Tokio
		locations.add(new Location(+15.3556, +44.2081)); // San'a' / Sanaa
		locations.add(new Location(+31.9394, +35.9349)); // Amman
		locations.add(new Location(+11.5434, +104.8984)); // Phnum Penh / Phnom
															// Penh
		locations.add(new Location(+51.1796, +71.4475)); // Astana
		locations.add(new Location(+25.2948, +51.5082)); // Ad Dawhah / Doha
		locations.add(new Location(+42.8679, +74.5984)); // Bishkek
		locations.add(new Location(+29.3721, +47.9824)); // Al Kuwayt / Kuwait
		locations.add(new Location(+17.9689, +102.6137)); // Viangchan /
															// Vientiane
		locations.add(new Location(+33.8872, +35.5134)); // Beyrouth / Beirut
		locations.add(new Location(+3.1502, +101.7077)); // Kuala Lumpur
		locations.add(new Location(+4.1742, +73.5109)); // Maale / Male (auf
														// Male)
		locations.add(new Location(+47.9138, +106.9220)); // Ulaanbaatar / Ulan
															// Bator
		locations.add(new Location(+19.7378, +96.2083)); // Pyinmana
		locations.add(new Location(+27.7058, +85.3157)); // Kathmandu
		locations.add(new Location(+39.0187, +125.7468)); // P'yongyang /
															// Pjöngjang
		locations.add(new Location(+23.6086, +58.5922)); // Masqat / Maskat
		locations.add(new Location(+33.6751, +73.0946)); // Islamabad
		locations.add(new Location(+14.5790, +120.9726)); // Manila
		locations.add(new Location(+24.6748, +46.6977)); // Ar Riyad / Riad
		locations.add(new Location(+1.2894, +103.8500)); // Singapore / Singapur
		locations.add(new Location(+6.9155, +79.8572)); // Colombo
		locations.add(new Location(+33.5158, +36.2939)); // Dimashq / Damaskus
		locations.add(new Location(+37.5139, +126.9828)); // Seoul
		locations.add(new Location(+38.5737, +68.7738)); // Dushanbe
		locations.add(new Location(+13.7573, +100.5020)); // Krung Thep /
															// Bangkok
		locations.add(new Location(-8.5662, +125.5880)); // Dili
		locations.add(new Location(+39.9439, +32.8560)); // Ankara
		locations.add(new Location(+37.9509, +58.3794)); // Asgabat / Ashgabat
		locations.add(new Location(+41.3193, +69.2481)); // Toshkent / Tashkent
		locations.add(new Location(+24.4764, +54.3705)); // Abu Zaby / Abu Dhabi
		locations.add(new Location(+21.0341, +105.8372)); // Ha Noi / Hanoi
		locations.add(new Location(+35.1676, +33.3736)); // Nicosia / Nikosia
		locations.add(new Location(+25.0338, +121.5645)); // T'ai-pei / Taipeh
		locations.add(new Location(+17.1175, -61.8456)); // Saint John's (auf
															// Antigua)
		locations.add(new Location(-34.6118, -58.4173)); // Buenos Aires
		locations.add(new Location(+25.0661, -77.3390)); // Nassau (auf New
															// Providence)
		locations.add(new Location(+13.0935, -59.6105)); // Bridgetown
		locations.add(new Location(+17.2534, -88.7713)); // Belmopan
		locations.add(new Location(-19.0421, -65.2559)); // Sucre
		locations.add(new Location(-15.7801, -47.9292)); // Brasilia
		locations.add(new Location(-33.4691, -70.6420)); // Santiago
		locations.add(new Location(+9.9402, -84.1002)); // San Jose
		locations.add(new Location(+15.2976, -61.3900)); // Roseau
		locations.add(new Location(+18.4790, -69.8908)); // Santo Domingo
		locations.add(new Location(-0.2295, -78.5243)); // Quito
		locations.add(new Location(+13.7034, -89.2073)); // San Salvador
		locations.add(new Location(+12.0540, -61.7486)); // Saint George's
		locations.add(new Location(+14.6248, -90.5328)); // Guatemala
		locations.add(new Location(+6.8046, -58.1548)); // Georgetown
		locations.add(new Location(+18.5392, -72.3288)); // Port-au-Prince
		locations.add(new Location(+14.0821, -87.2063)); // Tegucigalpa
		locations.add(new Location(+17.9927, -76.7920)); // Kingston
		locations.add(new Location(+45.4235, -75.6979)); // Ottawa
		locations.add(new Location(+4.6473, -74.0962)); // Bogota
		locations.add(new Location(+23.1333, -82.3667)); // La Habana / Havanna
		locations.add(new Location(+19.4271, -99.1276)); // Ciudad de Mexico
		locations.add(new Location(+12.1475, -86.2734)); // Managua
		locations.add(new Location(+8.9943, -79.5188)); // Panama
		locations.add(new Location(-25.3005, -57.6362)); // Asuncion
		locations.add(new Location(-12.0931, -77.0465)); // Lima
		locations.add(new Location(+17.2968, -62.7138)); // Basseterre (auf St.
															// Kitts)
		locations.add(new Location(+13.9972, -61.0018)); // Castries
		locations.add(new Location(+13.2035, -61.2653)); // Kingstown (auf St.
															// Vincent)
		locations.add(new Location(+5.8232, -55.1679)); // Paramaribo
		locations.add(new Location(+10.6596, -61.4789)); // Port-of-Spain (auf
															// Trinidad)
		locations.add(new Location(-34.8941, -56.0675)); // Montevideo
		locations.add(new Location(+10.4961, -66.8983)); // Caracas
		locations.add(new Location(+38.8921, -77.0241)); // Washington
		locations.add(new Location(+18.3405, -64.9326)); // Charlotte Amalie
															// (auf St. Thomas)
		locations.add(new Location(+18.2249, -63.0669)); // The Valley
		locations.add(new Location(+12.5246, -70.0265)); // Oranjestad
		locations.add(new Location(+32.2930, -64.7820)); // Hamilton (auf Main
															// Island)
		locations.add(new Location(+18.4328, -64.6235)); // Road Town (auf
															// Tortola)
		locations.add(new Location(+19.3022, -81.3857)); // George Town (auf
															// Grand Cayman)
		locations.add(new Location(-51.7010, -57.8492)); // Stanley (auf
															// Ostfalkland)
		locations.add(new Location(+4.9346, -52.3303)); // Cayenne
		locations.add(new Location(+64.1836, -51.7214)); // Nuuk
		locations.add(new Location(+15.9985, -61.7220)); // Basse-Terre
		locations.add(new Location(+14.5997, -61.0760)); // Fort-de-France
		locations.add(new Location(+16.6802, -62.2014)); // Plymouth
		locations.add(new Location(+12.1034, -68.9335)); // Willemstad (auf
															// Curacao)
		locations.add(new Location(+18.4500, -66.0667)); // San Juan
		locations.add(new Location(+46.7878, -56.1968)); // Saint-Pierre (auf
															// St. Pierre)
		locations.add(new Location(+21.4608, -71.1363)); // Cockburn Town (auf
															// Grand Turk)
		locations.add(new Location(+30.0571, +31.2272)); // Al Qahirah / Kairo
		locations.add(new Location(+36.7755, +3.0597)); // Alger / Algier
		locations.add(new Location(-8.8159, +13.2306)); // Luanda
		locations.add(new Location(+3.7523, +8.7741)); // Malabo (auf Bioko)
		locations.add(new Location(+9.0084, +38.7575)); // Addis Ababa / Addis
														// Abeba
		locations.add(new Location(+6.4779, +2.6323)); // Porto-Novo
		locations.add(new Location(-24.6570, +25.9089)); // Gaborone
		locations.add(new Location(+12.3569, -1.5352)); // Ouagadougou
		locations.add(new Location(-3.3818, +29.3622)); // Bujumbura
		locations.add(new Location(+6.8067, -5.2728)); // Yamoussoukro
		locations.add(new Location(+11.5806, +43.1425)); // Djibouti / Dschibuti
		locations.add(new Location(+15.3315, +38.9183)); // Asmara
		locations.add(new Location(+0.3858, +9.4496)); // Libreville
		locations.add(new Location(+13.4399, -16.6775)); // Banjul
		locations.add(new Location(+5.5401, -0.2074)); // Accra
		locations.add(new Location(+9.5370, -13.6785)); // Conakry
		locations.add(new Location(+11.8598, -15.5875)); // Bissau
		locations.add(new Location(+3.8612, +11.5217)); // Yaounde / Jaunde
		locations.add(new Location(+14.9195, -23.5153)); // Praia (auf Sao
															// Tiago)
		locations.add(new Location(-1.2762, +36.7965)); // Nairobi
		locations.add(new Location(-11.7004, +43.2412)); // Moroni (auf
															// Njazidja)
		locations.add(new Location(-4.2767, +15.2662)); // Brazzaville
		locations.add(new Location(-4.3369, +15.3271)); // Kinshasa
		locations.add(new Location(-29.2976, +27.4854)); // Maseru
		locations.add(new Location(+6.3106, -10.8047)); // Monrovia
		locations.add(new Location(+32.8830, +13.1897)); // Tarabulus / Tripolis
		locations.add(new Location(-18.9201, +47.5237)); // Antananarivo
		locations.add(new Location(-13.9899, +33.7703)); // Lilongwe
		locations.add(new Location(+12.6530, -7.9864)); // Bamako
		locations.add(new Location(+33.9905, -6.8704)); // Rabat
		locations.add(new Location(+18.0669, -15.9900)); // Nouakchott
		locations.add(new Location(-20.1654, +57.4896)); // Port Louis
		locations.add(new Location(-25.9686, +32.5804)); // Maputo
		locations.add(new Location(-22.5749, +17.0805)); // Windhoek / Windhuk
		locations.add(new Location(+13.5164, +2.1157)); // Niamey
		locations.add(new Location(+9.0580, +7.4891)); // Abuja
		locations.add(new Location(-1.9441, +30.0619)); // Kigali
		locations.add(new Location(-15.4145, +28.2809)); // Lusaka
		locations.add(new Location(+0.3360, +6.7311)); // Sao Tome (auf Sao
														// Tome)
		locations.add(new Location(+14.6953, -17.4439)); // Dakar
		locations.add(new Location(-4.6167, +55.4500)); // Victoria (auf Mahe)
		locations.add(new Location(+8.4697, -13.2659)); // Freetown
		locations.add(new Location(-17.8227, +31.0496)); // Harare
		locations.add(new Location(+2.0411, +45.3426)); // Muqdisho / Mogadishu
		locations.add(new Location(+15.5501, +32.5322)); // Al Khartum /
															// Khartoum
		locations.add(new Location(+4.8496, +31.6046)); // Dschuba / Juba
		locations.add(new Location(-26.3186, +31.1410)); // Mbabane
		locations.add(new Location(-25.7463, +28.1876)); // Pretoria
		locations.add(new Location(-6.1670, +35.7497)); // Dodoma
		locations.add(new Location(+6.1228, +1.2255)); // Lome
		locations.add(new Location(+12.1121, +15.0355)); // N'djamena
		locations.add(new Location(+36.8117, +10.1761)); // Tunis
		locations.add(new Location(+0.3133, +32.5714)); // Kampala
		locations.add(new Location(+4.3621, +18.5873)); // Bangui
		locations.add(new Location(-12.7806, +45.2278)); // Mamoudzou
		locations.add(new Location(-20.8732, +55.4603)); // Saint Denis
		locations.add(new Location(-15.9244, -5.7181)); // Jamestown
		locations.add(new Location(+27.1536, -13.2033)); // Laayoune / El Aaiun
		locations.add(new Location(-35.2820, +149.1286)); // Canberra
		locations.add(new Location(-18.1416, +178.4419)); // Suva (auf Viti
															// Levu)
		locations.add(new Location(+1.3282, +172.9784)); // Bairiki (auf Tarawa)
		locations.add(new Location(+7.1167, +171.3667)); // Dalap-Uliga-Darrit
															// (auf Majuro)
		locations.add(new Location(+6.9177, +158.1854)); // Palikir (auf
															// Pohnpei)
		locations.add(new Location(-0.5434, +166.9196)); // Yaren
		locations.add(new Location(-41.2865, +174.7762)); // Wellington
		locations.add(new Location(+7.5007, +134.6241)); // Melekeok (auf
															// Babelthuap)
		locations.add(new Location(-9.4656, +147.1969)); // Port Moresby
		locations.add(new Location(-9.4333, +159.9500)); // Honiara (auf
															// Guadalcanal)
		locations.add(new Location(-13.8314, -171.7518)); // Apia (auf Upolu)
		locations.add(new Location(-21.1360, -175.2164)); // Nuku'alofa (auf
															// Tongatapu)
		locations.add(new Location(-8.5210, +179.1983)); // Vaiaku (auf
															// Funafuti)
		locations.add(new Location(-17.7404, +168.3210)); // Port Vila (auf
															// Efate)
		locations.add(new Location(-14.2793, -170.7009)); // Pago Pago (auf
															// Tutuila)
		locations.add(new Location(-21.2039, -159.7658)); // Avarua (auf
															// Rarotonga)
		locations.add(new Location(-17.5350, -149.5696)); // Papeete (auf
															// Tahiti)
		locations.add(new Location(+13.4667, +144.7470)); // Hagatna
		locations.add(new Location(-12.1869, +96.8283)); // Pulu Panjang / West
															// Island
		locations.add(new Location(-22.2758, +166.4581)); // Noumea (auf Grande
															// Terre)
		locations.add(new Location(-19.0565, -169.9237)); // Alofi
		locations.add(new Location(+15.2069, +145.7197)); // Garapan (auf
															// Saipan)
		locations.add(new Location(-29.0545, +167.9666)); // Kingston
		locations.add(new Location(-25.0662, -130.1027)); // Adamstown (auf
															// Pitcairn)
		locations.add(new Location(-13.2784, -176.1430)); // Mata-Utu (auf
															// Wallis)
		locations.add(new Location(-10.4286, +105.6807)); // The Settlement /
															// Flying Fish Cove
		locations.add(new Location(+32.3754, -86.2996)); // Montgomery
		locations.add(new Location(+58.3637, -134.5721)); // Juneau
		locations.add(new Location(+33.4483, -112.0738)); // Phoenix
		locations.add(new Location(+34.7244, -92.2789)); // Little Rock
		locations.add(new Location(+38.5737, -121.4871)); // Sacramento
		locations.add(new Location(+39.7551, -104.9881)); // Denver
		locations.add(new Location(+41.7665, -72.6732)); // Hartford
		locations.add(new Location(+39.1615, -75.5136)); // Dover
		locations.add(new Location(+30.4382, -84.2806)); // Tallahassee
		locations.add(new Location(+33.7545, -84.3897)); // Atlanta
		locations.add(new Location(+21.2920, -157.8219)); // Honolulu (auf Oahu)
		locations.add(new Location(+43.6021, -116.2125)); // Boise
		locations.add(new Location(+39.8018, -89.6533)); // Springfield
		locations.add(new Location(+39.7670, -86.1563)); // Indianapolis
		locations.add(new Location(+41.5888, -93.6203)); // Des Moines
		locations.add(new Location(+39.0474, -95.6815)); // Topeka
		locations.add(new Location(+38.1894, -84.8715)); // Frankfort
		locations.add(new Location(+30.4493, -91.1882)); // Baton Rouge
		locations.add(new Location(+44.3294, -69.7323)); // Augusta
		locations.add(new Location(+38.9693, -76.5197)); // Annapolis
		locations.add(new Location(+42.3589, -71.0568)); // Boston
		locations.add(new Location(+42.7336, -84.5466)); // Lansing
		locations.add(new Location(+44.9446, -93.1027)); // Saint Paul
		locations.add(new Location(+32.3122, -90.1780)); // Jackson
		locations.add(new Location(+38.5698, -92.1941)); // Jefferson City
		locations.add(new Location(+46.5911, -112.0205)); // Helena
		locations.add(new Location(+40.8136, -96.7026)); // Lincoln
		locations.add(new Location(+39.1501, -119.7519)); // Carson City
		locations.add(new Location(+43.2314, -71.5597)); // Concord
		locations.add(new Location(+40.2202, -74.7642)); // Trenton
		locations.add(new Location(+35.6816, -105.9381)); // Santa Fe
		locations.add(new Location(+42.6517, -73.7551)); // Albany
		locations.add(new Location(+35.7797, -78.6434)); // Raleigh
		locations.add(new Location(+46.8084, -100.7694)); // Bismarck
		locations.add(new Location(+39.9622, -83.0007)); // Columbus
		locations.add(new Location(+35.4931, -97.4591)); // Oklahoma City
		locations.add(new Location(+44.9370, -123.0272)); // Salem
		locations.add(new Location(+40.2740, -76.8849)); // Harrisburg
		locations.add(new Location(+41.8270, -71.4087)); // Providence
		locations.add(new Location(+34.0007, -81.0353)); // Columbia
		locations.add(new Location(+44.3776, -100.3177)); // Pierre
		locations.add(new Location(+36.1589, -86.7821)); // Nashville
		locations.add(new Location(+30.2687, -97.7452)); // Austin
		locations.add(new Location(+40.7716, -111.8882)); // Salt Lake City
		locations.add(new Location(+44.2627, -72.5716)); // Montpelier
		locations.add(new Location(+37.5408, -77.4339)); // Richmond
		locations.add(new Location(+47.0449, -122.9016)); // Olympia
		locations.add(new Location(+38.3533, -81.6354)); // Charleston
		locations.add(new Location(+43.0632, -89.4007)); // Madison
		locations.add(new Location(+41.1389, -104.8165)); // Cheyenne
		return locations;
	}

}
