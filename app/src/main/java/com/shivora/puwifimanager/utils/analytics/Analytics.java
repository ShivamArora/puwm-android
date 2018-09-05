package com.shivora.puwifimanager.utils.analytics;

import android.content.Context;

import com.google.firebase.analytics.FirebaseAnalytics;

public class Analytics {

    public static final String EVENT_LOGIN_SUCCESSFUL = "login_successful";
    public static final String EVENT_LOGOUT_SUCCESSFUL = "logout_successful";
    public static final String EVENT_LOGIN_CLICKED = "login_clicked";
    public static final String EVENT_AUTHENTICATION_FAILED = "authentication_failed";
    public static final String EVENT_LOGOUT_FAILED = "logout_failed";
    public static final String EVENT_PASSWORD_CHANGE_SUCCESSFUL = "password_change_successful";
    public static final String EVENT_PASSWORD_CHANGE_FAILED = "password_change_failed";
    public static final String EVENT_LOGOUT_CLICKED = "logout_clicked";
    public static final String EVENT_CHANGE_PASSWORD_CLICKED = "change_password_clicked";


    //Login events
    public static void logEventLoginClicked(Context context){
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_LOGIN_CLICKED,null);
    }

    public static void logEventLoginSuccessful(Context context){
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_LOGIN_SUCCESSFUL,null);
    }

    public static void logEventAuthenticationFailed(Context context){
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_AUTHENTICATION_FAILED,null);
    }


    //Logout events
    public static void logEventLogoutClicked(Context context){
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_LOGOUT_CLICKED,null);
    }

    public static void logEventLogoutSuccessful(Context context){
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_LOGOUT_SUCCESSFUL,null);
    }

    public static void logEventLogoutFailed(Context context){
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_LOGOUT_FAILED,null);
    }

    //Change Password Events
    public static void logEventChangePasswordClicked(Context context){
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_CHANGE_PASSWORD_CLICKED,null);
    }

    public static void logEventChangePasswordSuccessful(Context context){
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_PASSWORD_CHANGE_SUCCESSFUL,null);
    }

    public static void logEventChangePasswordFailed(Context context){
        FirebaseAnalytics.getInstance(context).logEvent(EVENT_PASSWORD_CHANGE_FAILED,null);
    }
}
