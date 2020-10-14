package com.example.tcprototype2;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {
    private Button btnLogin;
    private EditText txtEmail, txtPassword;
    private TextView lblRegister, lblForget;
    private ProgressBar progressBar;
    private Intent intent;
    private String email, password;
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnLogin = findViewById(R.id.btn_login);
        txtEmail = findViewById(R.id.txt_login_email);
        txtPassword = findViewById(R.id.txt_login_password);
        lblRegister = findViewById(R.id.lbl_register);
        lblForget = findViewById(R.id.lbl_forget);
        progressBar = findViewById(R.id.pg_login);

        // Initialise Firebase Authorisation
        firebaseAuth = FirebaseAuth.getInstance();

        // Link to register screen
        lblRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(Login.this, Register.class);
                startActivity(intent);
            }
        });

        // Forget password link
        lblForget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Have Firebase send an email to reset password
                email = txtEmail.getText().toString().trim();
                if (!TextUtils.isEmpty(email)){
                    firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Login.this, "A request to reset your password was sent to " + email, Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Log.w(TAG, "sendPasswordResetEmail:failure", task.getException());
                                Toast.makeText(Login.this, "Failed to send email, please check you have entered the correct email.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(Login.this, "Enter your email in the text field", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnLogin.setEnabled(false);
                email = txtEmail.getText().toString().trim();
                password = txtPassword.getText().toString().trim();
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                    progressBar.setIndeterminate(true);
                    progressBar.setVisibility(View.VISIBLE);
                    // Sign in user
                    firebaseAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        progressBar.setVisibility(View.INVISIBLE);
                                        // Go to home screen
                                        intent = new Intent(Login.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                    else {
                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                                        Toast.makeText(Login.this, "Failed to login. Check your email and password.", Toast.LENGTH_SHORT).show();
                                        progressBar.setIndeterminate(false);
                                        progressBar.setVisibility(View.INVISIBLE);
                                        btnLogin.setEnabled(true);
                                    }
                                }
                            });
                }
                else {
                    Toast.makeText(Login.this, "Email and password cannot be empty.", Toast.LENGTH_SHORT).show();
                    btnLogin.setEnabled(true);
                }
            }
        });
    }
}