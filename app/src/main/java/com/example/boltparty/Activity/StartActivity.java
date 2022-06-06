package com.example.boltparty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class StartActivity extends AppCompatActivity implements View.OnClickListener {

    private ShapeableImageView profileImg;
    private TextView txtUsername;
    private MaterialButton btnProfile, btnBooking, btnFavorite, btnBookingDetails;
    private Users users;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        profileImg = findViewById(R.id.profile_img);
        txtUsername = findViewById(R.id.txtUsername);
        btnProfile = findViewById(R.id.btn_Profile);
        btnBooking = findViewById(R.id.btn_booking);
        btnBookingDetails = findViewById(R.id.btn_booking_details);
        btnFavorite = findViewById(R.id.btn_favorite);

        btnProfile.setOnClickListener(this);
        btnBooking.setOnClickListener(this);
        btnBookingDetails.setOnClickListener(this);
        btnFavorite.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users = snapshot.getValue(Users.class);
                txtUsername.setText(users.getName());
                if (users.getImageURL().equals("default")) {
                    profileImg.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(StartActivity.this).load(users.getImageURL()).into(profileImg);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StartActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Intent intent = new Intent(StartActivity.this, MainActivity.class);
                    startActivity(intent);
                    finishAffinity();
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_Profile:
                startActivity(new Intent(this, ProfileActivity.class));
                break;
            case R.id.btn_booking:
                Intent intent = new Intent(this, MapsActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_booking_details:
                startActivity(new Intent(this, BookedActivity.class));
                break;
            case R.id.btn_favorite:
                startActivity(new Intent(this, FavoriteActivity.class));
                break;
        }
    }

//    private String getFileExtension(Uri uri) {
//        ContentResolver contentResolver = getContentResolver();
//        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
//
//        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
//    }
}