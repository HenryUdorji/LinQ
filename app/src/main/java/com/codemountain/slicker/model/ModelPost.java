package com.codemountain.slicker.model;

public class ModelPost {
    //Use same name that was used to upload to firebase
    String uid, uName, uEmail, uDp, pId, pDescription, pImage, pTime, pLikes, pComments;

    public ModelPost() {
    }

    public ModelPost(String uid, String uName, String uEmail, String uDp, String pId, String pDescription, String pImage, String pTime, String pLikes, String pComments) {
        this.uid = uid;
        this.uName = uName;
        this.uEmail = uEmail;
        this.uDp = uDp;
        this.pId = pId;
        this.pDescription = pDescription;
        this.pImage = pImage;
        this.pTime = pTime;
        this.pLikes = pLikes;
        this.pComments = pComments;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getuEmail() {
        return uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }

    public String getuDp() {
        return uDp;
    }

    public void setuDp(String uDp) {
        this.uDp = uDp;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getpDescription() {
        return pDescription;
    }

    public void setpDescription(String pDescription) {
        this.pDescription = pDescription;
    }

    public String getpImage() {
        return pImage;
    }

    public void setpImage(String pImage) {
        this.pImage = pImage;
    }

    public String getpTime() {
        return pTime;
    }

    public void setpTime(String pTime) {
        this.pTime = pTime;
    }

    public String getpLikes() {
        return pLikes;
    }

    public void setpLikes(String pLikes) {
        this.pLikes = pLikes;
    }

    public String getpComments() {
        return pComments;
    }

    public void setpComments(String pComments) {
        this.pComments = pComments;
    }
}
