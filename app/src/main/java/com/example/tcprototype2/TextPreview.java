package com.example.tcprototype2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.view.View;
import android.widget.TextView;

public class TextPreview extends AppCompatActivity {
    ConstraintLayout previewLayout;
    String backgrounds[] = {"autumn", "blue_sky_clouds", "blurry_lights", "floral", "green_leaves", "hearts", "meadow", "party", "rainbow_gradient", "snowflakes", "space"};
    String colours[] = {"#0d0c0c", "#0322ab", "#593000", "#38a125", "#5e5e5e", "#d1730f", "#f589c6", "#610a94", "#b81d0f", "#008080", "#ebd300", "#ffffff"};
    TextView txtMsgBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_preview);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String msgBody;
        int clr, bg;

        txtMsgBody = findViewById(R.id.txt_preview_message);
        previewLayout = findViewById(R.id.layout_text_preview);

        Intent intent = getIntent();
        msgBody = intent.getStringExtra("message");
        clr = intent.getIntExtra("colour", 0);
        bg = intent.getIntExtra("background", 0);

        // Set text and colour
        txtMsgBody.setText(msgBody);
        txtMsgBody.setTextColor(Color.parseColor(colours[clr]));
        // Set background
        if (bg > 0){
            int id = this.getResources().getIdentifier("mipmap/" + backgrounds[bg - 1], null, this.getPackageName());
            previewLayout.setBackgroundResource(id);
        }

    }
}