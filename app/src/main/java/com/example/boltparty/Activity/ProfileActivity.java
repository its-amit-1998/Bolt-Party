package com.example.boltparty;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private ShapeableImageView imgProfile;
    private TextInputEditText edtFullName, edtEmail, edtPhone;
    private Button btnSave, btnChangePass;
    private final int PERMISSION_REQUEST_CODE = 1000;
    private final int PICK_REQUEST_CODE = 2000;
    private Uri imageUri;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Users users;
    private String saveNumber;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        imgProfile = findViewById(R.id.imgProfile);
        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtMobile);
        btnSave = findViewById(R.id.btnSave);
        btnChangePass = findViewById(R.id.btnChangePass);

        edtPhone.setOnKeyListener((view, i, event) -> {
            if (i == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                onClick(btnSave);
            }

            return false;
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users = snapshot.getValue(Users.class);
                edtFullName.setText(users.getName());
                edtEmail.setText(users.getEmail());
                saveNumber = users.getNumber().replace("+91", "");
                edtPhone.setText(saveNumber);
                if (users.getImageURL().equals("default")) {
                    imgProfile.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(ProfileActivity.this).load(users.getImageURL()).into(imgProfile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        imgProfile.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnChangePass.setOnClickListener(this);
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgProfile:
                if (Build.VERSION.SDK_INT >= 23) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Log.d("LOG", "Permission is granted");
                        openImage();
                    } else {
                        Log.d("LOG", "Permission is not granted");
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            new AlertDialog.Builder(this)
                                    .setMessage("We need permission for Read External Storage")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                                        }
                                    })
                                    .show();
                        } else {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                        }
                    }
                } else {
                    //Permission is automatically granted on SDK < 23 upon Installation.
                    Log.d("LOG", "Permission is granted");
                    openImage();
                }
                break;
            case R.id.btnSave:
                updateProfile();
                break;
            case R.id.btnChangePass:
                Intent intent = new Intent(this, ChangePasswordActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void updateProfile() {
        String name = edtFullName.getText().toString().trim();
        String number = edtPhone.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(number)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
        } else if (number.length() != 10) {
            Toast.makeText(this, "Phone number is not correct", Toast.LENGTH_SHORT).show();
        } else if (users.getName().equals(name) && saveNumber.equals(number)) {
            Toast.makeText(this, "Data has not been change", Toast.LENGTH_SHORT).show();
        } else if (!users.getName().equals(name) && !saveNumber.equals(number)) {
            updateProfileInfo(name, number);
        } else if (!users.getName().equals(name)) {
            updateProfileName(name);
        } else if (!saveNumber.equals(number)) {
            updateProfileNumber(number);
        }

    }

    private void updateProfileInfo(String name, String number) {
        String phoneNumber = new StringBuilder().append("+91").append(number).toString();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("please wait");
        progressDialog.show();

        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("number", phoneNumber);

        databaseReference.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    Toast.makeText(ProfileActivity.this, "Data has been Updated.", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog.dismiss();
                    Log.d("Profile", "Data has not been Updated.");
                }
            }
        });
    }

    private void updateProfileName(String name) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("please wait");
        progressDialog.show();

        databaseReference.child("name").setValue(name).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    Toast.makeText(ProfileActivity.this, "Data has been Updated.", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog.dismiss();
                    Log.d("Profile", "Data has not been Updated.");
                }
            }
        });
    }

    private void updateProfileNumber(String number) {
        String phoneNumber = new StringBuilder().append("+91").append(number).toString();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("please wait");
        progressDialog.show();

        databaseReference.child("number").setValue(phoneNumber).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    Toast.makeText(ProfileActivity.this, "Data has been Updated.", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog.dismiss();
                    Log.d("Profile", "Data has not been Updated.");
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("LOG", "Permission is granted");
                openImage();
            } else {
                //Permission NOT granted
                Log.d("LOG", "Permission is not granted");
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    //This block here means Permanently Denied Permission
                    new AlertDialog.Builder(this)
                            .setMessage("You have permanently denied this permission, go to settings to enable this permission")
                            .setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    gotoApplicationSettings();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .setCancelable(false)
                            .show();
                } else {
                    Toast.makeText(this, "You have denied the permission", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void gotoApplicationSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            try {
                imageUri = data.getData();
                uploadImage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        if (imageUri != null) {
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            assert bitmap != null;
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream);
            byte[] imageFileToByte = byteArrayOutputStream.toByteArray();
            final StorageReference imageReference = storageReference.child(users.getName() + System.currentTimeMillis() + ".jpeg");
            imageReference.delete();
            imageReference.putBytes(imageFileToByte).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            if (uri != null) {
                                String sDownloadUri = uri.toString();
                                HashMap<String, String> hashMap = new HashMap<>();
                                hashMap.put("imageUrl", sDownloadUri);
                                final DatabaseReference profileImagesReference = databaseReference.child("imageURL");

                                profileImagesReference.setValue(sDownloadUri).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            progressDialog.dismiss();
                                        } else {
                                            progressDialog.dismiss();
                                            Toast.makeText(ProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(ProfileActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            super.onBackPressed();
        else {
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    startActivity(intent);
                    finishAffinity();
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }
}