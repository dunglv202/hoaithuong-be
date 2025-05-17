package dev.dunglv202.hoaithuong.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = WebURLValidator.class)
@Documented
public @interface WebURL {
    String message() default "{url.invalid}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
