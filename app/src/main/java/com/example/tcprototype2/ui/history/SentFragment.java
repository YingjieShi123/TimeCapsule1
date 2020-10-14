package com.example.tcprototype2.ui.history;

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

import com.example.tcprototype2.AudioContent;
import com.example.tcprototype2.ImageContent;
import com.example.tcprototype2.R;
import com.example.tcprototype2.TextPreview;
import com.example.tcprototype2.VideoContent;
import com.example.tcprototype2.datamodel.TimeCapsule;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SentFragment extends Fragment {
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter adapter;

    public SentFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sent, container, false);

        db = FirebaseFirestore.getInstance();
        recyclerView = v.findViewById(R.id.rv_sent);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Query database for time capsules where the logged in user is the sender
        Query query = db.collection("timecapsules")
                .whereEqualTo("sender", user.getUid())
                .orderBy("created", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<TimeCapsule> options = new FirestoreRecyclerOptions.Builder<TimeCapsule>()
                .setQuery(query, TimeCapsule.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<TimeCapsule, SentFragment.SentViewHolder>(options) {
            @NonNull
            @Override
            public SentFragment.SentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_row, parent, false);
                return new SentFragment.SentViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull SentFragment.SentViewHolder holder, int position, @NonNull final TimeCapsule model) {
                // Format dates
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy 'at' HH:mm z");
                String openingDate = formatter.format(model.getOpening().toDate());
                String createdDate = formatter.format(model.getCreated().toDate());

                String recipientText = "Recipient: " + model.getR_name();
                String openText = "Date to be opened: " + openingDate;
                String sendText = "Date created: " + createdDate;
                holder.tv_title.setText(model.getTitle());
                holder.tv_recipient.setText(recipientText);
                holder.tv_open_date.setText(openText);
                holder.tv_send_date.setText(sendText);

                // The sender (this logged in user) can view sent time capsules regardless of opening date
                holder.rowLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent;
                        // Check content type, start appropriate display activity
                        String type = model.getType();
                        switch (type){
                            case "image":
                                intent = new Intent(getContext(), ImageContent.class);
                                intent.putExtra("url", model.getUri());
                                intent.putExtra("filename", model.getFilename());
                                startActivity(intent);
                                break;
                            case "video":
                                intent = new Intent(getContext(), VideoContent.class);
                                intent.putExtra("url", model.getUri());
                                intent.putExtra("filename", model.getFilename());
                                startActivity(intent);
                                break;
                            case "audio":
                                intent = new Intent(getContext(), AudioContent.class);
                                intent.putExtra("url", model.getUri());
                                intent.putExtra("filename", model.getFilename());
                                startActivity(intent);
                                break;
                            case "text":
                                intent = new Intent(getContext(), TextPreview.class);
                                intent.putExtra("message", model.getMessage());
                                intent.putExtra("colour", model.getColour());
                                intent.putExtra("background", model.getBackground());
                                startActivity(intent);
                                break;
                            default:
                                Toast.makeText(getContext(), "Error: unable to open time capsule - unknown content.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        };

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        return v;
    }

    private static class SentViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_title, tv_recipient, tv_send_date, tv_open_date;
        LinearLayout rowLayout;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_title = itemView.findViewById(R.id.txt_hist_title);
            tv_recipient = itemView.findViewById(R.id.txt_hist_user);
            tv_send_date = itemView.findViewById(R.id.txt_hist_sent_date);
            tv_open_date = itemView.findViewById(R.id.txt_hist_open_date);
            rowLayout = itemView.findViewById(R.id.history_row_layout);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}