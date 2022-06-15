package com.example.falldetectionsystem;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import helper.Action;
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

    MqttHelper mqttHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar homePageToolbar = findViewById(R.id.home_page_toolbar);
        setSupportActionBar(homePageToolbar);

        initViews();
        sharedPreferencesService = new SharedPreferencesService(getApplicationContext());

        String role = sharedPreferencesService.loadRole();
        if(getIntent().getExtras()!=null){
            user = getIntent().getParcelableExtra("User");
        }
        setUserInfo(user);

        if (role.equalsIgnoreCase("Keluarga")) {
            emergencyCallBtn.setVisibility(View.VISIBLE);
        } else if (role.equalsIgnoreCase("Medis")) {
            emergencyCallBtn.setVisibility(View.GONE);
        }

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
                String prediction = jsonObject.getString("prediction");

                String message = "Kondisi pasien: "+prediction;
                if(prediction.contains("jatuh")){
                    patientConditionTv.setTextColor(Color.RED);
                }else {
                    patientConditionTv.setTextColor(Color.BLACK);
                }
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
