package com.codemountain.slicker.notification;

public class Token {

    /**
     * An FCM Token, or more commonly known as registration Token
     *
     * An ID issued by GCM connection servers to the client app that allows it receive messages
     */

    private String token;

    public Token() {
    }

    public Token(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
