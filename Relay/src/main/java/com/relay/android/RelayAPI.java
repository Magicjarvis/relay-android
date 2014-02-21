package com.relay.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


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
    private static final String RELAYS_SAVED_ENDPOINT = API_URL + "/relays/saved/";
    private static final String FRIENDS_ENDPOINT = API_URL + "/users";
    private static final String LOGIN_ENDPOINT = API_URL + "/login";
    private static final String UNREGISTER_ENDPOINT = API_URL + "/unregister";

    private static final int SERVER_TIMEOUT = 1000;
    private static final int MAX_RETRIES = 3;

    private static final DefaultRetryPolicy RETRY_POLICY = new DefaultRetryPolicy(SERVER_TIMEOUT, MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

    private static final Map<FeedType, String> FEED_TYPE_URL_MAP = new HashMap<FeedType, String>() {{
        put(FeedType.FROM, RELAYS_FROM_ENDPOINT);
        put(FeedType.TO, RELAYS_TO_ENDPOINT);
        put(FeedType.SAVED, RELAYS_SAVED_ENDPOINT);

    }};

    private RequestQueue mRequestQueue;
    private static final Set<String> IN_FLIGHT_REQUEST_SET = new HashSet<String>();
    private RelayApplication mContext;
    private FriendList mFriendList;

    public RelayAPI(RelayApplication context) {
        mRequestQueue = Volley.newRequestQueue(context);
        mContext = context;
    }

    public void deleteRelay(final String username, final long relay_id, final Callback<String> callback) {
        if (IN_FLIGHT_REQUEST_SET.contains(DELETE_RELAY_ENDPOINT)) {
            return;
        }

        StringRequest sr = new StringRequest(Request.Method.POST,
                DELETE_RELAY_ENDPOINT, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                IN_FLIGHT_REQUEST_SET.remove(DELETE_RELAY_ENDPOINT);
                callback.run(s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                IN_FLIGHT_REQUEST_SET.remove(DELETE_RELAY_ENDPOINT);
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

        sr.setRetryPolicy(RETRY_POLICY);
        mRequestQueue.add(sr);
        IN_FLIGHT_REQUEST_SET.add(DELETE_RELAY_ENDPOINT);
    }


    public void sendRelay(final String url, final List<String> recipients, final Callback<String> callback) {
        if (IN_FLIGHT_REQUEST_SET.contains(RELAYS_ENDPOINT)) {
            return;
        }
        Log.i("JARVIS", "sending: " + url + " to: " + recipients);
        StringRequest sr = new StringRequest(Request.Method.POST, RELAYS_ENDPOINT, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                IN_FLIGHT_REQUEST_SET.remove(RELAYS_ENDPOINT);
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

        sr.setRetryPolicy(RETRY_POLICY);
        mRequestQueue.add(sr);
        IN_FLIGHT_REQUEST_SET.add(RELAYS_ENDPOINT);


    }

    public void fetchFriends(final Callback<FriendList> callback) {
        if (mFriendList != null || IN_FLIGHT_REQUEST_SET.contains(FRIENDS_ENDPOINT)) {
            callback.run(mFriendList);
            return;
        }
        Request request = new GsonRequest<FriendList>(FRIENDS_ENDPOINT,
            FriendList.class, null, new Response.Listener<FriendList>() {
            @Override
            public void onResponse(FriendList friendList) {
                IN_FLIGHT_REQUEST_SET.remove(FRIENDS_ENDPOINT);
                if (friendList.getUsers().contains(mContext.getUsername())) {
                    friendList.getUsers().remove(mContext.getUsername());
                }
                mFriendList = friendList;
                callback.run(friendList);

            }
        }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    //Toast.makeText(getActivity(), "shit's fucked", Toast.LENGTH_SHORT).show();
                    //Log.e("VolleyError", volleyError.toString());
                }
        });
        request.setRetryPolicy(RETRY_POLICY);
        mRequestQueue.add(request);
        IN_FLIGHT_REQUEST_SET.add(FRIENDS_ENDPOINT);

    }

    public void unRegisterGCM(final String username, final Callback<String> callback) {
        if (IN_FLIGHT_REQUEST_SET.contains(UNREGISTER_ENDPOINT)) {
            return;
        }
        Log.i("JARVIS", "trying to unregister gcm");
        StringRequest sr = new StringRequest(Request.Method.POST,
                UNREGISTER_ENDPOINT, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                IN_FLIGHT_REQUEST_SET.remove(UNREGISTER_ENDPOINT);
                callback.run(s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                IN_FLIGHT_REQUEST_SET.remove(UNREGISTER_ENDPOINT);
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

        sr.setRetryPolicy(RETRY_POLICY);
        mRequestQueue.add(sr);
        IN_FLIGHT_REQUEST_SET.add(UNREGISTER_ENDPOINT);

    }
    public void attemptLogin(final String username, final String password, final Callback<String> callback) {
        if (IN_FLIGHT_REQUEST_SET.contains(LOGIN_ENDPOINT)) {
            return;
        }
        StringRequest sr = new StringRequest(Request.Method.POST,
                LOGIN_ENDPOINT, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                IN_FLIGHT_REQUEST_SET.remove(LOGIN_ENDPOINT);
                callback.run(s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                IN_FLIGHT_REQUEST_SET.remove(LOGIN_ENDPOINT);
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

        sr.setRetryPolicy(RETRY_POLICY);
        mRequestQueue.add(sr);
        IN_FLIGHT_REQUEST_SET.add(LOGIN_ENDPOINT);

    }

    public void fetchRelays(String username, FeedType feedtype, int offset, final Callback<RelayList> callback) {
        final String baseUrl = FEED_TYPE_URL_MAP.get(feedtype) + username;
        if (IN_FLIGHT_REQUEST_SET.contains(baseUrl)) {
            Log.i("jarvis", "in flight: " + baseUrl);
            return;
        }
        final String url = baseUrl + "?offset=" + offset;
        Log.i("jarvis", "sending off the request: " + url);
        final Request request = new GsonRequest<RelayList>(url, RelayList.class, null, new Response.Listener<RelayList>() {
            @Override
            public void onResponse(RelayList relayList) {
                IN_FLIGHT_REQUEST_SET.remove(baseUrl);
                Log.i("jarvis", "removed "+baseUrl);
                callback.run(relayList);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                IN_FLIGHT_REQUEST_SET.remove(baseUrl);
                Log.i("JARVIS", "There was an error");
            }
        });
        request.setRetryPolicy(RETRY_POLICY);
        mRequestQueue.add(request);
        IN_FLIGHT_REQUEST_SET.add(baseUrl);
    }

    public static interface Callback<T> {
        public void run(T response);
    }


}
