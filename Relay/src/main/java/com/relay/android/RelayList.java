package com.relay.android;

import java.util.List;

/**
 * Created by jarvis on 12/5/13.
 */
public class RelayList {
    private List<Relay> relays;

    public RelayList(List<Relay> relays) {
        this.relays = relays;
    }

    public List<Relay> getRelays() {
        return relays;
    }

    public void setRelays(List<Relay> relays) {
        this.relays = relays;
    }

    public String toString() {
        return relays.toString();
    }

}
