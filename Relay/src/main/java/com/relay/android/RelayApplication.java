package com.relay.android;

import android.app.Application;

public class RelayApplication extends Application {
    private RelayAPI api;

    @Override
    public void onCreate() {
        super.onCreate();
        this.api = new RelayAPI(getApplicationContext());
    }

    public RelayAPI getApi() {
        return api;
    }
    private static boolean activityVisible = true;
    private static boolean feedVisible = false;
    private static RelayListFragment feed = null;

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    public static void refreshFeedIfVisible() {
        if (feedVisible && feed != null) {
            feed.lazyRefresh();
        }
    }
    public static void feedResumed() {
        feedVisible = true;
    }

    public static void feedPaused() {
        feedVisible = true;
    }

    public static void setFeed(RelayListFragment feedHandle) {
        feed = feedHandle;
    }

}