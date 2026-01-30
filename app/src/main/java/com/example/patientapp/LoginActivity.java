package com.example.patientapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        CardView digiBtn = findViewById(R.id.btn_digilocker_login);

        // When user clicks the main DigiLocker login button
        digiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVerificationProcess();
            }
        });
    }

    private void startVerificationProcess() {
        // Show the user that we are beginning the Aadhar verification
        Toast.makeText(this, "Redirecting to DigiLocker for Aadhar verification...", Toast.LENGTH_LONG).show();

        // Simulate a small delay for the "verification" before entering the app
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Once "verified", move to the Home screen (MainActivity)
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);

                // Finish LoginActivity so they can't go back
                finish();
            }
        }, 2000); // 2-second delay to simulate the verification flow
    }
}