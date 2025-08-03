package dev.dunglv202.hoaithuong.validator;

import dev.dunglv202.hoaithuong.dto.GetLectureVideoReq;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidGetVideoRequestValidator implements ConstraintValidator<ValidGetVideoRequest, GetLectureVideoReq> {
    @Override
    public boolean isValid(GetLectureVideoReq req, ConstraintValidatorContext constraintValidatorContext) {
        boolean hasClassSpecifier = req.getClassCode() != null || req.getClassUid() != null;
        boolean hasLectureSpecifier = req.getLecture() != null || req.getTimestamp() != null;
        return hasClassSpecifier && hasLectureSpecifier;
    }
}
