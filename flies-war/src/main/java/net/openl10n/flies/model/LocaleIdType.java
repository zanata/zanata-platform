package net.openl10n.flies.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.EntityMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.ImmutableType;
import org.hibernate.type.LiteralType;

public class LocaleIdType extends ImmutableType implements LiteralType {
	   private static final long serialVersionUID = 1L;
	
	   @Override
	   public Object get(ResultSet rs, String name) throws HibernateException, SQLException
	   {
	      return fromStringValue((String) Hibernate.STRING.get(rs, name));
	   }
	
	   @Override
	   public void set(PreparedStatement st, Object value, int index) throws HibernateException, SQLException
	   {
	      Hibernate.STRING.set(st, value.toString(), index);
	   }
	
	   @Override
	   public Object fromStringValue(String string)
	   {
	      if (string == null)
	      {
	         return null;
	      }
	      else
	      {
	         return new LocaleId(string);
	      }
	   }
	
	   @Override
	   public int compare(Object x, Object y, EntityMode entityMode)
	   {
	      return x.toString().compareTo(y.toString());
	   }
	
	   @Override
	   public int sqlType()
	   {
	      return Hibernate.STRING.sqlType();
	   }
	
	   @Override
	   public String toString(Object value) throws HibernateException
	   {
	      return value.toString();
	   }
	
	   @Override
	   public Class<?> getReturnedClass()
	   {
	      return LocaleId.class;
	   }
	
	   @Override
	   public String getName()
	   {
	      return "localeId";
	   }
	
	   @Override
	   public String objectToSQLString(Object value, Dialect dialect) throws Exception
	   {
	      return ((LiteralType) Hibernate.STRING).objectToSQLString(value.toString(), dialect);
	   }
}
