package com.example.falldetectionsystem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import helper.DatabaseHelper;
import helper.User;
import service.SharedPreferencesService;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

public class Register extends AppCompatActivity {

//    Declare EditText
    EditText usernameEdt;
    EditText patientNameEdt;
    EditText streetAddressEdt;
    EditText cityAddressEdt;
    EditText countryAddressEdt;
    EditText postalCodeAddressEdt;
    EditText passwordEdt;
    EditText roleEdt;

//    Declare TextView
    TextView usernameErrorTv;
    TextView patientNameErrorTv;
    TextView streetErrorTv;
    TextView cityErrorTv;
    TextView countryErrorTv;
    TextView postalCodeErrorTv;
    TextView passwordErrorTv;
    TextView roleErrorTv;
    TextView haveAccountTv;

//    Declare Button
    Button registerBtn;

//    Declare DatabaseHelper
    DatabaseHelper databaseHelper;

    SharedPreferencesService sharedPreferencesService;

    private ConstraintLayout constraintLayout;

    ImageButton backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        constraintLayout = findViewById(R.id.constraint_layout_register);

        databaseHelper = new DatabaseHelper(this);
        sharedPreferencesService = new SharedPreferencesService(getApplicationContext());

        initViews();

        roleEdt.setText(sharedPreferencesService.loadRole());
        roleEdt.setFocusable(false);
        roleEdt.setFocusableInTouchMode(false);
        roleEdt.setInputType(InputType.TYPE_NULL);

