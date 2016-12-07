package com.don.myplace.model;

import java.io.Serializable;

/**
 * Created by dli on 12/5/2016.
 */

public class Place implements Serializable{
    private String title;
    private String type;
    private String address;
    private String telephone;

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

    public Place(){}

    public Place(String title, String type, String address, String telephone) {
        this.title = title;
        this.type = type;
        this.address = address;
        this.telephone = telephone;
    }

    @Override
    public String toString() {
        return "Place{" +
                "title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", address='" + address + '\'' +
                ", telephone='" + telephone + '\'' +
                '}';
    }
}
