package htlhallein.at.serverdatenbrille;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CountDownLatch;

import htlhallein.at.serverdatenbrille.database.DatabaseHelper;
import htlhallein.at.serverdatenbrille.memoryObjects.DataPackage;
import htlhallein.at.serverdatenbrille.memoryObjects.OpenDataResource;
import htlhallein.at.serverdatenbrille.opendata.OpenDataUtil;
import htlhallein.at.serverdatenbrille.opendata.PackageCrawler;
import yuku.ambilwarna.AmbilWarnaDialog;

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
                                        final String name = tvName.getText().toString();
                                        final String openDataID = tvKey.getText().toString();
                                        final long timestamp = System.currentTimeMillis();
                                        AmbilWarnaDialog.OnAmbilWarnaListener listener = new AmbilWarnaDialog.OnAmbilWarnaListener() {
                                            @Override
                                            public void onCancel(AmbilWarnaDialog dialog) {

                                            }

                                            @Override
                                            public void onOk(AmbilWarnaDialog dialog, int color) {
                                                long packageID = DatabaseHelper.addPackage(openDataID, "", name, timestamp,color);
                                                DataPackage addPackage = new DataPackage(packageID, name, openDataID, false, timestamp,color, 1);
                                                packages.add(addPackage);
                                                notifyDataSetChanged();
                                                Toast.makeText(context, "Paket erfolgreich hinzugefügt!", Toast.LENGTH_LONG).show();
                                            }
                                        };

                                        AmbilWarnaDialog ambilWarnaDialog = new AmbilWarnaDialog(MainActivity.getContext(), 0xffffffff, listener);
                                        ambilWarnaDialog.show();

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

        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater mInflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(R.layout.fragment_datapoints_listviewrow, parent, false);
            }

            LinearLayout ll = (LinearLayout) convertView.findViewById(R.id.textlayout);
            CheckBox tvDisplayed = (CheckBox) convertView.findViewById(R.id.tvCheckBox);
            TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
            TextView tvKey = (TextView) convertView.findViewById(R.id.tvKey);
            final ImageView tvImage = (ImageView) convertView.findViewById(R.id.tvImage);

            final DataPackage actualPackage = packages.get(position);

            ll.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setTitle(context.getString(R.string.delete_Package));
                    dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DatabaseHelper.deletePackage(packages.get(position).getId());
                            packages.remove(position);
                            notifyDataSetChanged();
                        }
                    });
                    dialog.setNegativeButton("abbrechen", null);
                    dialog.show();
                    return true;
                }
            });

            if(actualPackage.getDisplayed() == 1){
                tvDisplayed.setChecked(true);
            }else{
                tvDisplayed.setChecked(false);
            }

            tvName.setText(actualPackage.getName());
            tvKey.setText(actualPackage.getIdOpenData());
            tvImage.setBackgroundColor(actualPackage.getColor());
            tvImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AmbilWarnaDialog.OnAmbilWarnaListener listener = new AmbilWarnaDialog.OnAmbilWarnaListener() {
                        @Override
                        public void onCancel(AmbilWarnaDialog dialog) {

                        }

                        @Override
                        public void onOk(AmbilWarnaDialog dialog, int color) {
                            DatabaseHelper.editPackageColor(packages.get(position).getId(), color);
                            packages = DatabaseHelper.getDataPackages();
                            tvImage.setBackgroundColor(color);
                        }
                    };

                    AmbilWarnaDialog ambilWarnaDialog = new AmbilWarnaDialog(MainActivity.getContext(), packages.get(position).getColor(), listener);
                    ambilWarnaDialog.show();
                }
            });

            tvDisplayed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        DatabaseHelper.editPackageDispalyed(actualPackage.getId(), 1);
                    }else{
                        DatabaseHelper.editPackageDispalyed(actualPackage.getId(), 0);
                    }
                    packages = DatabaseHelper.getDataPackages();
                }
            });
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
        private ProgressDialog dialog = new ProgressDialog(getActivity());
        private boolean isRunning = true;

        @Override
        protected String doInBackground(String... params) {
            try {
                List<List<String>> foundPackages = OpenDataUtil.searchForPackages(params[0]);

                if(!isRunning){
                    return "";
                }
                TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
                TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);

                final ScrollView scrollView = new ScrollView(getActivity());
                final TableLayout tableLayout = new TableLayout(getActivity());
                tableLayout.setLayoutParams(tableParams);
                scrollView.addView(tableLayout);
                for (final List<String> packages : foundPackages) {
                    TableRow row = new TableRow(getActivity());
                    row.setLayoutParams(rowParams);
                    row.setPadding(5, 5, 5, 5);
                    row.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d("Package added", "Name: \"" + packages.get(0) + "\", Key: \"" + packages.get(1) + "\"");
                            if (!packages.get(0).equals("")) {
                                if (!packages.get(1).equals("")) {
                                    try {
                                        Dialog dialog = new ProgressDialog(getActivity());
                                        dialog.setTitle(MainActivity.getContext().getString(R.string.wait));
                                        dialog.setCancelable(false);
                                        dialog.setCanceledOnTouchOutside(false);
                                        dialog.show();
                                        final String name = packages.get(0);
                                        final String openDataID = packages.get(1);
                                        final CountDownLatch latch = new CountDownLatch(1);
                                        final List<OpenDataResource> openDataResources = new ArrayList<>();
                                        Thread searchThread = new HandlerThread("searchThread"){
                                            @Override
                                            public void run(){
                                                for(OpenDataResource res:OpenDataUtil.getPackageById(openDataID).getResources()){
                                                    openDataResources.add(res);
                                                }
                                                latch.countDown();
                                            }
                                        };
                                        searchThread.start();
                                        latch.await();

                                        long timestamp = 0;
                                        for (OpenDataResource openDataResource : openDataResources) {
                                            for(String format:OpenDataUtil.supportedFiles) {
                                                if (openDataResource.getFormat().toUpperCase().equals(format)) {
                                                    timestamp = openDataResource.getCreationTimestamp();
                                                }
                                            }
                                        }

                                        final long finalTimestamp = timestamp;

                                        AmbilWarnaDialog.OnAmbilWarnaListener listener = new AmbilWarnaDialog.OnAmbilWarnaListener() {
                                            @Override
                                            public void onCancel(AmbilWarnaDialog dialog) {

                                            }

                                            @Override
                                            public void onOk(AmbilWarnaDialog dialog, int color) {
                                                long packageID = DatabaseHelper.addPackage(openDataID, "", name, finalTimestamp,color);
                                                DataPackage addPackage = new DataPackage(packageID, name, openDataID, false, finalTimestamp,color,1);

                                                new PackageCrawler().execute(addPackage.getIdOpenData());
                                                DatapointFragment.this.adapter.packages.add(addPackage);
                                                DatapointFragment.this.adapter.notifyDataSetChanged();
                                            }
                                        };

                                        if (dialog.isShowing())
                                            dialog.dismiss();
                                        AmbilWarnaDialog ambilWarnaDialog = new AmbilWarnaDialog(MainActivity.getContext(), 0xffffffff, listener);
                                        ambilWarnaDialog.show();
                                    } catch (Exception e) {
                                        Toast.makeText(getActivity(), "Fehler beim hinzufügen des Paketes", Toast.LENGTH_LONG).show();
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
                    nameView.setPadding(5, 10, 5, 10);

                    row.addView(nameView);

                    tableLayout.addView(row);
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        AlertDialog.Builder searchDialogBuilder = new AlertDialog.Builder(getActivity());
                        searchDialogBuilder.setView(scrollView);
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

        @Override
        protected void onPreExecute() {
            dialog.setTitle(MainActivity.getContext().getString(R.string.wait));
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setButton(DialogInterface.BUTTON_NEUTRAL, MainActivity.getContext().getString(R.string.cancel), (DialogInterface.OnClickListener) null);
            dialog.show();
            final Button dialogButton = dialog.getButton( DialogInterface.BUTTON_NEUTRAL );
            dialogButton.setOnClickListener( new View.OnClickListener() {

                @Override
                public void onClick ( View view ) {
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.setTitle(MainActivity.getContext().getString(R.string.package_searcher_cancel));
                    dialog.setIndeterminate(true);
                    isRunning = false;
                }

            });
        }

        @Override
        protected void onPostExecute(String s) {
            if (dialog.isShowing())
                dialog.dismiss();
        }
    }
}