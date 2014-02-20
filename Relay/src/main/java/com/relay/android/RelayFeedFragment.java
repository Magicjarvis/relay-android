package com.relay.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class RelayFeedFragment extends RelayListFragment {
    private static final String ARG_SECTION_NUMBER = "section_number";

    private static Map<FeedType, RelayAdapter> relayAdapterCache = new HashMap<FeedType, RelayAdapter>();
    private static Map<FeedType, RelayList> relayListCache = new HashMap<FeedType, RelayList>();
    private static Set<FeedType> staleFeeds = new HashSet<FeedType>();

    private PullToRefreshLayout mPullToRefreshLayout;


    private static final String TAG = "JARVIS";
    public enum FeedType {
        TO,
        FROM,
        SAVED
    }
    private String username;
    private FeedType feedType;
    private boolean atEndOfList;
    private View mFooterView;
    private View mEmptyView;
    private View mLoadingView;


    public RelayFeedFragment(){
       // this is required
    }
    public static RelayFeedFragment newInstance(String username, FeedType d) {

        RelayFeedFragment f = new RelayFeedFragment();
        f.setFeedType(d);
        f.setUsername(username);
        f.setAtEndOfList(false);
        return f;
    }

    public static RelayFeedFragment getInstance(int sectionNumber, String username) {
        FeedType feedType = FeedType.values()[sectionNumber - 1];
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        RelayFeedFragment f = RelayFeedFragment.newInstance(username, feedType);
        f.setArguments(args);
        return f;
    }

    public void setAtEndOfList(boolean atEndOfList) {
        this.atEndOfList = atEndOfList;
    }

    public void setFeedType(FeedType feedType) {
        this.feedType = feedType;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Relay r = (Relay) l.getAdapter().getItem(position);
        Intent linkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(r.getUrl()));
        startActivity(linkIntent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_relay_list, container, false);
        mFooterView = inflater.inflate(R.layout.progress_bar, null, false);
        mEmptyView = root.findViewById(R.id.empty_view);
        mLoadingView = root.findViewById(R.id.loading_view);
        ListView listView = (ListView) root.findViewById(android.R.id.list);
        listView.addFooterView(mFooterView, null, false);

        SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(listView, new SwipeDismissListViewTouchListener.DismissCallbacks() {
            @Override
            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                final RelayAdapter adapter = (RelayAdapter) getListAdapter();
                Log.i(TAG, "WHAT THE FUCK IS THIS: " + Arrays.toString(reverseSortedPositions));
                for (int position : reverseSortedPositions) {
                    deleteRelay(listView.getAdapter(), position);
                }
                adapter.notifyDataSetChanged();
                if (atEndOfList) return;
                if (mFooterView != null && mFooterView.isShown()) {
                    loadRelays(adapter.getCount());
                }

            }

            @Override
            public boolean canDismiss(int position) {
                return true;
            }
        });
        listView.setOnTouchListener(touchListener);
        final AbsListView.OnScrollListener touchScrollListener = touchListener.makeScrollListener();
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                touchScrollListener.onScrollStateChanged(absListView, scrollState);
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                touchScrollListener.onScroll(absListView, firstVisibleItem, visibleItemCount, totalItemCount);
                if (atEndOfList) return;
                if (mFooterView != null && mFooterView.isShown()) {
                    loadRelays(getListAdapter().getCount());
                }
            }
        });

        if (relayAdapterCache.containsKey(feedType)) {
            Log.i(TAG, "Using relayAdapterCache instead of making request");
            setListAdapter(relayAdapterCache.get(feedType));
            if (staleFeeds.contains(feedType)) {
                loadRelays(0);
                staleFeeds.remove(feedType);
            }
        } else {
            Log.i(TAG, "loading relays fresh");
            loadRelays(0);
        }

        return root;
    }


    private void deleteRelay(Adapter listAdapter, int i) {
        final Relay r = (Relay) listAdapter.getItem(i);
        RelayAdapter adapter = (RelayAdapter) getListAdapter();
        adapter.removeItem(i);
        // Create toast here, because we have a context. May not have context when callback runs.
        final Toast t = Toast.makeText(getActivity().getApplicationContext(), "Relay Deleted", Toast.LENGTH_SHORT);
        getApi().deleteRelay(username, r.getId(), new RelayAPI.Callback<String>() {
            @Override
            public void run(String s) {
                t.show();
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (relayListCache.containsKey(feedType) && isVisible() && getListView() != null) {
            RelayAdapter adapter = new RelayAdapter((RelayApplication)activity.getApplication(), relayListCache.get(feedType).getRelays());
            relayAdapterCache.put(feedType, adapter);
            setListAdapter(adapter);
            relayListCache.remove(feedType);
        }
        ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((RelayApplication) getActivity().getApplication()).setFeed(this);
        ViewGroup viewGroup = (ViewGroup) view;

        // We need to create a PullToRefreshLayout manually
        mPullToRefreshLayout = new PullToRefreshLayout(viewGroup.getContext());
        ActionBarPullToRefresh.from(getActivity()).insertLayoutInto(viewGroup)
                .theseChildrenArePullable(getListView(), getListView().getEmptyView())
                .listener(new OnRefreshListener() {
                    @Override
                    public void onRefreshStarted(View view) {
                        loadRelays(0);
                    }
                }
                ).setup(mPullToRefreshLayout);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((RelayApplication) getActivity().getApplication()).setFeed(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        lazyRefresh();
        ((RelayApplication) getActivity().getApplication()).feedResumed();
    }

    @Override
    public void onPause() {
        super.onPause();
        ((RelayApplication) getActivity().getApplication()).feedPaused();
    }

    public void lazyRefresh() {
        if (staleFeeds.contains(feedType)) {
            loadRelays(0);
            staleFeeds.remove(feedType);
        }
    }

    public static void evictCache() {
        staleFeeds.add(FeedType.FROM);
        staleFeeds.add(FeedType.TO);
        staleFeeds.add(FeedType.SAVED);
    }

    public void loadRelays(final int offset) {
        getApi().fetchRelays(username, feedType, offset, new RelayAPI.Callback<RelayList>() {
            @Override
            public void run(RelayList relayList) {
                mPullToRefreshLayout.setRefreshComplete();
                if (getActivity() != null) {
                }
                if (relayList == null) {
                    return;
                }
                Log.i("JARVIS", relayList.toString());
                if (relayList.getRelays().size() == 0) {
                    getListView().removeFooterView(mFooterView);
                    mLoadingView.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                    atEndOfList = true;
                    return;
                } else {
                    if (isVisible() && getListView().getFooterViewsCount() == 0) {
                        getListView().addFooterView(mFooterView, null, false);
                    }
                    mLoadingView.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                    atEndOfList = false;
                }
                if (getListAdapter() != null) {
                    Log.i("jarvis", "we have adapter");
                    RelayAdapter adapter = (RelayAdapter) getListAdapter();
                    if (offset == 0) {
                        adapter.handleRefresh(relayList);
                    } else {
                        adapter.appendRelays(relayList);
                    }
                } else {
                    Log.i("jarvis", "we dont have adapter");
                    if (getActivity() != null) {
                        Log.i("jarvis", "we have activity");
                        RelayAdapter adapter = new RelayAdapter((RelayApplication)getActivity().getApplication(), relayList.getRelays());
                        relayAdapterCache.put(feedType, adapter);
                        setListAdapter(adapter);
                    } else {
                        Log.i(TAG, "using the list cache");
                        relayListCache.put(feedType, relayList);
                    }
                }
            }
        });
    }
}
