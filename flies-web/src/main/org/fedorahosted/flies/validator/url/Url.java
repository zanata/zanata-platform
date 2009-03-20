package org.fedorahosted.flies.validator.url;

import java.lang.annotation.Retention;
import java.lang.annotation.*;

import org.hibernate.validator.ValidatorClass;

@ValidatorClass(UrlValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Url {

	String message() default "{validator.url}";
}
