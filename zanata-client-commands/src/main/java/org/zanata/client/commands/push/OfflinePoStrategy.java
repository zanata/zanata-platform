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
package org.zanata.client.commands.push;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.resteasy.client.ClientResponse;
import org.zanata.adapter.po.PoReader2;
import org.zanata.rest.client.ClientUtility;
import org.zanata.rest.client.ISourceDocResource;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;

/**
 * Similar to {@link GettextDirStrategy} but uses msgctxt to map text flow id.
 *
 * @author David Mason, <a
 *         href="mailto:damason@redhat.com">damason@redhat.com</a>
 */
public class OfflinePoStrategy extends GettextDirStrategy {
    private final ISourceDocResource sourceDocResource;
    private final PoReader2 poReader;

    private final URI uri;

    public OfflinePoStrategy(ISourceDocResource sourceDocResource, URI uri) {
        this.sourceDocResource = sourceDocResource;
        this.uri = uri;
        poReader = new PoReader2(true);
    }

    @Override
    protected PoReader2 getPoReader() {
        return poReader;
    }

    @Override
    public boolean isTransOnly() {
        return true;
    };

    /**
     * This implementation retrieves document names from the server. All
     * parameters are ignored as there is no disk scanning.
     */
    @Override
    public Set<String> findDocNames(File srcDir, List<String> includes,
            List<String> excludes, boolean useDefaultExclude,
            boolean caseSensitive, boolean excludeLocaleFilenames)
            throws IOException {
        ClientResponse<List<ResourceMeta>> getResponse =
                sourceDocResource.get(null);
        ClientUtility.checkResult(getResponse, uri);
        List<ResourceMeta> remoteDocList = getResponse.getEntity();
        Set<String> localDocNames = new HashSet<String>();
        for (ResourceMeta doc : remoteDocList) {
            localDocNames.add(doc.getName());
        }
        return localDocNames;
    }

    @Override
    public String[] getSrcFiles(File srcDir, List<String> includes,
            List<String> excludes, boolean excludeLocaleFilenames,
            boolean useDefaultExclude, boolean isCaseSensitive) {
        throw new RuntimeException(
                "Source files should never be accessed in a trans-only strategy");
    }

    @Override
    public String[] getSrcFiles(File srcDir, List<String> includes,
            List<String> excludes, List<String> fileExtensions,
            boolean useDefaultExcludes, boolean isCaseSensitive) {
        throw new RuntimeException(
                "Source files should never be accessed in a trans-only strategy");
    }

    @Override
    public Resource loadSrcDoc(File sourceDir, String docName)
            throws IOException {
        throw new RuntimeException(
                "Source files should never be accessed in a trans-only strategy");
    }

}
