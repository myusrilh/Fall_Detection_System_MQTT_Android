package com.example.falldetectionsystem;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import helper.Action;
import helper.DatabaseHelper;
import helper.Location;
import helper.MqttHelper;
import helper.Pair;
import helper.Request;
import helper.User;
import service.SharedPreferencesService;

public class HomeActivity extends AppCompatActivity implements Action {

    // Declare TextView
    TextView patientConditionTv;
    TextView usernameHomeTv;
    TextView patientNameTv;
    TextView patientAddressTv;

    ConstraintLayout constraintLayout;

    // Declare Button
    Button emergencyCallBtn;

    //Declare ImageButton
    ImageButton backArrowHomePage;

    MapView map;

    User user;

    SharedPreferencesService sharedPreferencesService;
    DatabaseHelper databaseHelper;
    MqttHelper mqttHelper;
    String CHANNEL_ID = "";
    String role = "";

    int countJatuh = 0;
    boolean jatuh = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar homePageToolbar = findViewById(R.id.home_page_toolbar);
        setSupportActionBar(homePageToolbar);

        initViews();
        sharedPreferencesService = new SharedPreferencesService(getApplicationContext());

        role = sharedPreferencesService.loadRole();
        if(getIntent().getExtras().getString("patientName") == null){
            user = getIntent().getParcelableExtra("User");
        }else{
            user = databaseHelper.getUserRole(getIntent().getExtras().getString("patientName"));
        }
        setUserInfo(user);

        if (role.equalsIgnoreCase("Keluarga")) {
            emergencyCallBtn.setVisibility(View.VISIBLE);
        } else if (role.equalsIgnoreCase("Medis")) {
            emergencyCallBtn.setVisibility(View.GONE);
        }

        CHANNEL_ID = "FallNotification"+user.getId();

        backArrowHomePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        emergencyCallBtn.setOnClickListener(v -> {
            try{
                onCall();
            }catch (Exception ex){
                Log.d("ACTION_CALL",ex.toString());
            }
        });


        //OSM Maps
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        map = findViewById(R.id.map);
        map.setMultiTouchControls(true);

        setMapsLocation(user, map);
        startMqtt();
    }

    public void onCall() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    123);
        } else {
            String phoneNumber = "085738242282";
            startActivity(new Intent(Intent.ACTION_CALL).setData(Uri.parse("tel:"+phoneNumber)));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                onCall();
            } else {
                Log.d("TAG", "Call Permission Not Granted");
            }
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "fall detection system";
            String description = "Detecting Fall";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    void showNotification(String message){
        createNotificationChannel();
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("patientName",user.getPatientname());
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        String place = user.getStreet()+"+"+user.getCity()+"+"+user.getCountry()+"+"+user.getPostalCode();
        Uri gmmIntentUri = Uri.parse("google.navigation:q="+place);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        this.startActivity(mapIntent);

        PendingIntent pendingIntent;
        PendingIntent mapPendingIntent;

        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        mapPendingIntent = PendingIntent.getActivity(this, 0, mapIntent, PendingIntent.FLAG_IMMUTABLE);


        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_fall_people)
                .setContentTitle("Fall Detection System")
                .setContentText(user.getPatientname()+" "+message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setAutoCancel(true)
                .setSound(alarmSound);
        if(role.equalsIgnoreCase("Keluarga")){
            builder.setContentIntent(pendingIntent)
                    .addAction(org.osmdroid.library.R.drawable.osm_ic_follow_me, "Go To Maps",mapPendingIntent);
        }else{
            builder.setContentIntent(mapPendingIntent);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(user.getId(), builder.build());
    }

    private void startMqtt(){
        mqttHelper = new MqttHelper(getApplicationContext());
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Debug",mqttMessage.toString());

                JSONObject jsonObject = new JSONObject(String.valueOf(mqttMessage));
                int prediction = Integer.parseInt(jsonObject.getString("prediction"));

                String message = "Kondisi pasien: ";
                String kondisi = "Beraktivitas normal";

                if(prediction == 1){
                    jatuh = true;
                    countJatuh += 1;
                }else {
                    patientConditionTv.setTextColor(Color.BLACK);

                    if(prediction == 0) {
                        countJatuh = 0;
                        kondisi = "Beraktivitas normal";
                    }

                    if(prediction == 2 && !jatuh){
                        kondisi = "Posisi di lantai";
                        showNotification(kondisi);
                        countJatuh = 0;
                    }

                }

                if (countJatuh >= 6 && jatuh){
                    kondisi = "Terjatuh!";

                    patientConditionTv.setTextColor(Color.RED);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        showNotification(kondisi);
                        countJatuh = 0;
                    }
                }
                message += kondisi;
                patientConditionTv.setText(message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

    private void initViews() {
        backArrowHomePage = findViewById(R.id.back_arrow_home_page);
        emergencyCallBtn = findViewById(R.id.emergency_call);
        patientConditionTv = findViewById(R.id.patient_condition_text);
        usernameHomeTv = findViewById(R.id.username_home);
        patientNameTv = findViewById(R.id.patient_name_home);
        patientAddressTv = findViewById(R.id.patient_address_home_tv);

        constraintLayout = findViewById(R.id.constraint_layout_home);
    }


    private void setUserInfo(User user){
        String username = "Nama Wali: "+user.getName();
        String patientName = "Nama Pasien: "+user.getPatientname();
        String address = user.getStreet()+", "+user.getCity()+", "+user.getCountry()+", "+user.getPostalCode();

        usernameHomeTv.setText(username);
        patientNameTv.setText(patientName);
        patientAddressTv.setText(address);
    }


    private void setMapsLocation(User user, MapView map){

        if(user.getStreet().contains(" ")) {
            String[] us = user.getStreet().split(" ");
            StringBuilder usr = new StringBuilder();
            for (String u : us) {
                usr.append("+").append(u);
            }
            user.setStreet(usr.toString());
        }
        Toast.makeText(getApplicationContext(),"Alamat: "+user.getStreet(),Toast.LENGTH_LONG).show();

        String address = user.getStreet()+"+"+user.getCity()+"+"+user.getCountry();
        Request.getPlaces(e -> Toast.makeText(HomeActivity.this, e.toString(), Toast.LENGTH_SHORT).show()
                , map
                , user
                , new ArrayList<Pair>(){
                    {
                        Pair p = new Pair("q",address);
                        add(p);
                    }
                });

    }


    @Override
    protected void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    protected void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }


    @Override
    public void action(Object e) {

    }
}
