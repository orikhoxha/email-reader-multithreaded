package com.gmail.api.config;

import org.springframework.core.convert.converter.Converter;

import java.util.Map;

public class GoogleOAuth2TokenClaimSetConverter implements Converter<Map<String, Object>, Map<String, Object>> {


    @Override
    public Map<String, Object> convert(Map<String, Object> claims) {
        String email = (String) claims.get("email");
        if (email != null) {
            claims.put("user_name", email);
        }
        return claims;
    }
}


