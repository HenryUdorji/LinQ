package com.codemountain.slicker.utils;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.fragment.app.FragmentTransaction;

import com.codemountain.slicker.R;
import com.codemountain.slicker.activities.DashboardActivity;
import com.codemountain.slicker.activities.UserProfileActivity;
import com.codemountain.slicker.fragments.ChatListFragment;
import com.codemountain.slicker.fragments.HomeFragment;
import com.codemountain.slicker.fragments.ProfileFragment;
import com.codemountain.slicker.fragments.UsersFragment;
import com.google.android.material.tabs.TabLayout;

public class TabLayoutHelper {
    private static final int HOME = 0;
    private static final int CHATS = 1;
    private static final int USERS = 2;
    private static final int PROFILE = 3;

    public static void EnableTabLayout(final Context context, TabLayout layout){
        layout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case HOME:
                        /*Intent callHome = new Intent(context, DashboardActivity.class);
                        context.startActivity(callHome);*/
                        HomeFragment homeFragment = new HomeFragment();
                        FragmentTransaction transaction = ((DashboardActivity)context).getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.container, homeFragment, "");
                        transaction.commit();
                        break;
                    case CHATS:
                        /*Intent callChats = new Intent(context, ChatListActivity.class);
                        context.startActivity(callChats);*/
                        ChatListFragment chatListFragment = new ChatListFragment();
                        FragmentTransaction transaction5 = ((DashboardActivity)context).getSupportFragmentManager().beginTransaction();
                        transaction5.replace(R.id.container, chatListFragment, "");
                        transaction5.commit();
                        break;
                    case USERS:
                        /*Intent callUsers = new Intent(context, AllUsersActivity.class);
                        context.startActivity(callUsers);*/
                        UsersFragment usersFragment = new UsersFragment();
                        FragmentTransaction transaction3 = ((DashboardActivity)context).getSupportFragmentManager().beginTransaction();
                        transaction3.replace(R.id.container, usersFragment, "");
                        transaction3.commit();
                        break;
                    case PROFILE:
                        /*Intent callProfile = new Intent(context, MyProfileActivity.class);
                        context.startActivity(callProfile);*/
                        ProfileFragment profileFragment = new ProfileFragment();
                        FragmentTransaction transaction2 = ((DashboardActivity)context).getSupportFragmentManager().beginTransaction();
                        transaction2.replace(R.id.container, profileFragment, "");
                        transaction2.commit();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case HOME:
                        /*Intent callHome = new Intent(context, DashboardActivity.class);
                        context.startActivity(callHome);*/
                        HomeFragment homeFragment = new HomeFragment();
                        FragmentTransaction transaction = ((DashboardActivity)context).getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.container, homeFragment, "");
                        transaction.commit();
                        break;
                    case CHATS:
                        /*Intent callChats = new Intent(context, ChatListActivity.class);
                        context.startActivity(callChats);*/
                        ChatListFragment chatListFragment = new ChatListFragment();
                        FragmentTransaction transaction5 = ((DashboardActivity)context).getSupportFragmentManager().beginTransaction();
                        transaction5.replace(R.id.container, chatListFragment, "");
                        transaction5.commit();
                        break;
                    case USERS:
                        /*Intent callUsers = new Intent(context, AllUsersActivity.class);
                        context.startActivity(callUsers);*/
                        UsersFragment usersFragment = new UsersFragment();
                        FragmentTransaction transaction3 = ((DashboardActivity)context).getSupportFragmentManager().beginTransaction();
                        transaction3.replace(R.id.container, usersFragment, "");
                        transaction3.commit();
                        break;
                    case PROFILE:
                        /*Intent callProfile = new Intent(context, MyProfileActivity.class);
                        context.startActivity(callProfile);*/
                        ProfileFragment profileFragment = new ProfileFragment();
                        FragmentTransaction transaction2 = ((DashboardActivity)context).getSupportFragmentManager().beginTransaction();
                        transaction2.replace(R.id.container, profileFragment, "");
                        transaction2.commit();
                        break;
                }
            }
        });
    }
}
