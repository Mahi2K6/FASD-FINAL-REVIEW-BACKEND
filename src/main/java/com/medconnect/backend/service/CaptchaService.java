package com.medconnect.backend.service;

public interface CaptchaService {

    boolean isValid(String captchaToken);
}
