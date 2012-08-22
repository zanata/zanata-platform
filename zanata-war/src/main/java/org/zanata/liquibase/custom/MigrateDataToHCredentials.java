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
package org.zanata.liquibase.custom;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Custom change set to migrate authentication data to the HCredentials table.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class MigrateDataToHCredentials implements CustomTaskChange
{
   private String dbAuthType;

   @Override
   public void execute(Database database) throws CustomChangeException
   {
      if( dbAuthType.equals("KERBEROS") || dbAuthType.equals("OPENID") || dbAuthType.equals("JAAS") )
      {
         JdbcConnection conn = (JdbcConnection)database.getConnection();

         try
         {
            Statement stmt = conn.createStatement();
            PreparedStatement insertStmt = conn.prepareStatement("insert into HCredentials " +
                  "(account_id, type, user, creationDate, lastChanged, versionNum) values" +
                  "(?, ?, ?, ?, ?, ?)");
            ResultSet rset = stmt.executeQuery("select id, username, creationDate, lastChanged from HAccount");

            while( rset.next() )
            {

               insertStmt.setLong(1, rset.getLong("id"));
               insertStmt.setString(2, dbAuthType);
               insertStmt.setString(3, rset.getString("username"));
               insertStmt.setDate(4, rset.getDate("creationDate"));
               insertStmt.setDate(5, rset.getDate("lastChanged"));
               insertStmt.setLong(6, 0);

               insertStmt.executeUpdate();
            }
         }
         catch (DatabaseException e)
         {
            throw new CustomChangeException(e);
         }
         catch (SQLException e)
         {
            throw new CustomChangeException(e);
         }
      }
   }

   @Override
   public String getConfirmationMessage()
   {
      return "User credentials migrated to HCredentials table";
   }

   @Override
   public void setUp() throws SetupException
   {
      // Get the zanata.properties file from the classpath
      ResourceBundle zanataProperties = ResourceBundle.getBundle("zanata");

      //# INTERNAL, KERBEROS, FEDORA_OPENID, JAAS
      if( zanataProperties.getString("zanata.security.auth.type").equals("FEDORA_OPENID") )
      {
         dbAuthType = "OPENID";
      }
      else if( zanataProperties.getString("zanata.security.auth.type").equals("JAAS") )
      {
         dbAuthType = "JAAS";
      }
      else if( zanataProperties.getString("zanata.security.auth.type").equals("KERBEROS") )
      {
         dbAuthType = "KERBEROS";
      }
   }

   @Override
   public void setFileOpener(ResourceAccessor resourceAccessor)
   {
   }

   @Override
   public ValidationErrors validate(Database database)
   {
      return new ValidationErrors();
   }
}
