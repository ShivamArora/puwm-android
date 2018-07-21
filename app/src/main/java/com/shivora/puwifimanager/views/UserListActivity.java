package com.shivora.puwifimanager.views;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.shivora.puwifimanager.R;
import com.shivora.puwifimanager.model.adapters.UserListAdapter;
import com.shivora.puwifimanager.model.database.UserDatabase;
import com.shivora.puwifimanager.model.database.UserEntry;
import com.shivora.puwifimanager.networking.NetportalService;
import com.shivora.puwifimanager.networking.Listeners;
import com.shivora.puwifimanager.networking.RetrofitClient;
import com.shivora.puwifimanager.networking.SecureLoginService;
import com.shivora.puwifimanager.utils.ConnectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserListActivity extends AppCompatActivity {

    private static final String TAG = UserListActivity.class.getSimpleName();
    private Context context;

    private UserDatabase mUserDatabase;
    private UserListAdapter mUserListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context,AddUserActivity.class));
            }
        });

        context = UserListActivity.this;
        mUserDatabase = UserDatabase.getInstance(context);

        RecyclerView recyclerView = findViewById(R.id.rv_userlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        mUserListAdapter = new UserListAdapter(null);
        recyclerView.setAdapter(mUserListAdapter);
        fetchUsers();
    }

    private void fetchUsers(){
        LiveData<List<UserEntry>> usersList = mUserDatabase.userDao().loadAllUsers();
        usersList.observe(UserListActivity.this, new Observer<List<UserEntry>>() {
            @Override
            public void onChanged(@Nullable List<UserEntry> userEntries) {
                //TODO: Display list of users using userEntries
                for (UserEntry userEntry: userEntries){
                    Log.d(TAG, "User: "+userEntry);
                }
                mUserListAdapter.setUserList(userEntries);
            }
        });
    }

    private void deleteUser(UserEntry user){
        mUserDatabase.userDao().deleteUser(user);
    }

    private void changePassword(final String user, final String password, final String newPassword, final Listeners.OnPasswordChangeSuccessfulListener passwordChangeSuccessfulListener) {
        login((Activity) context, user, password, new Listeners.OnLoginCompleteListener() {
            @Override
            public void onLoginComplete(boolean isLoggedIn) {
                if (isLoggedIn) {
                    netportalLogin(user, password, new Listeners.OnNetportalLoginCompleteListener() {
                        @Override
                        public void onNetportalLoginComplete() {
                            Map<String, String> changePasswordParams = new HashMap<>();
                            changePasswordParams.put(NetportalService.PARAM_OLD_PASS, password);
                            changePasswordParams.put(NetportalService.PARAM_NEW_PASSWORD, newPassword);

                            NetportalService netportalService = RetrofitClient.getNetportalInstance(context).create(NetportalService.class);
                            Call<String> changePasswordCall = netportalService.changePassword(changePasswordParams);
                            changePasswordCall.enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    if (response.body().contains(NetportalService.PASSWORD_CHANGE_SUCCESS)) {
                                        Log.i(TAG, "onResponse: " + "Password Changed Successfully!");
                                        if (passwordChangeSuccessfulListener!=null){
                                            passwordChangeSuccessfulListener.onPasswordChangeSuccessful();
                                        }
                                    } else {
                                        Log.e(TAG, "onResponse: " + "Failed to change password");
                                    }
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Log.e(TAG, "onFailure: " + t.getMessage());
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    private void netportalLogin(String user, String password, final Listeners.OnNetportalLoginCompleteListener netportalLoginCompleteListener) {
        //Build the params
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put(NetportalService.PARAM_LOGIN_ID, user);
        userInfo.put(NetportalService.PARAM_PASSWORD, password);

        final NetportalService netportalService = RetrofitClient.getNetportalInstance(context).create(NetportalService.class);
        Call<String> call = netportalService.loginNetPortal(userInfo);
        //Log.d(TAG, "Netportal call initiated");
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                String returnedHtmlData = response.body();
                if (returnedHtmlData.contains(NetportalService.NETPORTAL_LOGIN_SUCCESS)) {
                    Log.i(TAG, "onResponse: " + "Netportal Login Successful!");
                    if(netportalLoginCompleteListener!=null){
                        netportalLoginCompleteListener.onNetportalLoginComplete();
                    }
                }
                else{
                    Log.e(TAG, "onResponse: " + "Failed to login NetPortal" );
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    private void login(final Activity context, String user, String password, final Listeners.OnLoginCompleteListener onLoginCompleteListener) {
        //Build the parameters
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put(SecureLoginService.PARAM_USER_ID,user);
        userInfo.put(SecureLoginService.PARAM_PASSWORD,password);

        if (ConnectionUtils.isConnectedToPuWifi(context)) {
            SecureLoginService secureLoginService = RetrofitClient.getSecureLoginInstance().create(SecureLoginService.class);
            Call<String> call = secureLoginService.loginUser(userInfo);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    String returnedHtmlData = response.body();
                    //Log.i(TAG, "onResponse: " + returnedHtmlData);
                    //Log.i(TAG, "Headers: " + response.headers().toString());
                    if (returnedHtmlData.contains(SecureLoginService.LOGIN_SUCCESS_STRING)) {
                        Log.i(TAG, "onResponse: " + "Login Successful!");
                        onLoginCompleteListener.onLoginComplete(true);
                    } else if (returnedHtmlData.contains(SecureLoginService.LOGIN_FAILURE_STRING)) {
                        Log.e(TAG, "onResponse: " + "Login Failed");
                        onLoginCompleteListener.onLoginComplete(false);
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.e(TAG, "onFailure: " + t.getMessage());
                    if (t.getMessage().contains(SecureLoginService.LOGIN_ALREADY_LOGGED_IN)) {
                        Log.e(TAG, "onFailure: " + "User already logged in");
                    }
                    onLoginCompleteListener.onLoginComplete(true);
                    Toast.makeText(context, "Failed to login", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void logout(final Activity context, final Listeners.OnLogoutCompleteListener logoutCompleteListener) {
        if (ConnectionUtils.isConnectedToPuWifi(context)) {
            SecureLoginService secureLoginService = RetrofitClient.getSecureLoginInstance().create(SecureLoginService.class);
            Call<String> call = secureLoginService.logoutUser();
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    //Log.i(TAG, "onResponse: " + response.body());
                    String returnedHtmlData = response.body();
                    if (returnedHtmlData.contains(SecureLoginService.LOGOUT_SUCCESS_STRING)) {
                        Log.i(TAG, "onResponse: " + "Logout Successful");
                        if (logoutCompleteListener!=null){
                            logoutCompleteListener.onLogoutComplete();
                        }
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.e(TAG, "onFailure: " + t.getMessage());
                }
            });
        }
    }

}
