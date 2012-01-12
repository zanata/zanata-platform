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

package org.zanata.client.commands.pull;

import java.io.IOException;

import org.zanata.adapter.properties.PropWriter;
import org.zanata.client.config.LocaleMapping;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class PropertiesStrategy implements PullStrategy
{
   // "8859_1" is used in Properties.java...
   private static final String ISO_8859_1 = "ISO-8859-1";

   StringSet extensions = new StringSet("comment");
   private PullOptions opts;
   private final String charset;

   public PropertiesStrategy()
   {
      this(ISO_8859_1);
   }

   public PropertiesStrategy(String charset)
   {
      this.charset = charset;
   }

   @Override
   public void setPullOptions(PullOptions opts)
   {
      this.opts = opts;
   }

   @Override
   public StringSet getExtensions()
   {
      return extensions;
   }

   @Override
   public boolean needsDocToWriteTrans()
   {
      return false;
   }

   @Override
   public void writeSrcFile(Resource doc) throws IOException
   {
      PropWriter.write(doc, opts.getSrcDir(), charset);
   }

   @Override
   public void writeTransFile(Resource doc, String docName, LocaleMapping localeMapping, TranslationsResource targetDoc) throws IOException
   {
      PropWriter.write(targetDoc, opts.getTransDir(), docName, localeMapping.getJavaLocale(), charset);
   }

}
