package com.example.keo.mapmaestro;


public class UserBookmarks
{
    public String email;
    public String location;

    public UserBookmarks()
    {

    }

    public UserBookmarks(String email, String location)
    {
        this.email = email;
        this.location = location;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
