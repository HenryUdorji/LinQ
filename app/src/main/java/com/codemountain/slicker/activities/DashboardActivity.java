package com.codemountain.slicker.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.codemountain.slicker.R;
import com.codemountain.slicker.fragments.ChatListFragment;
import com.codemountain.slicker.fragments.FriendsFragment;
import com.codemountain.slicker.fragments.HomeFragment;
import com.codemountain.slicker.fragments.ProfileFragment;
import com.codemountain.slicker.fragments.UsersFragment;
import com.codemountain.slicker.model.ModelChat;
import com.codemountain.slicker.notification.Token;
import com.codemountain.slicker.utils.TabLayoutHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;


public class DashboardActivity extends AppCompatActivity {

    //Firebase
    private FirebaseAuth mAuth;
    private String myUid;
    private String otherPersonId;

    private ValueEventListener isSeenListener;
    private DatabaseReference userRefForSeen;

    private DatabaseReference userDbRef;
    private FirebaseDatabase firebaseDatabase;

    private Toolbar toolbar;
    private TabLayout tabLayout;

    private long backPressedTime;

    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userDbRef = firebaseDatabase.getReference("Users");
        userDbRef.keepSynced(true);

        Intent intent = getIntent();
        otherPersonId = intent.getStringExtra("otherPersonId");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Linq");

        tabLayout = findViewById(R.id.tabLayout);
        TabLayoutHelper.EnableTabLayout(DashboardActivity.this, tabLayout);

        HomeFragment homeFragment = new HomeFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, homeFragment, "");
        transaction.commit();

        seenMessage();
        checkUserStatus();

    }

    public void updateToken(String token){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(myUid).setValue(mToken);
    }

    private void checkUserStatus() {

        currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //User is signed in stay here

            myUid = currentUser.getUid();
            //Save uid of currently signed in user
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("CURRENT_USER_ID", myUid);
            editor.apply();

            //updateToken(FirebaseInstanceId.getInstance().getToken());
        }
        else {

            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {

        checkUserStatus();
        //checkOnlineStatus("online");
        super.onStart();
    }


    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate();
        } else {
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                finish();
            } else {
                Toast.makeText(this, "Press back again to close app", Toast.LENGTH_SHORT).show();
            }
            backPressedTime = System.currentTimeMillis();

        }
    }

   /* private void checkOnlineStatus(String status){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online_status", status);

        reference.updateChildren(hashMap);
    }*/


    @Override
    protected void onPause() {
        super.onPause();
       /* String lastSeenTime = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(lastSeenTime);*/
        userRefForSeen.removeEventListener(isSeenListener);
    }

    @Override
    protected void onResume() {
        //checkOnlineStatus("online");
        checkUserStatus();
        super.onResume();
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

    @Override
    protected void onStop() {
        super.onStop();

        if(currentUser != null){

            //String lastSeenTime = String.valueOf(System.currentTimeMillis());
            //checkOnlineStatus(lastSeenTime);
        }
    }

}
