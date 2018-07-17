package com.shivora.puwifimanager.networking;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface SecureLoginService {
    String LOGIN_SUCCESS_STRING = "External Welcome Page";
    String LOGOUT_SUCCESS_STRING = "Logout Successful";
    String USER_NOT_LOGGED_IN = "User not logged in";
    String LOGIN_ALREADY_LOGGED_IN = "Too many follow-up requests";
    String LOGIN_FAILURE_STRING = "Authentication failed";

    @GET("/cgi-bin/login?cmd=login")
    Call<String> loginUser(@QueryMap Map<String,String> userInfo);

    @GET("/cgi-bin/login?cmd=logout")
    Call<String> logoutUser();
}
