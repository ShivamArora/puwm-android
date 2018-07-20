package com.shivora.puwifimanager.views;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.shivora.puwifimanager.R;
import com.shivora.puwifimanager.database.UserDatabase;
import com.shivora.puwifimanager.database.UserEntry;

public class AddUserActivity extends AppCompatActivity {

    private Context context;
    private UserDatabase mUserDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        context = AddUserActivity.this;
        mUserDatabase = UserDatabase.getInstance(context);
    }

    private void fetchUser(String userId){
        final LiveData<UserEntry> user = mUserDatabase.userDao().loadUserById(userId);
        user.observe(AddUserActivity.this, new Observer<UserEntry>() {
            @Override
            public void onChanged(@Nullable UserEntry userEntry) {
                user.removeObserver(this);

                //TODO: Show user data in activity
            }
        });
    }

    private void insertUser(String userId,String password,String nickname){
        UserEntry user = new UserEntry(userId,password,nickname);
        mUserDatabase.userDao().insertUser(user);
    }

    private void updateUser(String userId,String password,String nickname){
        UserEntry user = new UserEntry(userId,password,nickname);
        mUserDatabase.userDao().updateUser(user);
    }
}
