package com.shivora.puwifimanager.views;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.shivora.puwifimanager.R;
import com.shivora.puwifimanager.networking.RetrofitClient;
import com.shivora.puwifimanager.networking.SecureLoginService;
import com.shivora.puwifimanager.utils.ConnectionUtils;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserListActivity extends AppCompatActivity {

    private static final String TAG = UserListActivity.class.getSimpleName();
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        context = UserListActivity.this;

        Map<String,String> userInfo = new HashMap<>();
        userInfo.put("user","146966");
        userInfo.put("password","shivam@#300A");

        
    }

    private void login(final Activity context, Map<String, String> userInfo) {
        if (ConnectionUtils.isConnectedToPuWifi(context)) {
            SecureLoginService secureLoginService = RetrofitClient.getSecureLoginInstance().create(SecureLoginService.class);
            Call<String> call = secureLoginService.loginUser(userInfo);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    String returnedHtmlData = response.body();
                    Log.i(TAG, "onResponse: " + returnedHtmlData);
                    Log.i(TAG, "Headers: " + response.headers().toString());
                    if (returnedHtmlData.contains(SecureLoginService.LOGIN_SUCCESS_STRING)) {
                        Log.i(TAG, "onResponse: " + "Login Successful!");
                    } else if (returnedHtmlData.contains(SecureLoginService.LOGIN_FAILURE_STRING)) {
                        Log.e(TAG, "onResponse: " + "Login Failed");
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.e(TAG, "onFailure: " + t.getMessage());
                    if (t.getMessage().contains(SecureLoginService.LOGIN_ALREADY_LOGGED_IN)) {
                        Log.e(TAG, "onFailure: " + "User already logged in");
                    }
                    Toast.makeText(context, "Failed to login", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void logout(final Activity context) {
        if (ConnectionUtils.isConnectedToPuWifi(context)) {
            SecureLoginService secureLoginService = RetrofitClient.getSecureLoginInstance().create(SecureLoginService.class);
            Call<String> call = secureLoginService.logoutUser();
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    Log.i(TAG, "onResponse: " + response.body());
                    String returnedHtmlData = response.body();
                    if (returnedHtmlData.contains(SecureLoginService.LOGOUT_SUCCESS_STRING)) {
                        Log.i(TAG, "onResponse: " + "Logout Successful");
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
