package htlhallein.at.serverdatenbrille;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

import htlhallein.at.serverdatenbrille.event.datapoint.DatapointEventHandler;
import htlhallein.at.serverdatenbrille.event.datapoint.DatapointEventListener;
import htlhallein.at.serverdatenbrille.event.datapoint.DatapointEventObject;

public class ObservableWebView extends WebView implements DatapointEventListener
{

    public ObservableWebView(final Context context)
    {
        super(context);
        DatapointEventHandler.addListener(this);
    }

    public ObservableWebView(final Context context, final AttributeSet attrs)
    {
        super(context, attrs);
        DatapointEventHandler.addListener(this);
    }

    public ObservableWebView(final Context context, final AttributeSet attrs, final int defStyle)
    {
        super(context, attrs, defStyle);
        DatapointEventHandler.addListener(this);
    }

    @Override
    public void datapointEventOccurred(DatapointEventObject eventObject) {
        this.loadDataWithBaseURL(null,eventObject.getHtmlText(),"text/html", "utf-8", null);
        ControllerFragment.text = eventObject.getHtmlText();
    }
}