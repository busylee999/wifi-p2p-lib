package com.busylee.network.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by busylee on 01.09.16.
 */
@Module
public class GsonModule {

    @Provides @Singleton
    public Gson provideGson() {
        return new GsonBuilder().create();
    }
}
