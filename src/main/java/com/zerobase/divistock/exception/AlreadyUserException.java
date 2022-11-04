package com.zerobase.divistock.exception;

import org.springframework.http.HttpStatus;

public class AlreadyUserException extends AbstractException{
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "이미 존재하는 사용자입니다.";
    }
}
