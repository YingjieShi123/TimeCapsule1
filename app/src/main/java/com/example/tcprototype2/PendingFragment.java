package com.example.tcprototype2;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tcprototype2.datamodel.TimeCapsule;
import com.example.tcprototype2.ui.history.ReceivedFragment;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PendingFragment extends Fragment {
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter adapter;
    private Timestamp now;

    public PendingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pending, container, false);

        db = FirebaseFirestore.getInstance();
        recyclerView = v.findViewById(R.id.rv_pending);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        now = Timestamp.now();

        // Query database for unopened time capsules where the logged in user is the recipient and not ready to be opened
        Query query = db.collection("timecapsules")
                .whereEqualTo("recipient", user.getUid())
                .whereEqualTo("status", "unopened")
                .whereGreaterThan("opening", now)
                .orderBy("opening", Query.Direction.ASCENDING);

        // Populate recycler view with query results
        FirestoreRecyclerOptions<TimeCapsule> options = new FirestoreRecyclerOptions.Builder<TimeCapsule>()
                .setQuery(query, TimeCapsule.class)
                .build();

        // Adapter for recycler view
        adapter = new FirestoreRecyclerAdapter<TimeCapsule, PendingViewHolder>(options) {
            @NonNull
            @Override
            public PendingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_row, parent, false);
                return new PendingViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull PendingViewHolder holder, int position, @NonNull final TimeCapsule model) {
                // Format dates
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy 'at' HH:mm z");
                final String openingDate = formatter.format(model.getOpening().toDate());
                String createdDate = formatter.format(model.getCreated().toDate());

                String senderText = "Sender: " + model.getS_name();
                String openText = "Date to be opened: " + openingDate;
                String sendText = "Date created: " + createdDate;
                holder.tv_title.setText(model.getTitle());
                holder.tv_sender.setText(senderText);
                holder.tv_open_date.setText(openText);
                holder.tv_send_date.setText(sendText);

                holder.rowLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Show time remaining until the selected capsule can be opened
                        getTimeRemaining(model.getOpening().toDate());
                    }
                });
            }
        };

        // Set adapter to recycler view
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        return v;
    }

    // View holder for each row in recycler view
    private static class PendingViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_title, tv_sender, tv_send_date, tv_open_date;
        LinearLayout rowLayout;

        public PendingViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_title = itemView.findViewById(R.id.txt_hist_title);
            tv_sender = itemView.findViewById(R.id.txt_hist_user);
            tv_send_date = itemView.findViewById(R.id.txt_hist_sent_date);
            tv_open_date = itemView.findViewById(R.id.txt_hist_open_date);
            rowLayout = itemView.findViewById(R.id.history_row_layout);
        }
    }

    // Function to calculate the time difference between two dates
    private void getTimeRemaining(Date openingDate) {
        // Seconds, minutes, hours and days in milliseconds for calculation
        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        Date now = new Date();
        // Convert both dates into milliseconds (long) and subtract
        long difference = openingDate.getTime() - now.getTime();

        // Extract seconds, minutes, hours and days from difference
        long remainingDays = difference / daysInMilli;
        difference = difference % daysInMilli;

        long remainingHours = difference / hoursInMilli;
        difference = difference % hoursInMilli;

        long remainingMinutes = difference / minutesInMilli;
        difference = difference % minutesInMilli;

        long remainingSeconds = difference / secondsInMilli;

        // Format result in string for display
        String timeRemaining = "Time capsule ready to be opened in:\n";
        if (remainingDays > 0)
            timeRemaining += remainingDays + " days, ";
        if (remainingHours > 0)
            timeRemaining += remainingHours + " hours, ";
        if (remainingMinutes > 0)
            timeRemaining += remainingMinutes + " minutes, ";
        timeRemaining += remainingSeconds + " seconds.";

        Toast.makeText(getContext(), timeRemaining, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        now = Timestamp.now();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}