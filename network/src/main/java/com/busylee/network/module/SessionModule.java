package com.busylee.network.module;

import com.busylee.network.Logger;
import com.busylee.network.serialization.Base64Context;
import com.busylee.network.session.SessionFactory;

import dagger.Module;
import dagger.Provides;

/**
 * Created by busylee on 01.09.16.
 */
@Module(includes = GsonModule.class)
public class SessionModule {

    @Provides
    public SessionFactory provideSessionFactory(Base64Context base64Context, Logger logger) {
        return new SessionFactory(base64Context, logger);
    }

}
