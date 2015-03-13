package htlhallein.at.serverdatenbrille_rewritten;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import htlhallein.at.serverdatenbrille_rewritten.database.DatabaseConnection;
import htlhallein.at.serverdatenbrille_rewritten.database.openDataUtilities.OpenDataUtilities;

public class DatapointFragment extends ListFragment {
    ListViewCustomAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_datapoints, null, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Button addPackageButton = (Button) getActivity().findViewById(R.id.addPackageButton);

        adapter = new ListViewCustomAdapter(getActivity(), addPackageButton, getListView());
        setListAdapter(adapter);
    }

    public static ArrayList<Package> getPackagesFromPreferences() {
        ArrayList<Package> packageList = new ArrayList<>();

        Package defaultPackage = new Package("Museen", "a5841caf-afe2-4f98-bb68-bd4899e8c9cb");

        final String defaultValue = "undefined JSON Object!";
        String preferencePackage = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext()).getString(MainActivity.getContext().getString(R.string.preferences_preference_packages), defaultValue);
        if (preferencePackage.equals(defaultValue)) {
            packageList.add(defaultPackage);
            storePackagesToPreferences(packageList);

            preferencePackage = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext()).getString(MainActivity.getContext().getString(R.string.preferences_preference_packages), defaultValue);
        }

        packageList.clear();
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Package>>() {
        }.getType();
        packageList = gson.fromJson(preferencePackage, type);

        return packageList;
    }

    public static void storePackagesToPreferences(List<Package> packageList) {
        Gson gson = new Gson();
        String json = gson.toJson(packageList);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext()).edit();
        editor.putString(MainActivity.getContext().getString(R.string.preferences_preference_packages), json);
        editor.apply();
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
                                    Package addPackage = new Package(tvName.getText().toString(), tvKey.getText().toString());

                                    try {
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
                            } else {
                                new PackageSearcher().execute(tvName.getText().toString());
                            }
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
                            new PackageRemover().execute(packages.get(position).getKey());
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
                convertView = mInflater.inflate(R.layout.fragment_datapoints_listviewrow, null);
            }

            TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
            TextView tvKey = (TextView) convertView.findViewById(R.id.tvKey);

            Package actualPackage = packages.get(position);
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

    public class PackageRemover extends AsyncTask<String, String, String> {
        private ProgressDialog dialog = new ProgressDialog(getActivity());

        @Override
        protected String doInBackground(String... params) {
            DatabaseConnection.deletePackageInclusiveDatapoints(params[0]);
            return null;
        }

        @Override
        protected void onPreExecute() {
            this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.dialog.setTitle("Please Wait");
            this.dialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    public class PackageSearcher extends AsyncTask<String, String, String> {
        private AlertDialog searchDialog;
        private Dialog dialog = new ProgressDialog(getActivity());
        protected SharedPreferences preferences;

        @Override
        protected String doInBackground(String... params) {
            try {
                List<List<String>> foundPackages = OpenDataUtilities.searchForPackages(params[0]);

                TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
                TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

                final TableLayout tableLayout = new TableLayout(getActivity());
                tableLayout.setLayoutParams(tableParams);
                for (final List<String> packages : foundPackages) {
                    TableRow row = new TableRow(getActivity());
                    row.setLayoutParams(rowParams);
                    row.setPadding(50, 10, 10, 10);
                    row.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.v("Package added", "Name: \"" + packages.get(0) + "\", Key: \"" + packages.get(1) + "\"");
                            if (!packages.get(0).equals("")) {
                                if (!packages.get(1).equals("")) {
                                    Package addPackage = new Package(packages.get(0), packages.get(1));

                                    try {
                                        ArrayList<Package> storedPackages = getPackagesFromPreferences();
                                        storedPackages.add(addPackage);
                                        storePackagesToPreferences(storedPackages);
                                        Toast.makeText(getActivity(), "Packet erfolgreich hinzugefügt!", Toast.LENGTH_LONG).show();
                                    } catch (Exception e) {
                                        Toast.makeText(getActivity(), "Fehler beim hinzufügen des Packetes", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    searchDialog.dismiss();
                                }
                            });
                        }
                    });
                    TextView nameView = new TextView(getActivity());
                    nameView.setText(packages.get(0));
                    nameView.setPadding(20, 20, 20, 20);

                    row.addView(nameView);

                    tableLayout.addView(row);
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        AlertDialog.Builder searchDialogBuilder = new AlertDialog.Builder(getActivity());
                        searchDialogBuilder.setView(tableLayout);
                        searchDialogBuilder.setTitle(getString(R.string.search));
                        searchDialogBuilder.setNegativeButton(getActivity().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        searchDialog = searchDialogBuilder.show();
                    }
                });
            } catch (Exception e) {
                Log.d("Error", "searchForPackages", e);
            }
            return null;
        }

        private ArrayList<Package> getPackagesFromPreferences() {
            ArrayList<Package> packageList = new ArrayList<>();

            Package defaultPackage = new Package("Museen", "a5841caf-afe2-4f98-bb68-bd4899e8c9cb");

            final String defaultValue = "undefined JSON Object!";
            String preferencePackage = preferences.getString(getActivity().getString(R.string.preferences_preference_packages), defaultValue);
            if (preferencePackage.equals(defaultValue)) {
                packageList.add(defaultPackage);
                storePackagesToPreferences(packageList);

                preferencePackage = preferences.getString(getActivity().getString(R.string.preferences_preference_packages), defaultValue);
            }

            packageList.clear();
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Package>>() {
            }.getType();
            packageList = gson.fromJson(preferencePackage, type);

            return packageList;
        }

        private void storePackagesToPreferences(List<Package> packageList) {
            Gson gson = new Gson();
            String json = gson.toJson(packageList);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(getActivity().getString(R.string.preferences_preference_packages), json);
            editor.apply();
        }

        @Override
        protected void onPreExecute() {
            this.preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            this.dialog.setTitle(getString(R.string.wait));
            this.dialog.show();
        }
    }
}

