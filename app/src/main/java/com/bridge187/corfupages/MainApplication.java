package com.bridge187.corfupages;

import android.app.Application;

import com.bridge187.corfupages.utilities.AppUtilities;
import com.bridge187.corfupages.utilities.ImageCache;

public class MainApplication extends Application
{
    private final AppUtilities appUtilities;
    private final ImageCache imageCache;

    public MainApplication()
    {
        appUtilities = new AppUtilities();
        imageCache = new ImageCache();
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        appUtilities.setLanguage(getResources().getConfiguration().locale);
    }

    public AppUtilities getAppUtilities()
    {
        return appUtilities;
    }

    public ImageCache getImageCache()
    {
        return imageCache;
    }


}
