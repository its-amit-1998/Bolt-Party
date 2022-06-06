package com.example.boltparty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ConfirmDetailsActivity extends AppCompatActivity {

    private TextView txtImgCafeName, txtHeaderCafeName, txtDate, txtTime, txtAddress;
    private ImageView imgCafe;
    private Button btnCancel;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_details);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid()).child("Booked");
        txtImgCafeName = findViewById(R.id.txtCafeName1);
        txtHeaderCafeName = findViewById(R.id.txtCafeName2);
        txtDate = findViewById(R.id.txtDate);
        txtTime = findViewById(R.id.txtTime);
        txtAddress = findViewById(R.id.txtAddress);
        imgCafe = findViewById(R.id.cafeImg);
        btnCancel = findViewById(R.id.btnCancel);

        Bundle extras = getIntent().getExtras();
        String cafeName = extras.getString("CafeName");
        String time = extras.getString("Time");
        String date = extras.getString("Date");
        String address = extras.getString("Address");
        String imageLink = extras.getString("ImageLink");

        txtImgCafeName.setText(cafeName);
        txtHeaderCafeName.setText(cafeName);
        txtDate.setText(date);
        txtTime.setText(time);
        txtAddress.setText(address);
        Glide.with(this).load(imageLink).into(imgCafe);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = new ProgressDialog(ConfirmDetailsActivity.this);
                progressDialog.setMessage("please wait...");
                progressDialog.show();

                databaseReference.child(cafeName).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Intent intent = new Intent(ConfirmDetailsActivity.this, BookedActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                            Toast.makeText(ConfirmDetailsActivity.this, "You have cancel the booking.", Toast.LENGTH_SHORT).show();
                        }
                    }

                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        int statusInfo = NetworkInfo.getNetworkStatus(this);
        if (statusInfo == NetworkInfo.WIFI_NETWORK || statusInfo == NetworkInfo.MOBILE_NETWORK) {
            return;
        } else if (statusInfo == NetworkInfo.NOT_CONNECTED) {
            startActivity(new Intent(this, NoNetworkActivity.class));
        }
    }
}