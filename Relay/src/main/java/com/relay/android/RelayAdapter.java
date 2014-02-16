package com.relay.android;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jarvis on 12/4/13.
 */
public class RelayAdapter extends BaseAdapter {

    private Context mContext;
    private List<Relay> mRelays;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    public RelayAdapter(Context context, List<Relay> relays) {
        mContext = context;
        mRelays = relays == null ? new ArrayList<Relay>() : relays;
        mRequestQueue = Volley.newRequestQueue(context);
        int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                .getMemoryClass();
        // Use 1/8th of the available memory for this memory cache.
        int cacheSize = 1024 * 1024 * memClass / 8;
        mImageLoader = new ImageLoader(mRequestQueue, new BitmapLruCache(cacheSize));
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

    public void handleRefresh(RelayList rList) {
        if (!mRelays.equals(rList.getRelays())) {
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
        View v = view != null ? view : LayoutInflater.from(mContext)
                .inflate(R.layout.relay_list_item, null);
        final Relay relay = (Relay) getItem(i);

        TextView description = (TextView) v.findViewById(R.id.relay_description);
        TextView title = (TextView) v.findViewById(R.id.relay_title);
        TextView site = (TextView) v.findViewById(R.id.relay_site);
        TextView people = (TextView) v.findViewById(R.id.relay_people);
        NetworkImageView mImageView = (NetworkImageView) v.findViewById(R.id.relay_image);


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




        if (relay.isRelayFromUser()) {
            people.setText("—" + TextUtils.join(", ", relay.getRecipients()));
        } else {
            people.setText("—" + relay.getSender());
        }

        return v;
    }

}
