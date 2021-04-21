package com.zohaib.smartattandance.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.zohaib.smartattandance.AlertDialogManager;
import com.zohaib.smartattandance.R;
import com.zohaib.smartattandance.adapters.AdapterToBeRegisterStudents;
import com.zohaib.smartattandance.adapters.AdapterRegisterdStudents;
import com.zohaib.smartattandance.models.ModelStudents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.vince.owlbottomsheet.OwlBottomSheet;
import io.paperdb.Paper;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class ActRegisterStudents extends AppCompatActivity {
    private OwlBottomSheet mBottomSheet;
    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;


    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS};


    ArrayList<ModelStudents> studentsAlreadyRegistered;
    RecyclerView recyclerView;
    AdapterRegisterdStudents adapterRegisteredStudents;
    AdapterToBeRegisterStudents adapterToBeRegisterStudents;
    int studentBeingRegisterPosition;

    String courseCode;
    String spreadSheetId;
    String courseName;
    String keyToDb;
    ArrayList<ModelStudents> studentsToBeRegister = new ArrayList<>();

    @Override
    public void onBackPressed() {
        mBottomSheet.collapse();
        super.onBackPressed();
    }


    ////////discovery nearby//////

    public static final Strategy STRATEGY = Strategy.P2P_CLUSTER;
    public String SERVICE_ID = "786";
    String discovererEndPointName = "abc";
    ModelStudents newRegisteredStudent;
    LottieAnimationView lottieAnimationView;

    @Override
    protected void onPause() {
        Log.d("TAGGG", "on pause discovery teacher");
        Nearby.getConnectionsClient(ActRegisterStudents.this).stopDiscovery();
        Nearby.getConnectionsClient(ActRegisterStudents.this).stopAllEndpoints();
        super.onPause();
    }

    @Override
    protected void onResume() {
        startDiscovery();
        super.onResume();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_students);
        spreadSheetId = getIntent().getStringExtra("spreadsheeturl");
        courseCode = getIntent().getStringExtra("courseCode");
        courseName = getIntent().getStringExtra("courseName");

        ///////discover endpoint name
        discovererEndPointName = courseCode + "@" + courseName;


        recyclerView = findViewById(R.id.rvStudents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);


        keyToDb = courseCode.toUpperCase() + "@" + "STUDENTS";
        studentsAlreadyRegistered = Paper.book().read(keyToDb, new ArrayList<ModelStudents>());

        adapterRegisteredStudents = new AdapterRegisterdStudents(this, studentsAlreadyRegistered, this, keyToDb);
        recyclerView.setAdapter(adapterRegisteredStudents);
        adapterRegisteredStudents.notifyDataSetChanged();

        mBottomSheet = findViewById(R.id.owl_bottom_sheetStudents);

        setupView();
        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new SwipeToDeleteCallback(adapterRegisteredStudents));
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
        private AdapterRegisterdStudents mAdapter;
        private Drawable icon;
        private final ColorDrawable background;

        public SwipeToDeleteCallback(AdapterRegisterdStudents adapter) {
            super(0, ItemTouchHelper.LEFT);
            mAdapter = adapter;
            icon = ContextCompat.getDrawable(getApplicationContext(),
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

        mBottomSheet.setActivityView((AppCompatActivity) this);

        //icon to show in collapsed sheet
        mBottomSheet.setIcon(R.drawable.ic_baseline_arrow_downward_24);

        //bottom sheet color
        mBottomSheet.setBottomSheetColor(ContextCompat.getColor(this, R.color.design_default_color_primary));

        //view shown in bottom sheet
        mBottomSheet.attachContentView(R.layout.bottom_register_student);
        //getting close button from view shown
        Button btnClose = mBottomSheet.getContentView().findViewById(R.id.bottomSheetClose);

        lottieAnimationView = mBottomSheet.getContentView().findViewById(R.id.lottieAnimationBottomSheet);
        lottieAnimationView.setVisibility(View.VISIBLE);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Nearby.getConnectionsClient(ActRegisterStudents.this).stopDiscovery();
                Nearby.getConnectionsClient(ActRegisterStudents.this).stopAllEndpoints();
                mBottomSheet.collapse();
            }
        });


        RecyclerView recyclerView = mBottomSheet.getContentView().findViewById(R.id.rv_add_students);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ActRegisterStudents.this));
        studentsToBeRegister.clear();


        adapterToBeRegisterStudents = new AdapterToBeRegisterStudents(studentsToBeRegister);
        recyclerView.setAdapter(adapterToBeRegisterStudents);
        adapterToBeRegisterStudents.notifyDataSetChanged();
        adapterToBeRegisterStudents.setIonItemClickListener(new AdapterToBeRegisterStudents.IonItemClickListener() {
            @Override
            public void onItemClick(int position) {
                studentBeingRegisterPosition = position;

                newRegisteredStudent = studentsToBeRegister.get(position);


                /* For Google Sheets */
                mCredential = GoogleAccountCredential.usingOAuth2(
                        ActRegisterStudents.this.getApplicationContext(), Arrays.asList(SCOPES))
                        .setBackOff(new ExponentialBackOff());
//send payload request connection
                requestConnection(newRegisteredStudent.getConnectionEndpoint());


            }
        });


    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private void getResultsFromApi(String rollnumber) {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
            Log.d("Spread", "play services not ");
        } else if (mCredential.getSelectedAccountName() == null) {
            Log.d("Spread", "chosing account ");
            chooseAccount();
        } else if (!isDeviceOnline()) {
            AlertDialogManager _dialog = new AlertDialogManager();
            _dialog.showAlertDialog(this, "No Internet", "To Create a Subject, Turn on Your Internet", false);
        } else {
            new MakeRequestTask1(mCredential, spreadSheetId, rollnumber).execute();
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
                this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = this.getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi(newRegisteredStudent.getRollNo());
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
                    this,
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
                    getResultsFromApi(newRegisteredStudent.getRollNo());
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = this.getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi(newRegisteredStudent.getRollNo());
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    // getResultsFromApi(newRegisteredStudent.getRollNo());
                }
                break;
        }
    }

    ////////////////////////////////////////////////////////////////
    private class MakeRequestTask1 extends AsyncTask<Void, Void, Void> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;
        com.google.api.services.sheets.v4.model.Spreadsheet mSpreadsheet;
        String spreadSheetId;
        String rollno;

        // The constructor
        MakeRequestTask1(GoogleAccountCredential credential, String spreadSheetId, String _rollnumber) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Android spreadsheet client")
                    .build();
            this.spreadSheetId = spreadSheetId;
            this.rollno = _rollnumber;
        }

        protected Void doInBackground(Void... params) {

            // function to create the spreadsheet
            try {
                String range = "Sheet1!A1";
                Log.d("Spread", "Entering Background " + spreadSheetId);
                List<List<Object>> values = Arrays.asList(
                        Arrays.asList(
                                // Cell values ...
                                newRegisteredStudent.getRollNo(),"b","c")
//                        ),Arrays.asList("b")
//                        ,Arrays.asList("c")
//                        ,Arrays.asList("d")
                        // Additional rows ...

                );
                
                ValueRange body = new ValueRange()
                        .setMajorDimension("COLUMNS")
                        .setValues(values);

                AppendValuesResponse result =
                        mService.spreadsheets()

                                .values()
                                .append(spreadSheetId, range, body)

                                .setValueInputOption("RAW")
                                .execute();


                Log.d("updateee", "cell value " + result.getTableRange());
            } catch (IOException e) {
                mLastError = e;
                cancel(true);
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPreExecute() {
            try {
                mProgress = new ProgressDialog(ActRegisterStudents.this);
                mProgress.setMessage("Adding Student");
                mProgress.show();

            } catch (Exception e) {
            }

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {


                mProgress.hide();

                ModelStudents updatedStudent = newRegisteredStudent;
                updatedStudent.setRegistered(true);

                studentsAlreadyRegistered.add(updatedStudent);
                studentsToBeRegister.set(studentBeingRegisterPosition, updatedStudent);
                adapterRegisteredStudents.notifyDataSetChanged();
                adapterToBeRegisterStudents.notifyDataSetChanged();
                Paper.book().write(keyToDb, studentsAlreadyRegistered);

            } catch (Exception e) {
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


    /////////////////////discovering//////////
    private void startDiscovery() {
        Toast.makeText(this, "Discovery Started", Toast.LENGTH_SHORT).show();
        DiscoveryOptions discoveryOptions = new DiscoveryOptions.Builder().setStrategy(STRATEGY).build();
        Nearby.getConnectionsClient(ActRegisterStudents.this).
                startDiscovery(SERVICE_ID, new EndpointDiscoveryCallback() {
                    @Override
                    public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
                        Log.d("TAGGG", discoveredEndpointInfo.getEndpointName().toString() + " iend point found ");

                        String endPointName = discoveredEndpointInfo.getEndpointName();
                        String[] splits = endPointName.split("@");
                        String discoveredName = splits[0];
                        String discoveredRoll = splits[1];
                        String discoveredDeviceId = splits[2];

                        Log.d("TAGGG", discoveredName + "  ::name");
                        Log.d("TAGGG", discoveredRoll + "  ::roll");
                        Log.d("TAGGG", discoveredDeviceId + "  ::deviceId");
                        Log.d("TAGGG", studentsAlreadyRegistered.size() + "  :");
                        Log.d("TAGGG", studentsToBeRegister.size() + "  :");


                        if (studentsAlreadyRegistered.size() == 0) {
                            studentsToBeRegister.add(new ModelStudents(discoveredName.toUpperCase(), discoveredRoll.toUpperCase(), discoveredDeviceId, false, endpointId, endPointName,"A1"));
                            adapterToBeRegisterStudents.notifyDataSetChanged();
                            lottieAnimationView.setVisibility(View.GONE);
                        } else {
                            for (ModelStudents modelStudent : studentsAlreadyRegistered) {
                                Log.d("TAGGG", modelStudent.getRollNo() + "  ::rollAlreadyReg");
                                if (!(modelStudent.getRollNo().equals(discoveredRoll))) {
                                    studentsToBeRegister.add(new ModelStudents(discoveredName.toUpperCase(), discoveredRoll.toUpperCase(), discoveredDeviceId, false, endpointId, endPointName,"A1"));
                                    adapterToBeRegisterStudents.notifyDataSetChanged();
                                    lottieAnimationView.setVisibility(View.GONE);
                                }
                            }
                        }


                    }

                    @Override
                    public void onEndpointLost(@NonNull String s) {
                        // disconnected
                        Log.d("TAGGG", s.toString() + " end pnt lost " + s);

                    }
                }, discoveryOptions);
    }


    private void requestConnection(String endPointId) {

        Nearby.getConnectionsClient(ActRegisterStudents.this).
                requestConnection(discovererEndPointName, endPointId, new ConnectionLifecycleCallback() {
                            @Override
                            public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {

                                Log.d("TAGGG", endpointId.toString() + " connection inititated d ");
                                Log.d("TAGGG", endpointId.toString() + " inoft::: d " + connectionInfo.getEndpointName().toString());

                                Nearby.getConnectionsClient(ActRegisterStudents.this).acceptConnection(endpointId, new PayloadCallback() {
                                    @Override
                                    public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
                                        Log.d("TAGGG", s.toString() + " Payload received discovery " + payload.toString());

                                    }

                                    @Override
                                    public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
                                        Log.d("TAGGG", s.toString() + " payload transfer update discover");

                                    }
                                });
                            }

                            @Override
                            public void onConnectionResult(@NonNull String s, @NonNull ConnectionResolution connectionResolution) {
                                switch (connectionResolution.getStatus().getStatusCode()) {
                                    case ConnectionsStatusCodes.STATUS_OK:
                                        // We're connected! Can now start sending and receiving data.
                                        Log.d("TAGGG", s.toString() + " connection result ok d ");

                                        sendPayLoad(s);
                                        break;
                                    case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                                        // The connection was rejected by one or both sides.
                                        Log.d("TAGGG", s.toString() + " connection result rejected d" + s);
                                        break;
                                    case ConnectionsStatusCodes.STATUS_ERROR:
                                        // The connection broke before it was able to be accepted.
                                        Log.d("TAGGG", s.toString() + " connection result err d" + s);
                                        break;
                                    default:
                                        // Unknown status code
                                        Log.d("TAGGG", s.toString() + " connection result unknwn d" + s);

                                }
                            }

                            @Override
                            public void onDisconnected(@NonNull String s) {
                                Log.d("TAGGG", s.toString() + " discnntd  ");

                            }
                        }
                );
    }


    private void sendPayLoad(final String endPointId) {
        Payload bytesPayload = Payload.fromBytes(String.valueOf("ok").getBytes());
        Nearby.getConnectionsClient(ActRegisterStudents.this).sendPayload(endPointId, bytesPayload).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                getResultsFromApi(newRegisteredStudent.getRollNo());
                Log.d("TAGGG", " payload sent");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAGGG", " payload send failed " + e.getLocalizedMessage());
            }
        });
    }
}