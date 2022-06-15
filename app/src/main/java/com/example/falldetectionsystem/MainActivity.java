package com.example.falldetectionsystem;

import androidx.appcompat.app.AppCompatActivity;
import service.SharedPreferencesService;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Objects.requireNonNull(getSupportActionBar()).hide();

        //() -> = new Runnable
        new Handler().postDelayed(() -> {
            intent = new Intent(MainActivity.this, ChooseCategoryActivity.class);
            startActivity(intent);
            finish();
        }, 2000);

    }
}