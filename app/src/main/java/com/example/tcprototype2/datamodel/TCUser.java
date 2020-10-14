package com.example.tcprototype2.datamodel;

import android.net.Uri;

public class TCUser{
    private String uid;
    private String name;
    private String email;
    private String status;
    private Uri displayPic;

    public TCUser(){}
    public TCUser(String name, String email, Uri pic){
        this.name = name;
        this.email = email;
        this.displayPic = pic;
    }

//    public TCUser(String name, String email){
//        this.name = name;
//        this.email = email;
//    }


    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    public Uri getDisplayPic() {
        return displayPic;
    }
}
