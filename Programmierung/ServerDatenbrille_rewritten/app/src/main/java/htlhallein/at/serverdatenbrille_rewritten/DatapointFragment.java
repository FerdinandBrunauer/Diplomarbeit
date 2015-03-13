package htlhallein.at.serverdatenbrille_rewritten;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import htlhallein.at.serverdatenbrille_rewritten.database.DatabaseHelper;
import htlhallein.at.serverdatenbrille_rewritten.memoryObjects.DataPackage;
import htlhallein.at.serverdatenbrille_rewritten.opendata.OpenDataUtil;

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

    private class ListViewCustomAdapter extends BaseAdapter {
        private Context context;
        private List<DataPackage> packages;

        public ListViewCustomAdapter(final Activity context, Button button, ListView listView) {
            this.context = context;

            packages = DatabaseHelper.getDataPackages();

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setTitle(context.getString(R.string.add_package));
                    LinearLayout layout = new LinearLayout(context);
                    layout.setOrientation(LinearLayout.VERTICAL);

                    final EditText tvName = new EditText(getActivity());
                    tvName.setHint("Name");
                    final TextView tvNameDescr = new TextView(getActivity());
                    tvNameDescr.setText("Name:");
                    tvNameDescr.setPadding(20, 20, 20, 20);
                    final EditText tvKey = new EditText(getActivity());
                    tvKey.setHint("OpenData - ID");
                    final TextView tvKeyDescr = new TextView(getActivity());
                    tvKeyDescr.setText("OpenData - ID:");
                    tvKeyDescr.setPadding(20, 20, 20, 20);

                    layout.addView(tvNameDescr);
                    layout.addView(tvName);
                    layout.addView(tvKeyDescr);
                    layout.addView(tvKey);

                    dialog.setView(layout);

                    dialog.setPositiveButton(context.getString(R.string.add), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(DatapointFragment.class.toString(), "Name: \"" + tvName.getText() + "\", Key: \"" + tvKey.getText() + "\"");
                            if (tvName.getText().toString().compareTo("") != 0) {
                                if (tvKey.getText().toString().compareTo("") != 0) {
                                    try {
                                        String name = tvName.getText().toString();
                                        String openDataID = tvKey.getText().toString();
                                        long packageID = DatabaseHelper.addPackage(openDataID, "", name);
                                        DataPackage addPackage = new DataPackage(packageID, name, openDataID);
                                        packages.add(addPackage);
                                        notifyDataSetChanged();
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
                            DatabaseHelper.deletePackage(packages.get(position).getId());
                            packages.remove(position);
                            notifyDataSetChanged();
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

            DataPackage actualPackage = packages.get(position);
            tvName.setText(actualPackage.getName());
            tvKey.setText(actualPackage.getIdOpenData());

            return convertView;
        }

        @Override
        public int getCount() {
            return packages.size();
        }

        @Override
        public DataPackage getItem(int position) {
            return packages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return -1L;
        }
    }

    private class PackageSearcher extends AsyncTask<String, String, String> {
        private AlertDialog searchDialog;
        private Dialog dialog = new ProgressDialog(getActivity());

        @Override
        protected String doInBackground(String... params) {
            try {
                List<List<String>> foundPackages = OpenDataUtil.searchForPackages(params[0]);

                TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
                TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);

                final TableLayout tableLayout = new TableLayout(getActivity());
                tableLayout.setLayoutParams(tableParams);
                for (final List<String> packages : foundPackages) {
                    TableRow row = new TableRow(getActivity());
                    row.setLayoutParams(rowParams);
                    row.setPadding(50, 10, 10, 10);
                    row.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d("Package added", "Name: \"" + packages.get(0) + "\", Key: \"" + packages.get(1) + "\"");
                            if (!packages.get(0).equals("")) {
                                if (!packages.get(1).equals("")) {
                                    try {
                                        String name = packages.get(0);
                                        String openDataID = packages.get(1);
                                        long packageID = DatabaseHelper.addPackage(openDataID, "", name);
                                        DataPackage addPackage = new DataPackage(packageID, name, openDataID);

                                        DatapointFragment.this.adapter.packages.add(addPackage);
                                        DatapointFragment.this.adapter.notifyDataSetChanged();

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
                        searchDialogBuilder.setTitle(getString(R.string.wait));
                        searchDialogBuilder.setNegativeButton(getActivity().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        searchDialog = searchDialogBuilder.show();
                    }
                });
            } catch (Exception e) {
                Log.d(DatapointFragment.class.toString(), "searchForPackages", e);
            }
            return null;
        }
    }
}