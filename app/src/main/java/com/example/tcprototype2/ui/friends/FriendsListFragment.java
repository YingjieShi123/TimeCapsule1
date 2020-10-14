package com.example.tcprototype2.ui.friends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tcprototype2.R;
import com.example.tcprototype2.datamodel.TCUser;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FriendsListFragment extends Fragment {
    private static final String TAG = "Friends List";
    private RecyclerView recyclerView;
    private FloatingActionButton fab;

    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter adapter;
    
    public FriendsListFragment(){}

    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_friends_list, container, false);
        db = FirebaseFirestore.getInstance();
        recyclerView = v.findViewById(R.id.rv_friends_list);

        Query query = db.collection("users").orderBy("name");
        FirestoreRecyclerOptions<TCUser> options = new FirestoreRecyclerOptions.Builder<TCUser>()
                .setQuery(query, TCUser.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<TCUser, FriendsViewHolder>(options) {
            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_row, parent, false);
                return new FriendsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull FriendsViewHolder holder, int position, @NonNull TCUser model) {
                holder.tv_name.setText(model.getName());
                holder.tv_status.setText(model.getStatus());
                holder.img_dp.setImageURI(model.getDisplayPic());
            }
        };

        //View v = inflater.inflate(R.layout.fragment_friends_list, container, false);
        //FriendsListAdapter friendListAdapter = new FriendsListAdapter(getContext(), getFriendsList());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        fab = v.findViewById(R.id.fab_add_friend);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Add friend section to be added", Toast.LENGTH_SHORT).show();
            }
        });

        return v;
    }

    private class FriendsViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_name;
        private TextView tv_status;
        private ImageView img_dp;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_name = itemView.findViewById(R.id.txt_fl_name);
            tv_status = itemView.findViewById(R.id.txt_fl_status);
            img_dp = itemView.findViewById(R.id.img_fl_icon);
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

//    private List<TCUser> getFriendsList(){
//        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        final FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        final List<TCUser> friendsList = new ArrayList<>();
//
//        // Get list of friends from database
//        String uid = user.getUid();
//        final List<String> friendUids = new ArrayList<>();
//        db.collection("users").document(uid).collection("friends")
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()){
//                            //Toast.makeText(getActivity(), "got some friends", Toast.LENGTH_SHORT).show();
//                            for (QueryDocumentSnapshot document : task.getResult()){
//                                friendUids.add(document.getId());
//                            }
//
//                            // Retrieve each friend's details
//                            //TODO: retrieve display pic after fixing storage
//                            //friendsList = new ArrayList<>();
//                            for (String id : friendUids){
//                                DocumentReference mDocRef = FirebaseFirestore.getInstance().document("users/" + id);
//                                mDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                                    @Override
//                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                                        if (documentSnapshot.exists()){
//                                            //docList.add(documentSnapshot);
//                                            friendsList.add(new TCUser(documentSnapshot.getString("name"), documentSnapshot.getString("email"), user.getPhotoUrl()));
//                                            Toast.makeText(getActivity(), "name :" + friendsList.size(), Toast.LENGTH_SHORT).show();
//                                        }
//                                    }
//                                });
//                            }
//                            //Toast.makeText(getActivity(), "list size: " + friendsList.size(), Toast.LENGTH_SHORT).show();
//                        }
//                        else {
//                            Log.w(TAG, "read friends collection: failure");
//                            Toast.makeText(getActivity(), "Error: Friends list could not be retrieved at this moment.", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//
//        return friendsList;
//    }


    // onchange listener
}