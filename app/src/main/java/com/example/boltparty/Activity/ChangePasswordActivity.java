package com.example.boltparty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangePasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputEditText edtOldPass, edtNewPass, edtConfirmPass;
    private Button btnChange;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edtOldPass = findViewById(R.id.edtOldPass);
        edtNewPass = findViewById(R.id.edtNewPass);
        edtConfirmPass = findViewById(R.id.edtConfirmPass);
        btnChange = findViewById(R.id.btnChange);

        edtConfirmPass.setOnKeyListener((view, i, keyEvent) -> {
            if (i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                onClick(btnChange);
            }

            return false;
        });

        btnChange.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        String userId = currentUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

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
        String oldPss = edtOldPass.getText().toString().trim();
        String newPss = edtNewPass.getText().toString().trim();
        String confirmPss = edtConfirmPass.getText().toString().trim();
        if (TextUtils.isEmpty(oldPss) || TextUtils.isEmpty(newPss) || TextUtils.isEmpty(confirmPss)) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
        } else if (oldPss.equals(newPss)) {
            Toast.makeText(this, "Old password and new password are saved.", Toast.LENGTH_SHORT).show();
        } else if (newPss.length() < 6) {
            Toast.makeText(this, "The new password length should be more than 6 character.", Toast.LENGTH_SHORT).show();
        } else if (!confirmPss.equals(newPss)) {
            Toast.makeText(this, "Confirm password does not match new password.", Toast.LENGTH_SHORT).show();
        } else {
            changePassword(oldPss, newPss);
        }
    }

    private void changePassword(String oldPss, String newPss) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("please wait...");
        progressDialog.show();

        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), oldPss);
        currentUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    currentUser.updatePassword(newPss).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                databaseReference.child("password").setValue(newPss);
                                progressDialog.dismiss();
                                auth.signOut();
                                Intent intent = new Intent(ChangePasswordActivity.this, MainActivity.class);
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finishAffinity();
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(ChangePasswordActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    Log.d("ChangePassword", task.getException().getMessage());
                    Toast.makeText(ChangePasswordActivity.this, "You have enter wrong password.", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                    if (task.isSuccessful()) {
                        startActivity(new Intent(ChangePasswordActivity.this, MainActivity.class));
                        finishAffinity();
                    }
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }
}