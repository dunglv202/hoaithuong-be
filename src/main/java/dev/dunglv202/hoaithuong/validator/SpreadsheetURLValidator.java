package dev.dunglv202.hoaithuong.validator;

import dev.dunglv202.hoaithuong.helper.SheetHelper;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SpreadsheetURLValidator implements ConstraintValidator<SpreadsheetURL, String> {
    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null || s.isBlank()) return true;
        return SheetHelper.SPREADSHEET_URL_PATTERN.matcher(s).matches();
    }
}
