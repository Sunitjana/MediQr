package com.example.project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button qrGeneratorButton, qrScannerButton;
    TextView welcomeText, sloganText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        qrGeneratorButton = findViewById(R.id.qrGeneratorButton);
        qrScannerButton = findViewById(R.id.qrScannerButton);
        welcomeText = findViewById(R.id.welcomeText);
        sloganText = findViewById(R.id.sloganText);

        qrGeneratorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle QR Generator button click
                // Navigate to QR Generator Activity
                Intent intent = new Intent(MainActivity.this, QrgeneretorActivity.class);
                startActivity(intent);
            }
        });

        qrScannerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle QR Scanner button click
                // Navigate to QR Scanner Activity
                Intent intent = new Intent(MainActivity.this, QrscanningActivity.class);
                startActivity(intent);
            }
        });
    }
}