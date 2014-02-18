package com.relay.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RelayFeedFragment extends RelayListFragment {
    private static final String ARG_SECTION_NUMBER = "section_number";

    private static Map<Direction, RelayAdapter> relayAdapterCache = new HashMap<Direction, RelayAdapter>();
    private static Map<Direction, RelayList> relayListCache = new HashMap<Direction, RelayList>();
    private static Set<Direction> staleDirections = new HashSet<Direction>();

    private static final String TAG = "JARVIS";
    public enum Direction {
        FROM,
        TO
    }
    private String username;
    private Direction direction;
    private int mOffset;
    private boolean atEndOfList;
    private View mFooterView;


    public RelayFeedFragment(){
       // this is required
    }
    public static RelayFeedFragment newInstance(String username, Direction d) {

        RelayFeedFragment f = new RelayFeedFragment();
        // TODO remove the direction enum
        // use bundle args TODO
        f.setDirection(d);
        f.setUsername(username);
        f.setOffset(0);
        f.setAtEndOfList(false);
        return f;
    }

    public static RelayFeedFragment getInstance(int sectionNumber, String username) {
        RelayFeedFragment.Direction direction = RelayFeedFragment.Direction.TO;
        if (sectionNumber == 2) {
            direction = RelayFeedFragment.Direction.FROM;
        }
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        RelayFeedFragment f = RelayFeedFragment.newInstance(username, direction);
        f.setArguments(args);
        return f;
    }

    public void setAtEndOfList(boolean atEndOfList) {
        this.atEndOfList = atEndOfList;
    }
    public void setOffset(int mOffset) {
        this.mOffset = mOffset;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
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
        ListView listView = (ListView) root.findViewById(android.R.id.list);
        listView.addFooterView(mFooterView);

        // TODO(coolbrow): uncomment
        /*SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(listView, new SwipeDismissListViewTouchListener.DismissCallbacks() {
            @Override
            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                final RelayAdapter adapter = (RelayAdapter) ((HeaderViewListAdapter)listView.getAdapter()).getWrappedAdapter();
                for (int position : reverseSortedPositions) {
                    adapter.removeItem(position);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public boolean canDismiss(int position) {
                return true;
            }
        });*/
        // TODO(coolbrow): uncomment
        //listView.setOnTouchListener(touchListener);
        // TODO(coolbrow): uncomment
        //final AbsListView.OnScrollListener touchScrollListener = touchListener.makeScrollListener();
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // TODO(coolbrow): uncomment
                //touchScrollListener.onScrollStateChanged(absListView, scrollState);
                if (atEndOfList) return;
                if (mFooterView != null && mFooterView.isShown()) {
                    loadRelays(mOffset);
                }

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // TODO(coolbrow): uncomment
                //touchScrollListener.onScroll(absListView, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Relay r = (Relay) adapterView.getAdapter().getItem(i);
                RelayAdapter adapter = (RelayAdapter) ((HeaderViewListAdapter)adapterView.getAdapter()).getWrappedAdapter();
                adapter.removeItem(i);
                getApi().deleteRelay(username, r.getId(), new RelayAPI.Callback<String>() {
                    @Override
                    public void run(String s) {
                        Toast.makeText(getActivity().getApplicationContext(), "Relay Deleted", Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            }
        });
        if (relayAdapterCache.containsKey(direction)) {
            Log.i(TAG, "Using relayAdapterCache instead of making request");
            listView.setAdapter(relayAdapterCache.get(direction));
            if (staleDirections.contains(direction)) {
                loadRelays(0);
                staleDirections.remove(direction);
            }
        } else {
            Log.i(TAG, "loading relays fresh");
            loadRelays(0);
        }

        return root;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (relayListCache.containsKey(direction) && getListView() != null) {
            RelayAdapter adapter = new RelayAdapter(activity.getApplicationContext(), relayListCache.get(direction).getRelays());
            relayAdapterCache.put(direction, adapter);
            setListAdapter(adapter);
            relayListCache.remove(direction);
        }
        ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((RelayApplication) getActivity().getApplication()).setFeed(this);
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
        if (staleDirections.contains(direction)) {
            loadRelays(0);
            staleDirections.remove(direction);
        }
    }

    public static void evictCache() {
        staleDirections.add(Direction.FROM);
        staleDirections.add(Direction.TO);
    }

    public void loadRelays(final int offset) {
        getApi().fetchRelays(username, direction == Direction.FROM, offset, new RelayAPI.Callback<RelayList>() {
            @Override
            public void run(RelayList relayList) {
                if (getActivity() != null) {
                }
                if (relayList == null) {
                    return;
                }
                Log.i("JARVIS", relayList.toString());
                if (relayList.getRelays().size() == 0) {
                    mFooterView.setVisibility(View.GONE);
                    atEndOfList = true;
                    return;
                }
                if (getListAdapter() != null) {
                    Log.i("jarvis", "we have adapter");
                    RelayAdapter adapter = (RelayAdapter) getListAdapter();
                    if (offset == 0) {
                        adapter.handleRefresh(relayList);
                    } else {
                        adapter.appendRelays(relayList);
                    }
                    mOffset = adapter.getCount();
                } else {
                    Log.i("jarvis", "we dont have adapter");
                    if (getActivity() != null) {
                        Log.i("jarvis", "we have activity");
                        RelayAdapter adapter = new RelayAdapter(getActivity().getApplicationContext(), relayList.getRelays());
                        relayAdapterCache.put(direction, adapter);
                        setListAdapter(adapter);
                        mOffset = adapter.getCount();
                    } else {
                        Log.i(TAG, "using the list cache");
                        relayListCache.put(direction, relayList);
                    }
                }
            }
        });
    }
}
