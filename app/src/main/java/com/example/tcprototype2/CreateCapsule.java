package com.example.tcprototype2;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tcprototype2.datamodel.TCUser;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class CreateCapsule extends AppCompatActivity {
    RecyclerView recyclerView;
    //String recipients[];
    //int icons = R.drawable.profile_icon;

    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_capsule);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //getActionBar().setTitle("Select recipient");
        // Connect to database to retrieve friends list
        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.rv_recipient);
        //recipients = getResources().getStringArray(R.array.friends);

        Query query = db.collection("users");
        FirestoreRecyclerOptions<TCUser> options = new FirestoreRecyclerOptions.Builder<TCUser>()
                .setQuery(query, TCUser.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<TCUser, RecipientViewHolder>(options) {
            @NonNull
            @Override
            public RecipientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipient_row, parent, false);
                return new RecipientViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull RecipientViewHolder holder, int position, @NonNull final TCUser model) {
                holder.tv_name.setText(model.getName());
                holder.tv_email.setText(model.getEmail());
                holder.img_dp.setImageURI(model.getDisplayPic());

                holder.rowLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Pass recipient data to next activity
                        Bundle extras = new Bundle();
                        extras.putString("r_uid", model.getUid());
                        extras.putString("r_name", model.getName());

                        Intent intent = new Intent(CreateCapsule.this, DateCapsule.class);
                        intent.putExtras(extras);
                        startActivity(intent);
                    }
                });
            }
        };

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private class RecipientViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_name;
        private TextView tv_email;
        private ImageView img_dp;
        LinearLayout rowLayout;

        public RecipientViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_name = itemView.findViewById(R.id.txt_rc_name);
            tv_email = itemView.findViewById(R.id.txt_rc_email);
            img_dp = itemView.findViewById(R.id.img_rc_icon);
            rowLayout = itemView.findViewById(R.id.recipient_row_layout);
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