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
package org.zanata.client.commands.pull;

import org.zanata.adapter.po.PoWriter2;

/**
 * Similar to {@link GettextDirStrategy} but uses msgctxt to map text flow id.
 *
 * @author David Mason, <a
 *         href="mailto:damason@redhat.com">damason@redhat.com</a>
 */
public class OfflinePoStrategy extends GettextDirStrategy {
    private PoWriter2 poWriter;

    public OfflinePoStrategy(PullOptions opts) {
        super(opts);
        poWriter =
                new PoWriter2(opts.getEncodeTabs(), true,
                        opts.isContinueAfterError());
    }

    @Override
    protected PoWriter2 getPoWriter() {
        return poWriter;
    }

    @Override
    public boolean isTransOnly() {
        return true;
    }
}
