package com.annabenson.viand.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GeminiClient {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/";
    private static GeminiClient instance;
    private final Retrofit retrofit;

    private GeminiClient() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized GeminiClient getInstance() {
        if (instance == null) {
            instance = new GeminiClient();
        }
        return instance;
    }

    public <T> T create(Class<T> service) {
        return retrofit.create(service);
    }
}
