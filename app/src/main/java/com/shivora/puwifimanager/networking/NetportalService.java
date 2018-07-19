package com.shivora.puwifimanager.networking;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface NetportalService {
    //Parameters
    String PARAM_LOGIN_ID = "loginid";
    String PARAM_PASSWORD = "passwd";
    String PARAM_OLD_PASS = "oldpass";
    String PARAM_NEW_PASSWORD = PARAM_PASSWORD;

    //Messages
    String PASSWORD_CHANGE_SUCCESS = "Password Updated";
    String NETPORTAL_LOGIN_SUCCESS = "Candidate Administration";

    @FormUrlEncoded
    @POST("/chklogin.php")
    Call<String> loginNetPortal(@FieldMap Map<String,String> userInfo);

    @FormUrlEncoded
    @POST("/change-password_proc.php")
    Call<String> changePassword(@FieldMap Map<String,String> changePasswordParams);
}
