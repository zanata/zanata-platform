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

package org.zanata.rest.service;

import java.io.IOException;

import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.Optional;

import nu.xom.Element;

/**
 * A strategy for exporting translation units to TMX
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 * @param <T> a translation unit (either ITextFlow or TransMemoryUnit)
 */
@ParametersAreNonnullByDefault
public interface TMXExportStrategy<T>
{
   /**
    * Builds a {@code <header>} element.
    * @return
    * @throws IOException
    */
   public abstract Element buildHeader() throws IOException;
   /**
    * Builds a {@code <tu>} element for the specified T and one or all of its translations.
    * @param tu the T whose contents and translations are to be exported
    * @param tuidPrefix String to be prepended to all resIds when generating tuids
    * @return a TU Element, or absent() if the TU is invalid
    * @throws IOException 
    * @throws Exception 
    */
   public abstract Optional<Element> buildTU(T tu) throws IOException;
}
