package com.example.boltparty;

public class Booked {

    private String cafeName;
    private String address;
    private String date;
    private String time;
    private String imageUrl;

    public Booked() {

    }

    public Booked(String cafeName, String address, String date, String time, String imageUrl) {
        this.cafeName = cafeName;
        this.address = address;
        this.date = date;
        this.time = time;
        this.imageUrl = imageUrl;
    }

    public String getCafeName() {
        return cafeName;
    }

    public void setCafeName(String cafeName) {
        this.cafeName = cafeName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
