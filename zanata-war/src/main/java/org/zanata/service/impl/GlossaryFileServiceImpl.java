/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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
package org.zanata.service.impl;

import static org.jboss.seam.ScopeType.STATELESS;

import java.io.InputStream;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.dao.GlossaryDAO;
import org.zanata.model.HGlossaryEntry;
import org.zanata.rest.dto.Glossary;
import org.zanata.service.GlossaryFileService;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
@Name("glossaryFileServiceImpl")
@Scope(STATELESS)
@AutoCreate
public class GlossaryFileServiceImpl implements GlossaryFileService
{
   private GlossaryDAO glossaryDAO;

   @Override
   public Glossary parseGlossaryFile(InputStream fileContents, String fileName)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public HGlossaryEntry saveGlossary(Glossary glossary)
   {
      // TODO Auto-generated method stub
      return null;
   }

}
