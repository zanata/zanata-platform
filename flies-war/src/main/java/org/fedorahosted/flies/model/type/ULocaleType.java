package org.fedorahosted.flies.model.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.EntityMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.ImmutableType;
import org.hibernate.type.LiteralType;

import com.ibm.icu.util.ULocale;

/**
 * <tt>ulocale</tt>: A type that maps an SQL VARCHAR to a ICU ULocale.
 * 
 * Adaped from org.hibernate.type.LocaleType
 */
public class ULocaleType extends ImmutableType implements LiteralType
{

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
         return new ULocale(string);
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

   public Class getReturnedClass()
   {
      return ULocale.class;
   }

   public String getName()
   {
      return "locale";
   }

   public String objectToSQLString(Object value, Dialect dialect) throws Exception
   {
      return ((LiteralType) Hibernate.STRING).objectToSQLString(value.toString(), dialect);
   }

}
