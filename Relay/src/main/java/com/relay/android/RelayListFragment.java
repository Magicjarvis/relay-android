package com.relay.android;

import android.support.v4.app.ListFragment;

/**
 * Created by jarvis on 2/17/14.
 */
public class RelayListFragment extends ListFragment {
    public RelayAPI getApi() {
        return ((RelayApplication) getActivity().getApplication()).getApi();
    }
}
