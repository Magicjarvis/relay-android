package com.relay.android;

import java.util.List;

/**
 * Created by jarvis on 12/5/13.
 */
public class FriendList {
    private List<String> users;
    public FriendList(List<String> users) {
        this.users = users;
    }

    public List<String> getUsers() {

        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

}
