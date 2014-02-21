package com.relay.android;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.HeaderViewListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jarvis on 12/4/13.
 */
public class RelayAdapter extends BaseAdapter {

    private RelayApplication mApplication;
    private List<Relay> mRelays;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private FeedType mFeedType;

    public RelayAdapter(RelayApplication application, List<Relay> relays, FeedType feedType) {
        mApplication = application;
        mRelays = relays == null ? new LinkedList<Relay>() : relays;
        mRequestQueue = Volley.newRequestQueue(mApplication);
        int memClass = ((ActivityManager) mApplication.getSystemService(Context.ACTIVITY_SERVICE))
                .getMemoryClass();
        // Use 1/8th of the available memory for this memory cache.
        int cacheSize = 1024 * 1024 * memClass / 8;
        mImageLoader = new ImageLoader(mRequestQueue, new BitmapLruCache(cacheSize));
        mFeedType = feedType;
    }

    @Override
    public Object getItem(int i) {
        return mRelays.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void removeItem(int i) {
        mRelays.remove(i);
        notifyDataSetChanged();
    }

    public void appendRelays(RelayList rList) {
        mRelays.addAll(rList.getRelays());
        notifyDataSetChanged();
    }

    public void handleRefresh(RelayList rList) {
        if (!mRelays.equals(rList.getRelays())) {
            Log.i("JARVIS", "THIS IS THE NEW LIST: "+rList.getRelays());
            mRelays = rList.getRelays();
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mRelays.size();
    }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = view != null ? view : LayoutInflater.from(mApplication)
                .inflate(R.layout.relay_list_item, null);
        final Relay relay = (Relay) getItem(i);

        TextView description = (TextView) v.findViewById(R.id.relay_description);
        TextView title = (TextView) v.findViewById(R.id.relay_title);
        TextView site = (TextView) v.findViewById(R.id.relay_site);
        TextView people = (TextView) v.findViewById(R.id.relay_people);
        NetworkImageView mImageView = (NetworkImageView) v.findViewById(R.id.relay_image);

        final Button saveButton = (Button) v.findViewById(R.id.save_button);
        final Button relayButton = (Button) v.findViewById(R.id.relay_button);

        saveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                saveButton.setText("Saved");
                saveButton.setTextColor(ColorStateList.valueOf(R.color.light_gray));
                saveButton.setClickable(false);
                mApplication.getApi().sendRelay(relay.getUrl(), new ArrayList<String>(), new RelayAPI.Callback<String>() {
                    @Override
                    public void run(String response) {
                        RelayFeedFragment.markSavedStale();
                        Toast.makeText(mApplication, "Relay Saved", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        relayButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), SendActivity.class);
                i.putExtra("url", relay.getUrl());
                // Must set this flag because context is not an Activity.
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                v.getContext().startActivity(i);
            }
        });


        // So effing dirty. What? I couldn't even think of a better way to do this.. other than
        // adding stupid functions, but the abstraction is not really that helpful.
        mImageView.setVisibility(relay.getImage() == null ? View.GONE : View.VISIBLE);
        mImageView.setImageUrl(relay.getImage(), mImageLoader);

        if (relay.getDescription() != null) {
            description.setVisibility(View.VISIBLE);
            description.setText(relay.getDescription());
        } else {
            description.setVisibility(View.GONE);
        }
        if (relay.getSite() != null) {
            site.setVisibility(View.VISIBLE);
            site.setText(relay.getSite());
        } else {
            site.setVisibility(View.GONE);
        }
        title.setText(relay.getTitle());

        if (mFeedType == FeedType.TO) {
            people.setText("—" + relay.getSender());
        } else if (mFeedType == FeedType.FROM) {
            people.setText("—" + TextUtils.join(", ", relay.getRecipients()));
        } else {
            saveButton.setVisibility(View.GONE);
            people.setText("");
        }

        return v;
    }

}
