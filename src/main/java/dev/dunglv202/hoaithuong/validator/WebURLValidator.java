package dev.dunglv202.hoaithuong.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class WebURLValidator implements ConstraintValidator<WebURL, String> {
    public static final String WEB_URL_PATTERN = "^https?://(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(?::\\d{1,5})?(?:/\\S*)?$";

    @Override
    public boolean isValid(String url, ConstraintValidatorContext constraintValidatorContext) {
        if (url == null) {
            return true;
        }

        return Pattern.compile(WEB_URL_PATTERN).matcher(url).matches();
    }
}
