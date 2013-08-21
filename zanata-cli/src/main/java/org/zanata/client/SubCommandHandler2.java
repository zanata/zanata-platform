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

package org.zanata.client;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.Setter;
import org.kohsuke.args4j.spi.SubCommand;
import org.kohsuke.args4j.spi.SubCommandHandler;

/**
 * Just like Args4j's SubCommandHandler, but designed so that SubCommand instantiation is mockable.
 * <p>
 * Args4j constructs OptionHandlers directly from class literals, so we
 * can only mock static methods.  This class extends SubCommandHandler
 * so that we can intercept the instantiate method using PowerMock.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class SubCommandHandler2 extends SubCommandHandler
{
   public SubCommandHandler2(CmdLineParser parser, OptionDef option, Setter<Object> setter)
   {
      super(parser, option, setter);
   }

   private Object superInstantiate(SubCommand c)
   {
      return super.instantiate(c);
   }

   /**
    * PowerMockable version of instantiate method.
    * @param self
    * @param c
    * @return
    */
   static Object instantiate(SubCommandHandler2 self, SubCommand c)
   {
      return self.superInstantiate(c);
   }

   /**
    * Overrides SubCommandHandler so that we can call the mockable static method.
    */
   @Override
   protected Object instantiate(SubCommand c)
   {
      return instantiate(this, c);
   }

}
