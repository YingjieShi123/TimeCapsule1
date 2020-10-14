package com.example.tcprototype2.ui.options;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.example.tcprototype2.CameraCapture;
import com.example.tcprototype2.CustomizeCapsule;
import com.example.tcprototype2.Register;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.tcprototype2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {
    private static final String TAG = "EditProfile";
    private static final int GALLERY_REQUEST_CODE = 2;
    private static final int CAMERA_REQUEST_CODE = 1;
    private EditText txtName, txtStatus;
    private Button btnChoosePic, btnSave;
    private ImageView imageView;
    private Uri imageUri;
    private String currentImagePath = null;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private DocumentReference mDocRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txtName = findViewById(R.id.txt_edit_name);
        txtStatus = findViewById(R.id.txt_edit_status);
        imageView = findViewById(R.id.img_edit_dp);
        btnChoosePic = findViewById(R.id.btn_change_dp);
        btnSave = findViewById(R.id.btn_save_edits);

        db = FirebaseFirestore.getInstance();

        user = FirebaseAuth.getInstance().getCurrentUser();
        // TODO: firestore for status
        // Load and set user profile details
        mDocRef = FirebaseFirestore.getInstance().document("users/" + user.getUid());
        mDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    imageUri = user.getPhotoUrl();
                    imageView.setImageURI(user.getPhotoUrl());
                    txtName.setText(user.getDisplayName());
                    txtStatus.setText(documentSnapshot.getString("status"));
                }
                else {
                    Toast.makeText(EditProfile.this, "Unable to connect to database.", Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(false);
                }
            }
        });


        // Select new picture from gallery or capture new image
        btnChoosePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] options = { "Capture new image", "Select existing image from gallery", "Cancel" };
                // Create alert dialog with options
                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfile.this);
                builder.setTitle("Select image source");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Capture new image")) {
                            Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (captureImage.resolveActivity(getPackageManager()) != null){
                                File imageFile = null;
                                try{
                                    imageFile = getImageFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                if (imageFile != null) {
                                    imageUri = FileProvider.getUriForFile(EditProfile.this, "com.example.tcprototype2.fileprovider", imageFile);
                                    captureImage.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                                    startActivityForResult(captureImage, CAMERA_REQUEST_CODE);
                                }
                            }
                        }
                        else if (options[item].equals("Select existing image from gallery")) {
                            Intent pickImage = new Intent(Intent.ACTION_PICK);
                            pickImage.setType("image/*");
                            startActivityForResult(Intent.createChooser(pickImage, "Select an image"), GALLERY_REQUEST_CODE);
                        }
                        else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = txtName.getText().toString().trim();
                String status = txtStatus.getText().toString().trim();
                if (!TextUtils.isEmpty(name)){
                    // Save status and name to database
                    Map<String, Object> update = new HashMap<>();
                    update.put("name", name);
                    update.put("status", status);
                    db.collection("users").document(user.getUid())
                            .set(update, SetOptions.merge())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "User details added to database.");
                                    Toast.makeText(EditProfile.this, "Updated user details to database", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error writing updates to database", e);
                                    Toast.makeText(EditProfile.this, e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });

                    // Update Firebase user display name and picture
                    UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .setPhotoUri(imageUri)
                            .build();
                    user.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(EditProfile.this, "Saved changes to profile.", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                            else {
                                Log.w(TAG, "updateProfile:failure", task.getException());
                                Toast.makeText(EditProfile.this, "Unable to save changes.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(EditProfile.this, "Display name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Save image from camera or gallery
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Set image to image view
                imageView.setImageURI(imageUri);
            }
            else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Image capture cancelled.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Failed to capture image.", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == GALLERY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                imageUri = data.getData();
                imageView.setImageURI(imageUri);
            }
            else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Image selection from gallery cancelled.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Failed to retrieve image from gallery.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Creates a timestamped file in local directory
    private File getImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "tc_img_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageName, ".jpg", storageDir);
        return imageFile;
    }
}