package com.relay.android;

import java.util.List;

/**
 * Created by jarvis on 12/5/13.
 */
public class Relay {
    private long id;
    private String url;
    private String sender;
    private List<String> recipients;
    private String description;
    private String favicon;
    private String image;
    private String site;
    private String title;
    private String kind;

    public Relay(long id, String url, String sender, List<String> recipients, String description, String favicon, String image, String site, String title, String kind) {
        this.id = id;
        this.url = url;
        this.sender = sender;
        this.recipients = recipients;
        this.description = description;
        this.favicon = favicon;
        this.image = image;
        this.site = site;
        this.title = title;
        this.kind = kind;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFavicon() {
        return favicon;
    }

    public void setFavicon(String favicon) {
        this.favicon = favicon;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public boolean isRelayToUser() {
        return !isRelayFromUser();
    }

    public boolean isRelayFromUser() {
        return sender == null && recipients != null;
    }

    public String toString() {
        return url;
    }
    public boolean equals(Object o) {
        Relay other = (Relay) o;
        return url.equalsIgnoreCase(other.getUrl());
    }
}
