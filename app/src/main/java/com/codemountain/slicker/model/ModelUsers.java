package com.codemountain.slicker.model;

public class ModelUsers {

    String name, email, cover_image, image, phone, uid, search, bio, onlineStatus, typingTo;

    public ModelUsers() {
    }

    public ModelUsers(String name, String email, String cover_image, String image, String phone, String uid, String search, String bio, String onlineStatus, String typingTo) {
        this.name = name;
        this.email = email;
        this.cover_image = cover_image;
        this.image = image;
        this.phone = phone;
        this.uid = uid;
        this.search = search;
        this.bio = bio;
        this.onlineStatus = onlineStatus;
        this.typingTo = typingTo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCover_image() {
        return cover_image;
    }

    public void setCover_image(String cover_image) {
        this.cover_image = cover_image;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getTypingTo() {
        return typingTo;
    }

    public void setTypingTo(String typingTo) {
        this.typingTo = typingTo;
    }
}
