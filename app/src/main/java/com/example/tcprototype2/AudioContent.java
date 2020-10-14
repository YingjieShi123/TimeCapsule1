package com.example.tcprototype2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class AudioContent extends AppCompatActivity {
    private Button btnPlay;
    private ProgressBar progressBar;
    private SeekBar seekBar;
    private MediaPlayer player;
    private Runnable runnable;
    private Handler handler;
    private Boolean complete = false;
    private File localFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_content);

        btnPlay = findViewById(R.id.btn_aud_play);
        progressBar = findViewById(R.id.pg_download_aud);
        seekBar = findViewById(R.id.seek_capsule_playback);
        handler = new Handler();

        // Get extras
        Intent intent = getIntent();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl(intent.getStringExtra("url"));
        String filename = intent.getStringExtra("filename");

        try {
            localFile = File.createTempFile(filename, "3gp");
            // Download from storage to file
            storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    progressBar.setVisibility(View.GONE);

                    // Set up media player
                    setupPlayer();

                    // Set up seek bar
                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (fromUser) {
                                player.seekTo(progress);
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {}

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {}
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AudioContent.this, "Download failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull FileDownloadTask.TaskSnapshot snapshot) {
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

        // Button to pause, resume or replay audio
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player == null) {
                    setupPlayer();
                    btnPlay.setText("Pause");
                }
                if (player != null && player.isPlaying()){
                    player.pause();
                    btnPlay.setText("Resume");
                }
                else if (player != null && !player.isPlaying()){
                    player.start();
                    btnPlay.setText("Pause");
                }
                else if (complete){
                    player.seekTo(0);
                    player.start();
                    changeSeekBar();
                    btnPlay.setText("Pause");
                    complete = false;
                }
            }
        });
    }

    // Set media player to play audio file
    private void setupPlayer() {
        player = MediaPlayer.create(AudioContent.this, Uri.fromFile(localFile));
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                seekBar.setMax(player.getDuration());
                player.start();
                btnPlay.setText("Pause");
                changeSeekBar();
            }
        });
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnPlay.setText("Replay");
                complete = true;
                //seekBar.setProgress(0);
            }
        });
    }

    private void changeSeekBar(){
        seekBar.setProgress(player.getCurrentPosition());
        if (player.isPlaying()){
            runnable = new Runnable() {
                @Override
                public void run() {
                    changeSeekBar();
                }
            };
            handler.postDelayed(runnable, 100);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.release();
            player = null;
        }
    }

}