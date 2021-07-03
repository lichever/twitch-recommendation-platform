package com.laioffer.jupiter.external;

import javax.swing.plaf.SpinnerUI;

public class TwitchException extends RuntimeException{// 起一个定位的作用

    public TwitchException(String errorMessage) {
        super(errorMessage);
    }

    public TwitchException(){}

    public TwitchException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }


    public TwitchException(Throwable cause)
    {
        super(cause);
    }

}
