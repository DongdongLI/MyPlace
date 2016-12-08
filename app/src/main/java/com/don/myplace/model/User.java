package com.don.myplace.model;

import java.util.List;

/**
 * Created by dli on 12/7/2016.
 */

public class User {
    private String displayName;
    private String emailAddress;
    private List<SavedPlace> savedPlaces;

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

    public List<SavedPlace> getSavedPlaces() {
        return savedPlaces;
    }

    public void setSavedPlaces(List<SavedPlace> savedPlaces) {
        this.savedPlaces = savedPlaces;
    }

    public User(){}

    public User(String displayName, String emailAddress, List<SavedPlace> savedPlaces) {
        this.displayName = displayName;
        this.emailAddress = emailAddress;
        this.savedPlaces = savedPlaces;
    }

}
