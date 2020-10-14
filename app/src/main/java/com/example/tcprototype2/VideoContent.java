package com.example.tcprototype2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class VideoContent extends AppCompatActivity {
    private VideoView videoView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_content);

        videoView = findViewById(R.id.vid_view_content);
        progressBar = findViewById(R.id.pg_download_vid);

        Intent intent = getIntent();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl(intent.getStringExtra("url"));
        String filename = intent.getStringExtra("filename");

        // Check if file already exists (already downloaded)

        // Create temp file
        try {
            final File localFile = File.createTempFile(filename, "3gp");
            storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    progressBar.setVisibility(View.GONE);
                    videoView.setVideoURI(Uri.fromFile(localFile));
                    videoView.start();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(VideoContent.this, "Download failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull FileDownloadTask.TaskSnapshot snapshot) {
                    //Toast.makeText(VideoContent.this, "Downloading time capsule content.", Toast.LENGTH_SHORT).show();
                    long fileSize = snapshot.getTotalByteCount();
                    long downloadBytes = snapshot.getBytesTransferred();
                    long progress = (100 * downloadBytes) / fileSize;
                    progressBar.setProgress((int) progress);
                    progressBar.setVisibility(View.VISIBLE);
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Failed to create temp file: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }
}