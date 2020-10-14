package com.example.tcprototype2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class ImageContent extends AppCompatActivity {
    private ImageView imageView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_content);

        imageView = findViewById(R.id.img_capsule);
        progressBar = findViewById(R.id.pg_download_img);

        // Get extras
        Intent intent = getIntent();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl(intent.getStringExtra("url"));
        String filename = intent.getStringExtra("filename");

        // Download image
        try {
            final File localFile = File.createTempFile(filename, "jpg");
            storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    progressBar.setVisibility(View.GONE);
                    imageView.setImageURI(Uri.fromFile(localFile));
                    imageView.setVisibility(View.VISIBLE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ImageContent.this, "Download failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
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