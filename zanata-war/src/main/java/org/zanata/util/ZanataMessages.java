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
package org.zanata.util;

import java.util.ResourceBundle;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Interpolator;

/**
 * Utility component to help with programmatic access to the message resource
 * bundle.
 *
 * Contrary to the {@link org.jboss.seam.international.Messages} component, this
 * component does not hold already interpolated messages, but rather allows
 * interpolation to happen on demmand. Use this component when there is a need
 * to access parametrized messages.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("zanataMessages")
@Scope(ScopeType.STATELESS)
@AutoCreate
public class ZanataMessages {
    @In
    private ResourceBundle resourceBundle;

    @In
    private Interpolator interpolator;

    public String getMessage(String key, Object... args) {
        String template = resourceBundle.getString(key);
        return interpolator.interpolate(template, args);
    }
}
