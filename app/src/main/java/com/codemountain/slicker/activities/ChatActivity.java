package com.codemountain.slicker.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.codemountain.slicker.R;
import com.codemountain.slicker.adapters.ChatAdapter;
import com.codemountain.slicker.model.ModelChat;
import com.codemountain.slicker.model.ModelUsers;
import com.codemountain.slicker.notification.Data;
import com.codemountain.slicker.notification.Sender;
import com.codemountain.slicker.notification.Token;
import com.codemountain.slicker.utils.GetTimeAgo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CircleImageView chatToolbarImage;
    private TextView chatToolbarName, chatToolbarOnline;
    private RecyclerView recyclerView;
    private EditText typeMessage;
    private ImageButton sendMessageImageBtn;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private DatabaseReference userDbRef;
    private FirebaseDatabase firebaseDatabase;

    private String otherPersonId;
    private String myUid;
    private String otherPersonImage;

    private ValueEventListener isSeenListener;
    private DatabaseReference userRefForSeen;

    private List<ModelChat> messageList = new ArrayList<>();
    private ChatAdapter chatAdapter;

    //Volley queue for notification
    private RequestQueue requestQueue;
    private boolean notify = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar = findViewById(R.id.chatToolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.chatRecyclerView);

        chatToolbarImage = findViewById(R.id.chatToolbarImage);
        chatToolbarName = findViewById(R.id.chatToolbarName);
        chatToolbarOnline = findViewById(R.id.onlineStatus);
        typeMessage = findViewById(R.id.messageEditText);
        sendMessageImageBtn = findViewById(R.id.sendMessageBtn);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //checkUserStatus();

        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        otherPersonId = intent.getStringExtra("otherPersonId");

        firebaseDatabase = FirebaseDatabase.getInstance();
        userDbRef = firebaseDatabase.getReference("Users");
        userDbRef.keepSynced(true);

        Query query = userDbRef.orderByChild("uid").equalTo(otherPersonId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()){

                    String name = ds.child("name").getValue().toString();
                    otherPersonImage = ds.child("image").getValue().toString();
                    String onlineStatus = ds.child("online_status").getValue().toString();
                    String typingStatus = ds.child("typing_to").getValue().toString();

                    //Checking typing status && online && offline
                    if(typingStatus.equals(myUid)){
                        chatToolbarOnline.setText("typing...");
                    }
                    else {
                        if(onlineStatus.equals("online")){
                            chatToolbarOnline.setText("online");
                        }
                        else {
                            long lastTime = Long.parseLong(onlineStatus);
                            String lastSeenTime = GetTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                            chatToolbarOnline.setText("Last seen: " + lastSeenTime);
                        }
                    }
                    chatToolbarName.setText(name);

                    //Loading the image
                    Glide.with(ChatActivity.this).load(otherPersonImage)
                            .placeholder(R.drawable.default_image)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .into(chatToolbarImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChatActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

        sendMessageImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                notify = true;
                String message = typeMessage.getText().toString().trim();
                if(TextUtils.isEmpty(message)){
                    Toast.makeText(ChatActivity.this, "Cannot send empty text", Toast.LENGTH_SHORT).show();
                }
                else {
                    sendMessage(message);
                }
                typeMessage.setText("");
            }
        });

        typeMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.toString().trim().length() == 0){
                    checkTypingStatus("noOne");
                }
                else {
                    checkTypingStatus(otherPersonId);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        readMessages();

        seenMessage();

    }

    private void seenMessage() {

        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        isSeenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()){

                    ModelChat seen = ds.getValue(ModelChat.class);
                    if(seen.getReceiver().equals(myUid) && seen.getSender().equals(otherPersonId)){
                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("isSeen", true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessages() {

        messageList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                messageList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){

                    ModelChat chat = ds.getValue(ModelChat.class);
                    if(chat.getReceiver().equals(myUid) && chat.getSender().equals(otherPersonId)
                            || chat.getReceiver().equals(otherPersonId) && chat.getSender().equals(myUid)){

                        messageList.add(chat);

                    }

                    chatAdapter = new ChatAdapter(ChatActivity.this, messageList, otherPersonImage);
                    chatAdapter.notifyDataSetChanged();
                    recyclerView.setAdapter(chatAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(final String message) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", otherPersonId);
        hashMap.put("message", message);
        hashMap.put("timestamp", timestamp);
        hashMap.put("isSeen", false);
        reference.child("Chats").push().setValue(hashMap);


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ModelUsers users = dataSnapshot.getValue(ModelUsers.class);

                if(notify){
                    sendNotification(otherPersonId, users.getName(), message);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /*#####################################################################*/
        //Create chatList node child in database root
        final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList").child(myUid).child(otherPersonId);
        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef1.child("id").setValue(otherPersonId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList").child(otherPersonId).child(myUid);
        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef2.child("id").setValue(myUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void checkUserStatus() {

        currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //User is signed in stay here

            myUid = currentUser.getUid();
        }
        else {

            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void sendNotification(final String otherPersonId, final String name, final String message){
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(otherPersonId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(myUid, name + ": " + message,  otherPersonId, "New message", R.drawable.default_image);

                    Sender sender = new Sender(data, token.getToken());
                    //FCM JSON object request
                    try{
                        JSONObject senderJSONObject = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJSONObject,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Log.d("JSONRESPONSE", "onResponse: "+ response.toString());
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("JSONRESPONSE", "onResponse: "+ error.toString());
                            }
                        }){
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {

                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key=AAAA30g9Dz8:APA91bFevBaQc6XJvatp7zN1UB3MwKdibiEcgjhvUT0Efw2F0ftBpZQgd3T3D3sOeocK6HwNAFv40B6w2e9_sN8z4CNbfQXBLKoH6oMEYyvZs8rPI174ouInOE74AOdCpIFoBKrLfWmf");
                                return headers;
                            }
                        };
                        requestQueue.add(jsonObjectRequest);
                    }
                    catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkOnlineStatus(String status){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online_status", status);

        reference.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typing_to", typing);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {

        checkUserStatus();

        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        String lastSeenTime = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(lastSeenTime);
        checkTypingStatus("noOne");
        userRefForSeen.removeEventListener(isSeenListener);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_save).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_logout) {
            mAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(currentUser != null){

            String lastSeenTime = String.valueOf(System.currentTimeMillis());
            //checkOnlineStatus(lastSeenTime);
        }
    }
}
