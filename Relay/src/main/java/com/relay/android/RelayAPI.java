package com.relay.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.internal.co;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by jarvis on 2/15/14.
 */
public class RelayAPI {

    private static final String API_URL = "http://relay-links.appspot.com";
    private static final String RELAYS_ENDPOINT = API_URL + "/relays";
    private static final String DELETE_RELAY_ENDPOINT = RELAYS_ENDPOINT + "/delete";
    private static final String RELAYS_TO_ENDPOINT = API_URL + "/relays/to/";
    private static final String RELAYS_FROM_ENDPOINT = API_URL + "/relays/from/";
    private static final String FRIENDS_ENDPOINT = API_URL + "/users";
    private static final String LOGIN_ENDPOINT = API_URL + "/login";
    private static final String UNREGISTER_ENDPOINT = API_URL + "/unregister";

    private RequestQueue mRequestQueue;
    private Set<String> mInFlightRequests;
    private Context mContext;

    public RelayAPI(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
        mContext = context;
        mInFlightRequests = new HashSet<String>();
    }

    public void deleteRelay(final String username, final long relay_id, final Callback<String> callback) {
        if (mInFlightRequests.contains(DELETE_RELAY_ENDPOINT)) {
            return;
        }

        StringRequest sr = new StringRequest(Request.Method.POST,
                DELETE_RELAY_ENDPOINT, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                mInFlightRequests.remove(DELETE_RELAY_ENDPOINT);
                callback.run(s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mInFlightRequests.remove(DELETE_RELAY_ENDPOINT);
                // errors? what are those.
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("relay_id", "" + relay_id);
                params.put("user_id", username);
                return params;

            }
        };

        mRequestQueue.add(sr);
        mInFlightRequests.add(DELETE_RELAY_ENDPOINT);
    }


    public void sendRelay(final String url, final List<String> recipients, final Callback<String> callback) {
        if (mInFlightRequests.contains(RELAYS_ENDPOINT)) {
            return;
        }
        StringRequest sr = new StringRequest(Request.Method.POST, RELAYS_ENDPOINT, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                mInFlightRequests.remove(RELAYS_ENDPOINT);
                callback.run(s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                // errors? what are those.
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                String username = prefs.getString("username", "Error");
                Map<String,String> params = new HashMap<String, String>();
                params.put("sender", username);
                params.put("url",url);
                params.put("recipients", TextUtils.join(",", recipients));
                return params;

            }
        };

        mRequestQueue.add(sr);
        mInFlightRequests.add(RELAYS_ENDPOINT);


    }

    public void fetchFriends(final Callback<FriendList> callback) {
        if (mInFlightRequests.contains(FRIENDS_ENDPOINT)) {
            return;
        }
        Request request = new GsonRequest<FriendList>(FRIENDS_ENDPOINT,
            FriendList.class, null, new Response.Listener<FriendList>() {
            @Override
            public void onResponse(FriendList friendList) {
                mInFlightRequests.remove(FRIENDS_ENDPOINT);
                callback.run(friendList);

            }
        }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    //Toast.makeText(getActivity(), "shit's fucked", Toast.LENGTH_SHORT).show();
                    //Log.e("VolleyError", volleyError.toString());
                }
        });
        mRequestQueue.add(request);
        mInFlightRequests.add(FRIENDS_ENDPOINT);

    }

    public void unRegisterGCM(final String username, final Callback<String> callback) {
        if (mInFlightRequests.contains(UNREGISTER_ENDPOINT)) {
            return;
        }
        StringRequest sr = new StringRequest(Request.Method.POST,
                UNREGISTER_ENDPOINT, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                mInFlightRequests.remove(UNREGISTER_ENDPOINT);
                callback.run(s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mInFlightRequests.remove(UNREGISTER_ENDPOINT);
                // errors? what are those.
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                SharedPreferences prefs = mContext.getSharedPreferences(RootActivity.class.getSimpleName(), Context.MODE_PRIVATE);
                String regid = prefs.getString(RootActivity.PROPERTY_REG_ID, null);
                if (regid != null) {
                    params.put("gcm_id", regid);
                }
                params.put("username", username);
                return params;

            }
        };

        mRequestQueue.add(sr);
        mInFlightRequests.add(UNREGISTER_ENDPOINT);

    }
    public void attemptLogin(final String username, final String password, final Callback<String> callback) {
        if (mInFlightRequests.contains(LOGIN_ENDPOINT)) {
            return;
        }
        StringRequest sr = new StringRequest(Request.Method.POST,
                LOGIN_ENDPOINT, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                mInFlightRequests.remove(LOGIN_ENDPOINT);
                callback.run(s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mInFlightRequests.remove(LOGIN_ENDPOINT);
                // errors? what are those.
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                SharedPreferences prefs = mContext.getSharedPreferences(RootActivity.class.getSimpleName(), Context.MODE_PRIVATE);
                String regid = prefs.getString(RootActivity.PROPERTY_REG_ID, null);
                if (regid != null) {
                    params.put("gcm_id", regid);
                }
                params.put("username", username);
                params.put("password", password);
                return params;

            }
        };

        mRequestQueue.add(sr);
        mInFlightRequests.add(LOGIN_ENDPOINT);

    }

    public void fetchRelays(String username, boolean from, int offset, final Callback<RelayList> callback) {
        String thing = RELAYS_TO_ENDPOINT + username;
        if (from) {
            thing = RELAYS_FROM_ENDPOINT + username;
        }

        final String final_thing = thing;
        if (mInFlightRequests.contains(final_thing)) {
            Log.i("jarvis", "in flight: " + final_thing);
            return;
        }
        final String url = thing + (from ? "" : "?offset=" + offset);

        Log.i("jarvis", "sending off the request");

        final Request request = new GsonRequest<RelayList>(url, RelayList.class, null, new Response.Listener<RelayList>() {
            @Override
            public void onResponse(RelayList relayList) {
                mInFlightRequests.remove(final_thing);
                Log.i("jarvis", "removed "+final_thing);
                //Log.i("jarvis", mInFlightRequests.toString());
                //Log.i("jarvis", "calling callback");
                callback.run(relayList);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mInFlightRequests.remove(final_thing);
                Log.i("JARVIS", "There was an error");
            }
        });
        mRequestQueue.add(request);
        mInFlightRequests.add(final_thing);
    }

    public static class Callback<T> {
        public void run(T response) {

        }
    }


}