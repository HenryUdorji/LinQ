package com.codemountain.slicker.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.codemountain.slicker.R;
import com.codemountain.slicker.activities.MainActivity;
import com.codemountain.slicker.activities.UserProfileActivity;
import com.codemountain.slicker.adapters.UsersAdapter;
import com.codemountain.slicker.model.ModelUsers;
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
public class UsersFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    /*private RecyclerView recyclerViewFriendRequest;
    private FriendRequestAdapter friendRequestAdapter;
    private List<ModelUsers> friendRequestList = new ArrayList<>();*/
    private RecyclerView recyclerViewUsers;
    private UsersAdapter usersAdapter;
    private List<ModelUsers> usersList = new ArrayList<>();

    private SwipeRefreshLayout swipeRefreshLayout;


    public UsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_users, container, false);

        //Init Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        //RecyclerView for all users
        recyclerViewUsers = view.findViewById(R.id.usersRecyclerView);
        recyclerViewUsers.setHasFixedSize(true);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getActivity()));

        //RecyclerView for friend request
        /*recyclerViewFriendRequest = view.findViewById(R.id.friendRequestRecyclerView);
        recyclerViewFriendRequest.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerViewFriendRequest.setLayoutManager(linearLayoutManager);*/

        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(this);

        getAllUsers();
        //getFriendRequest();
        return view;
    }

    /*private void getFriendRequest() {
        swipeRefreshLayout.setRefreshing(true);
        final String UID = mAuth.getUid();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference requestReference = FirebaseDatabase.getInstance().getReference("Friend_request").child(UID);
        requestReference.keepSynced(true);
        requestReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendRequestList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    if (!currentUser.getUid().equals(UID)) {
                        ModelUsers modelUsers = ds.getValue(ModelUsers.class);
                        friendRequestList.add(modelUsers);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    friendRequestAdapter = new FriendRequestAdapter(getActivity(), friendRequestList);
                    friendRequestAdapter.notifyDataSetChanged();
                    recyclerViewFriendRequest.setAdapter(friendRequestAdapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }*/

    private void getAllUsers() {
        swipeRefreshLayout.setRefreshing(true);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.keepSynced(true);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();

                for (DataSnapshot ds : dataSnapshot.getChildren()){

                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);

                    //get all users except currently signed in user
                    if(!modelUsers.getUid().equals(currentUser.getUid())){
                        usersList.add(modelUsers);
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    usersAdapter = new UsersAdapter(getActivity(), usersList);
                    usersAdapter.notifyDataSetChanged();
                    usersAdapter.setOnItemClickListener(new UsersAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            String otherPersonId = usersList.get(position).getUid();
                            Intent userProfileIntent = new Intent(getContext(), UserProfileActivity.class);
                            userProfileIntent.putExtra("otherPersonId", otherPersonId);
                            startActivity(userProfileIntent);
                        }
                    });
                    recyclerViewUsers.setAdapter(usersAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void searchUsers(final String query) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();

                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);

                    /**
                     * Search all users except currently signed in user
                     * text entered into the searchView are case insensitive
                     */

                    if(!modelUsers.getUid().equals(currentUser.getUid())){

                        if(modelUsers.getName().toLowerCase().contains(query.toLowerCase())
                                || modelUsers.getBio().toLowerCase().contains(query.toLowerCase())){

                            usersList.add(modelUsers);
                        }
                    }

                    usersAdapter = new UsersAdapter(getActivity(), usersList);
                    usersAdapter.notifyDataSetChanged();
                    recyclerViewUsers.setAdapter(usersAdapter);
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
        }
        else {

            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }


    /**
     * Inflating menu-item
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_save).setVisible(false);
        //SearchView
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setQueryHint("Search users");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!TextUtils.isEmpty(query)){
                    searchUsers(query);
                }
                else {
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if(!TextUtils.isEmpty(newText)){
                    searchUsers(newText);
                }
                else {
                    getAllUsers();
                }

                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }


    /**
     * Click listener for menu-item
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_logout:

                mAuth.signOut();
                checkUserStatus();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        getAllUsers();
    }
}
