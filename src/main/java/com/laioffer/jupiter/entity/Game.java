package com.laioffer.jupiter.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(builder = Game.Builder.class)//通过builder class 来创建外层 Game 的object
public class Game {

/*
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

    public Game(Builder builder) {
        this.name = builder.name;
        this.developer = builder.developer;
        this.releaseTime = builder.releaseTime;
        this.website = builder.website;
        this.price = builder.price;
    }


//当一个类的构造函数参数个数超过4个，而且这些参数有些是可选的参数，考虑使用构造者模式。
    public static class Builder {
        private String name;
        private String developer;
        private String releaseTime;
        private String website;
        private double price;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDeveloper(String developer) {
            this.developer = developer;
            return this;
        }

        public Builder setReleaseTime(String releaseTime) {
            this.releaseTime = releaseTime;
            return this;
        }

        public Builder setWebsite(String website) {
            this.website = website;
            return this;
        }

        public Builder setPrice(double price) {
            this.price = price;
            return this;
        }

        public Game build() {
            return new Game(this);
        }
    }*/




    @JsonProperty("id")
    private final String id;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("box_art_url")
    private final String boxArtUrl;

    //3个getter  一般外面这个类 不需要setter，除非中途会改变它的值
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBoxArtUrl() {
        return boxArtUrl;
    }

    private Game(Builder builder) {//外面这类的constructor是private 内部调用，参数是builder
        this.id = builder.id;
        this.name = builder.name;
        this.boxArtUrl = builder.boxArtUrl;
    }




    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Builder {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("box_art_url")
        private String boxArtUrl;


        //一般里面的 builder class 全是setter 无getter
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder boxArtUrl(String boxArtUrl) {
            this.boxArtUrl = boxArtUrl;
            return this;
        }

        public Game build() {
            return new Game(this);// 这个build 作为 最后builder设置好各种属性后，结尾调用 返回外层class的object
        }
    }






}
