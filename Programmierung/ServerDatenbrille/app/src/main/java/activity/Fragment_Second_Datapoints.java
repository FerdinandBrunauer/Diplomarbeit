package activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import htlhallein.at.serverdatenbrille.R;

public class Fragment_Second_Datapoints extends ListFragment {
    ListViewCustomAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_second_datapoints, null, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Button addPackageButton = (Button) getActivity().findViewById(R.id.addPackageButton);

        adapter = new ListViewCustomAdapter(getActivity(), addPackageButton, getListView());
        setListAdapter(adapter);
    }
}

class ListViewCustomAdapter extends BaseAdapter {
    private Context context;
    private List<Package> packages;
    protected SharedPreferences.OnSharedPreferenceChangeListener mySharedPreferenceslistener;
    protected SharedPreferences preferences;

    public ListViewCustomAdapter(final Activity context, Button button, ListView listView) {
        this.context = context;

        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.packages = getPackagesFromPreferences();
        this.mySharedPreferenceslistener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(context.getString(R.string.preferences_preference_packages))) {
                    packages = getPackagesFromPreferences();
                    notifyDataSetChanged();
                }
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(this.mySharedPreferenceslistener);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle(context.getString(R.string.add_Package));

                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);

                final EditText tvName = new EditText(context);
                tvName.setHint("Name");
                layout.addView(tvName);

                final EditText tvKey = new EditText(context);
                tvKey.setHint("Key");
                layout.addView(tvKey);

                dialog.setView(layout);
                dialog.setPositiveButton(context.getString(R.string.add), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.v("Package added", "Name: \"" + tvName.getText() + "\", Key: \"" + tvKey.getText() + "\"");
                        if (!tvName.getText().equals("")) {
                            if (!tvKey.getText().equals("")) {
                                Package addPackage = new Package(tvName.getText().toString(), tvKey.getText().toString());

                                try {
                                    loadOpendatapackage(tvKey.getText().toString());

                                    ArrayList<Package> storedPackages = getPackagesFromPreferences();
                                    storedPackages.add(addPackage);
                                    storePackagesToPreferences(storedPackages);
                                    Toast.makeText(context, "Packet erfolgreich hinzugefügt!", Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Toast.makeText(context, "Fehler beim hinzufügen des Packetes", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    }
                });
                dialog.setNegativeButton(context.getString(R.string.cancel), null);

                dialog.show();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle(context.getString(R.string.delete_Package));
                dialog.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        packages.remove(position);
                        storePackagesToPreferences(packages);
                    }
                });
                dialog.setNegativeButton(context.getString(R.string.cancel), null);
                dialog.show();
                return true;
            }
        });
    }

    private ArrayList<Package> getPackagesFromPreferences() {
        ArrayList<Package> packageList = new ArrayList<>();

        Package defaultPackage = new Package("Museen", "a5841caf-afe2-4f98-bb68-bd4899e8c9cb");

        final String defaultValue = "undefined JSON Object!";
        String preferencePackage = preferences.getString(this.context.getString(R.string.preferences_preference_packages), defaultValue);
        if (preferencePackage.equals(defaultValue)) {
            packageList.add(defaultPackage);
            storePackagesToPreferences(packageList);

            loadOpendatapackage(defaultPackage.getKey());

            preferencePackage = preferences.getString(this.context.getString(R.string.preferences_preference_packages), defaultValue);
        }

        packageList.clear();
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Package>>() {
        }.getType();
        packageList = gson.fromJson(preferencePackage, type);

        return packageList;
    }

    private void loadOpendatapackage(String key) {
        // TODO LOAD OPENDATAPACKAGE
        // OpenDataPackage openDataPackage = OpenDataUtilities.getPackageById(defaultPackage.getKey());
        // DatabaseConnection.getInstance(context).insertPackage(openDataPackage);
    }

    private void storePackagesToPreferences(List<Package> packageList) {
        Gson gson = new Gson();
        String json = gson.toJson(packageList);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(this.context.getString(R.string.preferences_preference_packages), json);
        editor.commit();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.fragment_second_datapoints_listviewrow, null);
        }

        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        TextView tvKey = (TextView) convertView.findViewById(R.id.tvKey);

        Package actualPackage = packages.get(position);
        ;
        tvName.setText(actualPackage.getName());
        tvKey.setText(actualPackage.getKey());

        return convertView;
    }

    @Override
    public int getCount() {
        return packages.size();
    }

    @Override
    public Package getItem(int position) {
        return packages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return -1L;
    }
}