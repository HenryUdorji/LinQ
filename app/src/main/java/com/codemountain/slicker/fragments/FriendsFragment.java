package com.codemountain.slicker.fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.codemountain.slicker.R;
import com.codemountain.slicker.activities.ChatActivity;
import com.codemountain.slicker.activities.MainActivity;
import com.codemountain.slicker.model.ModelFriends;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUserDatabase;

    FirebaseRecyclerOptions<ModelFriends> options;
    FirebaseRecyclerAdapter<ModelFriends, FriendsViewHolder> adapter;

    private RecyclerView recyclerView;

    private String currentUserId;
    private View mView;

    private String userName;


    private ProgressDialog loadingProgress;



    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_friends, container, false);

        //Init Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        currentUser = mAuth.getCurrentUser();

        recyclerView = mView.findViewById(R.id.friendsRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserId);
        mFriendsDatabase.keepSynced(true);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase.keepSynced(true);


        loadingProgress = new ProgressDialog(getActivity());
        loadingProgress.setMessage("loading friends");
        loadingProgress.setCanceledOnTouchOutside(false);

        return mView;
    }


    @Override
    public void onStart() {
        super.onStart();

        loadingProgress.show();

        options = new FirebaseRecyclerOptions.Builder<ModelFriends>()
                .setQuery(mFriendsDatabase, ModelFriends.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<ModelFriends, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull final ModelFriends model) {

                final String otherPersonId = getRef(position).getKey();
                mUserDatabase.child(otherPersonId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String bio = dataSnapshot.child("bio").getValue().toString();
                        userName = dataSnapshot.child("name").getValue().toString();
                        String image = dataSnapshot.child("image").getValue().toString();

                        //Checking if a user is online
                        /*
                        if(dataSnapshot.hasChild("online")){

                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            if(userOnline.equals("true")){

                                holder.onlineImageView.setVisibility(View.VISIBLE);
                            }
                            else {

                                holder.onlineImageView.setVisibility(View.GONE);
                            }


                        }
*/

                        holder.bioTextView.setText(bio);
                        holder.nameTextView.setText(userName);

                        if (getActivity() == null){
                            return;
                        }
                        Glide.with(getActivity()).load(image)
                                .placeholder(R.drawable.default_image)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(holder.usersImageView);

                        loadingProgress.dismiss();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                        loadingProgress.dismiss();
                    }
                });

                //handling clicks in the recyclerView
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent userProfileIntent = new Intent(getActivity(), ChatActivity.class);
                        userProfileIntent.putExtra("otherPersonId", otherPersonId);
                        userProfileIntent.putExtra("userName", userName);
                        startActivity(userProfileIntent);

                    }
                });
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_users, parent, false);
                return new FriendsViewHolder(v);
            }
        };

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Start Listening
        if(adapter != null){
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if(adapter != null){
            adapter.stopListening();
        }
    }


    //Viewholder class
    private class FriendsViewHolder extends RecyclerView.ViewHolder{

        View view;
        private CircleImageView usersImageView;
        private TextView nameTextView, bioTextView;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            usersImageView = itemView.findViewById(R.id.requestUsersImageView);
            nameTextView = itemView.findViewById(R.id.requestUserNameTextView);
            bioTextView = itemView.findViewById(R.id.requestBioTextView);

            view = itemView;
        }
    }

    private void checkUserStatus() {

        currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //User is signed in stay here
        }
        else {

            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }


    /**
     * Inflating menu-item
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_save).setVisible(false);

        //SearchView
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                if(!TextUtils.isEmpty(s.trim())){

                }
                else {

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                if(!TextUtils.isEmpty(s.trim())){

                }
                else {

                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }


    /**
     * Click listener for menu-item
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_logout:

                mAuth.signOut();
                checkUserStatus();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
