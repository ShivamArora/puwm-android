package com.shivora.puwifimanager.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.andrognito.flashbar.Flashbar;
import com.crashlytics.android.Crashlytics;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;
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
import com.shivora.puwifimanager.networking.Listeners;
import com.shivora.puwifimanager.networking.NetportalService;
import com.shivora.puwifimanager.networking.RetrofitClient;
import com.shivora.puwifimanager.networking.SecureLoginService;
import com.shivora.puwifimanager.utils.AppExecutors;
import com.shivora.puwifimanager.utils.ConnectionUtils;
import com.shivora.puwifimanager.utils.FlashbarUtils;
import com.shivora.puwifimanager.utils.analytics.Analytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserListActivity extends AppCompatActivity implements ListItemClickListener {

    private static final String TAG = UserListActivity.class.getSimpleName();
    public static final String EVENT_LOGIN_SUCCESSFUL = "Login Successful";
    public static final String PREFS_IS_ADD_USERS_SHOWN = "is_add_users_shown";
    public static final int RC_ADD_USER = 22;
    private Context context;

    private static UserDatabase mUserDatabase;
    private UserListAdapter mUserListAdapter;
    private static UserEntry mSelectedUser;

    private TextInputEditText etNewPassword, etConfirmPassword;
    private AdView mAdView;
    private FloatingActionButton fab;
    private Toolbar toolbar;

    SharedPreferences sharedPrefs;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_activity_user_list);
        setSupportActionBar(toolbar);
        setTitle(R.string.title_activity_user_list);

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
        introduceAddUserBtn();
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
                Log.d(TAG, "onAdFailedToLoad: true"+i);
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
        mSelectedUser = user;
        Log.i(TAG, "onListItemClick: " + user.getNickname());
        final UserOptionsBottomSheet userOptionsBottomSheet = new UserOptionsBottomSheet();
        userOptionsBottomSheet.setOnUserOptionClickListener(new UserOptionsBottomSheet.UserOptionsClickListener() {
            @Override
            public void onUserOptionClicked(View view) {
                int id = view.getId();
                userOptionsBottomSheet.dismiss();
                switch (id) {
                    case R.id.item_login:
                        Analytics.logEventLoginClicked(context);
                        login((Activity) context, user.getUserId(), user.getPassword(), new Listeners.OnLoginCompleteListener() {
                            @Override
                            public void onLoginComplete(boolean isLoggedIn) {
                                Log.d(TAG, "onLoginComplete: " + isLoggedIn);
                                //Toast.makeText(context, "Logged in?: " + isLoggedIn, Toast.LENGTH_SHORT).show();
                                if (isLoggedIn){
                                    Analytics.logEventLoginSuccessful(context);
                                    FlashbarUtils.showMessageDialog((Activity) context,"Login Successful","User has been logged in successfully!");
                                }
                                else{
                                    Analytics.logEventAuthenticationFailed(context);
                                    FlashbarUtils.showErrorDialog((Activity) context,"Authentication Failed","Either your user credentials are wrong or the user is already logged in somewhere else.");
                                }
                            }

                            @Override
                            public void onLoginComplete(boolean isLoggedIn, String msg) {
                                if (isLoggedIn){
                                    FlashbarUtils.showMessageDialog((Activity)context,msg);
                                }
                                else{
                                    FlashbarUtils.showErrorDialog((Activity)context,msg);
                                }
                            }
                        });
                        break;
                    case R.id.item_logout_user:
                        Analytics.logEventLogoutClicked(context);
                        logout((Activity) context, new Listeners.OnLogoutCompleteListener() {
                            @Override
                            public void onLogoutComplete() {
                                Analytics.logEventLogoutSuccessful(context);
                                FlashbarUtils.showMessageDialog((Activity) context,"Logout Sucessful!", "User has been logged out successfully!");
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==RC_ADD_USER){
            if (resultCode==RESULT_OK){
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putBoolean(PREFS_IS_ADD_USERS_SHOWN,true);
                editor.apply();
                introduceUserOptions();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        toolbar.inflateMenu(R.menu.menu_activity_user_list);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                Analytics.logEventLogoutClicked(context);
                logout((Activity) context, new Listeners.OnLogoutCompleteListener() {
                    @Override
                    public void onLogoutComplete() {
                        Analytics.logEventLogoutSuccessful(context);
                        FlashbarUtils.showMessageDialog((Activity) context,"Logout Sucessful!", "User has been logged out successfully!");
                    }
                });
                return true;
            case R.id.action_privacy_policy:
                Uri privacyPolicyUri = Uri.parse(getString(R.string.privacy_policy_url));
                Intent viewPrivacyPolicy = new Intent(Intent.ACTION_VIEW,privacyPolicyUri);
                viewPrivacyPolicy.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY|Intent.FLAG_ACTIVITY_NEW_DOCUMENT|Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                startActivity(viewPrivacyPolicy);
                return true;
            case R.id.action_rate_us:
                Uri uri = Uri.parse("market://details?id="+context.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW,uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY|Intent.FLAG_ACTIVITY_NEW_DOCUMENT|Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                try{
                    startActivity(goToMarket);
                }
                catch (ActivityNotFoundException e){
                    startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://play.google.com/store/apps/details?id="+context.getPackageName())));
                }
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
                                Analytics.logEventChangePasswordClicked(context);
                                changePassword(user.getUserId(), user.getPassword(), newPassword, new Listeners.OnPasswordChangeSuccessfulListener() {
                                    @Override
                                    public void onPasswordChangeSuccessful() {
                                        Analytics.logEventChangePasswordSuccessful(context);
                                        Log.d(TAG, "onPasswordChangeSuccessful: " + "Password Changed on network");
                                        //Change password on local database
                                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                updateUser(user.getUserId(), newPassword, user.getNickname());
                                            }
                                        });
                                        Log.d(TAG, "onPasswordChangeSuccessful: " + "Password changed on local database");
                                        FlashbarUtils.showMessageDialog((Activity) context,"Password Change Successful!","User Password has been changed successfully!" +
                                                "\n\nChanges may take sometime to take effect. Please be patient! :)");
                                    }
                                });
                            } else {
                                Log.d(TAG, "onClick: " + "Passwords are different");
                                FlashbarUtils.showErrorDialog((Activity) context, "Passwords must match!","The two passwords must match!");
                            }
                        } else {
                            FlashbarUtils.showErrorDialog((Activity) context, "Length error!","Length of password should be atleast 6 characters");
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
        FlashbarUtils.showConfirmationDialog((Activity) context,"Confirm Delete?", "Are you sure you want to delete this user?",
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
                        flashbar.dismiss();
                    }
                },
                new Flashbar.OnActionTapListener() {
                    @Override
                    public void onActionTapped(Flashbar flashbar) {
                        Log.i(TAG, "onActionTapped: "+"User selected NO");
                        flashbar.dismiss();
                    }
                });
    }


    public static void updateUser(String userId,String password,String nickname){
        mSelectedUser.setUserId(userId);
        mSelectedUser.setPassword(password);
        mSelectedUser.setNickname(nickname);
        mUserDatabase.userDao().updateUser(mSelectedUser);
    }

    private void introduceAddUserBtn(){
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isAddUsersShown = sharedPrefs.getBoolean(PREFS_IS_ADD_USERS_SHOWN,false);
        if (!isAddUsersShown){
            TapTargetView.showFor(this,
                    TapTarget.forView(findViewById(R.id.fab),getString(R.string.add_new_users),getString(R.string.info_add_new_users))
                                .transparentTarget(true),
                    new TapTargetView.Listener(){
                        @Override
                        public void onTargetClick(TapTargetView view) {
                            super.onTargetClick(view);
                            Intent addUserActivity = new Intent(context,AddUserActivity.class);
                            startActivityForResult(addUserActivity, RC_ADD_USER);
                        }
                    });
        }
    }

    private void introduceUserOptions() {
        View recyclerView = findViewById(R.id.rv_userlist);
        View view = recyclerView.findViewById(R.id.rv_userlist);
        Log.i(TAG, "introduceUserOptions: "+view);
            new TapTargetSequence(this).targets(
                    TapTarget.forView(view,getString(R.string.users_list),getString(R.string.intro_users_list))
                    .transparentTarget(true),
                    TapTarget.forToolbarMenuItem(toolbar,R.id.action_logout,getString(R.string.logout),getString(R.string.intro_logout))
                    .outerCircleColor(R.color.chuck_colorAccent).transparentTarget(true)
            ).start();
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
                else{
                    FlashbarUtils.showErrorDialog((Activity) context,"Authentication Failed","Either your user credentials are wrong or the user is already logged in somewhere else.");
                }
            }

            @Override
            public void onLoginComplete(boolean isLoggedIn, String msg) {
                if (isLoggedIn){
                    FlashbarUtils.showMessageDialog((Activity)context,msg);
                }
                else{
                    FlashbarUtils.showErrorDialog((Activity)context,msg);
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
                        onLoginCompleteListener.onLoginComplete(true);
                    }
                    else if(t.getMessage().contains(SecureLoginService.UNABLE_TO_RESOLVE_HOST)){
                        Log.e(TAG, "onFailure: "+getString(R.string.unable_to_resolve_host) );
                        onLoginCompleteListener.onLoginComplete(false,getString(R.string.unable_to_resolve_host));
                    }
                    //Toast.makeText(context, "Failed to login", Toast.LENGTH_SHORT).show();
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
                    else if(returnedHtmlData.contains(SecureLoginService.USER_NOT_LOGGED_IN)){
                        Log.i(TAG, "onResponse: "+SecureLoginService.USER_NOT_LOGGED_IN);
                        FlashbarUtils.showErrorDialog(context,SecureLoginService.USER_NOT_LOGGED_IN,"You haven't logged in with any user yet.");
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
