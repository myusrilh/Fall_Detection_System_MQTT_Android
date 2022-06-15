package com.example.falldetectionsystem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import helper.DatabaseHelper;
import helper.User;
import service.SharedPreferencesService;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

public class Login extends AppCompatActivity {

    // Declare edit text
    EditText usernameEdt;
    EditText passwordEdt;

    //Declare text view for error handling
    TextView usernameErrorTv;
    TextView passwordErrorTv;
    TextView notHaveAccountTv;

    // Declare button
    Button loginBtn;

    //Declare DatabaseHelper
    DatabaseHelper databaseHelper;

    ImageButton backArrow;

    private ConstraintLayout constraintLayout;
    SharedPreferencesService sharedPreferencesService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        constraintLayout = findViewById(R.id.constraint_layout_login);
        sharedPreferencesService = new SharedPreferencesService(getApplicationContext());

        databaseHelper = new DatabaseHelper(this);
        initViewLogin();

        notHaveAccountTv.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), Register.class);
            startActivity(i);
            finish();
        });

        backArrow.setOnClickListener(v -> {
            sharedPreferencesService.clearData();
            Intent i = new Intent(getApplicationContext(), ChooseCategoryActivity.class);
            startActivity(i);
            finish();
        });

        loginBtn.setOnClickListener(v -> {
            // Get values from editText

            String userName = usernameEdt.getText().toString();
            String passWord = passwordEdt.getText().toString();

            if (validate(userName, passWord)) {

                //Authenticate user
                String role = sharedPreferencesService.loadRole();
                boolean currentUser = databaseHelper.checkUser(userName, role);

                if (currentUser) {
                    //Save username and role to the session
                    //Intent to home (depended by role)
                    User user = databaseHelper.getUserRole(userName, passWord);
                    if (user != null) {
//                            SharedPreferencesService sharedPreferencesService = new SharedPreferencesService(getApplicationContext(),true);
//                            sharedPreferencesService.saveRole(user.getRole());
                        Intent i = null;
                        if(user.getRole().equalsIgnoreCase("Keluarga")) {
                            i = new Intent(getApplicationContext(), HomeActivity.class);
                        }else if(user.getRole().equalsIgnoreCase("Medis")){
                            i = new Intent(getApplicationContext(), PatientListActivity.class);
                        }
                        assert i != null;
                        i.putExtra("User", user);
                        startActivity(i);
                    } else {
                        Snackbar.make(constraintLayout, "Failed to Login, please try again!", Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    Snackbar.make(constraintLayout, "User not exist, please register first!", Snackbar.LENGTH_LONG).show();
                }
            } else {
                Snackbar.make(constraintLayout, "Input properly!", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void initViewLogin() {
        usernameEdt = findViewById(R.id.username_login_edt);
        passwordEdt = findViewById(R.id.password_login_edt);

        usernameErrorTv = findViewById(R.id.username_error_tv);
        passwordErrorTv = findViewById(R.id.password_error_tv);
        notHaveAccountTv = findViewById(R.id.not_have_account_tv);

        loginBtn = findViewById(R.id.login_btn);

        backArrow = findViewById(R.id.back_arrow_login_page);
    }

    //This method is used to validate input given by user
    public boolean validate(String userName, String passWord) {
        boolean valid;

        //Handling validation for Email field
        if (userName.isEmpty()) {
//            textInputLayoutEmail.setError("Please enter username!");
            usernameErrorTv.setVisibility(View.VISIBLE);
            usernameErrorTv.setTextColor(Color.parseColor("#FF0000")); //Red color
            usernameErrorTv.setText(R.string.enter_username_please);
        } else {
//            textInputLayoutEmail.setError(null);
            usernameErrorTv.setVisibility(View.GONE);
        }

        //Handling validation for Password field
        if (passWord.isEmpty()) {
            valid = false;
//            textInputLayoutPassword.setError("Please enter password!");
            passwordErrorTv.setVisibility(View.VISIBLE);
            passwordErrorTv.setTextColor(Color.parseColor("#FF0000")); //Red color
            passwordErrorTv.setText(R.string.please_enter_password);
        } else {
            valid = true;
//            textInputLayoutPassword.setError(null);
            passwordErrorTv.setVisibility(View.GONE);
        }

        return valid;
    }
}