package com.relay.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RelayListFragment extends RelayFragment {

    private static Map<Direction, RelayAdapter> relayAdapterCache = new HashMap<Direction, RelayAdapter>();
    private static Map<Direction, RelayList> relayListCache = new HashMap<Direction, RelayList>();
    private static Set<Direction> staleDirections = new HashSet<Direction>();
    private static final String ARG_SECTION_NUMBER = "section_number";

    private static final String TAG = "JARVIS";
    private ListView mListView;
    public enum Direction {
        FROM,
        TO
    }
    private String username;
    private Direction direction;


    public RelayListFragment(){
       // this is required
    }
    public static RelayListFragment newInstance(String username, Direction d) {

        RelayListFragment f = new RelayListFragment();
        // TODO remove the direction enum
        // use bundle args TODO
        f.setDirection(d);
        f.setUsername(username);
        return f;
    }

    public static RelayListFragment getInstance(int sectionNumber, String username) {
        RelayListFragment.Direction direction = RelayListFragment.Direction.TO;
        if (sectionNumber == 2) {
            direction = RelayListFragment.Direction.FROM;
        }
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        RelayListFragment f = RelayListFragment.newInstance(username, direction);
        f.setArguments(args);
        return f;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_relay_list, container, false);
        mListView = (ListView) root.findViewById(R.id.relay_list);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> l, View v, int position, long id) {
                Relay r = (Relay) l.getAdapter().getItem(position);
                Intent linkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(r.getUrl()));
                startActivity(linkIntent);
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Relay r = (Relay) adapterView.getAdapter().getItem(i);
                RelayAdapter adapter = (RelayAdapter) adapterView.getAdapter();
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
            mListView.setAdapter(relayAdapterCache.get(direction));
            if (staleDirections.contains(direction)) {
                loadRelays();
                staleDirections.remove(direction);
            }
        } else {
            Log.i(TAG, "loading relays fresh");
            loadRelays();
        }

        return root;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (relayListCache.containsKey(direction) && mListView != null) {
            RelayAdapter adapter = new RelayAdapter(activity.getApplicationContext(), relayListCache.get(direction).getRelays());
            relayAdapterCache.put(direction, adapter);
            mListView.setAdapter(adapter);
            relayListCache.remove(direction);
        }
        ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((RelayApplication) getActivity().getApplication()).setFeed(this);
        Log.i(TAG, "I just set the feed");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((RelayApplication) getActivity().getApplication()).setFeed(null);
        Log.i(TAG, "view destroyed");
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
            loadRelays();
            staleDirections.remove(direction);
        }
    }

    public static void evictCache() {
        staleDirections.add(Direction.FROM);
        staleDirections.add(Direction.TO);
    }

    public void loadRelays() {
        getApi().fetchRelays(username, direction == Direction.FROM, new RelayAPI.Callback<RelayList>() {
            @Override
            public void run(RelayList relayList) {
                if (relayList == null) {
                    Log.i(TAG, "relayList is null and that's weird");
                    return;
                }
                if (mListView.getAdapter() != null) {
                    RelayAdapter adapter = (RelayAdapter) mListView.getAdapter();
                    adapter.handleRefresh(relayList);
                } else {
                    if (getActivity() != null) {
                        RelayAdapter adapter = new RelayAdapter(getActivity().getApplicationContext(), relayList.getRelays());
                        relayAdapterCache.put(direction, adapter);
                        mListView.setAdapter(adapter);
                    } else {
                        relayListCache.put(direction, relayList);
                    }
                }
            }
        });
    }
}
