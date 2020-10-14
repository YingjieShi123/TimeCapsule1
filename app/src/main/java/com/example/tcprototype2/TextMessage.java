package com.example.tcprototype2;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// Allows users to write text messages and customise the message appearance
public class TextMessage extends AppCompatActivity {
    EditText edtMsg;
    TextView txtTitle, txtDate, txtRecipient;
    Spinner spinnerColour, spinnerBackground;
    Button btnPreview, btnConfirm;
    String message;
    Bundle extras;
    int selectedColour, selectedBackground;
    Date openDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_message);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        spinnerColour = findViewById(R.id.spin_text_colour);
        spinnerBackground = findViewById(R.id.spin_background);
        edtMsg = findViewById(R.id.txt_msg);
        txtTitle = findViewById(R.id.txt_aud_title);
        txtDate = findViewById(R.id.txt_aud_date);
        txtRecipient = findViewById(R.id.txt_aud_recipient);
        btnPreview = findViewById(R.id.btn_text_preview);
        btnConfirm = findViewById(R.id.btn_msg_send);

        // Get extras
        Intent previous = getIntent();
        extras = previous.getExtras();

        openDate = (Date) extras.getSerializable("open_date");
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy 'at' HH:mm z");
        String strDate = dateFormat.format(openDate);

        txtTitle.setText("Title: " + extras.getString("title"));
        txtRecipient.setText("Recipient: " + extras.getString("r_name"));
        txtDate.setText("Opening date: " + strDate);

        // Set spinner contents
        ArrayAdapter<CharSequence> colourAdapter = ArrayAdapter.createFromResource(this, R.array.colours, android.R.layout.simple_spinner_item);
        spinnerColour.setAdapter(colourAdapter);
        ArrayAdapter<CharSequence> backgroundAdapter = ArrayAdapter.createFromResource(this, R.array.backgrounds, android.R.layout.simple_spinner_item);
        spinnerBackground.setAdapter(backgroundAdapter);

        btnPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message = edtMsg.getText().toString();
                selectedColour = spinnerColour.getSelectedItemPosition();
                selectedBackground = spinnerBackground.getSelectedItemPosition();
                // Start text message preview activity with selected options
                Intent intent = new Intent(TextMessage.this, TextPreview.class);
                intent.putExtra("message", message);
                intent.putExtra("colour", selectedColour);
                intent.putExtra("background", selectedBackground);
                startActivity(intent);
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (message.isEmpty()) {
                    Toast.makeText(TextMessage.this, "Message is empty.", Toast.LENGTH_SHORT).show();
                }
                else {
                    // Create confirmation alert dialog box
                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(TextMessage.this);
                    alertDialog.setTitle("Send confirmation");
                    alertDialog.setMessage("Ready to send time capsule? Confirm text message appearance with the Preview Message button.");
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

    private void createTimeCapsule() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Object> timeCapsule = new HashMap<>();
        timeCapsule.put("sender", user.getUid());
        timeCapsule.put("s_name", user.getDisplayName());
        timeCapsule.put("recipient", extras.getString("r_uid"));
        timeCapsule.put("r_name", extras.getString("r_name"));
        timeCapsule.put("created", new Timestamp(new Date()));
        timeCapsule.put("opening", new Timestamp(openDate));
        timeCapsule.put("title", extras.getString("title"));
        timeCapsule.put("type", "text");
        timeCapsule.put("message", message);
        timeCapsule.put("colour", selectedColour);
        timeCapsule.put("background", selectedBackground);
        timeCapsule.put("status", "unopened");

        // Add timecapsule to database
        db.collection("timecapsules").add(timeCapsule)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()){
                            // Add randomly generated document id to a field
                            DocumentReference capsuleRef = task.getResult();
                            capsuleRef.update("id", capsuleRef.getId()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(TextMessage.this, "Time capsule was sent successfully!", Toast.LENGTH_SHORT).show();
                                    // Return to home screen
                                    Intent home = new Intent(getApplicationContext(), MainActivity.class);
                                    home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(home);
                                }
                            });
                        }
                        else {
                            Toast.makeText(TextMessage.this, "Time capsule failed to send.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}