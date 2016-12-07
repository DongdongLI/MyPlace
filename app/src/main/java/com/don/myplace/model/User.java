package com.don.myplace.model;

import java.util.List;

/**
 * Created by dli on 12/7/2016.
 */

public class User {
    private String displayName;
    private String emailAddress;
    private List<Place> savedPlaces;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public List<Place> getSavedPlaces() {
        return savedPlaces;
    }

    public void setSavedPlaces(List<Place> savedPlaces) {
        this.savedPlaces = savedPlaces;
    }

    public User(){}

    public User(String displayName, String emailAddress, List<Place> savedPlaces) {
        this.displayName = displayName;
        this.emailAddress = emailAddress;
        this.savedPlaces = savedPlaces;
    }

}
