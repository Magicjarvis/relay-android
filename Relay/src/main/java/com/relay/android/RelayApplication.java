package com.relay.android;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class RelayApplication extends Application {
    private RelayAPI api;

    @Override
    public void onCreate() {
        super.onCreate();
        this.api = new RelayAPI(this);
    }

    public String getUsername() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        return sp.getString("username", null);
    }

    public RelayAPI getApi() {
        return api;
    }
    private static boolean activityVisible = true;
    private static boolean feedVisible = false;
    private static RelayFeedFragment feed = null;

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    public static void hardRefreshFeed() {
        if (feedVisible && feed != null) {
            feed.loadRelays(0);
        }
    }
    public static void refreshFeedIfVisible() {
        Log.i("Jarvis", "Feed visible is: " + feedVisible);
        Log.i("Jarvis", "Feed is null status: " + (feed == null));
        if (feedVisible && feed != null) {
            Log.i("jarvis", "calling lazyRefresh()");
            feed.lazyRefresh();
        }
    }
    public static void feedResumed() {
        feedVisible = true;
    }

    public static void feedPaused() {
        feedVisible = true;
    }

    public static void setFeed(RelayFeedFragment feedHandle) {
        feed = feedHandle;
    }

}