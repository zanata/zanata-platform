/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.model.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.ImmutableType;
import org.hibernate.type.LiteralType;
import org.zanata.common.EntityStatus;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class EntityStatusType extends ImmutableType implements LiteralType, DiscriminatorType
{

   @Override
   public String toString(Object value) throws HibernateException
   {
      return String.valueOf(((EntityStatus) value).getInitial());
   }

   public Object get(ResultSet rs, String name) throws SQLException
   {
      String str = rs.getString(name);
      if (str == null)
      {
         return null;
      }
      else
      {
         return EntityStatus.valueOf(str.charAt(0));
      }
   }

   public Class<EntityStatus> getReturnedClass()
   {
      return EntityStatus.class;
   }

   public void set(PreparedStatement st, Object value, int index) throws SQLException
   {
      st.setString(index, String.valueOf(((EntityStatus) value).getInitial()));
   }

   public int sqlType()
   {
      return Types.CHAR;
   }

   public String getName()
   {
      return "entityStatus";
   }

   public String objectToSQLString(Object value, Dialect dialect) throws Exception
   {
      return "'" + ((EntityStatus) value).getInitial() + "'";
   }

   public Object stringToObject(String xml) throws Exception
   {
      if (xml.length() != 1)
         throw new MappingException("multiple or zero characters found parsing string");
      return EntityStatus.valueOf(xml.charAt(0));
   }

   public Object fromStringValue(String xml)
   {
      return EntityStatus.valueOf(xml.charAt(0));
   }

}
