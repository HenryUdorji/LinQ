package com.codemountain.slicker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.codemountain.slicker.R;
import com.codemountain.slicker.activities.PostDetailActivity;
import com.codemountain.slicker.model.ModelChatList;
import com.codemountain.slicker.model.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {

    private Context context;
    private List<ModelUsers> usersList;
    private OnItemClickListener listener;
    private HashMap<String, String> lastMessageMap;



    public ChatListAdapter(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.usersList = usersList;
        lastMessageMap = new HashMap<>();
    }




    @NonNull
    @Override
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, parent, false);
        return new ChatListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListViewHolder holder, int position) {

        String image = usersList.get(position).getImage();
        String name = usersList.get(position).getName();
        String hisUID = usersList.get(position).getUid();
        String lastMessage = lastMessageMap.get(hisUID);

        holder.chatListName.setText(name);
        if (lastMessage == null || lastMessage.equals("default")){
            holder.chatLisLastMessage.setVisibility(View.GONE);
        }
        else {
            holder.chatLisLastMessage.setVisibility(View.VISIBLE);
            holder.chatLisLastMessage.setText(lastMessage);

        }
        Glide.with(context).load(image)
                .placeholder(R.drawable.default_image)
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(holder.chatListImage);

        /*Set online status of other users in chatList*/
        /*if (usersList.get(position).getOnlineStatus().equals("online")){
            holder.chatListOnline.setVisibility(View.VISIBLE);

        }
        else {
            holder.chatListOnline.setVisibility(View.GONE);
        }*/

    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public void setLastMessageMap(String userId, String lastMessage){
        lastMessageMap.put(userId, lastMessage);
    }

    class ChatListViewHolder extends RecyclerView.ViewHolder{

        private CircleImageView chatListImage, chatListOnline;
        private TextView chatListName, chatLisLastMessage;

        public ChatListViewHolder(@NonNull View itemView) {
            super(itemView);

            chatLisLastMessage = itemView.findViewById(R.id.chatListLastMessage);
            chatListName = itemView.findViewById(R.id.chatlistName);
            chatListImage = itemView.findViewById(R.id.chatlistImage);
            chatListOnline = itemView.findViewById(R.id.chatListimageOnline);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION)
                        listener.onItemClicked(v, position);
                }
            });
        }
    }

    public interface OnItemClickListener{
        void onItemClicked(View view, int position);
    }

    public void setOnItemClickedListener(OnItemClickListener listener){
        this.listener = listener;
    }
}
