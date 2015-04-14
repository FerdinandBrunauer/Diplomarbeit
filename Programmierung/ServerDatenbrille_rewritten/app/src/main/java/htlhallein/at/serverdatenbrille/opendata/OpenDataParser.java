package htlhallein.at.serverdatenbrille.opendata;

import android.util.Base64;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class OpenDataParser {

    public static String parseWebsite(String htmlText) throws Exception {
        Document openDataWebsite = Jsoup.parse(htmlText);
        openDataWebsite.outputSettings().escapeMode(EscapeMode.extended).charset("ASCII");

        openDataWebsite.head().empty();
        openDataWebsite.head().append("<style>body {font-family: Arial, Helvetica, sans-serif;}</style>");

        Element contentDiv = openDataWebsite.getElementById("content_container");
        Elements images = contentDiv.select("img");
        for (int i = 0; i < images.size(); i++) {
            images.get(i).attr("src", "data:image/gif;base64," + Base64.encodeToString(getImageByteArrayFromUrl(new URL(images.get(i).attr("src"))), Base64.NO_WRAP));
        }
        openDataWebsite.body().empty();
        openDataWebsite.body().append(contentDiv.html());

        return openDataWebsite.html();
    }

    private static byte[] getImageByteArrayFromUrl(URL destination) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] chunk = new byte[128];
        int bytesRead;
        InputStream stream = destination.openStream();

        while ((bytesRead = stream.read(chunk)) > 0) {
            outputStream.write(chunk, 0, bytesRead);
        }

        return outputStream.toByteArray();
    }

}
