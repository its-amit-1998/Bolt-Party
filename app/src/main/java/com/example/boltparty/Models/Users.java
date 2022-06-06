package com.example.boltparty;

public class Users {

    private String email;
    private String name;
    private String number;
    private String password;
    private String booked;
    private String favorite;
    private String imageURL;

    public Users() {
    }

    public Users(String email, String name, String number, String password, String booked, String favorite, String imageURL) {
        this.email = email;
        this.name = name;
        this.number = number;
        this.password = password;
        this.booked = booked;
        this.favorite = favorite;
        this.imageURL = imageURL;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBooked() {
        return booked;
    }

    public void setBooked(String booked) {
        this.booked = booked;
    }

    public String getFavorite() {
        return favorite;
    }

    public void setFavorite(String favorite) {
        this.favorite = favorite;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
