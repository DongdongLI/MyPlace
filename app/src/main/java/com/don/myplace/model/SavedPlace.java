package com.don.myplace.model;

import java.io.Serializable;

/**
 * Created by dli on 12/5/2016.
 */

public class SavedPlace implements Serializable{
    private String placeId;

    private String title;
    private String type;
    private String address;
    private String telephone;

    private CustomType customType;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public CustomType getCustomType() {
        return customType;
    }

    public void setCustomType(CustomType customType) {
        this.customType = customType;
    }

    public SavedPlace(){}

    public SavedPlace(String title, String type, String address, String telephone) {
        this.title = title;
        this.type = type;
        this.address = address;
        this.telephone = telephone;
    }

    public SavedPlace(String str) {
        setPlaceId(str.substring(str.indexOf("placeId=")+8, str.indexOf("}")));
        str = str.substring(0, str.indexOf("placeId="));

        setTelephone(str.substring(str.indexOf("telephone=")+10, str.length()-2));
        str = str.substring(0, str.indexOf("telephone="));

        setType(str.substring(str.indexOf("type=")+5, str.length()-2));
        str = str.substring(0, str.indexOf("type="));

        setTitle(str.substring(str.indexOf("title=")+6, str.length()-2));
        str = str.substring(0, str.indexOf("title="));

        setAddress(str.substring(str.indexOf("address=")+8, str.length()-2));
    }

    @Override
    public String toString() {
        return "Place{" +
                "placeId='" + placeId+ '\'' +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", address='" + address + '\'' +
                ", telephone='" + telephone + '\'' +
                '}';
    }
}
