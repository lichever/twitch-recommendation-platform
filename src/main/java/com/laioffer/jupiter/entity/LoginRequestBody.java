package com.laioffer.jupiter.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginRequestBody {
//    @JsonProperty("user_id")
    private final String userId;
//    @JsonProperty("password")
    private final String password;

    @JsonCreator     //里面的JsonProperty 也只需单向， json to class  也可以放外面
    public LoginRequestBody(@JsonProperty("user_id") String userId, @JsonProperty("password") String password) {
        this.userId = userId;
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }
}





