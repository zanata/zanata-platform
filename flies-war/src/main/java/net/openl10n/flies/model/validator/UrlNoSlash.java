package net.openl10n.flies.model.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.hibernate.validator.ValidatorClass;

@ValidatorClass(UrlValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface UrlNoSlash
{
   String message() default "{validator.urlNoSlash}";
}
