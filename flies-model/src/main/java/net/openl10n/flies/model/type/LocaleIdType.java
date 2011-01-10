/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package net.openl10n.flies.model.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.openl10n.flies.common.LocaleId;

import org.hibernate.EntityMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.ImmutableType;
import org.hibernate.type.LiteralType;

public class LocaleIdType extends ImmutableType implements LiteralType
{

   private static final long serialVersionUID = 1251881884197592346L;

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
