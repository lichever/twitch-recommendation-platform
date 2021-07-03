package com.laioffer.jupiter.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GameTest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("developer")
    private String developer;

    @JsonProperty("release_time")
    private String releaseTime;

    @JsonProperty("website")
    private String website;

    @JsonProperty("price")
    private double price;


    public GameTest(String name, String developer, String releaseTime, String website, double price) {
        this.name = name;
        this.developer = developer;
        this.releaseTime = releaseTime;
        this.website = website;
        this.price = price;
    }


    public String getName() {
        return name;
    }

    public String getDeveloper() {
        return developer;
    }

    public String getReleaseTime() {
        return releaseTime;
    }

    public String getWebsite() {
        return website;
    }

    public double getPrice() {
        return price;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public void setReleaseTime(String releaseTime) {
        this.releaseTime = releaseTime;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Game{" +
                "name='" + name + '\'' +
                ", developer='" + developer + '\'' +
                ", releaseTime='" + releaseTime + '\'' +
                ", website='" + website + '\'' +
                ", price=" + price +
                '}';
    }

}
