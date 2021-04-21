package com.zohaib.smartattandance.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
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
import com.zohaib.smartattandance.R;
import com.zohaib.smartattandance.models.ModelAttendance;
import com.zohaib.smartattandance.models.ModelRollNo;
import com.zohaib.smartattandance.models.ModelStudents;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.paperdb.Paper;

public class ActTakeAttendance extends AppCompatActivity {
    String spreadSheetId;
    String courseCode;
    String courseName;
    public static final Strategy STRATEGY = Strategy.P2P_CLUSTER;
    public String SERVICE_ID = "786";
    String advitiserEndPointName = "initializing";

    TextView tvTotalStudents, tvPresentStudents, tvAbsentStudents, tvDate;
    Button stopLiveSession;
    ArrayList<ModelStudents> allStudentsListOfCourse = new ArrayList<>();
    ArrayList<ModelRollNo> presentStudentsList = new ArrayList<>();
    ArrayList<ModelAttendance> attendanceList = new ArrayList<>();


    private int totalStudents, presentStudents, absentStudents;

    String studentName;
    String studentRoll;
    String studentDeviceId;
    private String date;


    ////////advertiser//////////////


    @Override
    protected void onPause() {
        super.onPause();
        Nearby.getConnectionsClient(ActTakeAttendance.this).stopAdvertising();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAdvertising();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_take_attendance);
        spreadSheetId = getIntent().getStringExtra("spreadSheetId");
        courseCode = getIntent().getStringExtra("courseCode");
        courseName = getIntent().getStringExtra("courseName");


        tvTotalStudents = findViewById(R.id.textView6);
        tvPresentStudents = findViewById(R.id.textView7);
        tvAbsentStudents = findViewById(R.id.textView8);
        stopLiveSession = findViewById(R.id.button3);
        tvDate = findViewById(R.id.textView9);


        allStudentsListOfCourse = Paper.book().read(courseCode + "@STUDENTS");
        totalStudents = allStudentsListOfCourse.size();
        tvTotalStudents.setText(totalStudents + "");
        absentStudents = totalStudents;
        tvAbsentStudents.setText(absentStudents + "");

        //SERVICE_ID = courseCode;
        //advertiserName=courseCode
        advitiserEndPointName = courseCode;
        SERVICE_ID = courseCode;
        presentStudentsList = new ArrayList<ModelRollNo>();

        ///
        date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        tvDate.setText(date);
        Log.d("TAGG", "today's date is " + date);

        stopLiveSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Nearby.getConnectionsClient(ActTakeAttendance.this).stopAdvertising();

               ArrayList<ModelAttendance> previouseAttendance = Paper.book().read(courseCode.toUpperCase() + "@ATTENDANCE", new ArrayList<ModelAttendance>());
               previouseAttendance.add(new ModelAttendance(courseCode, date, presentStudentsList, 0));
                Paper.book().write(courseCode.toUpperCase() + "@ATTENDANCE", previouseAttendance);
                finish();

            }
        });


    }


    /////////////////api /////
    private void startAdvertising() {
        Toast.makeText(this, "Broadcast Starting...", Toast.LENGTH_SHORT).show();
        AdvertisingOptions advertisingOptions = new AdvertisingOptions.Builder().setStrategy(STRATEGY).build();
        Nearby.getConnectionsClient(this).startAdvertising(advitiserEndPointName, SERVICE_ID, new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionInitiated(@NonNull String s, @NonNull ConnectionInfo connectionInfo) {
                Log.d("TAGGG", s.toString() + " connection inititated " + s);
                Log.d("TAGGG", s.toString() + " inoft::: " + connectionInfo.getEndpointName().toString());

                String split[] = connectionInfo.getEndpointName().split("@");
                studentName = split[0];
                studentRoll = split[1];
                studentDeviceId = split[2];
                for (ModelStudents modelStudent : allStudentsListOfCourse) {

                    if (modelStudent.getEndPointName().equals(connectionInfo.getEndpointName())) {

                        Nearby
                                .getConnectionsClient(ActTakeAttendance.this)
                                .acceptConnection(
                                        s, new PayloadCallback() {
                                            @Override
                                            public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
                                                Log.d("TAGGG", s.toString() + " Payload received " + payload.toString().toString());
                                            }

                                            @Override
                                            public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
                                                Log.d("TAGGG", s.toString() + " payload transfer update");
                                            }
                                        }
                                );
                    }


                }


            }

            @Override
            public void onConnectionResult(@NonNull String s, @NonNull ConnectionResolution connectionResolution) {

                Log.d("TAGGG", s.toString() + " connection result " + s);
                switch (connectionResolution.getStatus().getStatusCode()) {
                    case ConnectionsStatusCodes.STATUS_OK:
                        // We're connected! Can now start sending and receiving data.
                        Log.d("TAGGG", s.toString() + " connection result ok " + s);
                        sendPayLoad(s);

                        break;
                    case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                        // The connection was rejected by one or both sides.
                        Log.d("TAGGG", s.toString() + " connection result rejected" + s);
                        break;
                    case ConnectionsStatusCodes.STATUS_ERROR:
                        // The connection broke before it was able to be accepted.
                        Log.d("TAGGG", s.toString() + " connection result err " + s);
                        break;
                    default:
                        // Unknown status code
                }
            }

            @Override
            public void onDisconnected(@NonNull String s) {
                Log.d("TAGGG", s.toString() + " disconnected");
            }

        }, advertisingOptions);

    }

    private void sendPayLoad(final String endPointId) {
        Payload bytesPayload = Payload.fromBytes(String.valueOf("ok").getBytes());
        Nearby.getConnectionsClient(ActTakeAttendance.this)
                .sendPayload(endPointId, bytesPayload).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("TAGGG", " payload sent");
                ///add student to markedAttendance present
                presentStudents++;
                tvPresentStudents.setText(presentStudents + "");
                absentStudents--;
                tvAbsentStudents.setText(absentStudents + "");
                presentStudentsList.add(new ModelRollNo(studentRoll, 1));
                Toast.makeText(ActTakeAttendance.this, studentName + " ::" + studentRoll + ":: Marked Present", Toast.LENGTH_SHORT).show();


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAGGG", " payload send failed " + e.getLocalizedMessage());
            }
        });
    }

    private void startDiscovery() {
        DiscoveryOptions discoveryOptions = new DiscoveryOptions.Builder().setStrategy(STRATEGY).build();
        Nearby.getConnectionsClient(ActTakeAttendance.this).
                startDiscovery(SERVICE_ID, new EndpointDiscoveryCallback() {
                    @Override
                    public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
                        Log.d("TAGGG", endpointId.toString() + " iend point found ");

                        Nearby.getConnectionsClient(ActTakeAttendance.this).
                                requestConnection("Device B", endpointId, new ConnectionLifecycleCallback() {
                                            @Override
                                            public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {

                                                Log.d("TAGGG", endpointId.toString() + " connection inititated d ");
                                                Log.d("TAGGG", endpointId.toString() + " inoft::: d " + connectionInfo.getEndpointName().toString());

                                                Nearby.getConnectionsClient(ActTakeAttendance.this).acceptConnection(endpointId, new PayloadCallback() {
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
                                                        Log.d("TAGGG", s.toString() + " connection result ok d" + s);
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
                                                Log.d("TAGGG", s.toString() + " discnntd   d" + s);

                                            }
                                        }
                                );
                    }

                    @Override
                    public void onEndpointLost(@NonNull String s) {
                        // disconnected
                        Log.d("TAGGG", s.toString() + " end pnt lost " + s);

                    }
                }, discoveryOptions);
    }
}