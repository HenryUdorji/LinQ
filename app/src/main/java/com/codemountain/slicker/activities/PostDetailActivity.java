package com.codemountain.slicker.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.codemountain.slicker.R;
import com.codemountain.slicker.adapters.CommentsAdapter;
import com.codemountain.slicker.model.ModelComments;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostDetailActivity extends AppCompatActivity {

    private String myUID, myEmail, myName, myDP, postID, pLikes, hisDP, hisName, hisUID, pImage;
    private boolean processComments = false;
    private boolean mProcessLikes = false;

    private CircleImageView posterImageView;
    private TextView posterName, timeOfPost, postDescription, pLikesTextView, pCommentsTextView;
    private ImageView postImage;
    private Button likesBtn, shareBtn;
    private ImageButton moreBtn;
    private LinearLayout linearLayout;
    private ProgressBar progressBar;

    private EditText commentEditText;
    private ImageButton sendCommentBtn, pickEmoji;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;

    private RecyclerView recyclerView;
    private CommentsAdapter adapter;
    private List<ModelComments> commentsList = new ArrayList<>();

    //private DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);


        Intent intent = getIntent();
        postID = intent.getStringExtra("POST_ID");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Post Detail");
        getSupportActionBar().setSubtitle(myEmail);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        posterImageView = findViewById(R.id.posterImage);
        posterName = findViewById(R.id.posterName);
        timeOfPost = findViewById(R.id.timeOfPost);
        postDescription = findViewById(R.id.postDescription);
        pLikesTextView = findViewById(R.id.postLikes);
        pCommentsTextView = findViewById(R.id.postComments);
        postImage = findViewById(R.id.postImage);
        likesBtn = findViewById(R.id.postLikeBtn);
        shareBtn = findViewById(R.id.postShareBtn);
        moreBtn = findViewById(R.id.postMore);
        linearLayout = findViewById(R.id.profileLayoutPost);
        progressBar = findViewById(R.id.rowProgressBar);

        pickEmoji = findViewById(R.id.pickEmoji);
        commentEditText = findViewById(R.id.commentEditText);
        sendCommentBtn = findViewById(R.id.sendComment);

        //initialize comments recyclerView
        recyclerView = findViewById(R.id.commentsRecyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting comment");
        progressDialog.setCanceledOnTouchOutside(false);

        loadPostInfo();
        checkUserStatus();
        loadComments();
        setLikes();

        sendCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        likesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions();
            }
        });

        pickEmoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PostDetailActivity.this, "Emoji clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadComments() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postID).child("Comments");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentsList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    final ModelComments comments = ds.getValue(ModelComments.class);
                    commentsList.add(comments);

                    adapter = new CommentsAdapter(PostDetailActivity.this, commentsList);
                    adapter.setOnItemClickedListener(new CommentsAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClicked(final View view, final int position) {
                            final String commentID = commentsList.get(position).getcID();
                            if (myUID.equals(commentsList.get(position).getUid())) {
                                //Currently signed in user comment
                                AlertDialog.Builder builder = new AlertDialog.Builder(PostDetailActivity.this);
                                builder.setTitle("Delete comment");
                                builder.setMessage("Are you sure you want to delete comment");
                                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        deleteComment(commentID);

                                    }
                                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();

                                    }
                                }).show();

                            }
                            else {
                                //Not currently signed in user comment
                                Toast.makeText(PostDetailActivity.this, "Cannot delete another persons comment", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                    adapter.notifyDataSetChanged();
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void deleteComment(String commentID) {
        //Users can only delete their own comment
        //check if the UID in the comment database is the same UID of the currently signed
        //in user then this user can delete the comment

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postID);
        reference.child("Comments").child(commentID).removeValue();

        //now update the comments count
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String comment = dataSnapshot.child("pComments").getValue().toString();
                int commentVal = Integer.parseInt(comment) - 1;
                reference.child("pComments").setValue("" + commentVal);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void showMoreOptions() {
        final PopupMenu popupMenu = new PopupMenu(this, moreBtn, Gravity.END);
        //Show delete option in posts that belongs to the currently signed in user
        if(hisUID.equals(myUID)){
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if (item.getItemId() == 0) {
                    beginDelete();
                }
                else if (item.getItemId() == 1){
                    Intent intent = new Intent(PostDetailActivity.this, PostDetailActivity.class);
                    intent.putExtra("POST_ID", postID);
                    startActivity(intent);
                }

                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete() {
        //Post can be with or without image
        if(postImage.equals("noImage")){
            //Delete post without image
            deletePostWithoutImage();
        }
        else {
            //Delete post with image
            deletePostWithImage();
        }
    }

    private void deletePostWithImage() {
        final ProgressDialog loadingProgress = new ProgressDialog(this);
        loadingProgress.setMessage("Deleting...");

        /**
         * First delete the post image from Firebase storage
         * then delete the data from Firebase database
         */
        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postID);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds : dataSnapshot.getChildren()){
                                    ds.getRef().removeValue();
                                }
                                Toast.makeText(PostDetailActivity.this, "Post deleted", Toast.LENGTH_SHORT).show();
                                loadingProgress.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(PostDetailActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingProgress.dismiss();
                        Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deletePostWithoutImage() {
        final ProgressDialog loadingProgress = new ProgressDialog(this);
        loadingProgress.setMessage("Deleting...");

        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    ds.getRef().removeValue();
                }
                Toast.makeText(PostDetailActivity.this, "Post deleted", Toast.LENGTH_SHORT).show();
                loadingProgress.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PostDetailActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLikes() {
        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postID).hasChild(myUID)){
                    //User has liked the post
                    //To indicate that the currently signed in user has liked the post yet
                    //change the drawable left of the like button from like to liked
                    likesBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
                    likesBtn.setText("Liked");
                }
                else {
                    //User has not liked the post
                    //To indicate that the currently signed in user has not liked the post yet
                    likesBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_likes, 0, 0, 0);
                    likesBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PostDetailActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void likePost() {
        /**
         * Get total number of likes a post whose like button is clicked
         * If currently signed in user has not liked the post before
         * increase value by 1 otherwise decrease value by 1
         */
        mProcessLikes = true;
        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        final DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mProcessLikes){
                    if (dataSnapshot.child(postID).hasChild(myUID)){
                        //post already liked, so remove like
                        postRef.child(postID).child("pLikes").setValue(""+ (Integer.parseInt(pLikes) - 1));
                        likesRef.child(postID).child(myUID).removeValue();
                        mProcessLikes = false;

                        likesBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_likes, 0, 0, 0);
                        likesBtn.setText("Like");
                    }
                    else {
                        //post not liked yet, like it
                        postRef.child(postID).child("pLikes").setValue("" + (Integer.parseInt(pLikes) + 1));
                        likesRef.child(postID).child(myUID).setValue("Liked");
                        mProcessLikes = false;

                        likesBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
                        likesBtn.setText("Liked");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PostDetailActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void postComment() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        Query query = ref.orderByChild("uid").equalTo(myUID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    final String myName = ""+ ds.child("name").getValue();
                    String myDP = ""+ ds.child("image").getValue();

                    /*########################################################*/
                    String commentText = commentEditText.getText().toString();
                    if (TextUtils.isEmpty(commentText)){
                        Toast.makeText(PostDetailActivity.this, "Comment is empty", Toast.LENGTH_SHORT).show();
                        return;

                    }


                    progressDialog.show();
                    String time = String.valueOf(System.currentTimeMillis());
                    //Each posts will have a child called comment where all the comment for the post will be stored
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postID).child("Comments");
                    HashMap<String, Object> commentHashMap = new HashMap<>();

                    commentHashMap.put("cID", time);
                    commentHashMap.put("comments", commentText);
                    commentHashMap.put("timeOfComment", time);
                    commentHashMap.put("uid", myUID);
                    commentHashMap.put("uEmail", myEmail);
                    commentHashMap.put("uDp", myDP);
                    commentHashMap.put("uName", myName);


                    reference.child(time).setValue(commentHashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //comment posted
                            progressDialog.dismiss();
                            Toast.makeText(PostDetailActivity.this, "Comment posted \n " + "name " + myName, Toast.LENGTH_SHORT).show();
                            commentEditText.setText("");
                            updateCommentCount();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(PostDetailActivity.this, "Posting comment failed \n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PostDetailActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });





    }

    private void updateCommentCount() {
        //Whenever a user adds comment increase the comment count
        processComments = true;
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postID);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (processComments){
                    String comment = dataSnapshot.child("pComments").getValue().toString();
                    int commentVal = Integer.parseInt(comment) + 1;
                    reference.child("pComments").setValue("" + commentVal);
                    processComments = false;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void loadPostInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = reference.orderByChild("pId").equalTo(postID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    String pDescription = ds.child("pDescription").getValue().toString();
                    pLikes = ds.child("pLikes").getValue().toString();
                    String postTime = ds.child("pTime").getValue().toString();
                    pImage = ds.child("pImage").getValue().toString();
                    hisDP = ds.child("uDp").getValue().toString();
                    hisUID = ds.child("uid").getValue().toString();
                    String posterEmail = ds.child("uEmail").getValue().toString();
                    hisName = ds.child("uName").getValue().toString();
                    String commentCount = ds.child("pComments").getValue().toString();

                    //converting the time value to proper convention
                    String timeStamp = GetTimeAgo.getTimeAgo(Long.parseLong(postTime), PostDetailActivity.this);

                    //set Data
                    posterName.setText(hisName);
                    postDescription.setText(pDescription);
                    pLikesTextView.setText(pLikes + " Likes");
                    timeOfPost.setText(timeStamp);
                    pCommentsTextView.setText(commentCount + " Comments");


                    if (pImage.equals("noImage")){
                        //post without image
                        postImage.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                    }
                    else {
                        postImage.setVisibility(View.VISIBLE);
                        //post with image
                        Glide.with(PostDetailActivity.this).load(pImage)
                                .placeholder(R.drawable.cover_default)
                                .addListener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        progressBar.setVisibility(View.GONE);
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        progressBar.setVisibility(View.GONE);
                                        return false;
                                    }
                                })
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                .into(postImage);
                    }
                    //poster image
                    Glide.with(PostDetailActivity.this).load(hisDP)
                            .placeholder(R.drawable.default_image)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .into(posterImageView);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PostDetailActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            //user is signed in
            myEmail = user.getEmail();
            myUID = user.getUid();

        }
        else {
            startActivity(new Intent(PostDetailActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            Intent intent = new Intent(PostDetailActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        }
        if (item.getItemId() == R.id.action_logout){
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_save).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }
}
