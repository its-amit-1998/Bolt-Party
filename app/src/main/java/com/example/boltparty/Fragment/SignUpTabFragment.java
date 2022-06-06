package com.example.boltparty;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUpTabFragment extends Fragment implements View.OnClickListener {

    private TextInputEditText edtEmail, edtFullName, edtPassword, edtConfirm, edtMobile;
    private Button btnSignUp;
    private ProgressDialog progressDialog;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    public Fragment getSignUpFragment() {
        return new SignUpTabFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_up_tab, container, false);

        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(getActivity());
        edtFullName = view.findViewById(R.id.edtFullName);
        edtEmail = view.findViewById(R.id.edtEmail);
        edtPassword = view.findViewById(R.id.edtPassword);
        edtConfirm = view.findViewById(R.id.edtConfirmPassword);
        edtMobile = view.findViewById(R.id.edtMobile);
        btnSignUp = view.findViewById(R.id.btnRegister);
        edtMobile.setOnKeyListener((view1, i, event) -> {
            if (i == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                onClick(btnSignUp);
            }

            return false;
        });
        btnSignUp.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        String name = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirm = edtConfirm.getText().toString().trim();
        String number = edtMobile.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirm) || TextUtils.isEmpty(number)) {
            Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
        } else if (password.length() < 6 && confirm.length() < 6) {
            Toast.makeText(getContext(), "Password length should be greater than 6", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(confirm)) {
            Toast.makeText(getContext(), "Password does not match", Toast.LENGTH_SHORT).show();
        } else if (number.length() != 10) {
            Toast.makeText(getContext(), "Phone number is not correct", Toast.LENGTH_SHORT).show();
        } else {
            register(email, password, name, number);
        }
    }

    private void register(String email, String password, String name, String number) {
        String phoneNumber = new StringBuilder().append("+91").append(number).toString();
        progressDialog.setMessage("please wait...");
        progressDialog.show();
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d("Registration", "Your account has been register");
                    HashMap<String, String> map = new HashMap<>();
                    map.put("name", name);
                    map.put("email", email);
                    map.put("password", password);
                    map.put("number", phoneNumber);
                    map.put("imageURL", "default");

                    String userId = auth.getCurrentUser().getUid();
                    databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                    databaseReference.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("Registration", "Data has been saved");
                                sendingEmailForVerification();
                            } else {
                                progressDialog.dismiss();
                                Log.d("Registration", "Data has not been saved");
                                Toast.makeText(getActivity(), "Data has not been saved", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    Log.d("Registration", "Account has been already register");
                    Toast.makeText(getActivity(), "Account has been already register", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println(e);
            }
        });
    }

    private void sendingEmailForVerification() {
        auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    Log.d("Registration", "Email Verification has been send");
                    edtFullName.setText("");
                    edtEmail.setText("");
                    edtPassword.setText("");
                    edtConfirm.setText("");
                    edtMobile.setText("");
                    Toast.makeText(getContext(), "Your account has been register", Toast.LENGTH_SHORT).show();
                } else {
//                    databaseReference.removeValue();
//                    auth.getCurrentUser().delete();
                    progressDialog.dismiss();
                    Log.d("Registration", "Your email account is not valid");
                    Toast.makeText(getContext(), "Your email account is not valid", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}