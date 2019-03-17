package com.shivora.puwifimanager.networking;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;

import okhttp3.Interceptor;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {
    public static final String TAG = RetrofitClient.class.getSimpleName();
    private static Retrofit secureLoginInstance;
    private static Retrofit netportalInstance;

    private static final String BASE_URL_SECURE_LOGIN = "https://securelogin.pu.ac.in";
    private static final String BASE_URL_NETPORTAL = "http://netportal.pu.ac.in";

    public static Retrofit getSecureLoginInstance() {
        if (secureLoginInstance==null) {
            OkHttpClient unsafeHttpClient = UnsafeOkHttpClient.getUnsafeOkHttpClient();
            secureLoginInstance = new Retrofit.Builder()
                    .baseUrl(BASE_URL_SECURE_LOGIN)
                    .client(unsafeHttpClient)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return secureLoginInstance;
    }

    public static Retrofit getNetportalInstance(Context context) {
        if (netportalInstance == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            //httpClient.addNetworkInterceptor(new ChuckInterceptor(context));
            //To enable storing cookies to maintain and retain the session
            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
            httpClient.cookieJar(new JavaNetCookieJar(cookieManager));

            netportalInstance = new Retrofit.Builder()
                    .baseUrl(BASE_URL_NETPORTAL)
                    .client(httpClient.build())
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return netportalInstance;
    }
}