        haveAccountTv.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(),Login.class);
            startActivity(i);
            finish();
        });

        backArrow.setOnClickListener(v -> {
            sharedPreferencesService.clearData();
            Intent i = new Intent(getApplicationContext(), ChooseCategoryActivity.class);
            startActivity(i);
            finish();
        });

        if(sharedPreferencesService.loadRole().equalsIgnoreCase("Medis")) {
            patientNameEdt.setText(R.string.no_patient_text);
            patientNameEdt.setFocusable(false);
            patientNameEdt.setFocusableInTouchMode(false);
            patientNameEdt.setInputType(InputType.TYPE_NULL);
        }


        registerBtn.setOnClickListener(v -> {
            String userName = usernameEdt.getText().toString();
            String patientName = patientNameEdt.getText().toString();
            String streetAddress = streetAddressEdt.getText().toString();
            String cityAddress = cityAddressEdt.getText().toString();
            String countryAddress = countryAddressEdt.getText().toString();
            String postalCode = postalCodeAddressEdt.getText().toString();
            String passWord = passwordEdt.getText().toString();

            if(validate(userName, patientName, streetAddress, cityAddress, countryAddress, postalCode, passWord)){

                String role = sharedPreferencesService.loadRole();
                if(!databaseHelper.checkUser(userName, role)){
                    User user = new User();
                    user.setName(userName);
                    user.setPatientname(patientName);
                    user.setStreet(streetAddress);
                    user.setCity(cityAddress);
                    user.setCountry(countryAddress);
                    user.setPostalCode(postalCode);
                    user.setPassword(passWord);
                    user.setRole(sharedPreferencesService.loadRole());

                    databaseHelper.addUser(user);

                    Snackbar.make(constraintLayout, "User created successfully! Please Login ", Snackbar.LENGTH_LONG).show();

                    //() -> = new Runnable
                    new Handler().postDelayed(() -> {
                        Intent i = new Intent(Register.this,Login.class);
                        startActivity(i);
                        finish();
                    }, Snackbar.LENGTH_SHORT);
                }else{

                    //Email exists with email input provided so show error user already exist
                    Snackbar.make(constraintLayout, "User already exists with same username", Snackbar.LENGTH_LONG).show();
                }

            }else{
                Snackbar.make(constraintLayout, "Input properly!", Snackbar.LENGTH_LONG).show();
            }
        });

    }

    private void initViews(){
        usernameEdt = findViewById(R.id.username_register_edt);
        patientNameEdt  = findViewById(R.id.patient_name_register_edt);
        streetAddressEdt = findViewById(R.id.street_register_edt);
        cityAddressEdt  = findViewById(R.id.city_register_edt);
        countryAddressEdt = findViewById(R.id.country_register_edt);
        postalCodeAddressEdt = findViewById(R.id.postalcode_register_edt);
        passwordEdt = findViewById(R.id.password_register_edt);
        roleEdt = findViewById(R.id.role_register_edt);

        usernameErrorTv = findViewById(R.id.username_register_error_tv);
        patientNameErrorTv = findViewById(R.id.patient_name_register_error_tv);
        streetErrorTv = findViewById(R.id.street_register_error_tv);
        cityErrorTv = findViewById(R.id.city_register_error_tv);
        countryErrorTv = findViewById(R.id.country_register_error_tv);
        postalCodeErrorTv = findViewById(R.id.postalcode_register_error_tv);
        passwordErrorTv = findViewById(R.id.password_register_error_tv);
        roleErrorTv = findViewById(R.id.role_register_error_tv);

        haveAccountTv = findViewById(R.id.have_account_tv);

        registerBtn = findViewById(R.id.register_btn);

        backArrow = findViewById(R.id.back_arrow_register_page);
    }

    private boolean validate(String username, String patientName, String password, String streetAddress, String cityAddress, String countryAddress, String postalCode){
        boolean valid;


        if(username.isEmpty()){
            usernameErrorTv.setVisibility(View.VISIBLE);
            usernameErrorTv.setTextColor(Color.parseColor("#FF0000"));
            usernameErrorTv.setText(R.string.username_must_not_empty);
        }else {
//            textInputLayoutEmail.setError(null);
            usernameErrorTv.setVisibility(View.GONE);
        }

        if(patientName.isEmpty()){
            patientNameErrorTv.setVisibility(View.VISIBLE);
            patientNameErrorTv.setTextColor(Color.parseColor("#FF0000"));
            patientNameErrorTv.setText(R.string.patient_name_must_not_empty);
        }else{
            patientNameErrorTv.setVisibility(View.GONE);
        }

        if(password.isEmpty()){
            passwordErrorTv.setVisibility(View.VISIBLE);
            passwordErrorTv.setTextColor(Color.parseColor("#FF0000"));
            passwordErrorTv.setText(R.string.password_must_not_empty);
        }else{
            passwordErrorTv.setVisibility(View.GONE);
        }

        if(streetAddress.isEmpty()){
            streetErrorTv.setVisibility(View.VISIBLE);
            streetErrorTv.setTextColor(Color.parseColor("#FF0000"));
            streetErrorTv.setText(R.string.street_address_must_not_empty);
        }else{
            streetErrorTv.setVisibility(View.GONE);
        }


        if(cityAddress.isEmpty()){
            cityErrorTv.setVisibility(View.VISIBLE);
            cityErrorTv.setTextColor(Color.parseColor("#FF0000"));
            cityErrorTv.setText(R.string.city_must_not_empty);
        }else{
            cityErrorTv.setVisibility(View.GONE);
        }


        if(countryAddress.isEmpty()){
            countryErrorTv.setVisibility(View.VISIBLE);
            countryErrorTv.setTextColor(Color.parseColor("#FF0000"));
            countryErrorTv.setText(R.string.country_must_not_empty);
        }else{
            countryErrorTv.setVisibility(View.GONE);
        }


        if(postalCode.isEmpty()){
            valid = false;
            postalCodeErrorTv.setVisibility(View.VISIBLE);
            postalCodeErrorTv.setTextColor(Color.parseColor("#FF0000"));
            postalCodeErrorTv.setText(R.string.postal_code_must_not_empty);
        }else{
            valid = true;
            postalCodeErrorTv.setVisibility(View.GONE);
        }
        return valid;
    }
}