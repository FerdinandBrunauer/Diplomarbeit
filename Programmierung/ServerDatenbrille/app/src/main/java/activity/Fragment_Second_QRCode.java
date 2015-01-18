package activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import htlhallein.at.serverdatenbrille.R;

public class Fragment_Second_QRCode extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_second_qrcode, container, false);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v("QR-Code", "resume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v("QR-Code", "pause");
    }
}
