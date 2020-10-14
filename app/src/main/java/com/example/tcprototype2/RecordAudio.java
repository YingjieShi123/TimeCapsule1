package com.example.tcprototype2;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RecordAudio extends AppCompatActivity {
    private static final String LOG_TAG = "AudioRecordTest";
    Button btnRecord, btnPlayback, btnConfirm;
    TextView txtTitle, txtDate, txtRecipient, txtMode;
    ProgressBar pgUpload, pgRecorder;
    Bundle extras;
    MediaRecorder recorder;
    MediaPlayer player;
    SeekBar seekBar;
    String fileName, filePath;
    Date openDate;
    File audioFile;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();
    }

    // Control function for audio recording
    private void onRecord(boolean start) {
        if (start) {
            startRecording();
            txtMode.setText("Now recording");
            txtMode.setVisibility(View.VISIBLE);
        } else {
            stopRecording();
            txtMode.setVisibility(View.INVISIBLE);
        }
    }
    // Control function for audio playback
    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
            txtMode.setText("Now playing");
            txtMode.setVisibility(View.VISIBLE);
        } else {
            stopPlaying();
            txtMode.setVisibility(View.INVISIBLE);
        }
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            // Set file to media player
            player.setDataSource(filePath);
            player.prepare();
            // Set seek bar to file duration
            int duration = player.getDuration();
            seekBar.setMax(duration / 1000);
            player.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        player.release();
        player = null;
    }

    private void startRecording() {
        // Prepare media recorder
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(filePath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        // Show recording spinning progress bar
        pgRecorder.setIndeterminate(true);
        pgRecorder.setVisibility(View.VISIBLE);

        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
        // Stop and hide spinning progress bar
        pgRecorder.setIndeterminate(false);
        pgRecorder.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_audio);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnRecord = findViewById(R.id.btn_start_record);
        btnPlayback = findViewById(R.id.btn_playback);
        btnConfirm = findViewById(R.id.btn_aud_send);
        txtTitle = findViewById(R.id.txt_aud_title);
        txtDate = findViewById(R.id.txt_aud_date);
        txtMode = findViewById(R.id.txt_aud_mode);
        txtRecipient = findViewById(R.id.txt_aud_recipient);
        pgUpload = findViewById(R.id.pg_upload_audio);
        pgRecorder = findViewById(R.id.prgbar_record);
        seekBar = findViewById(R.id.seek_preview_aud);

        // Get extras
        Intent intent = getIntent();
        extras = intent.getExtras();

        // Set capsule details in text views
        openDate = (Date) extras.getSerializable("open_date");
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy 'at' HH:mm z");
        String strDate = dateFormat.format(openDate);

        txtTitle.setText("Title: " + extras.getString("title"));
        txtRecipient.setText("Recipient: " + extras.getString("r_name"));
        txtDate.setText("Opening date: " + strDate);

        btnRecord.setOnClickListener(new View.OnClickListener() {
            boolean startRecording = true;
            @Override
            public void onClick(View v) {
                onRecord(startRecording);
                if (startRecording) {
                    btnRecord.setText("Stop recording");
                } else {
                    btnRecord.setText("Start recording");
                }
                startRecording = !startRecording;
            }
        });
        btnPlayback.setOnClickListener(new View.OnClickListener() {
            boolean startPlaying = true;
            @Override
            public void onClick(View v) {
                onPlay(startPlaying);
                if (startPlaying) {
                    btnPlayback.setText("Stop playing");
                } else {
                    btnPlayback.setText("Start playing");
                }
                startPlaying = !startPlaying;
            }
        });

        // Set local file path
        filePath = getExternalCacheDir().getAbsolutePath();
        fileName =  UUID.randomUUID().toString();
        filePath += "/" + fileName + ".3gp";
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        // TODO: , progress bar on playback,

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if file is empty
                audioFile = new File(filePath);
                if (audioFile.length() == 0){
                    Toast.makeText(RecordAudio.this, "Cannot send empty audio file.", Toast.LENGTH_SHORT).show();
                }
                else {
                    // Create confirmation alert
                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(RecordAudio.this);
                    alertDialog.setTitle("Send confirmation");
                    alertDialog.setMessage("Ready to send time capsule? Confirm audio recording with the Playback button.");
                    alertDialog.setCancelable(true);
                    alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Create new time capsule in database
                            createTimeCapsule();
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing
                        }
                    });
                    alertDialog.show();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private void createTimeCapsule() {
        btnConfirm.setEnabled(false);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();
        Uri fileUri = Uri.fromFile(new File(filePath));

        // Create reference to media file's path in storage
        String path = "audio/" + "/" + uid + "/" + fileName + "3gp";
        final StorageReference storageRef = storage.getReference(path);

        // Upload to storage
        UploadTask uploadTask = storageRef.putFile(fileUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RecordAudio.this, "Upload failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(RecordAudio.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(RecordAudio.this, "File upload complete.", Toast.LENGTH_SHORT).show();
                pgUpload.setVisibility(View.GONE);
            }
        }).addOnProgressListener(RecordAudio.this, new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                // Update progress bar
                long fileSize = snapshot.getTotalByteCount();
                long uploadBytes = snapshot.getBytesTransferred();
                long progress = (100 * uploadBytes) / fileSize;
                pgUpload.setProgress((int) progress);
                pgUpload.setVisibility(View.VISIBLE);
            }
        });

        // Get file download url once upload to storage is complete
        Task<Uri> getDownloadUriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return storageRef.getDownloadUrl();
            }
        });
        getDownloadUriTask.addOnCompleteListener(RecordAudio.this, new OnCompleteListener<Uri>() {
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
                    timeCapsule.put("type", "audio");
                    timeCapsule.put("filename", fileName);
                    timeCapsule.put("uri", downloadUri.toString());
                    timeCapsule.put("status", "unopened");

                    // Add time capsule to database
                    db.collection("timecapsules")
                            .add(timeCapsule)
                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if (task.isSuccessful()){
                                        // Append randomly generated document id as a field
                                        DocumentReference capsuleRef = task.getResult();
                                        capsuleRef.update("id", capsuleRef.getId()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(RecordAudio.this, "Time capsule was sent successfully!", Toast.LENGTH_SHORT).show();
                                                // Return to home screen
                                                Intent home = new Intent(getApplicationContext(), MainActivity.class);
                                                home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(home);
                                            }
                                        });
                                    }
                                    else {
                                        Toast.makeText(RecordAudio.this, "Time capsule failed to send.", Toast.LENGTH_SHORT).show();
                                        btnConfirm.setEnabled(true);
                                    }
                                }
                            });
                }
            }
        });
    }
}