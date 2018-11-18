package com.extricks.daiquiris;

import com.google.firebase.firestore.GeoPoint;

public class item {
    String name;
    String user;
    GeoPoint latlang;

    public  item(String name, String user, GeoPoint LatLang){
        this.name=name;
        this.user=user;
        this.latlang=LatLang;
    }

    public item() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public GeoPoint getLatlang() {
        return latlang;
    }

    public void setLatlang(GeoPoint latlang) {
        this.latlang = latlang;
    }
}
