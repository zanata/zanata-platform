package org.fedorahosted.flies.validators;

import java.lang.annotation.Retention;
import java.lang.annotation.*;

import org.hibernate.validator.ValidatorClass;

@ValidatorClass(UrlValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Url {

	String message() default "{validator.url}";
}
