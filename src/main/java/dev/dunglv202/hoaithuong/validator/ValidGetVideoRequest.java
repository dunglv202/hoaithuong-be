package dev.dunglv202.hoaithuong.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = ValidGetVideoRequestValidator.class)
@Documented
public @interface ValidGetVideoRequest {
    String message() default "{lecture.video.invalid_request}";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
