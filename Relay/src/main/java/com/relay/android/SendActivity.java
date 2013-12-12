package com.relay.android;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jarvis on 12/5/13.
 */
public class SendActivity extends Activity {
    private String url;
    private RequestQueue mRequestQueue;
    private List<String> mSelected;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_send);
        mSelected = new ArrayList<String>();
        Intent intent = getIntent();
        mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        if (intent.hasExtra("url")) {
            url = intent.getStringExtra("url");
            Toast.makeText(getApplicationContext(), url, Toast.LENGTH_SHORT).show();
        }
        final ListView lv = (ListView) findViewById(R.id.friend_list);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView tv = (TextView) view.findViewById(android.R.id.text1);
                String user = tv.getText().toString();
                if (mSelected.contains(user)) {
                    view.setBackgroundColor(Color.BLACK);
                    mSelected.remove(user);
                } else {
                    view.setBackgroundColor(Color.DKGRAY);
                    mSelected.add(tv.getText().toString());
                }

            }
        });
        final View button = (View) findViewById(R.id.send_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setProgressBarIndeterminateVisibility(true);
                StringRequest sr = new StringRequest(Request.Method.POST,
                        "http://relay-links.appspot.com/relays", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.i("VolleyGirl", s);
                        setProgressBarIndeterminateVisibility(false);
                        setResult(5);
                        finish();
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
                        params.put("sender","atom");
                        params.put("url",url);
                        params.put("recipients", TextUtils.join(",", mSelected));
                        return params;

                    }
                };

                mRequestQueue.add(sr);
                Log.i("Jarvis", "Adding a relay for: "+ url);
                button.setClickable(false);
            }
        });
        lv.setBackgroundColor(Color.BLACK);
        RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        mRequestQueue.add(
                new GsonRequest<FriendList>("http://relay-links.appspot.com/users",
                        FriendList.class, null, new Response.Listener<FriendList>() {
                    @Override
                    public void onResponse(FriendList friendList) {
                        String[] values = friendList.getUsers().toArray(new String[0]);
                        lv.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, android.R.id.text1, values));
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
