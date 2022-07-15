package com.example.falldetectionsystem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import service.SharedPreferencesService;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class ChooseCategoryActivity extends AppCompatActivity {

    Button keluargaBtn, tenagaKesehatanBtn;
    String CATEGORY = "Category";

    SharedPreferencesService sharedPreferencesService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_category);

        Toolbar connectPageToolbar = findViewById(R.id.connect_page_toolbar);
        setSupportActionBar(connectPageToolbar);

        keluargaBtn = findViewById(R.id.keluarga_btn);
        tenagaKesehatanBtn = findViewById(R.id.tenaga_kesehatan_btn);

        sharedPreferencesService = new SharedPreferencesService(getApplicationContext());

        ImageButton backArrowConnectPage = findViewById(R.id.back_arrow_connect_page);

        //v -> = new View.OnClickListener
        backArrowConnectPage.setOnClickListener(v -> finish());

        keluargaBtn.setOnClickListener(v -> {
            // Put to session
            sharedPreferencesService.saveRole("Keluarga");
            Intent i = new Intent(getApplicationContext(),Login.class);
            startActivity(i);
            finish();
        });

        tenagaKesehatanBtn.setOnClickListener(v -> {
            // Put to session
            sharedPreferencesService.saveRole("Medis");
            Intent i = new Intent(getApplicationContext(),Login.class);
            startActivity(i);
            finish();
        });

    }
}