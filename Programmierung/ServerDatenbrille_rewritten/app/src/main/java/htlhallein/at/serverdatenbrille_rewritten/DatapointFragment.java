package htlhallein.at.serverdatenbrille_rewritten;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

public class DatapointFragment extends ListFragment {
    ListViewCustomAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_datapoints, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Button addPackageButton = (Button) getActivity().findViewById(R.id.addPackageButton);

        adapter = new ListViewCustomAdapter(getActivity(), addPackageButton, getListView());
        setListAdapter(adapter);
    }

    public static ArrayList<htlhallein.at.serverdatenbrille_rewritten.memoryObjects.Package> getPackagesFromPreferences() {
        ArrayList<htlhallein.at.serverdatenbrille_rewritten.memoryObjects.Package> packageList = new ArrayList<>();

        htlhallein.at.serverdatenbrille_rewritten.memoryObjects.Package defaultPackage = new htlhallein.at.serverdatenbrille_rewritten.memoryObjects.Package("Museen", "a5841caf-afe2-4f98-bb68-bd4899e8c9cb");

        final String defaultValue = "undefined JSON Object!";
        String preferencePackage = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext()).getString(MainActivity.getContext().getString(R.string.preferences_preference_packages), defaultValue);
        if (preferencePackage.equals(defaultValue)) {
            packageList.add(defaultPackage);
            storePackagesToPreferences(packageList);

            preferencePackage = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext()).getString(MainActivity.getContext().getString(R.string.preferences_preference_packages), defaultValue);
        }

        packageList.clear();
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<htlhallein.at.serverdatenbrille_rewritten.memoryObjects.Package>>() {
        }.getType();
        packageList = gson.fromJson(preferencePackage, type);

        return packageList;
    }

    public static void storePackagesToPreferences(List<htlhallein.at.serverdatenbrille_rewritten.memoryObjects.Package> packageList) {
        Gson gson = new Gson();
        String json = gson.toJson(packageList);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext()).edit();
        editor.putString(MainActivity.getContext().getString(R.string.preferences_preference_packages), json);
        editor.apply();
    }

    class ListViewCustomAdapter extends BaseAdapter {
        private Context context;
        private List<htlhallein.at.serverdatenbrille_rewritten.memoryObjects.Package> packages;
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
                    dialog.setTitle(context.getString(R.string.add_package));
                    LinearLayout layout = new LinearLayout(context);
                    layout.setOrientation(LinearLayout.VERTICAL);
                    final EditText tvName = new EditText(getActivity());
                    final EditText tvKey = new EditText(getActivity());
                    tvName.setHint("Name");
                    layout.addView(tvName);
                    tvKey.setHint("Key");
                    layout.addView(tvKey);

                    dialog.setView(layout);

                    dialog.setPositiveButton(context.getString(R.string.add), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(DatapointFragment.class.toString(), "Name: \"" + tvName.getText() + "\", Key: \"" + tvKey.getText() + "\"");
                            if (tvName.getText().toString().compareTo("") != 0) {
                                if (tvKey.getText().toString().compareTo("") != 0) {
                                    htlhallein.at.serverdatenbrille_rewritten.memoryObjects.Package addPackage = new htlhallein.at.serverdatenbrille_rewritten.memoryObjects.Package(tvName.getText().toString(), tvKey.getText().toString());

                                    try {
                                        ArrayList<htlhallein.at.serverdatenbrille_rewritten.memoryObjects.Package> storedPackages = getPackagesFromPreferences();
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
                    dialog.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    dialog.setNeutralButton(context.getString(R.string.search), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String searchName = tvName.getText().toString();
                            if (searchName.compareTo("") == 0) {
                                Toast.makeText(context, context.getString(R.string.search_package_no_name), Toast.LENGTH_LONG).show();
                            } /* else {
                                // new PackageSearcher().execute(tvName.getText().toString());
                                TODO
                            } */
                        }
                    });

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
                            // new PackageRemover().execute(packages.get(position).getKey()); TODO
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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater mInflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(R.layout.fragment_datapoints_listviewrow, parent, false);
            }

            TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
            TextView tvKey = (TextView) convertView.findViewById(R.id.tvKey);

            htlhallein.at.serverdatenbrille_rewritten.memoryObjects.Package actualPackage = packages.get(position);
            tvName.setText(actualPackage.getName());
            tvKey.setText(actualPackage.getKey());

            return convertView;
        }

        @Override
        public int getCount() {
            return packages.size();
        }

        @Override
        public htlhallein.at.serverdatenbrille_rewritten.memoryObjects.Package getItem(int position) {
            return packages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return -1L;
        }
    }
}

