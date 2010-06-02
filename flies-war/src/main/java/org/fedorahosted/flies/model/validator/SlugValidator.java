package org.fedorahosted.flies.model.validator;

import java.io.Serializable;

import org.hibernate.validator.Validator;

public class SlugValidator implements Validator<Slug>, Serializable {

	public static final String PATTERN = "[a-zA-Z0-9]+([a-zA-Z0-9_\\-{.}]*[a-zA-Z0-9]+)?";

	public void initialize(Slug parameters) {
	}

	public boolean isValid(Object value) {
		if (value == null)
			return true;
		if (!(value instanceof String))
			return false;
		String string = (String) value;
		if (string.isEmpty())
			return true;
		return string.matches(PATTERN);
	}

}
