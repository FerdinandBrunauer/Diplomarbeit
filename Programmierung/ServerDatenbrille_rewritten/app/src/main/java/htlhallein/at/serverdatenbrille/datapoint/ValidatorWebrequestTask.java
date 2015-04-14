package htlhallein.at.serverdatenbrille.datapoint;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import htlhallein.at.serverdatenbrille.MainActivity;
import htlhallein.at.serverdatenbrille.R;
import htlhallein.at.serverdatenbrille.opendata.OpenDataUtil;

public class ValidatorWebrequestTask extends AsyncTask<String, Void, String> {

    private Dialog progressDialog = new ProgressDialog(MainActivity.getActivity());

    @Override
    protected String doInBackground(String... params) {
        try {
            return OpenDataUtil.getRequestResult(params[0]);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onPreExecute() {
        progressDialog.setTitle(MainActivity.getContext().getString(R.string.webrequestValidatorDialogTitle));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(String s) {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

}