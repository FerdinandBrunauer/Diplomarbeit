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

package htlhallein.at.serverdatenbrille_rewritten.datapoint.gps;

public class GPSCalculationMethods {
    private static final double EARTH_RADIUS = 6378.388d;

    /**
     * Calculates the distance between two Locations. The earth is a rectangular, the distance between the latitude is constant
     */
    public static double easy(double lat1, double lon1, double lat2, double lon2) {
        double dx = 71.5d * (lon1 - lon2);
        double dy = 111.3d * (lat1 - lat2);
        return Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
    }

    /**
     * Calculates the distance between two Locations. The earth is a rectangular, the distance between the latitude varies
     */
    public static double better(double lat1, double lon1, double lat2, double lon2) {
        double lat = (lat1 + lat2) / 2 * 0.01745d;
        double dx = 111.3d * Math.cos(Math.toRadians(lat)) * (lon1 - lon2);
        double dy = 111.3d * (lat1 - lat2);
        return Math.sqrt((dx * dx) + (dy * dy));
    }

    /**
     * Calculates the distance between two Locations. The earth is a perfect sphere
     */
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        return (2 * Math.asin(Math.sqrt(a))) * EARTH_RADIUS;
    }

    /**
     * Calculates the distance between two Locations with the vincenty Method
     */
    public static double vincenty(double lat1, double lon1, double lat2, double lon2) {
        double a = 6378137, b = 6356752.314245, f = 1 / 298.257223563; // WGS-84

        double L = Math.toRadians(lon2 - lon1);
        double U1 = Math.atan((1 - f) * Math.tan(Math.toRadians(lat1)));
        double U2 = Math.atan((1 - f) * Math.tan(Math.toRadians(lat2)));
        double sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
        double sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);

        double sinLambda, cosLambda, sinSigma, cosSigma, sigma, sinAlpha, cosSqAlpha, cos2SigmaM;
        double lambda = L, lambdaP, iterLimit = 100;
        do {
            sinLambda = Math.sin(lambda);
            cosLambda = Math.cos(lambda);
            sinSigma = Math.sqrt((cosU2 * sinLambda) * (cosU2 * sinLambda) + (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));
            if (sinSigma == 0)
                return 0; // co-incident points
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
            cosSqAlpha = 1 - sinAlpha * sinAlpha;
            cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
            if (Double.isNaN(cos2SigmaM))
                cos2SigmaM = 0; // equatorial line: cosSqAlpha=0 (�6)
            double C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
            lambdaP = lambda;
            lambda = L + (1 - C) * f * sinAlpha * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
        } while (Math.abs(lambda - lambdaP) > 1e-12 && --iterLimit > 0);

        if (iterLimit == 0)
            return Double.NaN; // formula failed to converge

        double uSq = cosSqAlpha * (a * a - b * b) / (b * b);
        double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
        double deltaSigma = B * sinSigma * (cos2SigmaM + B / 4 * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) - B / 6 * cos2SigmaM * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));
        double dist = b * A * (sigma - deltaSigma);

        return dist / 1000.0d; // because earth radius is in MM
    }

    /**
     * Calculates the course Angle between two Points. NORTH = 0°, EAST = 90°, SOUTH = 180°, WEST = 270°
     */
    public static double getCourseAngle(double lat1, double lon1, double lat2, double lon2) {
        double pos_breite_A = Math.toRadians(lat1);
        double pos_laenge_A = Math.toRadians(lon1);
        double pos_breite_B = Math.toRadians(lat2);
        double pos_laenge_B = Math.toRadians(lon2);

        double length_orthodrome = 2.0 * Math.asin(Math.sqrt(Math.pow(Math.sin((pos_breite_A - pos_breite_B) / 2.0), 2) + Math.cos(pos_breite_A) * Math.cos(pos_breite_B) * Math.pow(Math.sin((pos_laenge_B - pos_laenge_A) / 2.0), 2)));

        double courseAngle = Math.acos((Math.sin(pos_breite_B) - Math.sin(pos_breite_A) * Math.cos(length_orthodrome)) / (Math.cos(pos_breite_A) * Math.sin(length_orthodrome)));

        if (pos_laenge_A <= pos_laenge_B && (pos_laenge_B - pos_laenge_A <= Math.PI)) {
            return courseAngle * 180 / Math.PI;
        } else if (pos_laenge_A > pos_laenge_B && (pos_laenge_A - pos_laenge_B <= Math.PI)) {
            return 360 - (courseAngle * 180 / Math.PI);
        } else if (pos_laenge_A < pos_laenge_B && (pos_laenge_B - pos_laenge_A > Math.PI)) {
            return 360 - courseAngle * 180 / Math.PI;
        } else if (pos_laenge_A > pos_laenge_B && (pos_laenge_A - pos_laenge_B > Math.PI)) {
            return courseAngle * 180 / Math.PI;
        }
        return -999; // This cant happen theoretically, because every possible
        // case is defined above
    }

}
