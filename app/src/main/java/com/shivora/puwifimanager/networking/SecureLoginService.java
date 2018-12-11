package com.shivora.puwifimanager.networking;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface SecureLoginService {
    //Parameters
    String PARAM_USER_ID = "user";
    String PARAM_PASSWORD = "password";

    //Response messages
    String LOGIN_SUCCESS_STRING = "External Welcome Page";
    String LOGOUT_SUCCESS_STRING = "Logout Successful";
    String USER_NOT_LOGGED_IN = "User not logged in";
    String LOGIN_ALREADY_LOGGED_IN = "Too many follow-up requests";
    String LOGIN_FAILURE_STRING = "Authentication failed";
    String UNABLE_TO_RESOLVE_HOST = "Unable to resolve host \"securelogin.pu.ac.in\"";

    @GET("/cgi-bin/login?cmd=login")
    Call<String> loginUser(@QueryMap Map<String,String> userInfo);

    @GET("/cgi-bin/login?cmd=logout")
    Call<String> logoutUser();
}
