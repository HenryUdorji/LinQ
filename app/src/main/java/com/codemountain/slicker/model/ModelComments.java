package com.codemountain.slicker.model;

public class ModelComments {

    private String cID, comments, timeOfComment, uid, uEmail, uName, uDp;

    public ModelComments() {
    }

    public ModelComments(String cID, String comments, String timeOfComment, String uid, String uEmail, String uName, String uDp) {
        this.cID = cID;
        this.comments = comments;
        this.timeOfComment = timeOfComment;
        this.uid = uid;
        this.uEmail = uEmail;
        this.uName = uName;
        this.uDp = uDp;
    }

    public String getcID() {
        return cID;
    }

    public void setcID(String cID) {
        this.cID = cID;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getTimeOfComment() {
        return timeOfComment;
    }

    public void setTimeOfComment(String timeOfComment) {
        this.timeOfComment = timeOfComment;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getuEmail() {
        return uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getuDp() {
        return uDp;
    }

    public void setuDp(String uDp) {
        this.uDp = uDp;
    }
}
