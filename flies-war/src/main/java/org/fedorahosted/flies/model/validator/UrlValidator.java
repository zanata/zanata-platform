package org.fedorahosted.flies.model.validator;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import org.hibernate.validator.Validator;

public class UrlValidator implements Validator<Url>, Serializable {

	public void initialize(Url parameters) {
	}

	public boolean isValid(Object value) {
		if (value == null)
			return true;
		if (!(value instanceof String))
			return false;
		String string = (String) value;

		try {
			new URL(string);
			return true;
		} catch (MalformedURLException e) {
			return false;
		}
	}

}
