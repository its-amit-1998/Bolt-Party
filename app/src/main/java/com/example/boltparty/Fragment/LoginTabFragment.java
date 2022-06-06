package com.example.boltparty;

import android.app.ProgressDialog;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginTabFragment extends Fragment implements View.OnClickListener {

    private TextInputEditText edtEmail, edtPassword;
    private TextView txtForget;
    private Button btnLogin;
    private ProgressDialog progressDialog;
    private FirebaseAuth auth;

    public Fragment getLoginFragment() {
        return new LoginTabFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login_tab, container, false);

        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(getActivity());
        edtEmail = view.findViewById(R.id.edtEmail);
        edtPassword = view.findViewById(R.id.edtPassword);
        txtForget = view.findViewById(R.id.txtForget);
        btnLogin = view.findViewById(R.id.btnLogin);
        txtForget.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        edtPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent event) {
                if (i == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    onClick(btnLogin);
                }

                return false;
            }
        });

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(getActivity(), StartActivity.class));
            getActivity().finish();
        }

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txtForget:
                startActivity(new Intent(getContext(), ForgetActivity.class));
                break;
            case R.id.btnLogin:
                String email = edtEmail.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(getContext(), "Fields can't be Empty", Toast.LENGTH_SHORT).show();
                } else {
                    login(email, password);
                }
                break;
        }
    }

    private void login(String email, String password) {
        progressDialog.setMessage("please wait...");
        progressDialog.show();
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                if (auth.getCurrentUser().isEmailVerified()) {
                    progressDialog.dismiss();
                    Intent intent = new Intent(getActivity(), StartActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    getActivity().finish();
                    Log.d("LogIn", "Successfully logged in");
                    Toast.makeText(getActivity(), "Successfully logged in", Toast.LENGTH_SHORT).show();
                } else {
                    auth.signOut();
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Email is not verified", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d("LogInFailed", e.toString());
                Toast.makeText(getActivity(), "Your email and password does not match.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}