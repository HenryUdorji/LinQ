package com.codemountain.slicker.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.codemountain.slicker.R;
import com.codemountain.slicker.activities.UserProfileActivity;
import com.codemountain.slicker.model.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {

    private Context context;
    private List<ModelUsers> usersList;
    private String CURRENT_STATE, senderUserId, otherPersonId, uid;
    private OnItemClickListener listener;

    /*private DatabaseReference rootReference;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;*/

    public UsersAdapter(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_users, parent, false);
        return new UsersViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull final UsersViewHolder holder, int position) {

        otherPersonId = usersList.get(position).getUid();
        final String username = usersList.get(position).getName();
        String userImage = usersList.get(position).getImage();
        String bio = usersList.get(position).getBio();

        holder.nameTextView.setText(username);
        holder.bioTextView.setText(bio);
        /*try{
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.default_image)
                    .into(holder.usersImageView);
        }
        catch (Exception e){
            Picasso.get().load(R.drawable.default_image).into(holder.usersImageView);
        }*/
        Glide.with(context).load(userImage)
                .placeholder(R.drawable.default_image)
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.usersImageView);

       /* holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent userProfileIntent = new Intent(context, UserProfileActivity.class);
                userProfileIntent.putExtra("otherPersonId", otherPersonId);
                context.startActivity(userProfileIntent);

            }
        });*/

        /*holder.addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth = FirebaseAuth.getInstance();
                currentUser = mAuth.getCurrentUser();
                senderUserId = mAuth.getUid();
                maintenanceOfButtons(holder);

                CURRENT_STATE = "not_friends";
                if(!senderUserId.equals(otherPersonId)){

                    if(CURRENT_STATE.equals("not_friends")){
                        sendFriendRequest(otherPersonId, holder);
                    }
                    if(CURRENT_STATE.equals("request_sent")){
                        cancelFriendRequest(otherPersonId, holder);
                    }
                    if(CURRENT_STATE.equals("friends")){
                        unFriendExistingFriend(otherPersonId, holder);
                    }

                }
            }
        });*/
    }

   /* private void unFriendExistingFriend(String otherPersonId, final UsersViewHolder holder) {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        senderUserId = mAuth.getUid();
        rootReference = FirebaseDatabase.getInstance().getReference();
        rootReference.keepSynced(true);

        Map unFriendMap = new HashMap();
        unFriendMap.put("Friends/" + senderUserId + "/" + otherPersonId , null);
        unFriendMap.put("Friends/" + otherPersonId + "/" + senderUserId , null);

        rootReference.updateChildren(unFriendMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                if(databaseError == null){
                    CURRENT_STATE = "not_friends";
                    holder.addFriendButton.setText("Add friend");
                }
            }
        });
    }

    private void cancelFriendRequest(String otherPersonId, final UsersViewHolder holder) {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        senderUserId = mAuth.getUid();
        rootReference = FirebaseDatabase.getInstance().getReference();
        rootReference.keepSynced(true);

        Map friendRequestMap = new HashMap();
        friendRequestMap.put("Friend_request/" + senderUserId + "/" + otherPersonId +  "/request_type", null);
        friendRequestMap.put("Friend_request/" + otherPersonId + "/" + senderUserId +  "/request_type", null);

        rootReference.updateChildren(friendRequestMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                if(databaseError == null){
                    CURRENT_STATE = "not_friends";
                    holder.addFriendButton.setText("Add friend");
                }
            }
        });
    }

    private void sendFriendRequest(String otherPersonId, final UsersViewHolder holder) {

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        senderUserId = mAuth.getUid();
        rootReference = FirebaseDatabase.getInstance().getReference();
        rootReference.keepSynced(true);

        Map friendRequestMap = new HashMap();
        friendRequestMap.put("Friend_request/" + senderUserId + "/" + otherPersonId +  "/request_type", "sent");
        friendRequestMap.put("Friend_request/" + otherPersonId + "/" + senderUserId +  "/request_type", "received");

        rootReference.updateChildren(friendRequestMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                if(databaseError == null){
                    CURRENT_STATE = "request_sent";
                    holder.addFriendButton.setText("Cancel");
                }
            }
        });
    }

    //maintaining the sendFriendRequestButton && declineFriendRequestButton
    private void maintenanceOfButtons(final UsersViewHolder holder) {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        senderUserId = mAuth.getUid();
        rootReference = FirebaseDatabase.getInstance().getReference();
        rootReference.keepSynced(true);

        rootReference.child("Friend_request").child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(otherPersonId)){
                    String requestType = dataSnapshot.child(otherPersonId).child("request_type").getValue().toString();

                    if(requestType.equals("sent")){
                        CURRENT_STATE = "request_sent";
                        holder.addFriendButton.setText("Cancel");
                    }
                    else if(requestType.equals("received")){
                        CURRENT_STATE = "request_received";
                        holder.addFriendButton.setText("Confirm");
                    }
                }
                else {

                    rootReference.child("Friends").child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(otherPersonId)){
                                CURRENT_STATE = "friends";
                                holder.addFriendButton.setText("UnFriend");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private CircleImageView usersImageView;
        private TextView nameTextView, bioTextView;
        private OnItemClickListener listener;
        //private Button addFriendButton;

        public UsersViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            itemView.setOnClickListener(this);
            usersImageView = itemView.findViewById(R.id.requestUsersImageView);
            nameTextView = itemView.findViewById(R.id.requestUserNameTextView);
            bioTextView = itemView.findViewById(R.id.requestBioTextView);
            this.listener = listener;
        }

        @Override
        public void onClick(View v) {
            listener.onItemClick(v, getAdapterPosition());
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
    }

}
