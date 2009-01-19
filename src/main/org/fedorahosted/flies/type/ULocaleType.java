package org.fedorahosted.flies.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.ibm.icu.util.ULocale;

import org.hibernate.EntityMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.ImmutableType;
import org.hibernate.type.LiteralType;

/**
 * <tt>ulocale</tt>: A type that maps an SQL VARCHAR to a ICU ULocale.
 * 
 * Adaped from org.hibernate.type.LocaleType
 */
public class ULocaleType extends ImmutableType implements LiteralType {

	public Object get(ResultSet rs, String name) throws HibernateException, SQLException {
		return fromStringValue( (String) Hibernate.STRING.get(rs, name) );
	}

	public void set(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
		Hibernate.STRING.set(st, value.toString(), index);
	}

	public Object fromStringValue(String string) {
		if (string == null) {
			return null;
		}
		else {
			return new ULocale(string);
		}
	}
	
	public int compare(Object x, Object y, EntityMode entityMode) {
		return x.toString().compareTo( y.toString() );
	}

	public int sqlType() {
		return Hibernate.STRING.sqlType();
	}

	public String toString(Object value) throws HibernateException {
		return value.toString();
	}

	public Class getReturnedClass() {
		return ULocale.class;
	}

	public String getName() {
		return "locale";
	}

	public String objectToSQLString(Object value, Dialect dialect) throws Exception {
		return ( (LiteralType) Hibernate.STRING ).objectToSQLString( value.toString(), dialect );
	}

}






