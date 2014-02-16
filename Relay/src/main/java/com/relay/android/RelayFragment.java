package com.relay.android;

import android.support.v4.app.Fragment;

/**
 * Created by jarvis on 2/15/14.
 */
public class RelayFragment extends Fragment {

    public RelayAPI getApi() {
        return ((RelayApplication) getActivity().getApplication()).getApi();
    }
}
