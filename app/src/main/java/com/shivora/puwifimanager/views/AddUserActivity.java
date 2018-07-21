package com.shivora.puwifimanager.views;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.shivora.puwifimanager.R;
import com.shivora.puwifimanager.model.database.UserDatabase;
import com.shivora.puwifimanager.model.database.UserEntry;
import com.shivora.puwifimanager.utils.AppExecutors;

public class AddUserActivity extends AppCompatActivity {

    private static final String EXTRA_USER_ID = "user_id";
    private static final String DEFAULT_USER_ID = "000000";

    private EditText etNickName,etUserId,etPassword;

    private Context context;
    private UserDatabase mUserDatabase;
    private UserEntry user;

    private String mUserId = DEFAULT_USER_ID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        context = AddUserActivity.this;
        mUserDatabase = UserDatabase.getInstance(context);

        initUI();

        if (getIntent().hasExtra(EXTRA_USER_ID)){
            mUserId = getIntent().getStringExtra(EXTRA_USER_ID);
            fetchUser(mUserId);
        }

    }

    private void initUI() {
        etNickName = findViewById(R.id.et_user_nickname);
        etUserId = findViewById(R.id.et_user_id);
        etPassword = findViewById(R.id.et_password);
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
        user = new UserEntry(userId,password,nickname);
        mUserDatabase.userDao().insertUser(user);
    }

    private void updateUser(String userId,String password,String nickname){
        user.setUserId(userId);
        user.setPassword(password);
        user.setNickname(nickname);
        mUserDatabase.userDao().updateUser(user);
    }

    public void submit(View view) {
        final String nickname = etNickName.getText().toString().trim();
        final String userId = etUserId.getText().toString().trim();
        final String password = etUserId.getText().toString().trim();

        if (TextUtils.isEmpty(nickname)||TextUtils.isEmpty(userId)||TextUtils.isEmpty(password)){
            //TODO: Show error - All Fields are required
        }
        else{
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    if (mUserId.equals(DEFAULT_USER_ID)) {
                        insertUser(userId, password, nickname);
                    }
                    else{
                        updateUser(userId,password,nickname);
                    }
                    finish();
                }
            });
        }
    }
}
