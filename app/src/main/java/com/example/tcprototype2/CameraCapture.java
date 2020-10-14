package com.example.tcprototype2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CameraCapture extends AppCompatActivity {
    ImageView imgPreview;
    Button btnSend;
    Bundle extras;
    TextView txtTitle, txtRecipient, txtDate;
    ProgressBar progressBar;
    String currentImagePath = null;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_capture);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imgPreview = findViewById(R.id.img_pic_preview);
        btnSend = findViewById(R.id.btn_send_img);
        txtTitle = findViewById(R.id.txt_img_title);
        txtRecipient = findViewById(R.id.txt_img_recipient);
        txtDate = findViewById(R.id.txt_img_date);
        progressBar = findViewById(R.id.pg_upload_img);

        // Get extras
        Intent previous = getIntent();
        extras = previous.getExtras();

        final Date openDate = (Date) extras.getSerializable("open_date");
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy 'at' HH:mm z");
        String strDate = dateFormat.format(openDate);

        txtTitle.setText("Title: " + extras.getString("title"));
        txtRecipient.setText("Recipient: " + extras.getString("r_name"));
        txtDate.setText("Opening date: " + strDate);

        imageUri = Uri.parse(extras.getString("image_uri"));
        try {
            Bitmap bitmap = handleSamplingAndRotationBitmap(this, imageUri);
            imgPreview.setImageBitmap(bitmap);
        } catch (Exception e){
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
        //imgPreview.setImageURI(imageUri);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri != null) {
                    btnSend.setEnabled(false);
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    final String uid = user.getUid();

                    // Create reference to media file's path in storage
                    final String filename = UUID.randomUUID().toString();
                    String path = "images/" + "/" + uid + "/" + filename + ".jpg";
                    final StorageReference storageReference = storage.getReference(path);

                    // Upload to storage
                    UploadTask uploadTask = storageReference.putFile(imageUri);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CameraCapture.this, "Upload failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    }).addOnSuccessListener(CameraCapture.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(CameraCapture.this, "File upload complete.", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }).addOnProgressListener(CameraCapture.this, new OnProgressListener<UploadTask.TaskSnapshot>() {
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
                            return storageReference.getDownloadUrl();
                        }
                    });
                    getDownloadUriTask.addOnCompleteListener(CameraCapture.this, new OnCompleteListener<Uri>() {
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
                                timeCapsule.put("type", "image");
                                timeCapsule.put("filename", filename);
                                timeCapsule.put("uri", downloadUri.toString());
                                timeCapsule.put("status", "unopened");

                                // Add time capsule to database
                                db.collection("timecapsules")
                                        .add(timeCapsule)
                                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if (task.isSuccessful()){
                                                    // Add randomly generated document id to a field
                                                    DocumentReference capsuleRef = task.getResult();
                                                    capsuleRef.update("id", capsuleRef.getId())
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(CameraCapture.this, "Time capsule was sent successfully!", Toast.LENGTH_SHORT).show();
                                                            // Return to home screen
                                                            Intent home = new Intent(getApplicationContext(), MainActivity.class);
                                                            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                            startActivity(home);
                                                        }
                                                    });
                                                }
                                                else {
                                                    Toast.makeText(CameraCapture.this, "Time capsule failed to send.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(CameraCapture.this, "Nothing to upload...", Toast.LENGTH_SHORT).show();
                    btnSend.setEnabled(true);
                }
            }
        });
    }


    /*
    * Functions that detects if image from camera should be rotated for proper display of the image,
    * and handles image resizing.
    * Code sourced from:
    * https://stackoverflow.com/questions/14066038/why-does-an-image-captured-using-camera-intent-gets-rotated-on-some-devices-on-a
    */
    public static Bitmap handleSamplingAndRotationBitmap(Context context, Uri selectedImage)
            throws IOException {
        int MAX_HEIGHT = 1024;
        int MAX_WIDTH = 1024;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(context, img, selectedImage);
        return img;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }
}