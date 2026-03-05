package com.library_management.library_management_artifact.exception;

public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException(String message){
        super(message);
    }
}
