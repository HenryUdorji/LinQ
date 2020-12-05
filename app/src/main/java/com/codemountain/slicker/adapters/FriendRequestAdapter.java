/*
package com.codemountain.slicker.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.codemountain.slicker.R;
import com.codemountain.slicker.activities.UserProfileActivity;
import com.codemountain.slicker.model.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.FriendRequestViewHolder> {

    private Context context;
    private List<ModelUsers> friendRequestList;
    //List<ModelUsers> usersListFull;
    private String CURRENT_STATE, senderUserId, otherPersonId, uid;

    private DatabaseReference rootReference;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;


    public FriendRequestAdapter(Context context, List<ModelUsers> friendRequestList) {
        this.context = context;
        this.friendRequestList = friendRequestList;
        //usersListFull = new ArrayList<>(friendRequestList);
    }

    @NonNull
    @Override
    public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_friend_request, parent, false);
        return new FriendRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FriendRequestViewHolder holder, int position) {

        otherPersonId = friendRequestList.get(position).getUid();
        final String username = friendRequestList.get(position).getName();
        String userImage = friendRequestList.get(position).getImage();

        holder.nameTextView.setText(username);
        */
/*try{
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.default_image)
                    .into(holder.usersImageView);
        }
        catch (Exception e){
            Picasso.get().load(R.drawable.default_image).into(holder.usersImageView);
        }*//*

        Glide.with(context).load(userImage)
                .placeholder(R.drawable.default_image)
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .thumbnail(0.1f)
                .into(holder.usersImageView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent userProfileIntent = new Intent(context, UserProfileActivity.class);
                userProfileIntent.putExtra("otherPersonId", otherPersonId);
                context.startActivity(userProfileIntent);

            }
        });
        
        holder.acceptFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptFriendRequest(otherPersonId, holder);
            }
        });

        holder.declineFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                declineFriendRequest(otherPersonId, holder);
            }
        });
    }

    private void declineFriendRequest(String otherPersonId, FriendRequestViewHolder holder) {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        senderUserId = mAuth.getUid();

        Map friendRequestMap = new HashMap();
        friendRequestMap.put("Friend_request/" + senderUserId + "/" + otherPersonId +  "/request_type", null);
        friendRequestMap.put("Friend_request/" + otherPersonId + "/" + senderUserId +  "/request_type", null);

        rootReference.updateChildren(friendRequestMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                if(databaseError == null){
                    CURRENT_STATE = "not_friends";
                }
                else {
                    Toast.makeText(context, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void acceptFriendRequest(String otherPersonId, final FriendRequestViewHolder holder) {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        senderUserId = mAuth.getUid();

        final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
        Map friendRequestMap = new HashMap();
        friendRequestMap.put("Friends/" + senderUserId + "/" + otherPersonId +  "/date", currentDate);
        friendRequestMap.put("Friends/" + otherPersonId + "/" + senderUserId +  "/date", currentDate);

        friendRequestMap.put("Friend_request/" + senderUserId + "/" + otherPersonId +  "/request_type", null);
        friendRequestMap.put("Friend_request/" + otherPersonId + "/" + senderUserId +  "/request_type", null);

        rootReference.updateChildren(friendRequestMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError == null){
                    CURRENT_STATE = "friends";
                    */
/*holder.acceptFriendRequestBtn.setText("UnFriend");
                    holder.acceptFriendRequestBtn.setGravity(Gravity.CENTER);

                    holder.declineFriendRequestBtn.setVisibility(View.GONE);*//*


                }
                else {
                    Toast.makeText(context, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return friendRequestList.size();
    }

    public class FriendRequestViewHolder extends RecyclerView.ViewHolder{

        private CircleImageView usersImageView;
        private TextView nameTextView;
        private Button acceptFriendRequestBtn, declineFriendRequestBtn;

        public FriendRequestViewHolder(@NonNull View itemView) {
            super(itemView);

            usersImageView = itemView.findViewById(R.id.requestUsersImageView);
            nameTextView = itemView.findViewById(R.id.requestUserNameTextView);
            acceptFriendRequestBtn = itemView.findViewById(R.id.confirmRequestBtn);
            declineFriendRequestBtn = itemView.findViewById(R.id.deleteRequestBtn);
        }
    }
}
*/
