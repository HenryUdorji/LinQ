package com.codemountain.slicker.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.codemountain.slicker.R;
import com.codemountain.slicker.adapters.PostAdapter;
import com.codemountain.slicker.model.ModelPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView profileCoverImageView;
    private CircleImageView profileImageView;
    private TextView profileDisplayName, profileBioTextView, profileLocation, profileDOB, profileJoinDate, profilePhoneNumber, profileEmail;
    private Button sendFriendRequestBtn, declineFriendRequestBtn;
    private ProgressBar profileImageProgressBar, coverImageProgressBar;

    private DatabaseReference reference;
    private DatabaseReference rootReference;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private RecyclerView postRecyclerView;
    private List<ModelPost> postList = new ArrayList<>();
    private PostAdapter postAdapter;

    private Toolbar toolbar;

    private String CURRENT_STATE, senderUserId, otherPersonId, uid;

    private ProgressDialog loadingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.keepSynced(true);

        rootReference = FirebaseDatabase.getInstance().getReference();
        rootReference.keepSynced(true);

        otherPersonId = getIntent().getStringExtra("otherPersonId");
        senderUserId = mAuth.getUid();

        profileCoverImageView = findViewById(R.id.profileCoverImageView);
        profileImageView = findViewById(R.id.profileImageView);
        profileDisplayName = findViewById(R.id.profileName);
        profileBioTextView = findViewById(R.id.profileBio);
        profileLocation = findViewById(R.id.editLocation);
        profileDOB = findViewById(R.id.birthDate);
        profileJoinDate = findViewById(R.id.joinedDate);
        profilePhoneNumber = findViewById(R.id.profilePhoneNo);
        profileEmail = findViewById(R.id.profileEmail);
        sendFriendRequestBtn = findViewById(R.id.sendRequest);
        declineFriendRequestBtn = findViewById(R.id.declineRequest);
        profileImageProgressBar = findViewById(R.id.profileImageProgressBar);
        coverImageProgressBar = findViewById(R.id.coverImageProgressBar);

        //RecyclerView
        postRecyclerView = findViewById(R.id.profilesPostRecyclerView);
        postRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postRecyclerView.setLayoutManager(linearLayoutManager);

        loadingProgress = new ProgressDialog(this);

        reference.child(otherPersonId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    String coverImage = dataSnapshot.child("cover_image").getValue().toString();
                    String image = dataSnapshot.child("image").getValue().toString();
                    String displayName = dataSnapshot.child("name").getValue().toString();
                    String bio = dataSnapshot.child("bio").getValue().toString();
                    String email = dataSnapshot.child("email").getValue().toString();
                    String location = dataSnapshot.child("location").getValue().toString();
                    String phone = dataSnapshot.child("phone").getValue().toString();
                    String dob = dataSnapshot.child("date_of_birth").getValue().toString();
                    String regDate = dataSnapshot.child("registration_date").getValue().toString();

                    profileBioTextView.setText(bio);
                    profileDisplayName.setText(displayName);
                    profileEmail.setText(email);
                    profileLocation.setText(location);
                    profileJoinDate.setText(regDate);
                    profilePhoneNumber.setText(phone);
                    profileDOB.setText(dob);

                    /*try{
                        Picasso.get().load(image).placeholder(R.drawable.default_image).into(profileImageView);
                    }
                    catch (Exception e){

                        Picasso.get().load(R.drawable.default_image).into(profileImageView);
                    }*/
                    //Setting image to the profile image
                    Glide.with(UserProfileActivity.this).load(image)
                            .placeholder(R.drawable.default_image)
                            .addListener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    profileImageProgressBar.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    profileImageProgressBar.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(profileImageView);

                   /* try{
                        Picasso.get().load(coverImage).placeholder(R.color.colorPrimary).into(profileCoverImageView);
                    }
                    catch (Exception e){

                        Picasso.get().load(R.color.colorPrimary).into(profileCoverImageView);
                    }*/
                    //setting image to the profile cover
                    Glide.with(UserProfileActivity.this).load(coverImage)
                            .placeholder(R.drawable.cover_default)
                            .addListener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    coverImageProgressBar.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    coverImageProgressBar.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(profileCoverImageView);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        declineFriendRequestBtn.setVisibility(View.GONE);
        declineFriendRequestBtn.setEnabled(false);

        CURRENT_STATE = "not_friends";
        if(!senderUserId.equals(otherPersonId)){

            sendFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    sendFriendRequestBtn.setEnabled(false);

                    if(CURRENT_STATE.equals("not_friends")){

                        sendFriendRequest();
                    }
                    if(CURRENT_STATE.equals("request_sent")){

                        cancelFriendRequest();
                    }

                    if(CURRENT_STATE.equals("request_received")){

                        acceptFriendRequest();
                    }

                    if(CURRENT_STATE.equals("friends")){

                        unFriendExistingFriend();
                    }
                }
            });

        }
        else {

            sendFriendRequestBtn.setVisibility(View.INVISIBLE);
            declineFriendRequestBtn.setVisibility(View.INVISIBLE);
        }


        maintenanceOfButtons();
        checkUserStatus();
        loadOtherPost();
    }

    private void loadOtherPost() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.keepSynced(true);
        Query query = reference.orderByChild("uid").equalTo(otherPersonId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    ModelPost post = ds.getValue(ModelPost.class);
                    postList.add(post);
                }
                postAdapter = new PostAdapter(UserProfileActivity.this, postList);
                postRecyclerView.setAdapter(postAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UserProfileActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserStatus() {

        currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //User is signed in stay here
            uid = currentUser.getUid();
        }
        else {

            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    //This method is for un-Friend
    private void unFriendExistingFriend() {

        Map unFriendMap = new HashMap();
        unFriendMap.put("Friends/" + senderUserId + "/" + otherPersonId , null);
        unFriendMap.put("Friends/" + otherPersonId + "/" + senderUserId , null);

        rootReference.updateChildren(unFriendMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                if(databaseError == null){

                    sendFriendRequestBtn.setEnabled(true);
                    CURRENT_STATE = "not_friends";
                    sendFriendRequestBtn.setText("Add friend");
                    sendFriendRequestBtn.setGravity(Gravity.CENTER);

                    declineFriendRequestBtn.setVisibility(View.GONE);
                    declineFriendRequestBtn.setEnabled(false);

                }
            }
        });
    }

    //The reciever of the friend-request accepting the request
    private void acceptFriendRequest() {

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

                    sendFriendRequestBtn.setEnabled(true);
                    CURRENT_STATE = "friends";
                    sendFriendRequestBtn.setText("UnFriend");

                    sendFriendRequestBtn.setGravity(Gravity.CENTER);
                    declineFriendRequestBtn.setVisibility(View.GONE);
                    declineFriendRequestBtn.setEnabled(false);

                }
            }
        });
    }

    //Current user cancelling friend request sent
    private void cancelFriendRequest() {

        Map friendRequestMap = new HashMap();
        friendRequestMap.put("Friend_request/" + senderUserId + "/" + otherPersonId +  "/request_type", null);
        friendRequestMap.put("Friend_request/" + otherPersonId + "/" + senderUserId +  "/request_type", null);

        rootReference.updateChildren(friendRequestMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                if(databaseError == null){

                    sendFriendRequestBtn.setEnabled(true);
                    CURRENT_STATE = "not_friends";
                    sendFriendRequestBtn.setText("Add friend");
                    sendFriendRequestBtn.setGravity(Gravity.CENTER);

                    declineFriendRequestBtn.setVisibility(View.GONE);
                    declineFriendRequestBtn.setEnabled(false);

                }
            }
        });
    }

    //maintaining the sendFriendRequestButton && declineFriendRequestButton
    private void maintenanceOfButtons() {

        rootReference.child("Friend_request").child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(otherPersonId)){
                    String requestType = dataSnapshot.child(otherPersonId).child("request_type").getValue().toString();

                    if(requestType.equals("sent")){
                        CURRENT_STATE = "request_sent";
                        sendFriendRequestBtn.setText("Cancel");

                        declineFriendRequestBtn.setVisibility(View.GONE);
                        declineFriendRequestBtn.setEnabled(false);
                    }
                    else if(requestType.equals("received")){
                        CURRENT_STATE = "request_received";
                        sendFriendRequestBtn.setText("Confirm");

                        declineFriendRequestBtn.setVisibility(View.VISIBLE);
                        declineFriendRequestBtn.setEnabled(true);

                        declineFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                cancelFriendRequest();
                            }
                        });

                    }
                }
                else {

                    rootReference.child("Friends").child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(otherPersonId)){
                                CURRENT_STATE = "friends";
                                sendFriendRequestBtn.setText("UnFriend");

                                declineFriendRequestBtn.setVisibility(View.GONE);
                                declineFriendRequestBtn.setEnabled(true);

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
    }

    private void sendFriendRequest() {
        Map friendRequestMap = new HashMap();
        friendRequestMap.put("Friend_request/" + senderUserId + "/" + otherPersonId +  "/request_type", "sent");
        friendRequestMap.put("Friend_request/" + otherPersonId + "/" + senderUserId +  "/request_type", "received");

        rootReference.updateChildren(friendRequestMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                if(databaseError == null){
                    sendFriendRequestBtn.setEnabled(true);
                    CURRENT_STATE = "request_sent";
                    sendFriendRequestBtn.setText("Cancel");
                    sendFriendRequestBtn.setGravity(Gravity.CENTER);

                    declineFriendRequestBtn.setVisibility(View.GONE);
                    declineFriendRequestBtn.setEnabled(false);

                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            Intent intent = new Intent(UserProfileActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
