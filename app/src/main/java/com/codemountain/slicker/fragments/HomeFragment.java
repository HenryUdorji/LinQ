package com.codemountain.slicker.fragments;


import android.app.ProgressDialog;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.codemountain.slicker.R;
import com.codemountain.slicker.activities.AddPostActivity;
import com.codemountain.slicker.activities.MainActivity;
import com.codemountain.slicker.adapters.PostAdapter;
import com.codemountain.slicker.model.ModelPost;
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
public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<ModelPost> postList = new ArrayList<>();

    private SwipeRefreshLayout swipeRefreshLayout;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //Init Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        recyclerView = view.findViewById(R.id.homeFragmentRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setHasFixedSize(true);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(this);

        loadPost();

        return view;
    }

    private void loadPost() {
        swipeRefreshLayout.setRefreshing(true);
        currentUser = mAuth.getCurrentUser();

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.keepSynced(true);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){

                    ModelPost post = ds.getValue(ModelPost.class);
                    postList.add(post);
                    swipeRefreshLayout.setRefreshing(false);
                }
                postAdapter = new PostAdapter(getActivity(), postList);
                recyclerView.setAdapter(postAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getActivity(), ""+ databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void searchPost(final String searchQuery){
        currentUser = mAuth.getCurrentUser();

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.keepSynced(true);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){

                    ModelPost post = ds.getValue(ModelPost.class);
                    if(post.getpDescription().toLowerCase().contains(searchQuery.toLowerCase())){
                        postList.add(post);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
                postAdapter = new PostAdapter(getActivity(), postList);
                recyclerView.setAdapter(postAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getActivity(), ""+ databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
        menu.findItem(R.id.action_save).setVisible(false);
        //menu.findItem(R.id.action_search).setVisible(false);
        //SearchView
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setQueryHint("Search post");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!TextUtils.isEmpty(query)){
                    searchPost(query);
                }
                else {
                    loadPost();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //usersAdapter.getFilter().filter(newText);
                if(!TextUtils.isEmpty(newText)){
                    searchPost(newText);
                }
                else {
                    loadPost();
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

            case R.id.action_add_post:
                startActivity(new Intent(getActivity(), AddPostActivity.class));

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        loadPost();
    }
}
