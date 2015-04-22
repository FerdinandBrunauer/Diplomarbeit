package htlhallein.at.serverdatenbrille;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import htlhallein.at.serverdatenbrille.event.scroll.ScrollEventDirection;
import htlhallein.at.serverdatenbrille.event.scroll.ScrollEventHandler;
import htlhallein.at.serverdatenbrille.event.scroll.ScrollEventObject;

public class ControllerFragment extends Fragment {

    public static ObservableWebView webView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_controlling, container, false);


        webView = (ObservableWebView) rootView.findViewById(R.id.controllerWebView);
        webView.loadDataWithBaseURL(null, "<h3>Herzlich wilkommen zur Digital Salzburg App</h3><br>"
                + "Für weitere Optionen wischen Sie von links nach rechts.<br><br>"
                + "Wichtig beim ersten start ist das drücken des Synchronisieren Buttons, der sich oben rechts befindet."
                + "Dadurch wird das Standard Paket Museen im Land Salzburg (derzeit das einzig unterstützte, demnächst werden alle Packages der Stadt Salzburg welche Koordinaten beinhaltet unterstützt) heruntergeladen und lokal gespeichert.<br><br>"
                + "Eine Internetverbindung ist nur beim herunterladen der Open Data Packages "
                + "erforderlich oder beim Einscannen von Webdaten wie zum Beispiel Wetter in Hallein.<br>", "text/html", "utf-8", null);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.clearCache(true);
        webView.clearHistory();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);


        return rootView;
    }


}
