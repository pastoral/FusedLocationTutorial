package com.example.munirulhoque.locationpractice2;

import android.app.ProgressDialog;
import android.os.AsyncTask;

/**
 * Created by munirul.hoque on 10/16/2016.
 */

public class LocationAsyncRunner extends AsyncTask<Void,Void,Void> {

     MainActivity mainActivity = new MainActivity();
    private String resp;
    ProgressDialog progressDialog;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        publishProgress();
        mainActivity.checkLocationSettings();
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}
