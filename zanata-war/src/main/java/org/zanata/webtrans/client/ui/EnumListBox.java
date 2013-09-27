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

package org.zanata.webtrans.client.ui;

import java.util.Arrays;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SimpleKeyProvider;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class EnumListBox<E extends Enum<?>> extends ValueListBox<E> {

    public EnumListBox(Class<E> clazz, Renderer<E> renderer) {
        this(clazz.getEnumConstants(), renderer);
    }

    public EnumListBox(E[] values, Renderer<E> renderer) {
        this(values, renderer, new SimpleKeyProvider<E>());
    }

    public EnumListBox(E[] values, Renderer<E> renderer,
            ProvidesKey<E> keyProvider) {
        super(renderer, keyProvider);
        init(values);
    }

    private void init(E[] values) {
        // this avoids the automatic null entry in the acceptableValues array
        setValue(values[0]);
        setAcceptableValues(Arrays.asList(values));
    }

}
