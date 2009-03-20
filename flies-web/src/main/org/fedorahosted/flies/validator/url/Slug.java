package org.fedorahosted.flies.validator.url;

import java.lang.annotation.Retention;
import java.lang.annotation.*;

import org.hibernate.validator.ValidatorClass;

/**
 * A slug is a short label for something, containing only letters, numbers,
 * underscores or hyphens. It is typically used in urls
 * 
 * @author asgeirf
 * 
 */
@ValidatorClass(SlugValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Slug {

	String message() default "{validator.slug}";
}
