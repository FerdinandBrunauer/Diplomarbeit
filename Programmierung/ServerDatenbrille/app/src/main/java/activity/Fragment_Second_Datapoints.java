package activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import database.DatabaseConnection;
import database.openDataUtilities.Datapoint;
import htlhallein.at.serverdatenbrille.R;

public class Fragment_Second_Datapoints extends ListFragment implements AdapterView.OnItemClickListener {
    ListViewCustomAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_second_datapoints, null, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new ListViewCustomAdapter(getActivity(), getActivity(), getListView());
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.v("Datapoint Clicked", adapter.getItem(position).getIdDatapoint() + "");
        // TODO show datapoint in dialog
    }
}

class ListViewCustomAdapter extends BaseAdapter implements AbsListView.OnScrollListener {
    private Context context;
    private Activity activity;
    private SortedList<Datapoint> datapoints;
    private int VISIBLE_DATAPOINTS = 50;
    private boolean alreadyLoading = false;

    private int lastMiddleScroll = 0;
    private int lastShouldScroll = 0;

    public ListViewCustomAdapter(Context context, Activity activity, ListView listView) {
        this.context = context;
        this.activity = activity;
        listView.setOnScrollListener(this);
        this.datapoints = new SortedList<>();

        try {
            List<Datapoint> addDatapoints = DatabaseConnection.getInstance(context).getDatapointsEnd(context, 0, VISIBLE_DATAPOINTS);
            for (Datapoint datapoint : addDatapoints)
                this.datapoints.add(datapoint);
        }catch (Exception e){
            Log.wtf("Error", "load Datapoints from Database", e);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.fragment_second_datapoints_listviewrow, null);
        }

        ImageView iv_Picture = (ImageView) convertView.findViewById(R.id.icon);
        TextView tv_Name = (TextView) convertView.findViewById(R.id.title);

        Datapoint datapoint = datapoints.get(position);
        tv_Name.setText(datapoint.getTitle());
        if (datapoint.getImage() != null) {
            iv_Picture.setImageBitmap(datapoint.getImage());
        } else {
            iv_Picture.setImageResource(R.drawable.no_image_available);
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return datapoints.size();
    }

    @Override
    public Datapoint getItem(int position) {
        return datapoints.get(position);
    }

    @Override
    public long getItemId(int position) {
        return -1L;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        Log.v("Scroll", "FirstVisibleItem: " + firstVisibleItem);
        Log.v("Scroll", "VisibleItemCount: " + visibleItemCount);
        Log.v("Scroll", "TotalItemCount: " + totalItemCount);

        final int mitleresSichtbaresElement = firstVisibleItem + (visibleItemCount / 2);
        final int solltengeladenseinHalf = totalItemCount / 2;

        if ((mitleresSichtbaresElement == lastMiddleScroll) && (solltengeladenseinHalf == lastShouldScroll)) {
            return;
        } else {
            lastMiddleScroll = mitleresSichtbaresElement;
            lastShouldScroll = solltengeladenseinHalf;
        }

        if (!alreadyLoading) {
            alreadyLoading = true;
            if ((mitleresSichtbaresElement > solltengeladenseinHalf) && ((mitleresSichtbaresElement - solltengeladenseinHalf) > 5)) {
                // Elemente an das hintere ende laden
                Log.v("Nachladen", "ENDE");
                new Thread() {
                    @Override
                    public void run() {
                        final List<Datapoint> addDatapoints = DatabaseConnection.getInstance(context).getDatapointsEnd(context, datapoints.get(datapoints.size() - 1).getIdDatapoint(), mitleresSichtbaresElement - solltengeladenseinHalf);
                        for (Datapoint actualDatapoint : addDatapoints) {
                            ListViewCustomAdapter.this.datapoints.remove(0);
                            ListViewCustomAdapter.this.datapoints.add(actualDatapoint);
                        }
                        ListViewCustomAdapter.this.activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ListViewCustomAdapter.this.notifyDataSetChanged();
                            }
                        });
                        ListViewCustomAdapter.this.alreadyLoading = false;
                    }
                }.start();
            } else if ((solltengeladenseinHalf > mitleresSichtbaresElement) && ((solltengeladenseinHalf - mitleresSichtbaresElement) > 5)) {
                // Elemente an das vordere ende laden
                Log.v("Nachladen", "BEGIN");
                alreadyLoading = false;
            } else {
                alreadyLoading = false;
            }
        }

        Log.v("Scroll", "==============================");

        return;
    }
}

class SortedList<E> extends AbstractList<Datapoint> {

    private ArrayList<Datapoint> internalList = new ArrayList<>();

    @Override
    public boolean add(Datapoint datapoint) {
        internalList.add(datapoint);
        Collections.sort(internalList, new Comparator<Datapoint>() {
            @Override
            public int compare(Datapoint lhs, Datapoint rhs) {
                return ((Integer) lhs.getIdDatapoint()).compareTo(rhs.getIdDatapoint());
            }
        });
        return true;
    }

    @Override
    public Datapoint get(int i) {
        return internalList.get(i);
    }

    @Override
    public int size() {
        return internalList.size();
    }

    @Override
    public Datapoint remove(int location) {
        return internalList.remove(location);
    }
}