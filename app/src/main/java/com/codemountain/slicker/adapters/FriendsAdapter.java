/*
package com.codemountain.slicker.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codemountain.slicker.R;
import com.codemountain.slicker.activities.ChatActivity;
import com.codemountain.slicker.activities.UserProfileActivity;
import com.codemountain.slicker.model.ModelFriends;
import com.codemountain.slicker.model.ModelUsers;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

*/
/**
 * This adapter is identical to UsersAdapter so it would inflate the same view as the former
 * they both share a lot of features
 *//*


public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder>{

    Context context;
    List<ModelFriends> friendsList;

    public FriendsAdapter(Context context, List<ModelFriends> friendsList) {
        this.context = context;
        this.friendsList = friendsList;
    }

    @NonNull
    @Override
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_users, parent, false);
        return new FriendsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsViewHolder holder, int position) {

        final String otherPersonId = friendsList.get(position).getUid();
        final String username = friendsList.get(position).getName();
        String userImage = friendsList.get(position).getImage();
        String status = friendsList.get(position).getBio();

        holder.nameTextView.setText(username);
        holder.statusTextView.setText(status);
        try{
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.default_image)
                    .into(holder.usersImageView);
        }
        catch (Exception e){
            Picasso.get().load(R.drawable.default_image).into(holder.usersImageView);
        }

        */
/**
         * Handle item click
         *//*

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent userProfileIntent = new Intent(context, ChatActivity.class);
                userProfileIntent.putExtra("otherPersonId", otherPersonId);
                context.startActivity(userProfileIntent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    public class FriendsViewHolder extends RecyclerView.ViewHolder{

        View view;
        private CircleImageView usersImageView;
        private TextView nameTextView, statusTextView;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            usersImageView = itemView.findViewById(R.id.usersImageView);
            nameTextView = itemView.findViewById(R.id.userNameTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);

            view = itemView;
        }
    }
}
*/
