package activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import htlhallein.at.serverdatenbrille.R;

public class Fragment_Third_Datapoints extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_third_datapoints, container, false);

        // TODO design datapoints fragment
        // TODO list of all Datapoints (especially the LIST!)
        // TODO crawler logic here

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v("Datapoints", "resume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v("Datapoints", "pause");
    }
}
