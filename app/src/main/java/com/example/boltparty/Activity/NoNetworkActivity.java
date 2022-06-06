package com.example.boltparty;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class NoNetworkActivity extends AppCompatActivity {

    private Button btnRetry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_network);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        btnRetry = findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(v -> {
            int statusInfo = NetworkInfo.getNetworkStatus(this);
            if (statusInfo == NetworkInfo.WIFI_NETWORK || statusInfo == NetworkInfo.MOBILE_NETWORK) {
                super.onBackPressed();
            } else if (statusInfo == NetworkInfo.NOT_CONNECTED) {
                Toast.makeText(this, "You're not connected with network.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        int statusInfo = NetworkInfo.getNetworkStatus(this);
        if (statusInfo == NetworkInfo.WIFI_NETWORK || statusInfo == NetworkInfo.MOBILE_NETWORK) {
            super.onBackPressed();
        } else if (statusInfo == NetworkInfo.NOT_CONNECTED) {
            Toast.makeText(this, "You're not connected with network.", Toast.LENGTH_SHORT).show();
        }
    }
}