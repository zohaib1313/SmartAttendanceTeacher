package com.zohaib.smartattandance.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import io.paperdb.Paper;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;

import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.sheets.v4.SheetsScopes;

import com.google.api.services.sheets.v4.model.*;
import com.zohaib.smartattandance.AlertDialogManager;
import com.zohaib.smartattandance.R;
import com.zohaib.smartattandance.adapters.AdapterNotSyncedAttendance;
import com.zohaib.smartattandance.models.ModelAttendance;
import com.zohaib.smartattandance.models.ModelCourses;
import com.zohaib.smartattandance.models.ModelRollNo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.*;


public class ActViewCourseDetails extends AppCompatActivity {
    Button btngooglesheets;

    MaterialButton btnTakeAttendance;

    String network;

    ArrayList<ModelCourses> _dbCourse;
    ArrayList<ModelAttendance> _dbAttendance;
    /* For Google Sheets */
    GoogleAccountCredential mCredential;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS};
    AdapterNotSyncedAttendance adapterNotSyncedAttendance;
    ArrayList<ModelAttendance> notSycAttendance = new ArrayList<>();
    ProgressDialog mProgress;
    String updateDate;
    String courseCode;
    String courseName;
    String spreadSheetOfCourseId;
    ArrayList<String> updateAttendance;

    int updateIndex;

    /* For Google Sheets */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_view_course_details);

        if (!(getIntent().getStringExtra("courseName")).isEmpty()) {
            this.courseName = getIntent().getStringExtra("courseName");
            this.courseCode = getIntent().getStringExtra("courseCode");
            this.spreadSheetOfCourseId = getIntent().getStringExtra("spreadSheetId");
        }


        btnTakeAttendance = findViewById(R.id.btnTakeAttendence);

        _dbCourse = Paper.book().read("COURSES", new ArrayList<ModelCourses>());
        _dbAttendance = Paper.book().read(courseCode.toUpperCase() + "@ATTENDANCE", new ArrayList<ModelAttendance>());
        Log.d("kjkj", _dbAttendance.size() + " attendance size");

        Log.d("kjkj", courseCode + " course code");


        adapterNotSyncedAttendance = new AdapterNotSyncedAttendance(notSycAttendance);
        RecyclerView rvNotSynced = findViewById(R.id.rvNotSyncedAttendance);
        rvNotSynced.setLayoutManager(new LinearLayoutManager(this));
        rvNotSynced.setHasFixedSize(true);
        rvNotSynced.setAdapter(adapterNotSyncedAttendance);
        adapterNotSyncedAttendance.setiOnItemClickListener(new AdapterNotSyncedAttendance.IOnItemClickListener() {
            @Override
            public void onItemClick(ModelAttendance modelAttendance, int position) {
                getResultsFromApi();
            }
        });

        getNotSyncedDataList();
        /* Google Sheets */
        updateAttendance = new ArrayList<String>();
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());


        btngooglesheets = findViewById(R.id.googlesheets);
        btngooglesheets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<ModelCourses> _dbCourse = Paper.book().read("Courses", new ArrayList<ModelCourses>());
                String spreadsheeturl = "https://docs.google.com/spreadsheet/";
                for (ModelCourses ele : _dbCourse) {
                    if (ele.getId().equals(courseCode)) {
                        spreadsheeturl = "https://docs.google.com/spreadsheets/d/" + ele.getSpreadSheetId() + "/edit#gid=0";
                    }
                }
                Log.d("Spread", "sprdurl=" + spreadsheeturl);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(spreadsheeturl));
                startActivity(browserIntent);

            }
        });
        /* Google Sheets */

    }

    private void getNotSyncedDataList() {
        notSycAttendance.clear();
        for (ModelAttendance modelAttendance : _dbAttendance) {
            if (modelAttendance.courseId.equals(courseCode) && modelAttendance.isSynced == 0) {
                notSycAttendance.add(modelAttendance);
            }
        }
        adapterNotSyncedAttendance.notifyDataSetChanged();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    ///button click listener
    public void onClickBtnTakeAttendance(View view) {


        Intent intent = new Intent(ActViewCourseDetails.this, ActTakeAttendance.class);
        if (courseCode != null) {
            for (ModelCourses ele : _dbCourse) {
                if (ele.getId().equals(courseCode)) {
                    intent.putExtra("courseName", courseName);

                    intent.putExtra("courseCode", courseCode);
                    intent.putExtra("spreadSheetId", ele.getSpreadSheetId());
                    startActivity(intent);
                }
            }

        }


    }

    public void onClickBtnStudents(View view) {
        Intent intent = new Intent(ActViewCourseDetails.this, ActRegisterStudents.class);

        if (courseCode != null) {

            for (ModelCourses ele : _dbCourse) {
                if (ele.getId().equals(courseCode)) {
                    intent.putExtra("courseName", courseName);
                    intent.putExtra("spreadsheeturl", ele.getSpreadSheetId());
                    intent.putExtra("courseCode", courseCode);
                    startActivityForResult(intent, 2);
                }
            }
        }

    }

    /*For Google Sheets */

    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            AlertDialogManager _dialog = new AlertDialogManager();
            _dialog.showAlertDialog(this, "No Internet", "To Create a Subject, Turn on Your Internet", false);
            Log.d("Spread", "No network connection available.");
        } else {
            new MakeRequestTask(mCredential, "Googleapp").execute();
            Log.d("Spread", "Successsss ");
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                ActViewCourseDetails.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                Log.d("Spread", "chooseAccount:");
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
                Log.d("Spread", "chooseAccount: ::");
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Log.d("Spread", "This app requires Google Play Services. Please install " +
                            "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;

        }
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }


    private class MakeRequestTask extends AsyncTask<Void, Void, Void> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;

        // The constructor
        MakeRequestTask(GoogleAccountCredential credential, String title) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Android spreadsheet client")
                    .build();
        }

        protected Void doInBackground(Void... params) {

            // function to create the spreadsheet
            String spreadsheetId = spreadSheetOfCourseId;
            String range = "Sheet1";

            return null;
        }



        @Override
        protected void onPreExecute() {

            try {
                mProgress = new ProgressDialog(ActViewCourseDetails.this);
                mProgress.setMessage("Updating Attendance");
                mProgress.show();

            } catch (Exception ignored) {
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mProgress.hide();

        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            Log.d("Spread", "onCancelled: ");
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            ActViewCourseDetails.REQUEST_AUTHORIZATION);
                } else {
                    Log.d("Spread", "The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                Log.d("Spread", "Request cancelled.");
            }
        }
    }
    /* For  Google Sheets */
}