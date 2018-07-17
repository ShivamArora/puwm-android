package com.shivora.puwifimanager.networking;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;

    private static final String BASE_URL_SECURE_LOGIN = "https://securelogin.pu.ac.in";
    private static final String BASE_URL_NETPORTAL = "https://netportal.pu.ac.in";

    public static Retrofit getSecureLoginInstance(){
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL_SECURE_LOGIN)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static Retrofit getNetportalInstance(){
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL_NETPORTAL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
