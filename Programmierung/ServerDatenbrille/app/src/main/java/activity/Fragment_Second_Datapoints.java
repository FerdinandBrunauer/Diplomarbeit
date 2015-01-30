package activity;

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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import database.DatabaseConnection;
import database.openDataUtilities.Datapoint;
import htlhallein.at.serverdatenbrille.R;

public class Fragment_Second_Datapoints extends ListFragment implements AdapterView.OnItemClickListener {

    private final static int SHOW_DATAPOINTS = 100;

    ListViewCustomAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_second_datapoints, null, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new ListViewCustomAdapter(getActivity(), DatabaseConnection.getInstance(getActivity()).getDatapoints(getActivity(), 0, SHOW_DATAPOINTS));
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                Log.v("Scroll", "First Visible: " + firstVisibleItem + " Count Visible: " + visibleItemCount + " Total Count" + totalItemCount);
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.v("Datapoint Clicked", adapter.getItem(position).getIdDatapoint() + "");
    }
}

class ListViewCustomAdapter extends BaseAdapter {
    private Context context;
    private List<Datapoint> datapoints;

    public ListViewCustomAdapter(Context context) {
        this.context = context;
        this.datapoints = new ArrayList<>();
    }

    public ListViewCustomAdapter(Context context, List<Datapoint> datapoints) {
        this.context = context;
        this.datapoints = datapoints;
    }

    public void clear() {
        datapoints.clear();
        notifyDataSetChanged();
    }

    public void addDatapoint(Datapoint datapoint, boolean ignoreNotifiyDataSet) {
        datapoints.add(datapoint);
        if (!ignoreNotifiyDataSet)
            notifyDataSetChanged();
    }

    public void addDatapoint(Datapoint datapoint) {
        addDatapoint(datapoint, false);
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
        return datapoints.indexOf(getItem(position));
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
}