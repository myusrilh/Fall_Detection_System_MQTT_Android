package com.example.falldetectionsystem;

import adapters.PatientRecyclerAdapter;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import helper.DatabaseHelper;
import helper.Request;
import helper.User;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PatientListActivity extends AppCompatActivity {

    private AppCompatActivity activity = PatientListActivity.this;

    //Declare TextView
    private TextView medicNameTv;
    private TextView medicAddressTv;

    //Declare RecyclerView
    private RecyclerView recyclerViewPatient;
    //Declare List
    private List<User> patientList;
    //Declare PatientRecyclerAdapter
    private PatientRecyclerAdapter patientRecyclerAdapter;
    //Declare DatabaseHelper
    private DatabaseHelper databaseHelper;

    //Declare ImageButton
    ImageButton backArrowPatientList;

    User user = null;

    private PatientRecyclerAdapter.RecyclerViewClickListener recyclerViewClickListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_list);

        if(getIntent().getExtras()!=null){
            user = getIntent().getParcelableExtra("User");
        }

        initViews();
        initObjects();

        String nama = "Nama : "+user.getName();
        String address = "Alamat : "+user.getStreet()+", "+user.getCity()+",\n"+user.getCountry()+", "+user.getPostalCode();

        medicNameTv.setText(nama);
        medicAddressTv.setText(address);

        backArrowPatientList.setOnClickListener(v -> finish());
    }

    private void initViews(){
        medicNameTv = findViewById(R.id.medic_name_patient_list);
        medicAddressTv = findViewById(R.id.medic_address_patient_list);
        backArrowPatientList = findViewById(R.id.back_arrow_patient_list);

        recyclerViewPatient = findViewById(R.id.recycler_view_patient_list);
    }

    private void initObjects(){
        setOnClickListener();

        patientList = new ArrayList<>();
        patientRecyclerAdapter = new PatientRecyclerAdapter(patientList, recyclerViewClickListener);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewPatient.setLayoutManager(mLayoutManager);
        recyclerViewPatient.setItemAnimator(new DefaultItemAnimator());
        recyclerViewPatient.setHasFixedSize(true);
        recyclerViewPatient.setAdapter(patientRecyclerAdapter);

        databaseHelper = new DatabaseHelper(getApplicationContext());

        Request.getDataFromSQL(databaseHelper, patientList, patientRecyclerAdapter);
    }

    private void setOnClickListener() {
        // (v, position) -> = new PatientRecyclerAdapter.RecyclerViewClickListener()
        recyclerViewClickListener = (v, position) -> {
            Intent i = new Intent(PatientListActivity.this, HomeActivity.class);
            i.putExtra("User", patientList.get(position));
            startActivity(i);
        };
    }


}