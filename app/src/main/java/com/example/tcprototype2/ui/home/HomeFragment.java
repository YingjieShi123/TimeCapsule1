package com.example.tcprototype2.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.tcprototype2.CreateCapsule;
import com.example.tcprototype2.R;
import com.example.tcprototype2.UnopenedCapsules;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;

public class HomeFragment extends Fragment {
    private HomeViewModel homeViewModel;
    private FirebaseFirestore db;
    private String userId;
    private TextView txtReady, txtNotify;
    private ImageView imgOrange;
    private ListenerRegistration listenerRegistration;
    private Query query;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.textTimecapsule2);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        final Button btnCreate = root.findViewById(R.id.btn_create);
        final Button btnView = root.findViewById(R.id.btn_view);
        txtReady = root.findViewById(R.id.txt_ready);
        imgOrange = root.findViewById(R.id.img_orange);
        txtNotify = root.findViewById(R.id.txt_notification);
        imgOrange.bringToFront();
        txtReady.bringToFront();

        // Initialise database and time capsule query
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        query = db.collection("timecapsules")
                .whereEqualTo("recipient", userId)
                .whereEqualTo("status", "unopened");

        // Check for new capsules once
        checkNewCapsules();
        // Attach time capsule listener
        checkIncomingCapsules();

        // Start create new time capsule process
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CreateCapsule.class);
                startActivity(intent);
            }
        });

        // Start activity to view list of unopened capsules
        btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UnopenedCapsules.class);
                startActivity(intent);
            }
        });
        return root;
    }

    // Get number of unopened capsules addressed to user
    private void checkNewCapsules () {
        db.collection("timecapsules")
                .whereEqualTo("recipient", userId)
                .whereEqualTo("status", "unopened")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int ready = 0;
                            int notReady = 0;
                            Date opening;
                            Date timeNow = new Date();
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                opening = doc.getDate("opening");
                                if (timeNow.compareTo(opening) >= 0) {
                                    ready++;
                                } else {
                                    notReady++;
                                }
                            }
                            // Show numbers on screen
                            if (ready > 0) {
                                String text;
                                if (ready == 1) {
                                    text = ready + " new time capsule ready for opening.";
                                } else {
                                    text = ready + " new time capsules ready for opening.";
                                }
                                txtNotify.setText(text);
                                txtNotify.setVisibility(View.VISIBLE);
                            } else {
                                txtNotify.setVisibility(View.INVISIBLE);
                            }
                        }
                        else {
                            Toast.makeText(getContext(), "Unable to retrieve time capsule information. Check your network connection", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /*  Listener that checks for new time capsules addressed to the user when a new document is
        created in the time capsules collection in database */
    private void checkIncomingCapsules() {
        listenerRegistration = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(getContext(), "Could not retrieve live updates for new time capsules.", Toast.LENGTH_SHORT).show();
                    return;
                }

                int ready = 0;
                int notReady = 0;
                Date opening;
                Date timeNow = new Date();
                // For each result check if current time is past the opening date
                for (QueryDocumentSnapshot doc : value){
                    opening = doc.getDate("opening");
                    if (timeNow.compareTo(opening) >= 0) {
                        ready++;
                    } else {
                        notReady++;
                    }
                }
                // Update UI with numbers
                if (ready > 0) {
                    String text;
                    if (ready == 1) {
                        text = ready + " new time capsule ready for opening.";
                    } else {
                        text = ready + " new time capsules ready for opening.";
                    }
                    txtNotify.setText(text);
                    txtNotify.setVisibility(View.VISIBLE);
                } else {
                    txtNotify.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    // Detach listener if fragment is inactive to save resources
    @Override
    public void onPause() {
        super.onPause();
        listenerRegistration.remove();
    }
    @Override
    public void onStop() {
        super.onStop();
        listenerRegistration.remove();
    }

    // Reattach listener on resume
    @Override
    public void onResume() {
        super.onResume();
        checkNewCapsules();
        checkIncomingCapsules();
    }
}