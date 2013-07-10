/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
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

package org.zanata.database;

import java.sql.SQLException;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class StreamingResultSetSQLException extends SQLException
{

   public StreamingResultSetSQLException()
   {
   }

   public StreamingResultSetSQLException(String reason)
   {
      super(reason);
   }

   public StreamingResultSetSQLException(Throwable cause)
   {
      super(cause);
   }

   public StreamingResultSetSQLException(String reason, String SQLState)
   {
      super(reason, SQLState);
   }

   public StreamingResultSetSQLException(String reason, Throwable cause)
   {
      super(reason, cause);
   }

   public StreamingResultSetSQLException(String reason, String SQLState, int vendorCode)
   {
      super(reason, SQLState, vendorCode);
   }

   public StreamingResultSetSQLException(String reason, String sqlState, Throwable cause)
   {
      super(reason, sqlState, cause);
   }

   public StreamingResultSetSQLException(String reason, String sqlState, int vendorCode, Throwable cause)
   {
      super(reason, sqlState, vendorCode, cause);
   }

}
