package com.shivora.puwifimanager.networking;

public class Listeners {
    public static interface OnLoginCompleteListener{
        void onLoginComplete(boolean isLoggedIn);
    }

    public static interface OnNetportalLoginCompleteListener{
        void onNetportalLoginComplete();
    }
}
