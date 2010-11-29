/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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
package net.openl10n.flies.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;
import javax.sql.DataSource;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.security.auth.spi.DatabaseServerLoginModule;

public class FliesNukesLoginModule extends DatabaseServerLoginModule
{
   private static final LogProvider log = Logging.getLogProvider(FliesNukesLoginModule.class);
   public static final String AUTHENTICATED_NUKE_USER = "authenticated.nuke.user";
   public boolean validatePassword(String inputPassword, String expectedPassword)
   {
      inputPassword = toHexString(md5(inputPassword));
      boolean result = super.validatePassword(inputPassword, expectedPassword);
      return result;
   }

    public boolean login() throws LoginException
    {
    boolean success = super.login();
    getUserInfo();
    return success;
   
    }

   public static class NukeUser
   {
      public String username;
      public String password;
      public String email;
      public String name;

      public NukeUser(String username, String password, String email, String name)
      {
         this.username = username;
         this.password = password;
         this.email = email;
         this.name = name;
      }
   }


   public void getUserInfo()
    {
      Context context;
      Transaction tx = null;
      ResultSet rs = null;
      Connection con = null;
      DataSource ds = null;
      PreparedStatement ps = null;
      if (suspendResume)
      {
         try
         {
            if (tm == null)
               throw new IllegalStateException("Transaction Manager is null");
            tx = tm.suspend();
         }
         catch (SystemException e)
         {
            throw new RuntimeException(e);
         }
      }

    try
    {
         context = new InitialContext();
         ds = (DataSource) context.lookup(dsJndiName);
         con = ds.getConnection();
         String sqlQuery = "SELECT * FROM nuke_users WHERE pn_uname = ?";
         ps = con.prepareStatement(sqlQuery);
         ps.setString(1, getUsername());

         rs = ps.executeQuery();
         if (rs.next())
         {
            if (Contexts.isSessionContextActive())
            {
               Contexts.getSessionContext().set(AUTHENTICATED_NUKE_USER, new NukeUser(rs.getString("pn_uname"), rs.getString("pn_pass"), rs.getString("pn_email"), rs.getString("pn_name")));
            }
         }
    }
    catch (NamingException e)
    {
         log.warn(e.getMessage());
    }
    catch (SQLException e)
    {
         log.warn(e.getMessage());
    }
      finally
      {
         if (rs != null)
         {
            try
            {
               rs.close();
            }
            catch (SQLException e)
            {
            }
         }
         if (ps != null)
         {
            try
            {
               ps.close();
            }
            catch (SQLException e)
            {
            }
         }
         if (con != null)
         {
            try
            {
               con.close();
            }
            catch (SQLException ex)
            {
            }
         }
         if (suspendResume)
         {
            // TransactionDemarcationSupport.resumeAnyTransaction(tx);
            try
            {
               tm.resume(tx);
            }
            catch (Exception e)
            {
               throw new RuntimeException(e);
            }
         }
      }
    }


   private String toHexString(byte[] bytes)
   {
      if (bytes == null)
      {
         throw new IllegalArgumentException("byte array must not be null");
      }
      StringBuffer hex = new StringBuffer(bytes.length * 2);
      for (byte aByte : bytes)
      {
         hex.append(Character.forDigit((aByte & 0XF0) >> 4, 16));
         hex.append(Character.forDigit((aByte & 0X0F), 16));
      }
      return hex.toString();
   }


   private byte[] md5(String text)
   {
      // arguments check
      if (text == null)
      {
         throw new NullPointerException("null text");
      }

      try
      {
         MessageDigest md = MessageDigest.getInstance("MD5");
         md.update(text.getBytes());
         return md.digest();
      }
      catch (NoSuchAlgorithmException e)
      {
         throw new RuntimeException("Cannot find MD5 algorithm");
      }
   }

}
