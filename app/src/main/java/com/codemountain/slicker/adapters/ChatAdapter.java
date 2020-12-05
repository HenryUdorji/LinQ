package com.codemountain.slicker.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.codemountain.slicker.R;
import com.codemountain.slicker.activities.ChatActivity;
import com.codemountain.slicker.model.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder>{

    private static final int MESSAGE_TYPE_LEFT = 0;
    private static final int MESSAGE_TYPE_RIGHT = 1;

    private FirebaseUser currentUser;

    private Context context;
    private List<ModelChat> messageList;
    private String imageUrl;

    public ChatAdapter(Context context, List<ModelChat> messageList, String imageUrl) {
        this.context = context;
        this.messageList = messageList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == MESSAGE_TYPE_LEFT){

            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);

            return new ChatViewHolder(view);
        }
        else {

            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);

            return new ChatViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, final int position) {

        String message = messageList.get(position).getMessage();
        String timeStamp = messageList.get(position).getTimestamp();

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm a");
        String time = dateFormat.format(Long.parseLong(timeStamp));

        holder.messageText.setText(message);
        holder.messageTime.setText(time);
        //Loading the image
        Glide.with(context).load(imageUrl)
                .placeholder(R.drawable.default_image)
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(holder.messageImage);

        /**
         * Setting of seen or delivered to messages
         */
        if(position == messageList.size() - 1){

            if(messageList.get(position).isSeen()){

                holder.messageIsSeen.setText("Seen");
            }
            else {

                holder.messageIsSeen.setText("Delivered");
            }
        }
        else {

            holder.messageIsSeen.setVisibility(View.GONE);
        }

        //Setting click listener to the layout to show alert dialog
        holder.layoutChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure you want to delete this message");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        deleteMessage(position);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });

    }

    private void deleteMessage(int position) {

        final String  myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        /**
         * Logic
         * Get the timeStamp of the clicked message
         * Compare the timestamp of clicked message with all chats
         * where both values match delete
         */

        String msgTimeStamp = messageList.get(position).getTimestamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){

                    //User can only delete his or her message
                    if(ds.child("sender").getValue().equals(myUid)){

                        /**
                         * Two ways of deleting message
                         * 1-> Delete message without showing anything
                         * 2-> Delete message and show a note to both users that message has been deleted
                         */

                        //ds.getRef().removeValue();

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("message", "This message was deleted");
                        ds.getRef().updateChildren(hashMap);
                    }
                    else {
                        Toast.makeText(context, "You can delete only your message", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(messageList.get(position).getSender().equals(currentUser.getUid())){

            return MESSAGE_TYPE_RIGHT;
        }
        else {

            return MESSAGE_TYPE_LEFT;
        }

    }

    public class ChatViewHolder extends RecyclerView.ViewHolder{

        private CircleImageView messageImage;
        private TextView messageText, messageTime, messageIsSeen;
        private LinearLayout layoutChat;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            messageImage = itemView.findViewById(R.id.messageImage);
            messageText = itemView.findViewById(R.id.chatMessage);
            messageTime = itemView.findViewById(R.id.messageTime);
            messageIsSeen = itemView.findViewById(R.id.messageIsSeen);
            layoutChat = itemView.findViewById(R.id.layoutChat);
        }
    }
}
