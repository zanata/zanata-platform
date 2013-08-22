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
package org.zanata.tmx;

import java.io.InputStream;

import javax.persistence.EntityExistsException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import nu.xom.Element;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.transaction.Transaction;
import org.zanata.common.util.ElementBuilder;
import org.zanata.model.tm.TransMemory;
import org.zanata.util.TMXParseException;
import org.zanata.xml.TmxDtdResolver;


/**
 * Parses TMX input.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("tmxParser")
@AutoCreate
@Slf4j
public class TMXParser
{
   // Batch size to commit in a new transaction for long files
   private static final int BATCH_SIZE = 100;

   @In
   private Session session;
   @In
   private TransMemoryAdapter transMemoryAdapter;

   @Transactional
   public void parseAndSaveTMX(InputStream input, TransMemory transMemory)
         throws TMXParseException, SecurityException, IllegalStateException, RollbackException,
         HeuristicMixedException, HeuristicRollbackException, SystemException, NotSupportedException
   {
      int handledTUs = 0;
      try
      {
         log.info("parsing started for: {}", transMemory.getSlug());
         session.setFlushMode(FlushMode.MANUAL);
         session.setCacheMode(CacheMode.IGNORE);
         XMLInputFactory factory = XMLInputFactory.newInstance();
         factory.setProperty(XMLInputFactory.SUPPORT_DTD, true);
         factory.setProperty(XMLInputFactory.IS_VALIDATING, true);
         factory.setXMLResolver(new TmxDtdResolver());
         @Cleanup
         XMLStreamReader reader = factory.createXMLStreamReader(input);

         QName tmx = new QName("tmx");
         QName header = new QName("header");
         QName tu = new QName("tu");

         while (reader.hasNext() && reader.next() != XMLStreamConstants.START_ELEMENT) {}
         if (!reader.hasNext()) throw new TMXParseException("No root element");
         if (!reader.getName().equals(tmx)) throw new TMXParseException("Wrong root element: expected tmx");

         // At this point, event = START_ELEMENT and name = tmx
         while (reader.hasNext())
         {
            int eventType = reader.next();
            if (eventType == XMLStreamConstants.START_ELEMENT)
            {
               if( handledTUs > 0 && handledTUs % BATCH_SIZE == 0 )
               {
                  commitBatch(handledTUs);
               }
               QName elemName = reader.getName();
               if (elemName.equals(tu))
               {
                  Element tuElem = ElementBuilder.buildElement(reader);
                  transMemoryAdapter.processTransUnit(transMemory, tuElem);
                  handledTUs++;
               }
               else if (elemName.equals(header))
               {
                  Element headerElem = ElementBuilder.buildElement(reader);
                  transMemoryAdapter.processHeader(transMemory, headerElem);
               }
            }
         }
         commitBatch(handledTUs); // A new transaction is needed for Seam to commit it
      }
      catch (EntityExistsException e)
      {
         String msg = "Possible duplicate TU (duplicate tuid or duplicate" +
               "src content without tuid)";
         throw new TMXParseException(msg, e);
      }
      catch (XMLStreamException e)
      {
         throw new TMXParseException(e);
      }
      finally
      {
         log.info("parsing stopped for: {}, TU count={}", transMemory.getSlug(), handledTUs);
      }
   }

   private void commitBatch(int numProcessed)
         throws SecurityException, IllegalStateException, RollbackException,
         HeuristicMixedException, HeuristicRollbackException, SystemException, NotSupportedException
   {
      session.flush();
      session.clear();
      Transaction.instance().commit();
      Transaction.instance().begin();
   }

}
