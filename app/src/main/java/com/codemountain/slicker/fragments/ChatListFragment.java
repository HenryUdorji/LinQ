package com.codemountain.slicker.fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.codemountain.slicker.R;
import com.codemountain.slicker.activities.AddPostActivity;
import com.codemountain.slicker.activities.ChatActivity;
import com.codemountain.slicker.activities.MainActivity;
import com.codemountain.slicker.adapters.ChatListAdapter;
import com.codemountain.slicker.model.ModelChat;
import com.codemountain.slicker.model.ModelChatList;
import com.codemountain.slicker.model.ModelUsers;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private FloatingActionButton friendFab;
    private RecyclerView recyclerView;
    private ChatListAdapter chatListAdapter;
    private List<ModelUsers> usersList = new ArrayList<>();
    private List<ModelChatList> chatLists = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference reference;


    public ChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        //Initialize recyclerView
        recyclerView = view.findViewById(R.id.chatListRecyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(this);

        friendFab = view.findViewById(R.id.friendsFab);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        reference = FirebaseDatabase.getInstance().getReference("ChatList").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatLists.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelChatList modelChatList = ds.getValue(ModelChatList.class);
                    chatLists.add(modelChatList);
                }
                loadChats();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        friendFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FriendsFragment friendsFragment = new FriendsFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.container, friendsFragment, "find this fragment");
                transaction.addToBackStack("CHAT_LIST_FRAGMENT");
                transaction.commit();
            }
        });

        return view;
    }

    private void loadChats() {
        swipeRefreshLayout.setRefreshing(true);
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);
                    for (ModelChatList modelChatList : chatLists){
                        if (modelUsers.getUid() != null && modelUsers.getUid().equals(modelChatList.getId())){
                            usersList.add(modelUsers);
                            break;
                        }
                    }

                    chatListAdapter = new ChatListAdapter(getActivity(), usersList);
                    chatListAdapter.setOnItemClickedListener(new ChatListAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClicked(View view, int position) {
                            String id = usersList.get(position).getUid();
                            Intent intent = new Intent(getContext(), ChatActivity.class);
                            intent.putExtra("otherPersonId", id);
                            startActivity(intent);

                        }
                    });
                    chatListAdapter.notifyDataSetChanged();
                    recyclerView.setAdapter(chatListAdapter);

                    //set last message
                    for (int i = 0; i < usersList.size(); i++){
                        lastMessage(usersList.get(i).getUid());
                    }
                    swipeRefreshLayout.setRefreshing(false);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void lastMessage(final String uid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String lastMessage = "default";
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    ModelChat modelChat = ds.getValue(ModelChat.class);
                    if (modelChat == null){
                        continue;
                    }
                    String sender = modelChat.getSender();
                    String receiver = modelChat.getReceiver();
                    if (sender == null || receiver == null){
                        continue;
                    }
                    if (modelChat.getReceiver().equals(currentUser.getUid()) && modelChat.getSender().equals(uid)
                            || modelChat.getReceiver().equals(uid) && modelChat.getSender().equals(currentUser.getUid())){
                        lastMessage = modelChat.getMessage();

                    }
                }
                chatListAdapter.setLastMessageMap(uid, lastMessage);
                chatListAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_save).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_logout:

                mAuth.signOut();
                checkUserStatus();
                break;

            case R.id.action_add_post:
                startActivity(new Intent(getActivity(), AddPostActivity.class));

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus() {

        currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //User is signed in stay here
        }
        else {

            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onRefresh() {
        loadChats();

    }
}
