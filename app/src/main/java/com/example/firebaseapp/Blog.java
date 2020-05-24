package com.example.firebaseapp;

public class Blog {
    private String title;
    private String description;
    private String imageUri;
    private String username;

    public Blog () {
    }

    public Blog(String title, String description, String imageUri, String username) {
        this.title = title;
        this.description = description;
        this.imageUri = imageUri;
        this.username = username;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
