package com.fmeng48.staybooking.location;


public class InvalidAddressException extends RuntimeException{


    public InvalidAddressException() {
        super("Invalid address");
    }
}
