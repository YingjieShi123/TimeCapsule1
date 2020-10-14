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
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ReadyFragment extends Fragment {
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter adapter;
    private Timestamp now;
    private Intent next;

    public ReadyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_ready, container, false);

        db = FirebaseFirestore.getInstance();
        recyclerView = v.findViewById(R.id.rv_ready);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        now = Timestamp.now();

        // Query database for unopened time capsules where the logged in user is the recipient and
        // opening date is past current time
        Query query = db.collection("timecapsules")
                .whereEqualTo("recipient", user.getUid())
                .whereEqualTo("status", "unopened")
                .whereLessThanOrEqualTo("opening", now)
                .orderBy("opening", Query.Direction.ASCENDING);

        // Populate recycler view with query results
        FirestoreRecyclerOptions<TimeCapsule> options = new FirestoreRecyclerOptions.Builder<TimeCapsule>()
                .setQuery(query, TimeCapsule.class)
                .build();

        // Adapter for recycler view
        adapter = new FirestoreRecyclerAdapter<TimeCapsule, ReadyViewHolder>(options) {
            @NonNull
            @Override
            public ReadyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_row, parent, false);
                return new ReadyViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ReadyViewHolder holder, int position, @NonNull final TimeCapsule model) {
                // Format dates
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy 'at' HH:mm z");
                String openingDate = formatter.format(model.getOpening().toDate());
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
                        // Check content type, start appropriate display activity
                        String type = model.getType();
                        switch (type){
                            case "image":
                                next = new Intent(getContext(), ImageContent.class);
                                next.putExtra("url", model.getUri());
                                next.putExtra("filename", model.getFilename());
                                setStatusOpen(model.getId());
                                break;
                            case "video":
                                next = new Intent(getContext(), VideoContent.class);
                                next.putExtra("url", model.getUri());
                                next.putExtra("filename", model.getFilename());
                                setStatusOpen(model.getId());
                                break;
                            case "audio":
                                next = new Intent(getContext(), AudioContent.class);
                                next.putExtra("url", model.getUri());
                                next.putExtra("filename", model.getFilename());
                                setStatusOpen(model.getId());
                                break;
                            case "text":
                                next = new Intent(getContext(), TextPreview.class);
                                next.putExtra("message", model.getMessage());
                                next.putExtra("colour", model.getColour());
                                next.putExtra("background", model.getBackground());
                                setStatusOpen(model.getId());
                                break;
                            default:
                                Toast.makeText(getContext(), "Error: unable to open time capsule - unknown content.", Toast.LENGTH_SHORT).show();
                        }
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
    private static class ReadyViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_title, tv_sender, tv_send_date, tv_open_date;
        LinearLayout rowLayout;

        public ReadyViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_title = itemView.findViewById(R.id.txt_hist_title);
            tv_sender = itemView.findViewById(R.id.txt_hist_user);
            tv_send_date = itemView.findViewById(R.id.txt_hist_sent_date);
            tv_open_date = itemView.findViewById(R.id.txt_hist_open_date);
            rowLayout = itemView.findViewById(R.id.history_row_layout);
        }
    }

    // Update selected time capsule status to opened, start activity to view capsule
    private void setStatusOpen (String id) {
        db.collection("timecapsules").document(id)
                .update("status", "opened")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                            startActivity(next);
                        else
                            Toast.makeText(getContext(), "Error: could not connect to the database server.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        now = Timestamp.now();
        adapter.startListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        now = Timestamp.now();
        adapter.startListening();
    }

    @Override
    public void onPause() {
        super.onPause();
        adapter.stopListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}