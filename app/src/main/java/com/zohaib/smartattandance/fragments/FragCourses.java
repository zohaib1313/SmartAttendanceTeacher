package com.zohaib.smartattandance.fragments;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.zohaib.smartattandance.AlertDialogManager;
import com.zohaib.smartattandance.R;
import com.zohaib.smartattandance.adapters.AdapterRvCourses;
import com.zohaib.smartattandance.models.ModelCourses;
import com.zohaib.smartattandance.models.ModelRollNo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.vince.owlbottomsheet.OwlBottomSheet;
import io.paperdb.Paper;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.app.Activity.RESULT_OK;


public class FragCourses extends Fragment {


    private OwlBottomSheet mBottomSheet;
    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS};

    ArrayList<String> modelRollNos = new ArrayList<>();
    ModelCourses modelCourses;


    ArrayList<ModelCourses> _dbCourse;
    RecyclerView recyclerView;
    AdapterRvCourses adapterRvCourses;

    String keyToDbCourses;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_frag_courses, container, false);
        mBottomSheet = view.findViewById(R.id.owl_bottom_sheet);
        setupView();
        keyToDbCourses = "COURSES";
        _dbCourse = Paper.book().read(keyToDbCourses, new ArrayList<ModelCourses>());
        Log.d("sizeeeee", _dbCourse.size() + " ");
        recyclerView = view.findViewById(R.id.rvCoursesFrag);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        adapterRvCourses = new AdapterRvCourses(getContext().getApplicationContext(), _dbCourse, keyToDbCourses, getActivity());
        recyclerView.setAdapter(adapterRvCourses);
        adapterRvCourses.notifyDataSetChanged();
        adapterRvCourses.setiOnItemClickListener(new AdapterRvCourses.IOnItemClickListener() {
            @Override
            public void onItemClick(ModelCourses modelCourse) {
                Log.d("modelll", modelCourse.getId() + "");
                Log.d("modelll", modelCourse.getName() + "");
                Log.d("modelll", modelCourse.getSpreadSheetId() + "");
                Log.d("modelll", modelCourse.getModelRollNos().size() + "");
            }
        });
        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new FragCourses.SwipeToDeleteCallback(adapterRvCourses));
        itemTouchHelper.attachToRecyclerView(recyclerView);
        return view;
    }

    public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
        private AdapterRvCourses mAdapter;
        private Drawable icon;
        private final ColorDrawable background;

        public SwipeToDeleteCallback(AdapterRvCourses adapter) {
            super(0, ItemTouchHelper.LEFT);
            mAdapter = adapter;
            icon = ContextCompat.getDrawable(getContext().getApplicationContext(),
                    R.drawable.ic_baseline_delete_24);
            background = new ColorDrawable(Color.RED);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            mAdapter.deleteItem(position);
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX,
                    dY, actionState, isCurrentlyActive);

            View itemView = viewHolder.itemView;
            int backgroundCornerOffset = 20;

            int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
            int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
            int iconBottom = iconTop + icon.getIntrinsicHeight();

            if (dX > 0) { // Swiping to the right
                background.setBounds(itemView.getLeft(), itemView.getTop(),
                        itemView.getLeft() + ((int) dX) + backgroundCornerOffset,
                        itemView.getBottom());

            } else if (dX < 0) { // Swiping to the left
                int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
                int iconRight = itemView.getRight() - iconMargin;
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                        itemView.getTop(), itemView.getRight(), itemView.getBottom());
            } else { // view is unSwiped
                background.setBounds(0, 0, 0, 0);
            }
            background.draw(c);
            icon.draw(c);
        }
    }

    private void setupView() {

        mBottomSheet.setActivityView((AppCompatActivity) getActivity());

        //icon to show in collapsed sheet
        mBottomSheet.setIcon(R.drawable.ic_baseline_arrow_downward_24);

        //bottom sheet color
        mBottomSheet.setBottomSheetColor(ContextCompat.getColor(getContext(), R.color.design_default_color_primary));

        //view shown in bottom sheet
        mBottomSheet.attachContentView(R.layout.bottom_sheet_add_course);

        //getting close button from view shown
        mBottomSheet.getContentView().findViewById(R.id.button2)
                .setOnClickListener(v -> mBottomSheet.collapse());

        EditText etCourseTitle, etCourseCode;
        Button btnSubmit;

        etCourseTitle = mBottomSheet.getContentView().findViewById(R.id.editTextTextPersonName);
        etCourseCode = mBottomSheet.getContentView().findViewById(R.id.textView);
        btnSubmit = mBottomSheet.getContentView().findViewById(R.id.button);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etCourseCode.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Enter course Code", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (etCourseTitle.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Enter course Title", Toast.LENGTH_SHORT).show();
                    return;
                }


                ArrayList<ModelRollNo> emptyModelRollNos = new ArrayList<>();

                modelCourses = new ModelCourses(etCourseCode.getText().toString().toUpperCase(),
                        etCourseTitle.getText().toString().toUpperCase(),
                        emptyModelRollNos
                );



                /* For Google Sheets */
                mCredential = GoogleAccountCredential.usingOAuth2(
                        getContext().getApplicationContext(), Arrays.asList(SCOPES))
                        .setBackOff(new ExponentialBackOff());

                getResultsFromApi(modelRollNos);
                etCourseTitle.setText("");
                etCourseCode.setText("");
                mBottomSheet.collapse();
            }
        });


    }


    private boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private void getResultsFromApi(ArrayList<String> _rollnumbers) {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
            Log.d("Spread", "play services not ");
        } else if (mCredential.getSelectedAccountName() == null) {
            Log.d("Spread", "chosing account ");
            chooseAccount();
        } else if (!isDeviceOnline()) {
            AlertDialogManager _dialog = new AlertDialogManager();
            _dialog.showAlertDialog(getContext(), "No Internet", "To Create a Subject, Turn on Your Internet", false);
        } else {
            new MakeRequestTask1(mCredential, modelCourses.getName(), _rollnumbers).execute();
            Log.d("Spread", "Successsss ");


        }
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(getContext());
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(getContext());
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                getActivity(),
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                getContext(), Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getActivity().getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi(modelRollNos);
            } else {
                // Start a dialog from which the user can choose an account
                Log.d("Spread", "chooseAccount:");
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
                Log.d("Spread", "chooseAccount: ::");
            }
        } else {
            Log.d("Spread", "requesting permission: ::");
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    getActivity(),
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Log.d("Spread", "This app requires Google Play Services. Please install " +
                            "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi(modelRollNos);
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi(modelRollNos);
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi(modelRollNos);
                }
                break;
        }
    }

    ////////////////////////////////////////////////////////////////
    private class MakeRequestTask1 extends AsyncTask<Void, Void, Void> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;
        com.google.api.services.sheets.v4.model.Spreadsheet mSpreadsheet;
        String title;
        ArrayList<String> rollno;

        // The constructor
        MakeRequestTask1(GoogleAccountCredential credential, String title, ArrayList<String> _rollnumber) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Android spreadsheet client")
                    .build();
            this.title = title;
            this.rollno = _rollnumber;
        }

        protected Void doInBackground(Void... params) {

            // function to create the spreadsheet
            try {
                Log.d("Spread", "Entering Background");
                createSpreadSheet(title);
            } catch (IOException e) {
                mLastError = e;
                cancel(true);
                e.printStackTrace();
            }

            return null;
        }

        // creates a new spreadsheet
        private void createSpreadSheet(String title) throws IOException {
            com.google.api.services.sheets.v4.model.Spreadsheet newSpreadSheet;
            mSpreadsheet = new Spreadsheet();
            SpreadsheetProperties spreadsheetProperties = new SpreadsheetProperties();
            spreadsheetProperties.setTitle(title);// name of your spreadsheet
            mSpreadsheet = mSpreadsheet.setProperties(spreadsheetProperties);
            newSpreadSheet = mService.spreadsheets()
                    .create(mSpreadsheet)
                    .execute();

            Log.d("Spread", "SpreadSheetID: " + newSpreadSheet.getSpreadsheetId());
            // this 'newSpreadsheet' is ready to use for write/read operation.

            //adding roll numbers


            String spreadsheetId = newSpreadSheet.getSpreadsheetId();
//            thisCourse.setSpreadSheetId(spreadsheetId);
            modelCourses.setSpreadSheetId(spreadsheetId);
            _dbCourse.add(modelCourses);
            String range = "Sheet1!A1:A";
            List<List<Object>> values = Arrays.asList(
                    Arrays.asList(
                            "Roll No"
                    )
            );

            ValueRange valueRange = new ValueRange();
            valueRange.setValues(values)
                    .setMajorDimension("COLUMNS");

            UpdateValuesResponse response = this.mService.spreadsheets()
                    .values().update(spreadsheetId, range, valueRange)
                    .setValueInputOption("RAW")
                    .execute();
            Log.d("updateee", response.getUpdatedRange() + " =is range");

            //
//            List<List<Object>> values = Arrays.asList(
//                    Arrays.asList(
//                            // Cell values ...
//                            "hi"
//                    ),
//                    Arrays.asList(
//                            // Cell values ...
//                            "hi"
//                    ),
//                    Arrays.asList(
//                            // Cell values ...
//                            "hi"
//                    )
//                    // Additional rows ...
//            );
//            ValueRange body = new ValueRange()
//                    .setValues(values);
//            List<String> results = new ArrayList<String>();
//            UpdateValuesResponse response = this.mService.spreadsheets().values()
//                    .update(spreadsheetId, range, body)
//                    .setValueInputOption("RAW")
//                    .execute();
//            Log.d("Spread", spreadsheetId + "for adding "+response.values());
//            results.add(response.getUpdatedCells().toString());
        }


        @Override
        protected void onPreExecute() {


            try {
                mProgress = new ProgressDialog(getContext());
                mProgress.setMessage("Creating Google Sheet");
                mProgress.show();
            } catch (Exception ignored) {
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                adapterRvCourses.notifyDataSetChanged();
                mProgress.hide();
                Paper.book().write(keyToDbCourses, _dbCourse);

            } catch (Exception ignored) {
            }


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
                            REQUEST_AUTHORIZATION);
                } else {
                    Log.d("Spread", "The following error occurred:\n"
                            + mLastError.getLocalizedMessage());
                }
            } else {
                Log.d("Spread", "Request cancelled.");
            }
        }
    }
}