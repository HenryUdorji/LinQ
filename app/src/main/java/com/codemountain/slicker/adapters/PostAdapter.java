package com.codemountain.slicker.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.codemountain.slicker.R;
import com.codemountain.slicker.activities.AddPostActivity;
import com.codemountain.slicker.activities.DashboardActivity;
import com.codemountain.slicker.activities.PostDetailActivity;
import com.codemountain.slicker.activities.UserProfileActivity;
import com.codemountain.slicker.fragments.ProfileFragment;
import com.codemountain.slicker.model.ModelPost;
import com.codemountain.slicker.utils.GetTimeAgo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostAdapterViewHolder> {

    private Context context;
    private List<ModelPost> postList;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String myUid;
    private DatabaseReference likesRef;
    private DatabaseReference postRef;
    private boolean mProcessLikes = false;

    public PostAdapter(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public PostAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, parent, false);
        return new PostAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostAdapterViewHolder holder, final int position) {

        final String uid = postList.get(position).getUid();
        String uName = postList.get(position).getuName();
        String uEmail = postList.get(position).getuEmail();
        final String pId = postList.get(position).getpId();
        String uDp = postList.get(position).getuDp();
        final String image = postList.get(position).getpImage();
        String pDescription = postList.get(position).getpDescription();
        String pTime = postList.get(position).getpTime();
        String pLikes = postList.get(position).getpLikes();
        String pComments = postList.get(position).getpComments();

        //Converting the time with an helper class
        long lastTime = Long.parseLong(pTime);
        String lastSeenTime = GetTimeAgo.getTimeAgo(lastTime, context);

        //Set data
        holder.posterNameTv.setText(uName);
        holder.postDescriptionTv.setText(pDescription);
        holder.timeOfPostTv.setText(lastSeenTime);
        holder.postLikesTv.setText(pLikes + " Likes");
        holder.postCommentsTv.setText(pComments + " Comments");

        setLikes(holder, pId);

        //Load the image of the user that uploaded the post
        Glide.with(context).load(uDp)
                .placeholder(R.drawable.default_image)
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(holder.posterImageView);

        //If the post does not have an image then hide the imageView
        if(image.equals("noImage")){
            holder.postImageView.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.GONE);
        }
        else {
            Glide.with(context).load(image)
                    .placeholder(R.drawable.cover_default)
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            holder.progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(holder.postImageView);
        }

        holder.postDescriptionTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("POST_ID", pId);
                context.startActivity(intent);

            }
        });

        //More button
        holder.postMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.postMoreBtn, uid, myUid, pId, image);
            }
        });
        //Like button
        holder.postLikeBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Get total number of likes a post whose like button is clicked
                 * If currently signed in user has not liked the post before
                 * increase value by 1 otherwise decrease value by 1
                 */
                final int pLikes = Integer.parseInt(postList.get(position).getpLikes());
                mProcessLikes = true;
                final String postID = postList.get(position).getpId();
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (mProcessLikes){
                            if (dataSnapshot.child(postID).hasChild(myUid)){
                                //post already liked, so remove like
                                postRef.child(postID).child("pLikes").setValue(""+ (pLikes - 1));
                                likesRef.child(postID).child(myUid).removeValue();
                                mProcessLikes = false;
                            }
                            else {
                                //post not liked yet, like it
                                postRef.child(postID).child("pLikes").setValue("" + (pLikes + 1));
                                likesRef.child(postID).child(myUid).setValue("Liked");
                                mProcessLikes = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        //Comment button
        holder.postCommentBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("POST_ID", pId);
                context.startActivity(intent);
            }
        });
        //Share button
        holder.postShareBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Share clicked", Toast.LENGTH_SHORT).show();
            }
        });
        holder.profileLayoutPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * When the user clicks on this on the post layout it takes the user to
                 * that user specific post
                 */
                mAuth = FirebaseAuth.getInstance();
                currentUser = mAuth.getCurrentUser();
                if(currentUser.getUid().equals(uid)){

                    ((DashboardActivity)context).getSupportActionBar().setTitle("Profile");
                    ProfileFragment profileFragment = new ProfileFragment();
                    FragmentTransaction transaction = ((DashboardActivity)context).getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, profileFragment, "");
                    transaction.commit();

                }
                else {
                    Intent callUserProfile = new Intent(context, UserProfileActivity.class);
                    callUserProfile.putExtra("otherPersonId", uid);
                    context.startActivity(callUserProfile);
                }
            }
        });

    }

    private void setLikes(final PostAdapterViewHolder holder, final String pId)  {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(pId).hasChild(myUid)){
                    //User has liked the post
                    //To indicate that the currently signed in user has liked the post yet
                    //change the drawable left of the like button from like to liked
                    holder.postLikeBtn1.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
                    holder.postLikeBtn1.setText("Liked");
                }
                else {
                    //User has not liked the post
                    //To indicate that the currently signed in user has not liked the post yet
                    holder.postLikeBtn1.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_likes, 0, 0, 0);
                    holder.postLikeBtn1.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showMoreOptions(ImageButton postMoreBtn, final String uid, final String myUid, final String pId, final String image) {
        final PopupMenu popupMenu = new PopupMenu(context, postMoreBtn, Gravity.END);
        //Show delete option in posts that belongs to the currently signed in user
        if(uid.equals(myUid)){
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
        }
        popupMenu.getMenu().add(Menu.NONE, 1, 0, "View detail");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if (item.getItemId() == 0) {
                    beginDelete(pId, image);
                }
                else if (item.getItemId() == 1){
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("POST_ID", pId);
                    context.startActivity(intent);
                }

                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete(String pId, String image) {
        //Post can be with or without image
        if(image.equals("noImage")){
            //Delete post without image
            deletePostWithoutImage(pId);
        }
        else {
            //Delete post with image
            deletePostWithImage(pId, image);
        }
    }

    private void deletePostWithoutImage(String pId) {
        final ProgressDialog loadingProgress = new ProgressDialog(context);
        loadingProgress.setMessage("Deleting...");

        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    ds.getRef().removeValue();
                }
                Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show();
                loadingProgress.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deletePostWithImage(final String pId, String image){
        final ProgressDialog loadingProgress = new ProgressDialog(context);
        loadingProgress.setMessage("Deleting...");

        /**
         * First delete the post image from Firebase storage
         * then delete the data from Firebase database
         */
        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(image);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds : dataSnapshot.getChildren()){
                                    ds.getRef().removeValue();
                                }
                                Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show();
                                loadingProgress.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(context, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingProgress.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    class PostAdapterViewHolder extends  RecyclerView.ViewHolder{

        private CircleImageView posterImageView;
        private TextView posterNameTv, timeOfPostTv, postDescriptionTv, postLikesTv, postCommentsTv;
        private ImageView postImageView;
        private Button postLikeBtn1, postCommentBtn2, postShareBtn3;
        private ImageButton postMoreBtn;
        private LinearLayout profileLayoutPost;
        private ProgressBar progressBar;

        public PostAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            posterImageView = itemView.findViewById(R.id.posterImage);
            posterNameTv = itemView.findViewById(R.id.posterName);
            timeOfPostTv = itemView.findViewById(R.id.timeOfPost);
            postDescriptionTv = itemView.findViewById(R.id.postDescription);
            postLikesTv = itemView.findViewById(R.id.postLikes);
            postCommentsTv = itemView.findViewById(R.id.postComments);
            postImageView = itemView.findViewById(R.id.postImage);
            postLikeBtn1 = itemView.findViewById(R.id.postLikeBtn);
            postCommentBtn2 = itemView.findViewById(R.id.postCommentBtn);
            postShareBtn3 = itemView.findViewById(R.id.postShareBtn);
            postMoreBtn = itemView.findViewById(R.id.postMore);
            profileLayoutPost = itemView.findViewById(R.id.profileLayoutPost);
            progressBar = itemView.findViewById(R.id.rowProgressBar);

        }
    }
}
