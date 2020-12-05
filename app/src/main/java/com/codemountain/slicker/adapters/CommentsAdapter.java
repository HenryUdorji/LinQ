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
import com.codemountain.slicker.model.ModelComments;
import com.codemountain.slicker.utils.GetTimeAgo;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {

    private Context context;
    private List<ModelComments> commentsList;
    private OnItemClickListener listener;

    public CommentsAdapter(Context context, List<ModelComments> commentsList) {
        this.context = context;
        this.commentsList = commentsList;
    }

    @NonNull
    @Override
    public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_comments, parent, false);

        return new CommentsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsViewHolder holder, int position) {
        String time = commentsList.get(position).getTimeOfComment();
        String comment = commentsList.get(position).getComments();
        String image = commentsList.get(position).getuDp();
        String name = commentsList.get(position).getuName();
        String uID = commentsList.get(position).getUid();
        String cID = commentsList.get(position).getcID();
        String email = commentsList.get(position).getuEmail();

        String newTime = GetTimeAgo.getTimeAgo(Long.parseLong(time), context);

        holder.nameText.setText(name);
        holder.timeText.setText(newTime);
        holder.commentText.setText(comment);
        Glide.with(context).load(image)
                .placeholder(R.drawable.default_image)
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(holder.commenterImage);

    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }


    class CommentsViewHolder extends RecyclerView.ViewHolder{
        private CircleImageView commenterImage;
        private TextView commentText, timeText, nameText;

        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);

            commenterImage = itemView.findViewById(R.id.commentImageView);
            commentText = itemView.findViewById(R.id.commentTextView);
            timeText = itemView.findViewById(R.id.commentTimeText);
            nameText = itemView.findViewById(R.id.nameTextView);

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
