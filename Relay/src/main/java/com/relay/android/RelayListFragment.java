package com.relay.android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jarvis on 12/4/13.
 */
public class RelayListFragment extends Fragment {
    private ListView mListView;
    public enum Direction {
        FROM,
        TO
    }
    private RequestQueue mRequestQueue;
    private String url;
    private String username;

    public RelayListFragment(){
       // this is required
    }
    public static RelayListFragment newInstance(String username, Direction d) {

        RelayListFragment f = new RelayListFragment();
        // use bundle args TODO
        String url = String.format("http://relay-links.appspot.com/relays/%s/%s", d == Direction.FROM ? "from" : "to", username);
        f.setUrl(url);
        f.setUsername(username);
        return f;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mRequestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
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
                StringRequest sr = new StringRequest(Request.Method.POST,
                        "http://relay-links.appspot.com/relays/delete", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Toast.makeText(getActivity().getApplicationContext(), "Relay Deleted", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        // errors? what are those.
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String,String> params = new HashMap<String, String>();
                        params.put("relay_id", "" + r.getId());
                        params.put("user_id", username);
                        return params;

                    }
                };

                mRequestQueue.add(sr);
                return true;
            }
        });

        loadRelays();
        return root;
    }

    public void loadRelays() {
        Log.i("Jarvis", "calling loadRelays() at "+url);
        if (url == null) {
            return;
        }
        mRequestQueue.add(
                new GsonRequest<RelayList>(url,
                        RelayList.class, null, new Response.Listener<RelayList>() {
                    @Override
                    public void onResponse(RelayList relayList) {
                        Log.i("jarfish", relayList.toString());
                        if (mListView.getAdapter() != null) {
                            RelayAdapter adapter = (RelayAdapter) mListView.getAdapter();
                            adapter.handleRefresh(relayList);
                        } else {
                            mListView.setAdapter(new RelayAdapter(getActivity().getApplicationContext(), relayList.getRelays()));
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Toast.makeText(getActivity(), "shit's fucked", Toast.LENGTH_SHORT).show();
                        //Log.e("VolleyError", volleyError.toString());
                    }
                }
                )
        );
    }

}
