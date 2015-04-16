package at.htlhallein.clientdatenbrille;

import at.htlhallein.clientdatenbrille.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.BufferedReader;
import java.util.ArrayList;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MainActivity extends Activity {

    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
    private SystemUiHider mSystemUiHider;
    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    public WebView webView;
    private NetworkManager networkManager;

    private boolean uiShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button settingsButton = (Button) findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(settingsButton.getVisibility() == Button.VISIBLE) {
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                }
            }
        });

        final Button reloadButton = (Button) findViewById(R.id.reload_button);
        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(settingsButton.getVisibility() == Button.VISIBLE) {
                    networkManager.closeConnection();
                    networkManager.startConnection();
                }
            }
        });

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.fullscreen_content_controls);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(uiShown) {
                    reloadButton.setVisibility(View.INVISIBLE);
                    settingsButton.setVisibility(View.INVISIBLE);
                    mHideHandler.post(mHideRunnable);
                    uiShown = false;
                }else{
                    reloadButton.setVisibility(View.VISIBLE);
                    settingsButton.setVisibility(View.VISIBLE);
                    mSystemUiHider.show();
                    uiShown = true;
                }
            }
        });


        webView = (WebView) findViewById(R.id.fullscreen_content);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.clearCache(true);
        webView.clearHistory();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);


        mSystemUiHider = SystemUiHider.getInstance(this, webView, HIDER_FLAGS);
        mSystemUiHider.setup();

        networkManager = new NetworkManager(this,this,webView);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.post(mHideRunnable);
    }

    @Override
    protected void onStart(){
        super.onStart();
        networkManager.onStart();
    }

    @Override
    protected void onStop(){
        super.onStop();
        networkManager.onStop();
    }
}
