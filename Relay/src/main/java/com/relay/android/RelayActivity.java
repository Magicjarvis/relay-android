package com.relay.android;

import android.support.v7.app.ActionBarActivity;

public class RelayActivity extends ActionBarActivity {
    public RelayAPI getApi() {
        return ((RelayApplication) getApplication()).getApi();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((RelayApplication) getApplication()).activityPaused();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((RelayApplication) getApplication()).activityResumed();
    }
}
