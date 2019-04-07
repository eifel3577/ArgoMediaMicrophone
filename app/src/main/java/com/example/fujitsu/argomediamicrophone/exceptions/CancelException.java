package com.example.fujitsu.argomediamicrophone.exceptions;


public class CancelException extends Exception {

    public CancelException() {
        super("remote device canceled connection");
    }
}
