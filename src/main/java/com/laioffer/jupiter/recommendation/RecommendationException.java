package com.laioffer.jupiter.recommendation;

public class RecommendationException extends RuntimeException{

    public RecommendationException(String message) {
        super(message);
    }


    public RecommendationException(Throwable cause) {
        super(cause);
    }
}
