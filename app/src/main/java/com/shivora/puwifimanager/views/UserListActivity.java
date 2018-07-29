package com.shivora.puwifimanager.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.andrognito.flashbar.Flashbar;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.shivora.puwifimanager.R;
import com.shivora.puwifimanager.model.adapters.ListItemClickListener;
import com.shivora.puwifimanager.model.adapters.UserListAdapter;
import com.shivora.puwifimanager.model.database.UserDatabase;
import com.shivora.puwifimanager.model.database.UserEntry;
import com.shivora.puwifimanager.networking.NetportalService;
import com.shivora.puwifimanager.networking.Listeners;
import com.shivora.puwifimanager.networking.RetrofitClient;
import com.shivora.puwifimanager.networking.SecureLoginService;
import com.shivora.puwifimanager.utils.AppExecutors;
import com.shivora.puwifimanager.utils.ConnectionUtils;
import com.shivora.puwifimanager.utils.FlashbarUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserListActivity extends AppCompatActivity implements ListItemClickListener {

    private static final String TAG = UserListActivity.class.getSimpleName();
    private Context context;

    private UserDatabase mUserDatabase;
    private UserListAdapter mUserListAdapter;

    private TextInputEditText etNewPassword, etConfirmPassword;
    private AdView mAdView;
    private FloatingActionButton fab;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, AddUserActivity.class));
            }
        });

        context = UserListActivity.this;
        mUserDatabase = UserDatabase.getInstance(context);
        //Init analytics
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        //Init Mobile Ads
        MobileAds.initialize(context, getString(R.string.admob_app_id));

        RecyclerView recyclerView = findViewById(R.id.rv_userlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        mUserListAdapter = new UserListAdapter(new ArrayList<UserEntry>(), this);
        recyclerView.setAdapter(mUserListAdapter);
        fetchUsers();
        loadAds();
    }

    private void loadAds() {
        mAdView = findViewById(R.id.adview_userlist_banner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setVisibility(View.GONE);
        //Listener to make sure adview is visible only when ad is successfully loaded
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.d(TAG, "onAdLoaded: true");
                mAdView.setVisibility(View.VISIBLE);
                int margin = (int) getResources().getDimension(R.dimen.fab_margin);
                Log.d(TAG, "onAdLoaded: " + margin);
                setFabBottomMargin(convertDpToPixel(66));
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                Log.d(TAG, "onAdFailedToLoad: true");
                mAdView.setVisibility(View.GONE);
                setFabBottomMargin(convertDpToPixel(16));
            }
        });
    }

    private void setFabBottomMargin(int margin) {
        int defaultFabMargin = (int) getResources().getDimension(R.dimen.fab_margin);
        if (fab.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
            params.setMargins(defaultFabMargin, defaultFabMargin, defaultFabMargin, margin);
            fab.requestLayout();
        }
    }

    private int convertDpToPixel(int dp) {
        Resources resources = getResources();
        float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
        return Math.round(pixels);
    }

    @Override
    public void onListItemClick(final UserEntry user) {
        Log.i(TAG, "onListItemClick: " + user.getNickname());
        final UserOptionsBottomSheet userOptionsBottomSheet = new UserOptionsBottomSheet();
        userOptionsBottomSheet.setOnUserOptionClickListener(new UserOptionsBottomSheet.UserOptionsClickListener() {
            @Override
            public void onUserOptionClicked(View view) {
                int id = view.getId();
                userOptionsBottomSheet.dismiss();
                switch (id) {
                    case R.id.item_login:
                        login((Activity) context, user.getUserId(), user.getPassword(), new Listeners.OnLoginCompleteListener() {
                            @Override
                            public void onLoginComplete(boolean isLoggedIn) {
                                Log.d(TAG, "onLoginComplete: " + isLoggedIn);
                                Toast.makeText(context, "Logged in?: " + isLoggedIn, Toast.LENGTH_SHORT).show();
                                if (isLoggedIn){
                                    FlashbarUtils.showMessageDialog((Activity) context,"Login Successful");
                                }
                                else{
                                    FlashbarUtils.showErrorDialog((Activity) context,"Failed to login");
                                }
                            }
                        });
                        break;

                    case R.id.item_edit_user:
                        Intent editUserIntent = new Intent(context, AddUserActivity.class);
                        editUserIntent.putExtra(AddUserActivity.EXTRA_USER_ID, user.getUserId());
                        startActivity(editUserIntent);
                        break;
                    case R.id.item_change_password:
                        AlertDialog changePasswordDialog = buildChangePasswordDialog(user);
                        changePasswordDialog.show();
                        break;
                    case R.id.item_delete_user:
                        deleteUser(user);
                        break;
                    default:
                        Log.e(TAG, "onUserOptionClicked: " + "UnsupportedOperation");
                        Crashlytics.logException(new Exception("Unsupported User Option Clicked"));
                }
            }
        });
        userOptionsBottomSheet.show(getSupportFragmentManager(), "UserOptions");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_user_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                logout((Activity) context, new Listeners.OnLogoutCompleteListener() {
                    @Override
                    public void onLogoutComplete() {
                        //TODO: Notify logout complete
                        FlashbarUtils.showMessageDialog((Activity) context, "Logged out successfully!");
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Gets the saved users list from the database
     * and display the list using LiveData
     */
    private void fetchUsers() {
        LiveData<List<UserEntry>> usersList = mUserDatabase.userDao().loadAllUsers();
        usersList.observe(UserListActivity.this, new Observer<List<UserEntry>>() {
            @Override
            public void onChanged(@Nullable List<UserEntry> userEntries) {
                for (UserEntry userEntry : userEntries) {
                    Log.d(TAG, "User: " + userEntry);
                }
                mUserListAdapter.setUserList(userEntries);
            }
        });
    }


    private AlertDialog buildChangePasswordDialog(final UserEntry user) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = getLayoutInflater();

        View dialog = inflater.inflate(R.layout.dialog_change_password, null);
        etNewPassword = dialog.findViewById(R.id.et_new_password);
        etConfirmPassword = dialog.findViewById(R.id.et_confirm_password);

        builder.setTitle("Change Password")
                .setView(dialog)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String newPassword = etNewPassword.getText().toString().trim();
                        String confirmPassword = etConfirmPassword.getText().toString().trim();

                        //Check if length of password is minimum 6 characters
                        if (newPassword.length() >= 6) {
                            //Check if both new and confirm password are equal
                            if (TextUtils.equals(newPassword, confirmPassword)) {
                                Log.d(TAG, "onClick: " + "Passwords are same");
                                //Change Password
                                changePassword(user.getUserId(), user.getPassword(), newPassword, new Listeners.OnPasswordChangeSuccessfulListener() {
                                    @Override
                                    public void onPasswordChangeSuccessful() {
                                        Log.d(TAG, "onPasswordChangeSuccessful: " + "Password Changed on network");
                                        AddUserActivity.updateUser(user.getUserId(), newPassword, user.getNickname());
                                        Log.d(TAG, "onPasswordChangeSuccessful: " + "Password changed on local database");
                                        FlashbarUtils.showMessageDialog((Activity) context,"Password changed successfully!");
                                    }
                                });
                            } else {
                                //TODO: Display dialog two passwords should match
                                Log.d(TAG, "onClick: " + "Passwords are different");
                                FlashbarUtils.showErrorDialog((Activity) context, "The two passwords must match!");
                            }
                        } else {
                            //TODO: Display dialog length of password should be atleast 6 characters
                            FlashbarUtils.showErrorDialog((Activity) context, "Length of password should be atleast 6 characters");
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        return builder.create();
    }

    /**
     * Deletes the user from the database
     *
     * @param user
     */
    private void deleteUser(final UserEntry user) {
        //TODO: Ask for confirmation before deleting
        FlashbarUtils.showConfirmationDialog((Activity) context, "Are you sure you want to delete this user?",
                new Flashbar.OnActionTapListener() {
                    @Override
                    public void onActionTapped(Flashbar flashbar) {
                        Log.i(TAG, "onActionTapped: "+"User selected YES");
                        //Delete user
                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                mUserDatabase.userDao().deleteUser(user);
                            }
                        });
                    }
                },
                new Flashbar.OnActionTapListener() {
                    @Override
                    public void onActionTapped(Flashbar flashbar) {
                        Log.i(TAG, "onActionTapped: "+"User selected NO");
                    }
                });
    }

    /**
     * Changes the password on network
     *
     * @param user
     * @param password
     * @param newPassword
     * @param passwordChangeSuccessfulListener
     */
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
                                        if (passwordChangeSuccessfulListener != null) {
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
                    if (netportalLoginCompleteListener != null) {
                        netportalLoginCompleteListener.onNetportalLoginComplete();
                    }
                } else {
                    Log.e(TAG, "onResponse: " + "Failed to login NetPortal");
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
        userInfo.put(SecureLoginService.PARAM_USER_ID, user);
        userInfo.put(SecureLoginService.PARAM_PASSWORD, password);

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
                        if (logoutCompleteListener != null) {
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
