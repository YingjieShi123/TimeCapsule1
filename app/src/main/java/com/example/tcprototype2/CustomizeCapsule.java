package com.example.tcprototype2;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomizeCapsule extends AppCompatActivity {
    Button btnPhoto, btnVideo, btnAudio, btnWrite;
    Intent intent;
    Bundle extras;
    String imageName;
    private Uri videoUri = null;
    private Uri imageUri = null;
    private static int IMAGE_REQUEST = 100;
    private static int GALLERY_IMAGE_REQUEST = 101;
    private static int VIDEO_REQUEST = 200;
    private static int GALLERY_VIDEO_REQUEST = 201;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_capsule);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get intent extras
        intent = getIntent();
        extras = intent.getExtras();

        btnPhoto = findViewById(R.id.btn_capture_img);
        btnVideo = findViewById(R.id.btn_capture_vid);
        btnAudio = findViewById(R.id.btn_record);
        btnWrite = findViewById(R.id.btn_write);

        // Capture new image with camera, or select image file from device
        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] options = { "Capture new image", "Select existing image from gallery", "Cancel" };
                // Create alert dialog with options
                AlertDialog.Builder builder = new AlertDialog.Builder(CustomizeCapsule.this);
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
                                    imageUri = FileProvider.getUriForFile(CustomizeCapsule.this, "com.example.tcprototype2.fileprovider", imageFile);
                                    captureImage.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                                    startActivityForResult(captureImage, IMAGE_REQUEST);
                                }
                            }
                        }
                        else if (options[item].equals("Select existing image from gallery")) {
                            Intent pickImage = new Intent(Intent.ACTION_PICK);
                            pickImage.setType("image/*");
                            startActivityForResult(Intent.createChooser(pickImage, "Select an image"), GALLERY_IMAGE_REQUEST);
                        }
                        else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });

        // Record new video using camera, or select video clip file from device
        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] options = { "Record new video clip", "Select existing video from gallery", "Cancel" };
                // Create alert dialog with options
                AlertDialog.Builder builder = new AlertDialog.Builder(CustomizeCapsule.this);
                builder.setTitle("Select video source");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Record new video clip")) {
                            Intent captureVideo = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                            if (captureVideo.resolveActivity(getPackageManager()) != null){
                                startActivityForResult(captureVideo, VIDEO_REQUEST);
                            }
                        }
                        else if (options[item].equals("Select existing video from gallery")) {
                            Intent pickVideo = new Intent(Intent.ACTION_PICK);
                            pickVideo.setType("video/*");
                            startActivityForResult(Intent.createChooser(pickVideo, "Select a video"), GALLERY_VIDEO_REQUEST);
                        }
                        else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });

        // Go to record audio activity
        btnAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(CustomizeCapsule.this, RecordAudio.class);
                intent.putExtras(extras);
                startActivity(intent);
            }
        });

        // Go to create text message activity
        btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(CustomizeCapsule.this, TextMessage.class);
                intent.putExtras(extras);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check which intent result was received
        if (requestCode == IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                // Proceed to confirmation screen
                Intent next = new Intent(this, CameraCapture.class);
                extras.putString("content_type", "image");
                extras.putString("image_uri", imageUri.toString());
                next.putExtras(extras);
                startActivity(next);
            }
            else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Image capture cancelled.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Failed to capture image.", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == GALLERY_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                imageUri = data.getData();
                Intent next = new Intent(this, CameraCapture.class);
                extras.putString("content_type", "image");
                extras.putString("image_uri", imageUri.toString());
                next.putExtras(extras);
                startActivity(next);
            }
            else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Image selection from gallery cancelled.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Failed to retrieve image from gallery.", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == VIDEO_REQUEST) {
            if (resultCode == RESULT_OK) {
                videoUri = data.getData();
                // Proceed to confirmation screen
                Intent next = new Intent(this, PreviewVideo.class);
                extras.putString("content_type", "video");
                extras.putString("video_uri", videoUri.toString());
                next.putExtras(extras);
                startActivity(next);
            }
            else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Video recording cancelled.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Failed to record video.", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == GALLERY_VIDEO_REQUEST) {
            if (resultCode == RESULT_OK) {
                videoUri = data.getData();
                // Proceed to confirmation screen
                Intent next = new Intent(this, PreviewVideo.class);
                extras.putString("content_type", "video");
                extras.putString("video_uri", videoUri.toString());
                next.putExtras(extras);
                startActivity(next);
            }
            else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Video selection from gallery cancelled.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Failed to retrieve video from gallery.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Creates a timestamped file in local directory
    private File getImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageName = "tc_img_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageName, ".jpg", storageDir);
        return imageFile;
    }
}