package com.example.tcprototype2;

import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    private Button btnRegister;
    private TextView lblGoBack;
    private EditText txtName, txtEmail, txtPassword;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private static final String TAG = "Register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnRegister = findViewById(R.id.btn_register);
        lblGoBack = findViewById(R.id.lbl_registered);
        txtName = findViewById(R.id.txt_name);
        txtEmail = findViewById(R.id.txt_email);
        txtPassword = findViewById(R.id.txt_pwd);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = txtName.getText().toString().trim();
                final String email = txtEmail.getText().toString().trim();
                String password = txtPassword.getText().toString().trim();
                // Perform input validation
                // Check for empty fields
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                    Toast.makeText(Register.this, "No fields can be empty.", Toast.LENGTH_SHORT).show();
                }
                // Check if email matches email format
                else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    Toast.makeText(Register.this, "Please enter a valid email.", Toast.LENGTH_SHORT).show();
                }
                // Check password length
                else if (password.length() < 6) {
                    Toast.makeText(Register.this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show();
                }
                else {
                    btnRegister.setEnabled(false);
                    // Initialise Firebase Authorisation
                    firebaseAuth = FirebaseAuth.getInstance();
                    // Initialise Cloud Firestore (cloud database)
                    db = FirebaseFirestore.getInstance();

                    // Create new user in Firebase project with email and password
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        FirebaseUser user = firebaseAuth.getCurrentUser();
                                        // Add name to new user in Firebase
                                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(name).build();
                                        user.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful())
                                                    //Toast.makeText(Register.this, "Updated display name", Toast.LENGTH_SHORT).show();
                                                    Log.d(TAG, "User name updated.");
                                            }
                                        });

                                        // Create new user entry in database
                                        Map<String, Object> newUser = new HashMap<>();
                                        newUser.put("uid", user.getUid());
                                        newUser.put("name", name);
                                        newUser.put("email", email);
                                        newUser.put("status", "I am using TIMECAPSULE App!");
                                        db.collection("users").document(user.getUid())
                                                .set(newUser)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "User details added to database.");
                                                        Toast.makeText(Register.this, "Added user to database", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Error writing user to database", e);
                                                        Toast.makeText(Register.this, e.toString(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                        Toast.makeText(Register.this, "New account for " + email + " registered successfully.", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                    else{
                                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(Register.this, "Could not register new user. Check your internet connection.", Toast.LENGTH_SHORT).show();
                                        btnRegister.setEnabled(true);
                                    }
                                }
                            });
                }
            }
        });

        lblGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


}