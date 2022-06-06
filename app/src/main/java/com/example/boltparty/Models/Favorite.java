package com.example.boltparty;

public class Favorite {

    private String cafeName;
    private String address;
    private String imageUrl;

    public Favorite() {

    }

    public Favorite(String cafeName, String address, String imageUrl) {
        this.cafeName = cafeName;
        this.address = address;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
