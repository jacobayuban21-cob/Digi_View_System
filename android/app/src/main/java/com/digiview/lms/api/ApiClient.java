package com.digiview.lms.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    // Default to port 8085 (or 8080 / Render URL)
    private static String baseUrl = "http://10.0.2.2:8085/";
    private static Retrofit retrofit = null;

    public static void setBaseUrl(String url) {
        if (!url.endsWith("/")) {
            url += "/";
        }
        baseUrl = url;
        retrofit = null; // force rebuild
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    public static DigiViewApi getApi() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(DigiViewApi.class);
    }
}
