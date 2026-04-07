package com.medconnect.backend.service.impl;

import com.medconnect.backend.service.CaptchaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class GoogleCaptchaServiceImpl implements CaptchaService {

    private final RestClient restClient = RestClient.create();

    @Value("${app.recaptcha.enabled:true}")
    private boolean recaptchaEnabled;

    @Value("${app.recaptcha.verify-url:https://www.google.com/recaptcha/api/siteverify}")
    private String verifyUrl;

    @Value("${app.recaptcha.secret:}")
    private String secretKey;

    @Override
    public boolean isValid(String captchaToken) {
        if (!recaptchaEnabled) {
            return true;
        }
        if (captchaToken == null || captchaToken.isBlank()) {
            return false;
        }
        if (secretKey == null || secretKey.isBlank()) {
            return false;
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("secret", secretKey);
        form.add("response", captchaToken);

        Map<?, ?> response = restClient.post()
                .uri(verifyUrl)
                .body(form)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            return false;
        }
        Object success = response.get("success");
        return Boolean.TRUE.equals(success);
    }
}
