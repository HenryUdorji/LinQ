package com.codemountain.slicker.notification;

public class Data {

    private String user, body, sent, title;
    private int icon;

    public Data() {
    }

    public Data(String user, String body, String sent, String title, int icon) {
        this.user = user;
        this.body = body;
        this.sent = sent;
        this.title = title;
        this.icon = icon;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
