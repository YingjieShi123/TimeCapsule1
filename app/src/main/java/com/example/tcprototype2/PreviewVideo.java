package com.example.tcprototype2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PreviewVideo extends AppCompatActivity {
    VideoView videoPreview;
    Intent intent;
    Bundle extras;
    TextView txtTitle, txtRecipient, txtDate;
    Button btnSend;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_video);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        intent = getIntent();
        extras = intent.getExtras();

        videoPreview = findViewById(R.id.vidviewPreview);
        txtTitle = findViewById(R.id.txt_aud_title);
        txtRecipient = findViewById(R.id.txt_aud_recipient);
        txtDate = findViewById(R.id.txt_aud_date);
        btnSend = findViewById(R.id.btn_send_vid);
        progressBar = findViewById(R.id.pg_upload_vid);

        final Date openDate = (Date) extras.getSerializable("open_date");
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy 'at' HH:mm z");
        String strDate = dateFormat.format(openDate);

        txtTitle.setText("Title: " + extras.getString("title"));
        txtRecipient.setText("Recipient: " + extras.getString("r_name"));
        txtDate.setText("Opening date: " + strDate);

        final Uri videoUri = Uri.parse(extras.getString("video_uri"));
        videoPreview.setVideoURI(videoUri);
        videoPreview.start();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoUri != null) {
                    btnSend.setEnabled(false);
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    final String uid = user.getUid();

                    // Create reference to media file's path in storage
                    final String filename = UUID.randomUUID().toString();
                    String path = "videos/" + "/" + uid + "/" + filename + "3gp";
                    final StorageReference videoRef = storage.getReference(path);

                    // Upload to storage
                    UploadTask uploadTask = videoRef.putFile(videoUri);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PreviewVideo.this, "Upload failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    }).addOnSuccessListener(PreviewVideo.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(PreviewVideo.this, "File upload complete.", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }).addOnProgressListener(PreviewVideo.this, new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            // Update progress bar
                            long fileSize = snapshot.getTotalByteCount();
                            long uploadBytes = snapshot.getBytesTransferred();
                            long progress = (100 * uploadBytes) / fileSize;
                            progressBar.setProgress((int) progress);
                            progressBar.setVisibility(View.VISIBLE);
                        }
                    });

                    // Get file download url once upload to storage is complete
                    Task<Uri> getDownloadUriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()){
                                throw task.getException();
                            }
                            return videoRef.getDownloadUrl();
                        }
                    });
                    getDownloadUriTask.addOnCompleteListener(PreviewVideo.this, new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()){
                                Uri downloadUri = task.getResult();

                                // Create database entry for the new time capsule
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                Map<String, Object> timeCapsule = new HashMap<>();
                                timeCapsule.put("sender", uid);
                                timeCapsule.put("s_name", user.getDisplayName());
                                timeCapsule.put("recipient", extras.getString("r_uid"));
                                timeCapsule.put("r_name", extras.getString("r_name"));
                                timeCapsule.put("created", new Timestamp(new Date()));
                                timeCapsule.put("opening", new Timestamp(openDate));
                                timeCapsule.put("title", extras.getString("title"));
                                timeCapsule.put("type", "video");
                                timeCapsule.put("filename", filename);
                                timeCapsule.put("uri", downloadUri.toString());
                                timeCapsule.put("status", "unopened");
                                db.collection("timecapsules")
                                        .add(timeCapsule)
                                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if (task.isSuccessful()){
                                                    // Add randomly generated document id to a field
                                                    DocumentReference capsuleRef = task.getResult();
                                                    capsuleRef.update("id", capsuleRef.getId()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(PreviewVideo.this, "Time capsule was sent successfully!", Toast.LENGTH_SHORT).show();
                                                            // Return to home screen
                                                            Intent home = new Intent(getApplicationContext(), MainActivity.class);
                                                            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                            startActivity(home);
                                                        }
                                                    });
                                                }
                                                else {
                                                    Toast.makeText(PreviewVideo.this, "Time capsule failed to send.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(PreviewVideo.this, "Nothing to upload...", Toast.LENGTH_SHORT).show();
                    btnSend.setEnabled(true);
                }
            }
        });
    }
}