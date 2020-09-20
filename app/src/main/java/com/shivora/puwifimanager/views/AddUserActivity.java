package com.shivora.puwifimanager.views;

import android.app.Activity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import android.content.Context;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.shivora.puwifimanager.R;
import com.shivora.puwifimanager.model.database.UserDatabase;
import com.shivora.puwifimanager.model.database.UserEntry;
import com.shivora.puwifimanager.utils.AppExecutors;
import com.shivora.puwifimanager.utils.FlashbarUtils;

public class AddUserActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "user_id";
    private static final String DEFAULT_USER_ID = "000000";
    public static final String TAG = AddUserActivity.class.getSimpleName();

    private EditText etNickName,etUserId,etPassword;
    private Button btnSubmit;
    private Context context;
    private static UserDatabase mUserDatabase;
    private static UserEntry mUser;

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
        btnSubmit = findViewById(R.id.btn_add);
    }

    private void fetchUser(String userId){
        final LiveData<UserEntry> user = mUserDatabase.userDao().loadUserById(userId);
        user.observe(AddUserActivity.this, new Observer<UserEntry>() {
            @Override
            public void onChanged(@Nullable UserEntry userEntry) {
                user.removeObserver(this);

                mUser = userEntry;
                etNickName.setText(userEntry.getNickname());
                etUserId.setText(userEntry.getUserId());
                etPassword.setText(userEntry.getPassword());
                Log.d(TAG, "Password: "+userEntry.getPassword());

                //Update button UI
                btnSubmit.setText("Save User");
                btnSubmit.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_baseline_save_24px,0,0);
                //Update title
                setTitle(R.string.update_user);
            }
        });
    }

    private long insertUser(String userId,String password,String nickname){
        mUser = new UserEntry(userId,password,nickname);
        return mUserDatabase.userDao().insertUser(mUser);
    }

    public static void updateUser(String userId,String password,String nickname){
        mUser.setUserId(userId);
        mUser.setPassword(password);
        mUser.setNickname(nickname);
        mUserDatabase.userDao().updateUser(mUser);
    }

    public void submit(View view) {
        final String nickname = etNickName.getText().toString().trim();
        final String userId = etUserId.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(nickname)||TextUtils.isEmpty(userId)||TextUtils.isEmpty(password)){
            FlashbarUtils.showErrorDialog((Activity) context,"All fields are required");
        }
        else if (password.length() < 6){
            FlashbarUtils.showErrorDialog((Activity) context, "Length error!","Length of password should be atleast 6 characters");
        }
        else{
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: executor");
                    if (mUserId.equals(DEFAULT_USER_ID)) {
                        long id = insertUser(userId, password, nickname);
                        if (id<1){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    FlashbarUtils.showErrorDialog((Activity) context,"Failed to create user","A user with the same user id might already exist!");
                                }
                            });
                            return;
                        }
                        //Set Result for AddUserIntro
                        setResult(RESULT_OK,null);
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
